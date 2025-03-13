package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.AccountDeletionRequest;
import com.agimuseum.magi.exception.PasswordMismatchException;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(UserService userService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Validates the provided password against the current user's stored password
     * @param confirmPassword The password to validate
     * @return true if the password matches, false otherwise
     */
    public boolean validatePassword(String confirmPassword) {
        // Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        // Validate password - ensure both values are non-null before comparison
        if (confirmPassword == null || currentUser.getPassword() == null) {
            return false;
        }

        // Use passwordEncoder.matches to verify the password
        boolean matches = passwordEncoder.matches(confirmPassword, currentUser.getPassword());

        // For debugging
        System.out.println("Password validation result: " + matches);

        return matches;
    }

    /**
     * Process account deletion request with password validation
     * @param request The account deletion request
     * @param permanent Whether to permanently delete the account or just deactivate it
     */
    public void processAccountDeletion(AccountDeletionRequest request, boolean permanent) {
        // Validate the password first
        if (!validatePassword(request.getConfirmPassword())) {
            throw new PasswordMismatchException("Password confirmation failed. Cannot delete account.");
        }

        // If password is valid, proceed with deletion
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));

        Integer userId = currentUser.getId();

        // Log deletion for audit purposes
        System.out.println("Deleting account for user: " + username + ", permanent: " + permanent);

        // Choose deletion method based on parameter
        if (permanent) {
            userService.deleteUser(userId);
        } else {
            userService.softDeleteUser(userId);
        }
    }
}