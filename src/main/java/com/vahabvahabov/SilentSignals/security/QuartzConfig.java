package com.vahabvahabov.SilentSignals.security;

import com.vahabvahabov.SilentSignals.schedule.SosAlertReminderJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    @Bean
    public JobDetail sosAlertReminderJobDetail() {
        return JobBuilder.newJob(SosAlertReminderJob.class)
                .withIdentity("sosAlertReminderJob")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger sosAlertReminderTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(sosAlertReminderJobDetail())
                .withIdentity("sosAlertReminderTrigger")
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(10)
                        .repeatForever())
                .build();
    }
}