package com.vahabvahabov.SilentSignals.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface UserController {
    public ResponseEntity<?> getUserProfile();

}



