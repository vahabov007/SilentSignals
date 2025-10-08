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

            List<SOSAlert> alertsNeedingReminders = getAlertsNeedingReminders();

            logger.info("Found {} alerts needing reminders", alertsNeedingReminders.size());

            int successfulReminders = 0;
            int failedReminders = 0;

            for (SOSAlert alert : alertsNeedingReminders) {
                try {
                    logger.info("Sending reminder for SOS Alert ID: {} (User ID: {})",
                            alert.getId(), alert.getUser().getId());

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
                }
            }

            logger.info("SOS Alert Reminder Job completed. Success: {}, Failed: {}, Total: {}",
                    successfulReminders, failedReminders, alertsNeedingReminders.size());

        } catch (Exception e) {
            logger.error("Error in SOS Alert Reminder Job: {}", e.getMessage(), e);
            throw new JobExecutionException("Failed to execute SOS Alert Reminder Job", e, false); // Don't refire immediately
        }
    }


    private List<SOSAlert> getAlertsNeedingReminders() {
        List<SOSAlert> activeAlerts = sosAlertRepository.findByAlertStatus(AlertStatus.ACTIVE);

        return activeAlerts.stream()
                .filter(alert -> {
                    boolean needsReminder = alert.getTriggeredAt().isBefore(LocalDateTime.now().minusMinutes(5));

                    boolean isNotReminder = !alert.getDescription().startsWith("REMINDER:");

                    return needsReminder && isNotReminder;
                })
                .collect(java.util.stream.Collectors.toList());
    }


    private String extractOriginalDescription(String description) {
        if (description.startsWith("REMINDER: ")) {
            return description.substring("REMINDER: ".length());
        }
        return description;
    }
}