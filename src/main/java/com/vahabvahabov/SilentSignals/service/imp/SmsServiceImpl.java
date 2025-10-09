package com.vahabvahabov.SilentSignals.service.imp;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.vahabvahabov.SilentSignals.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${twilio.account.sid:}")
    private String twilioSid;

    @Value("${twilio.auth.token:}")
    private String twilioToken;

    @Value("${twilio.phone.number:}")
    private String twilioPhone;

    private boolean twilioInitialized = false;

    @Override
    public void sendSosAlert(String toPhone, String username, String description, String locationAddress) {
        logger.info("Attempting to send SMS to: {}", toPhone);

        if (!isTwilioConfigValid()) {
            logger.error("Cannot send SMS: Twilio configuration is invalid");
            return;
        }

        String formattedToPhone = formatPhoneNumber(toPhone);
        String formattedTwilioPhone = formatPhoneNumber(twilioPhone);

        logger.debug("Formatted - To: {}, From: {}", formattedToPhone, formattedTwilioPhone);

        if (isSameNumber(formattedToPhone, formattedTwilioPhone)) {
            logger.warn("Skipping SMS - To and From numbers are the same: {}", formattedToPhone);
            return;
        }

        if (!isValidPhoneNumber(formattedToPhone)) {
            logger.error("Invalid phone number format: {}", formattedToPhone);
            return;
        }

        initializeTwilio();

        if (!twilioInitialized) {
            logger.error("Twilio not initialized, cannot send SMS");
            return;
        }

        try {
            String messageBody = String.format(
                    "🚨 SOS ALERT from %s\n\n%s\n\nLocation: %s\n\nPlease respond immediately!",
                    username,
                    description,
                    locationAddress != null ? locationAddress : "Location not available"
            );

            logger.debug("SMS Content - To: {}, From: {}, Body: {}", formattedToPhone, formattedTwilioPhone, messageBody);

            Message message = Message.creator(
                    new PhoneNumber(formattedToPhone),
                    new PhoneNumber(formattedTwilioPhone),
                    messageBody
            ).create();

            logger.info("SMS sent successfully to: {}. Message SID: {}, Status: {}",
                    formattedToPhone, message.getSid(), message.getStatus());

        } catch (ApiException e) {
            if (e.getCode() == 21266) {
                logger.warn("Twilio blocked SMS to same number: {}", formattedToPhone);
                return;
            }
            logger.error("Twilio API error for {}: {}", formattedToPhone, e.getMessage());

        } catch (Exception e) {
            logger.error("Unexpected error sending SMS to {}: {}", formattedToPhone, e.getMessage());
        }
    }

    /**
     * Check if Twilio configuration is valid
     */
    private boolean isTwilioConfigValid() {
        if (twilioSid == null || twilioSid.trim().isEmpty() ||
                twilioToken == null || twilioToken.trim().isEmpty() ||
                twilioPhone == null || twilioPhone.trim().isEmpty()) {
            logger.error("Twilio configuration missing. SID: {}, Token: {}, Phone: {}",
                    twilioSid != null ? "SET" : "MISSING",
                    twilioToken != null ? "SET" : "MISSING",
                    twilioPhone != null ? "SET" : "MISSING");
            return false;
        }
        return true;
    }

    /**
     * Check if two phone numbers are the same (ignoring formatting differences)
     */
    private boolean isSameNumber(String phone1, String phone2) {
        if (phone1 == null || phone2 == null) return false;

        String clean1 = phone1.replaceAll("[^\\d]", "");
        String clean2 = phone2.replaceAll("[^\\d]", "");

        boolean isSame = clean1.equals(clean2);

        if (isSame) {
            logger.debug("Detected same numbers: {} and {}", phone1, phone2);
        }

        return isSame;
    }

    /**
     * Validate phone number format
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return phone.startsWith("+") && phone.replaceAll("[^\\d]", "").length() >= 10;
    }

    /**
     * Initialize Twilio only once
     */
    private void initializeTwilio() {
        if (!twilioInitialized) {
            try {
                Twilio.init(twilioSid, twilioToken);
                twilioInitialized = true;
                logger.info("Twilio initialized successfully");
            } catch (Exception e) {
                logger.error("Failed to initialize Twilio: {}", e.getMessage());
            }
        }
    }

    /**
     * Format phone number to E.164 format
     */
    private String formatPhoneNumber(String phone) {
        if (phone == null) return null;

        String cleaned = phone.replaceAll("[^\\d+]", "");

        if (cleaned.startsWith("994")) {
            return "+" + cleaned;
        } else if (cleaned.startsWith("0")) {
            return "+994" + cleaned.substring(1);
        } else if (cleaned.startsWith("+")) {
            return cleaned;
        } else if (cleaned.length() == 9) {
            return "+994" + cleaned;
        }

        return cleaned;
    }
}