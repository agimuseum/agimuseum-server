package com.agimuseum.magi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for changing the password of a logged-in user.
 * Contains the old password for verification and the new password with confirmation.
 */
public record ChangeCurrentPasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank(message = "New password is required")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Password must be 8 characters long and contain at least one number, one uppercase, one lowercase letter and one special character")
        String newPassword,

        @NotBlank(message = "Confirm password is required")
        String confirmPassword
) {
    /**
     * Validates that both new password and confirmation match.
     * @return true if passwords match, false otherwise
     */
    public boolean passwordsMatch() {
        return newPassword != null && newPassword.equals(confirmPassword);
    }
}