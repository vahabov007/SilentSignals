package com.vahabvahabov.SilentSignals.controller.imp;

import com.vahabvahabov.SilentSignals.controller.TrustedContactController;
import com.vahabvahabov.SilentSignals.dto.TrustedContactDTO;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.model.contact.TrustedContact;
import com.vahabvahabov.SilentSignals.service.TrustedContactService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/trusted/api")
public class TrustedContactControllerImpl implements TrustedContactController {

    private static final Logger logger = LoggerFactory.getLogger(TrustedContactControllerImpl.class);

    @Autowired
    private TrustedContactService trustedContactService;

    @Override
    @PostMapping("/addContact")
    public ResponseEntity<?> addTrustedContact(@Valid @RequestBody TrustedContact trustedContact,
                                               BindingResult result) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            logger.warn("Unauthorized attempt to add trusted contact");
            return ResponseEntity.status(401).body(createResponse(false, "User not authenticated"));
        }

        User user = (User) authentication.getPrincipal();
        String username = user.getUsername();

        logger.info("Adding trusted contact for CURRENT user: {} (ID: {})", username, user.getId());

        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            logger.warn("Validation errors while adding trusted contact for user: {} - {}", username, errors);
            return ResponseEntity.badRequest().body(createResponse(false, "Please check your input", errors));
        }

        try {
            TrustedContactDTO trustedContactDTO = trustedContactService.addTrustedContact(username, trustedContact);
            logger.info("Trusted contact added successfully for user: {}", username);
            return ResponseEntity.ok(createResponse(true, "Trusted contact added successfully", trustedContactDTO));
        } catch (Exception e) {
            logger.error("Error adding trusted contact for user {}: {}", username, e.getMessage());

            String errorMessage;
            if (e.getMessage().contains("User not found")) {
                errorMessage = "User account not found";
            } else if (e.getMessage().contains("already exists")) {
                errorMessage = "You already have a contact with this email address in your trusted contacts.";
            } else if (e.getMessage().contains("Database error")) {
                errorMessage = "A system error occurred. Please try again";
            } else {
                errorMessage = "Failed to add trusted contact: " + e.getMessage();
            }

            return ResponseEntity.badRequest().body(createResponse(false, errorMessage));
        }
    }

    @Override
    @GetMapping("/getAllContacts")
    public ResponseEntity<?> getAllTrustedContacts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            logger.warn("Unauthorized attempt to get all trusted contacts");
            return ResponseEntity.status(401).body(createResponse(false, "User not authenticated"));
        }

        try {
            User user = (User) authentication.getPrincipal();
            List<TrustedContactDTO> contacts = trustedContactService.getAllTrustedContactsByUserId(user.getId());
            logger.info("Retrieved {} trusted contacts for user: {}", contacts.size(), user.getUsername());
            return ResponseEntity.ok(createResponse(true, "Trusted contacts retrieved successfully", contacts));
        } catch (Exception e) {
            logger.error("Error retrieving trusted contacts: {}", e.getMessage());
            return ResponseEntity.status(500).body(createResponse(false, "Failed to retrieve trusted contacts: " + e.getMessage()));
        }
    }

    @Override
    @DeleteMapping("/deleteContact/{contactId}")
    public ResponseEntity<?> deleteTrustedContactById(@PathVariable(name = "contactId") Long contactId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            logger.warn("Unauthorized attempt to delete trusted contact");
            return ResponseEntity.status(401).body(createResponse(false, "User not authenticated"));
        }

        User user = (User) authentication.getPrincipal();
        Long userId = user.getId();

        logger.info("Received request to delete trusted contact ID: {} for CURRENT user ID: {} ({})",
                contactId, userId, user.getUsername());

        try {
            trustedContactService.removeTrustedContact(userId, contactId);
            logger.info("Trusted contact ID: {} deleted successfully for user ID: {}", contactId, userId);
            return ResponseEntity.ok(createResponse(true, "Trusted contact deleted successfully"));
        } catch (Exception e) {
            logger.error("Error deleting trusted contact ID {} for user ID {}: {}", contactId, userId, e.getMessage());

            String errorMessage;
            if (e.getMessage().contains("permission")) {
                errorMessage = "You don't have permission to delete this contact";
            } else if (e.getMessage().contains("not found")) {
                errorMessage = "Contact not found";
            } else {
                errorMessage = "Failed to delete trusted contact: " + e.getMessage();
            }

            return ResponseEntity.badRequest().body(createResponse(false, errorMessage));
        }
    }

    private Map<String, Object> createResponse(boolean success, String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        response.put("data", data);
        return response;
    }

    private Map<String, Object> createResponse(boolean success, String message) {
        return createResponse(success, message, null);
    }
}