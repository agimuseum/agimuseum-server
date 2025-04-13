package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.ChangeCurrentPasswordRequest;
import com.agimuseum.magi.exception.PasswordMismatchException;
import com.agimuseum.magi.exception.PasswordValidationException;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to manage user password operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Change password for the currently authenticated user
     * @param request Contains current password and new password details
     * @return true if password was changed successfully
     */
    @Transactional
    public boolean changeCurrentUserPassword(ChangeCurrentPasswordRequest request) {
        // Get current authenticated user
        User currentUser = getCurrentUser();

        // Verify current password
        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            log.warn("Current password verification failed for user: {}", currentUser.getUsername());
            throw new PasswordMismatchException("Current password is incorrect");
        }

        // Verify new password and confirm password match
        if (!request.passwordsMatch()) {
            log.warn("New password and confirmation do not match for user: {}", currentUser.getUsername());
            throw new PasswordMismatchException("New password and confirmation do not match");
        }

        // Ensure new password is different from current password
        if (passwordEncoder.matches(request.newPassword(), currentUser.getPassword())) {
            log.warn("New password cannot be the same as current password for user: {}", currentUser.getUsername());
            throw new PasswordValidationException("New password must be different from current password");
        }

        // Encode new password and save
        String encodedPassword = passwordEncoder.encode(request.newPassword());
        currentUser.setPassword(encodedPassword);
        userRepository.save(currentUser);

        log.info("Password changed successfully for user: {}", currentUser.getUsername());
        return true;
    }

    /**
     * Get the currently authenticated user
     * @return The user entity for the authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}