package com.vahabvahabov.SilentSignals.service.imp;

import com.vahabvahabov.SilentSignals.exception.RateLimitExceededException;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.model.alert.AlertStatus;
import com.vahabvahabov.SilentSignals.model.alert.SOSAlert;
import com.vahabvahabov.SilentSignals.model.contact.TrustedContact;
import com.vahabvahabov.SilentSignals.repository.SOSAlertRepository;
import com.vahabvahabov.SilentSignals.repository.TrustedContractRepository;
import com.vahabvahabov.SilentSignals.repository.UserRepository;
import com.vahabvahabov.SilentSignals.security.InMemoryRateLimiterUtil;
import com.vahabvahabov.SilentSignals.service.AlertService;
import com.vahabvahabov.SilentSignals.service.EmailService;
import com.vahabvahabov.SilentSignals.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertServiceImpl implements AlertService {

    private final Logger logger = LoggerFactory.getLogger(AlertServiceImpl.class);

    @Autowired
    private SOSAlertRepository sosAlertRepository;

    @Autowired
    private TrustedContractRepository trustedContractRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private InMemoryRateLimiterUtil rateLimiter;

    /**
     * Send alert using User object (for HTTP requests)
     */
    @Override
    @Transactional
    public void sendAlert(User user, String description, String locationCoordinates, String locationAddress) {
        logger.info("Sending SOS alert for user: {}", user.getUsername());
        // Delegate to the user ID method to avoid code duplication
        sendAlert(user.getId(), description, locationCoordinates, locationAddress);
    }

    /**
     * Send alert using user ID (for background jobs and Quartz scheduler)
     * Applies rate limiting for regular alerts
     */
    @Override
    @Transactional
    public void sendAlert(Long userId, String description, String locationCoordinates, String locationAddress) {
        // Check rate limiting for regular alerts
        if (!rateLimiter.isAllowed(userId)) {
            Long remainingTime = rateLimiter.getTimeUntilReset(userId);
            logger.warn("Rate limit exceeded for user ID: {}. Try again in {} seconds", userId, remainingTime);
            throw new RateLimitExceededException(userId, remainingTime);
        }

        processAlert(userId, description, locationCoordinates, locationAddress, false);
    }

    /**
     * Send reminder alert (bypasses rate limiting for reminders)
     */
    @Override
    @Transactional
    public void sendReminderAlert(Long userId, String description, String locationCoordinates, String locationAddress) {
        logger.info("Sending reminder alert for user ID: {}", userId);
        processAlert(userId, "REMINDER: " + description, locationCoordinates, locationAddress, true);
    }

    /**
     * Core alert processing logic - used by both regular alerts and reminders
     */
    private void processAlert(Long userId, String description, String locationCoordinates,
                              String locationAddress, boolean isReminder) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("User not found with ID: " + userId);
        }

        User user = userOptional.get();

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is not enabled: " + user.getUsername());
        }

        if (!user.isEmailVerified()) {
            throw new RuntimeException("User email is not verified: " + user.getUsername());
        }

        String alertType = isReminder ? "reminder" : "SOS";
        logger.info("Processing {} alert for user: {}", alertType, user.getUsername());

        SOSAlert sosAlert = createSosAlert(user, description, locationCoordinates, locationAddress);
        sosAlertRepository.save(sosAlert);
        logger.info("{} alert saved with ID: {}", alertType, sosAlert.getId());

        List<TrustedContact> activeContacts = getActiveTrustedContacts(userId);

        if (activeContacts.isEmpty()) {
            logger.warn("No active trusted contacts found for user: {}", user.getUsername());
            return;
        }

        logger.info("Found {} active contacts for user {}", activeContacts.size(), user.getUsername());

        sendNotificationsToContacts(activeContacts, user.getUsername(), description, locationAddress, isReminder);

        logger.info("{} alert processed successfully for user: {}", alertType, user.getUsername());
    }

    /**
     * Create SOS alert entity
     */
    private SOSAlert createSosAlert(User user, String description, String locationCoordinates, String locationAddress) {
        SOSAlert sosAlert = new SOSAlert();
        sosAlert.setUser(user);
        sosAlert.setDescription(description);
        sosAlert.setAlertStatus(AlertStatus.ACTIVE);
        sosAlert.setLocationAddress(locationAddress != null ? locationAddress : "Location not available");
        sosAlert.setTriggeredAt(LocalDateTime.now());
        sosAlert.setLocationCoordinates(locationCoordinates != null ? locationCoordinates : "Coordinates not available");
        return sosAlert;
    }

    /**
     * Get active trusted contacts for a user
     */
    private List<TrustedContact> getActiveTrustedContacts(Long userId) {
        return trustedContractRepository.findByUserId(userId)
                .stream()
                .filter(TrustedContact::isActive)
                .sorted(Comparator.comparingInt(TrustedContact::getPriorityOrder))
                .collect(Collectors.toList());
    }

    /**
     * Send notifications to all trusted contacts
     */
    private void sendNotificationsToContacts(List<TrustedContact> activeContacts, String username,
                                             String description, String locationAddress, boolean isReminder) {
        boolean webSocketDelivered = false;

        webSocketDelivered = sendWebSocketNotifications(activeContacts, username, description, locationAddress, isReminder);

        sendEmailAndSmsNotifications(activeContacts, username, description, locationAddress, isReminder);

        if (!webSocketDelivered) {
            logger.info("WebSocket delivery failed, relying on email/SMS notifications");
        }
    }

    /**
     * Send WebSocket notifications to online users
     */
    private boolean sendWebSocketNotifications(List<TrustedContact> activeContacts, String username,
                                               String description, String locationAddress, boolean isReminder) {
        boolean delivered = false;

        String prefix = isReminder ? "REMINDER - " : "";

        for (TrustedContact contact : activeContacts) {
            if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                try {
                    String alertMessage = String.format(
                            "%sSOS Alert from %s: %s at %s",
                            prefix, username, description, locationAddress
                    );

                    messagingTemplate.convertAndSendToUser(
                            contact.getEmail(),
                            "/topic/alerts",
                            alertMessage
                    );
                    delivered = true;
                    logger.info("WebSocket {}alert sent to: {}", isReminder ? "reminder " : "", contact.getEmail());
                } catch (Exception e) {
                    logger.error("WebSocket delivery failed for {}: {}", contact.getEmail(), e.getMessage());
                }
            }
        }

        return delivered;
    }

    /**
     * Send email and SMS notifications
     */
    private void sendEmailAndSmsNotifications(List<TrustedContact> activeContacts, String username,
                                              String description, String locationAddress, boolean isReminder) {
        int emailCount = 0;
        int smsCount = 0;

        for (TrustedContact contact : activeContacts) {
            if (contact.getEmail() != null && !contact.getEmail().trim().isEmpty()) {
                try {
                    if (isReminder) {
                        emailService.sendSosAlertReminder(contact.getEmail(), username, description, locationAddress);
                    } else {
                        emailService.sendSosAlert(contact.getEmail(), username, description, locationAddress);
                    }
                    emailCount++;
                    logger.info("SOS {}Email sent to: {}", isReminder ? "Reminder " : "", contact.getEmail());
                } catch (Exception e) {
                    logger.error("Failed to send SOS Email to {}: {}", contact.getEmail(), e.getMessage());
                }
            }

            if (contact.getPhone() != null && !contact.getPhone().trim().isEmpty()) {
                try {
                    String smsDescription = isReminder ? "REMINDER: " + description : description;
                    smsService.sendSosAlert(contact.getPhone(), username, smsDescription, locationAddress);
                    smsCount++;
                    logger.info("SOS {}SMS sent to: {}", isReminder ? "Reminder " : "", contact.getPhone());
                } catch (Exception e) {
                    logger.error("Failed to send SOS SMS to {}: {}", contact.getPhone(), e.getMessage());
                }
            }
        }

        String alertType = isReminder ? "reminder " : "";
        logger.info("{}Notifications sent: {} emails, {} SMS messages", alertType, emailCount, smsCount);
    }

    /**
     * Additional method to check if user can send alerts (for pre-validation)
     */
    @Override
    @Transactional(readOnly = true)
    public boolean canUserSendAlert(Long userId) {
        // Check rate limiting
        if (!rateLimiter.isAllowed(userId)) {
            return false;
        }

        // Check if user exists and is active
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            return false;
        }

        User user = userOptional.get();
        return user.isEnabled() && user.isEmailVerified();
    }

    /**
     * Get remaining rate limit for user
     */
    @Override
    public Long getRemainingRateLimitTime(Long userId) {
        return rateLimiter.getTimeUntilReset(userId);
    }
}