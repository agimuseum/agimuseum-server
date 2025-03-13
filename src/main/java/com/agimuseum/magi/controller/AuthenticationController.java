package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.LoginRequest;
import com.agimuseum.magi.dto.RegisterRequest;
import com.agimuseum.magi.dto.TokenResponse;
import com.agimuseum.magi.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@Slf4j
public class AuthenticationController {

    private final AuthenticationService authService;

    public AuthenticationController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for user: {}", request.getUsername());
        TokenResponse response = authService.register(request);
        log.info("Registration successful for user: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login request received for user: {}", request.getUsername());
        TokenResponse response = authService.authenticate(request);
        log.info("Login successful for user: {}", request.getUsername());
        return ResponseEntity.ok(response);
    }
}