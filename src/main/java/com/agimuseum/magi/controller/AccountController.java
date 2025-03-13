package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.AccountDeletionRequest;
import com.agimuseum.magi.dto.UserDTO;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserService userService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteAccount(@RequestBody AccountDeletionRequest request,
                                           @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(401).body("User not authenticated");
            }

            String username = authentication.getName();

            // Print debug information
            System.out.println("Processing account deletion for: " + username);
            System.out.println("Provided password length: " +
                    (request.getConfirmPassword() != null ? request.getConfirmPassword().length() : "null"));

            // Get user from database
            User currentUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalStateException("Current user not found"));

            // Explicit password validation with detailed logging
            boolean passwordMatches = false;
            try {
                passwordMatches = passwordEncoder.matches(request.getConfirmPassword(), currentUser.getPassword());
                System.out.println("Password validation result: " + passwordMatches);
            } catch (Exception e) {
                System.err.println("Error during password validation: " + e.getMessage());
                return ResponseEntity.badRequest().body("Password validation error");
            }

            // Explicit check and early return if password doesn't match
            if (!passwordMatches) {
                System.out.println("Password validation failed, returning 400");
                return ResponseEntity.badRequest().body("Password confirmation failed");
            }

            // If we get here, password is valid, proceed with deletion
            System.out.println("Password validated, proceeding with deletion");

            if (permanent) {
                userService.deleteUser(currentUser.getId());
            } else {
                userService.softDeleteUser(currentUser.getId());
            }

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }
    }

    // Admin endpoint to delete any user account
    @DeleteMapping("/admin/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> adminDeleteUser(@PathVariable Integer id,
                                                @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        if (permanent) {
            userService.deleteUser(id);
        } else {
            userService.softDeleteUser(id);
        }
        return ResponseEntity.noContent().build();
    }
}