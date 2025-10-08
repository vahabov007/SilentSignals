package com.vahabvahabov.SilentSignals.controller;

import com.vahabvahabov.SilentSignals.model.register.PinVerificationRequest;
import com.vahabvahabov.SilentSignals.model.register.ResetPasswordRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface ForgotPasswordController {

    public ResponseEntity<?> sendPin(Map<String, String> request);

    public ResponseEntity<?> verifyPin(PinVerificationRequest request);

    public ResponseEntity<?> resetPassword(ResetPasswordRequest request);
}
