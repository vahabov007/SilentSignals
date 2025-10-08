package com.vahabvahabov.SilentSignals.schedule;

import com.vahabvahabov.SilentSignals.model.alert.AlertStatus;
import com.vahabvahabov.SilentSignals.model.alert.SOSAlert;
import com.vahabvahabov.SilentSignals.repository.SOSAlertRepository;
import com.vahabvahabov.SilentSignals.service.AlertService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class SosAlertReminderJob implements Job {

    private final Logger logger = LoggerFactory.getLogger(SosAlertReminderJob.class);

    @Autowired
    private SOSAlertRepository sosAlertRepository;

    @Autowired
    private AlertService alertService;

    @Override
    @Transactional
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            logger.info("Running SOS Alert Reminder Job at {}", LocalDateTime.now());

            // Fetch active alerts that need reminders
            List<SOSAlert> alertsNeedingReminders = getAlertsNeedingReminders();

            logger.info("Found {} alerts needing reminders", alertsNeedingReminders.size());

            int successfulReminders = 0;
            int failedReminders = 0;

            for (SOSAlert alert : alertsNeedingReminders) {
                try {
                    logger.info("Sending reminder for SOS Alert ID: {} (User ID: {})",
                            alert.getId(), alert.getUser().getId());

                    // Use the dedicated reminder method that bypasses rate limiting
                    alertService.sendReminderAlert(
                            alert.getUser().getId(),
                            extractOriginalDescription(alert.getDescription()),
                            alert.getLocationCoordinates(),
                            alert.getLocationAddress()
                    );

                    successfulReminders++;
                    logger.info("Reminder sent successfully for alert ID: {}", alert.getId());

                } catch (Exception e) {
                    failedReminders++;
                    logger.error("Failed to send reminder for alert ID {}: {}", alert.getId(), e.getMessage());
                    // Continue with other alerts even if one fails
                }
            }

            logger.info("SOS Alert Reminder Job completed. Success: {}, Failed: {}, Total: {}",
                    successfulReminders, failedReminders, alertsNeedingReminders.size());

        } catch (Exception e) {
            logger.error("Error in SOS Alert Reminder Job: {}", e.getMessage(), e);
            throw new JobExecutionException("Failed to execute SOS Alert Reminder Job", e, false); // Don't refire immediately
        }
    }

    /**
     * Get alerts that need reminders:
     * - Active alerts triggered more than 5 minutes ago
     * - Not already reminder alerts (to avoid reminder loops)
     */
    private List<SOSAlert> getAlertsNeedingReminders() {
        List<SOSAlert> activeAlerts = sosAlertRepository.findByAlertStatus(AlertStatus.ACTIVE);

        return activeAlerts.stream()
                .filter(alert -> {
                    // Check if alert was triggered more than 5 minutes ago
                    boolean needsReminder = alert.getTriggeredAt().isBefore(LocalDateTime.now().minusMinutes(5));

                    // Don't send reminders for reminder alerts (avoid infinite loops)
                    boolean isNotReminder = !alert.getDescription().startsWith("REMINDER:");

                    return needsReminder && isNotReminder;
                })
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Extract original description from reminder alerts
     */
    private String extractOriginalDescription(String description) {
        if (description.startsWith("REMINDER: ")) {
            return description.substring("REMINDER: ".length());
        }
        return description;
    }
}