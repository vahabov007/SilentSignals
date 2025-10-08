package com.vahabvahabov.SilentSignals.controller.imp;

import com.vahabvahabov.SilentSignals.controller.AuthController;
import com.vahabvahabov.SilentSignals.security.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
public class AuthControllerImpl implements AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;


    @Override
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody Map<String, String> authRequest) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.get("username"), authRequest.get("password"))
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(createResponse(false, "Invalid username or password"));
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.get("username"));
        final String jwt = jwtUtil.generateToken(userDetails);

        Map<String, Object> response = createResponse(true, "Login successful");
        response.put("token", jwt);
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> createResponse(boolean success, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("message", message);
        return response;
    }
}
