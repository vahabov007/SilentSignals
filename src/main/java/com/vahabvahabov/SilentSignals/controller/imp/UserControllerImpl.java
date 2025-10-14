package com.vahabvahabov.SilentSignals.controller.imp;

import com.vahabvahabov.SilentSignals.controller.UserController;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserControllerImpl implements UserController {

    private final Logger logger = LoggerFactory.getLogger(UserControllerImpl.class);

    @Autowired
    private UserService userService;

    @Override
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
                return ResponseEntity.status(401).body(createResponse(false, "User not authenticated"));
            }

            User user = (User) authentication.getPrincipal();
            logger.info("Loading profile for CURRENT user: {} (ID: {})", user.getUsername(), user.getId());

            Map<String, Object> userProfile = new HashMap<>();
            userProfile.put("id", user.getId());
            userProfile.put("username", user.getUsername());
            userProfile.put("email", user.getMail());
            userProfile.put("dateOfBirth", user.getDate_of_birth());
            userProfile.put("enabled", user.isEnabled());
            userProfile.put("emailVerified", user.isEmailVerified());
            userProfile.put("createdAt", user.getCreatedAt());

            return ResponseEntity.ok(createResponse(true, userProfile));

        } catch (Exception e) {
            logger.error("Error loading user profile: {}", e.getMessage());
            return ResponseEntity.status(500).body(createResponse(false, "Failed to load user profile"));
        }

    }
    private Map<String, Object> createResponse(boolean success, Object message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
}