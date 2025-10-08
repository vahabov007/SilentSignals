package com.vahabvahabov.SilentSignals.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

public interface AuthController {

    public ResponseEntity<?> createAuthenticationToken(Map<String, String> authRequest);
}
