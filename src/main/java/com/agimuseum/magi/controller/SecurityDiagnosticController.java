package com.agimuseum.magi.controller;

import com.agimuseum.magi.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for diagnosing security issues
 * This should be disabled in production!
 */
@RestController
@RequestMapping("/api/debug/security")
@Slf4j
public class SecurityDiagnosticController {

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSecurityStatus(HttpServletRequest request) {
        Map<String, Object> status = new HashMap<>();

        // Log request details for debugging
        SecurityUtils.logRequestDetails(request);

        // Get current authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null) {
            status.put("authenticated", authentication.isAuthenticated());
            status.put("principal", authentication.getName());
            status.put("authorities", authentication.getAuthorities().toString());
        } else {
            status.put("authenticated", false);
            status.put("principal", "none");
            status.put("authorities", "none");
        }

        status.put("requestMethod", request.getMethod());
        status.put("requestURI", request.getRequestURI());

        // Add security headers that were received
        Map<String, String> securityHeaders = new HashMap<>();
        securityHeaders.put("Origin", request.getHeader("Origin"));
        securityHeaders.put("Access-Control-Request-Method", request.getHeader("Access-Control-Request-Method"));
        securityHeaders.put("Access-Control-Request-Headers", request.getHeader("Access-Control-Request-Headers"));
        securityHeaders.put("Authorization", request.getHeader("Authorization") != null ? "PRESENT" : "MISSING");

        status.put("securityHeaders", securityHeaders);

        return ResponseEntity.ok(status);
    }

    @GetMapping("/public-test")
    public ResponseEntity<String> publicTest() {
        return ResponseEntity.ok("This is a public endpoint that should be accessible without authentication");
    }

    @GetMapping("/protected-test")
    public ResponseEntity<String> protectedTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok("This is a protected endpoint. You are authenticated as: " + auth.getName());
    }
}