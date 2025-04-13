package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.ChangeCurrentPasswordRequest;
import com.agimuseum.magi.exception.PasswordMismatchException;
import com.agimuseum.magi.exception.PasswordValidationException;
import com.agimuseum.magi.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling password-related operations
 */
@RestController
@RequestMapping("/api/account/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordController {

    private final PasswordService passwordService;

    /**
     * Endpoint for changing password of a logged-in user
     *
     * @param request Contains current password and new password details
     * @return Response with success message or error details
     */
    @PostMapping("/change")
    public ResponseEntity<?> changePassword(@Valid @RequestBody ChangeCurrentPasswordRequest request) {
        log.info("Processing password change request");
        Map<String, Object> response = new HashMap<>();

        try {
            passwordService.changeCurrentUserPassword(request);

            response.put("success", true);
            response.put("message", "Password changed successfully");
            return ResponseEntity.ok(response);

        } catch (PasswordMismatchException e) {
            log.warn("Password mismatch: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (PasswordValidationException e) {
            log.warn("Password validation error: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (Exception e) {
            log.error("Error processing password change request", e);
            response.put("success", false);
            response.put("message", "An unexpected error occurred");
            return ResponseEntity.internalServerError().body(response);
        }
    }
}