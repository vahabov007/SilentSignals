package com.vahabvahabov.SilentSignals.service;

import jakarta.mail.MessagingException;

public interface EmailService {

    public void sendPinToEmail(String toEmail, String pin) throws MessagingException;
    void sendSosAlert(String toEmail, String username, String description, String locationAddress) throws MessagingException;
    public void sendSosAlertReminder(String toEmail, String username, String description, String locationAddress) throws MessagingException;

}
