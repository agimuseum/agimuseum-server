package com.agimuseum.magi.controller;

import com.agimuseum.magi.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller for testing password validation
 * This should be removed in production!
 */
@RestController
@RequestMapping("/api/debug")
public class DebugController {

    private final AccountService accountService;

    public DebugController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/validate-password")
    public ResponseEntity<?> validatePassword(@RequestBody Map<String, String> request) {
        String password = request.get("password");

        boolean isValid = accountService.validatePassword(password);

        Map<String, Object> response = new HashMap<>();
        response.put("passwordValid", isValid);

        return ResponseEntity.ok(response);
    }
}