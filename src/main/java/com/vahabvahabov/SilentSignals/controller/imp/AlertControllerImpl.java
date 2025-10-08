package com.vahabvahabov.SilentSignals.controller.imp;

import com.vahabvahabov.SilentSignals.controller.AlertController;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.service.AlertService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/alert")
public class AlertControllerImpl implements AlertController {

    @Autowired
    private AlertService alertService;

    private final Logger logger = LoggerFactory.getLogger(AlertControllerImpl.class);

    @Override
    @PostMapping("/send")
    public ResponseEntity<?> sendAlert(Authentication authentication,
                                       @RequestBody Map<String, String> body) {
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("Unauthorized attempt to send SOS alert");
            return ResponseEntity.status(401).body(createResponse(false, "User not authenticated"));
        }

        User user = (User) authentication.getPrincipal();

        if (!user.isEnabled()) {
            logger.warn("User {} attempted to send SOS alert without email verification", user.getUsername());
            return ResponseEntity.status(403).body(createResponse(false, "Email not verified"));
        }

        try {
            String description = body.get("description");
            String locationCoordinates = body.getOrDefault("locationCoordinates", "");
            String locationAddress = body.getOrDefault("locationAddress", "");

            if (description == null || description.trim().isEmpty()) {
                logger.warn("Invalid SOS alert request: description is empty for user {}", user.getUsername());
                return ResponseEntity.badRequest().body(createResponse(false, "Description is required"));
            }

            alertService.sendAlert(user, description, locationCoordinates, locationAddress);
            logger.info("SOS alert sent successfully by user: {}", user.getUsername());
            return ResponseEntity.ok(createResponse(true, "Alert sent successfully"));
        } catch (Exception e) {
            logger.error("Error sending SOS alert for user {}: {}", user.getUsername(), e.getMessage());
            return ResponseEntity.status(500).body(createResponse(false, "Failed to send alert: " + e.getMessage()));
        }
    }

    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
}