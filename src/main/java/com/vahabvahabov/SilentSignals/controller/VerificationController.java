package com.vahabvahabov.SilentSignals.controller;

import com.vahabvahabov.SilentSignals.model.register.PinVerificationRequest; // DTO paketini istifadə edirik
import com.vahabvahabov.SilentSignals.model.register.RegisterRequest; // DTO paketini istifadə edirik
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;

public interface VerificationController {

    public ResponseEntity<?> sendPin(@RequestBody Map<String, String> request) throws MessagingException;

    public ResponseEntity<?> verifyPin(@RequestBody PinVerificationRequest request);

    public ResponseEntity<?> completeRegistration(@RequestBody RegisterRequest request);

}