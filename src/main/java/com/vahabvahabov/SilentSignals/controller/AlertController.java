package com.vahabvahabov.SilentSignals.controller;

import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface AlertController {

    public ResponseEntity<?> sendAlert(Authentication authentication, Map<String, String> body);


}
