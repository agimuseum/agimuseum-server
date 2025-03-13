package com.agimuseum.magi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Data Transfer Object for password change operations.
 * Contains both the new password and the repeated password for confirmation.
 */
public record ChangePassword(
        @NotBlank(message = "Password is required")
        @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
                message = "Password must be 8 characters long and contain at least one number, one uppercase, one lowercase letter and one special character")
        String password,

        @NotBlank(message = "Confirm password is required")
        String repeatPassword
) {
    /**
     * Validates that both passwords match.
     * @return true if passwords match, false otherwise
     */
    public boolean passwordsMatch() {
        return password != null && password.equals(repeatPassword);
    }
}