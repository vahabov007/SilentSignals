package com.vahabvahabov.SilentSignals.service.imp;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.vahabvahabov.SilentSignals.service.SmsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SmsServiceImpl implements SmsService {

    private final Logger logger = LoggerFactory.getLogger(SmsServiceImpl.class);

    @Value("${twilio.account.sid}")
    private String twilioSid;

    @Value("${twilio.auth.token}")
    private String twilioToken;

    @Value("${twilio.phone.number}")
    private String twilioPhone;


    @Override
    public void sendSosAlert(String toPhone, String username, String description, String locationAddress) {
        try {
            Twilio.init(twilioSid, twilioToken);
            Message.creator(
                    new com.twilio.type.PhoneNumber(toPhone),
                    new com.twilio.type.PhoneNumber(twilioPhone),
                    "SOS Alert from " + username + ": " + description + " at " + locationAddress
            ).create();
            logger.info("SMS sent to: {}", toPhone);
        } catch (Exception e) {
            logger.error("SMS sending failed for {}: {}", toPhone, e.getMessage());
            throw new RuntimeException("Failed to send SMS: " + e.getMessage());
        }

    }
}
