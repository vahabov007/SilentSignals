package com.vahabvahabov.SilentSignals.controller.imp;

import com.vahabvahabov.SilentSignals.controller.VerificationController;
import com.vahabvahabov.SilentSignals.model.register.PinVerificationRequest;
import com.vahabvahabov.SilentSignals.model.register.RegisterRequest;
import com.vahabvahabov.SilentSignals.model.User;
import com.vahabvahabov.SilentSignals.service.EmailService;
import com.vahabvahabov.SilentSignals.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.dao.DataAccessException;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/register")
public class VerificationControllerImpl implements VerificationController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, String> pinStorage = new HashMap<>();
    private Map<String, Date> pinExpirationStorage = new HashMap<>();

    @Override
    @PostMapping("/send-pin")
    public ResponseEntity<?> sendPin(@RequestBody Map<String, String> request) throws MessagingException {
        String mail = request.get("mail");

        if (mail == null || mail.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createResponse(false, "Email address is required"));
        }

        Optional<User> existingUser = userService.findUserByMail(mail);
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body(createResponse(false, "This email address is already registered!"));
        }

        String pin = String.format("%06d", new Random().nextInt(999999));
        Date expirationTime = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10 minutes

        pinStorage.put(mail, pin);
        pinExpirationStorage.put(mail, expirationTime);

        emailService.sendPinToEmail(mail, pin);

        return ResponseEntity.ok(createResponse(true, "Your code has been sent to your email address."));
    }

    @Override
    @PostMapping("/verify-pin")
    public ResponseEntity<?> verifyPin(@RequestBody PinVerificationRequest request) {
        String mail = request.getMail();
        String pin = request.getPin();

        if (mail == null || pin == null) {
            return ResponseEntity.badRequest().body(createResponse(false, "Email and PIN are required"));
        }

        String storedPin = pinStorage.get(mail);
        Date expirationTime = pinExpirationStorage.get(mail);

        if (storedPin == null || expirationTime == null) {
            return ResponseEntity.badRequest().body(createResponse(false, "No PIN found for this email. Please request a new PIN."));
        }

        if (!storedPin.equals(pin)) {
            return ResponseEntity.badRequest().body(createResponse(false, "The entered PIN code is incorrect!"));
        }

        if (new Date().after(expirationTime)) {
            pinStorage.remove(mail);
            pinExpirationStorage.remove(mail);
            return ResponseEntity.badRequest().body(createResponse(false, "The PIN code has expired!"));
        }

        pinStorage.remove(mail);
        pinExpirationStorage.remove(mail);
        return ResponseEntity.ok(createResponse(true, "Email confirmed!"));
    }

    @Override
    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(@RequestBody RegisterRequest request) {
        try {
            if (request == null) {
                return ResponseEntity.badRequest().body(createResponse(false, "Registration data is required."));
            }

            if (request.getMail() == null || request.getMail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(false, "Email is required."));
            }

            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(false, "Username is required."));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(false, "Password is required."));
            }

            if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createResponse(false, "Password confirmation is required."));
            }

            if (request.getDateOfBirth() == null) {
                return ResponseEntity.badRequest().body(createResponse(false, "Date of birth is required."));
            }

            if (userService.findUserByMail(request.getMail()).isPresent()) {
                return ResponseEntity.badRequest().body(createResponse(false, "This email address is already registered!"));
            }

            String lowercaseUsername = request.getUsername().toLowerCase().trim();
            if (userService.findUserByUsername(lowercaseUsername).isPresent()) {
                return ResponseEntity.badRequest().body(createResponse(false, "This username is already registered!"));
            }

            String password = request.getPassword();
            String confirmPassword = request.getConfirmPassword();

            if (password.length() < 8) {
                return ResponseEntity.badRequest().body(createResponse(false, "Password must be at least 8 characters long."));
            }

            if (!password.matches(".*\\d.*")) {
                return ResponseEntity.badRequest().body(createResponse(false, "Password must contain at least one number."));
            }

            if (!password.equals(confirmPassword)) {
                return ResponseEntity.badRequest().body(createResponse(false, "The passwords do not match!"));
            }

            User newUser = new User();
            newUser.setUsername(lowercaseUsername);
            newUser.setMail(request.getMail().trim());
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setDate_of_birth(request.getDateOfBirth());
            newUser.setEmailVerified(true);
            newUser.setCreatedAt(LocalDateTime.now());

            userService.saveNewUser(newUser);

            return ResponseEntity.ok(createResponse(true, "Registration has been completed successfully!"));

        } catch (DataAccessException e) {
            System.err.println("Database access error during registration: " + e.getMessage());
            return ResponseEntity.badRequest().body(createResponse(false, "There was a problem with the database. Please try again."));
        } catch (Exception e) {
            System.err.println("Unexpected error during registration: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(createResponse(false, "Something went wrong. Please try again."));
        }
    }

    @PostMapping("/resend-pin")
    public ResponseEntity<?> resendPin(@RequestBody Map<String, String> request) throws MessagingException {
        String mail = request.get("mail");
        if (mail == null || mail.isEmpty()) {
            return ResponseEntity.badRequest().body(createResponse(false, "Email address is required!"));
        }

        Optional<User> existingUser = userService.findUserByMail(mail);
        if (existingUser.isPresent()) {
            return ResponseEntity.badRequest().body(createResponse(false, "This email address is already registered!"));
        }

        String pin = String.format("%06d", new Random().nextInt(999999));
        Date expirationTime = new Date(System.currentTimeMillis() + 10 * 60 * 1000); // 10 minutes

        pinStorage.put(mail, pin);
        pinExpirationStorage.put(mail, expirationTime);

        emailService.sendPinToEmail(mail, pin);

        return ResponseEntity.ok(createResponse(true, "A new PIN has been sent to your email address."));
    }

    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
}