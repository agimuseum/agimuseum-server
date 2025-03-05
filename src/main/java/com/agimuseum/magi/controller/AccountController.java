package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.AccountDeletionRequest;
import com.agimuseum.magi.dto.UserDTO;
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
    private final PasswordEncoder passwordEncoder;

    public AccountController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/delete")
    public ResponseEntity<Void> deleteAccount(@RequestBody AccountDeletionRequest request,
                                              @RequestParam(value = "permanent", defaultValue = "false") boolean permanent) {
        try {
            // Get the current authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() ||
                    "anonymousUser".equals(authentication.getPrincipal())) {
                return ResponseEntity.status(401).build(); // Unauthorized
            }

            String username = authentication.getName();

            // Get user from username
            UserDTO currentUser = userService.getUserByUsername(username);
            if (currentUser == null) {
                return ResponseEntity.status(404).build(); // Not found
            }

            Integer userId = currentUser.getId();

            // Choose deletion method based on parameter
            if (permanent) {
                // Hard delete - completely remove the account
                userService.deleteUser(userId);
            } else {
                // Soft delete - deactivate the account but keep record
                userService.softDeleteUser(userId);
            }

            // Return 204 No Content status
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            // Log the exception
            e.printStackTrace();
            return ResponseEntity.status(500).build(); // Internal server error
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

    // Additional account management endpoints can be added here
}