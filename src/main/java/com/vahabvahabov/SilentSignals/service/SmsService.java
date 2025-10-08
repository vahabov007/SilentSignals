package com.vahabvahabov.SilentSignals.service;

public interface SmsService {
    void sendSosAlert(String toPhone, String username, String description, String locationAddress);
}