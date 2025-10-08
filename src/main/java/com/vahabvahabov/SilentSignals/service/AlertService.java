package com.vahabvahabov.SilentSignals.service;

import com.vahabvahabov.SilentSignals.model.User;

public interface AlertService {
    void sendAlert(User user, String description, String locationCoordinates, String locationAddress);
    void sendAlert(Long userId, String description, String locationCoordinates, String locationAddress);
    void sendReminderAlert(Long userId, String description, String locationCoordinates, String locationAddress);
    boolean canUserSendAlert(Long userId);
    Long getRemainingRateLimitTime(Long userId);
}