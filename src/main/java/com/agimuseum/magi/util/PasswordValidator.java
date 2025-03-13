package com.agimuseum.magi.util;

import com.agimuseum.magi.exception.PasswordMismatchException;
import com.agimuseum.magi.exception.PasswordValidationException;

import java.util.regex.Pattern;

/**
 * Utility class for password validation
 */
public class PasswordValidator {

    // Regex pattern for password validation
    // Requires at least 8 characters, one uppercase, one lowercase, one number, and one special character
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");

    /**
     * Validates that passwords match
     *
     * @param password The password
     * @param confirmPassword The confirmation password
     * @throws PasswordMismatchException if passwords don't match
     */
    public static void validatePasswordsMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException("Passwords do not match");
        }
    }

    /**
     * Validates password strength using the defined pattern
     *
     * @param password The password to validate
     * @throws PasswordValidationException if password doesn't meet requirements
     */
    public static void validatePasswordStrength(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new PasswordValidationException("Password cannot be empty");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            throw new PasswordValidationException(
                    "Password must be at least 8 characters long and contain at least " +
                            "one uppercase letter, one lowercase letter, one number, and one special character"
            );
        }
    }

    /**
     * Performs comprehensive password validation
     *
     * @param password The password
     * @param confirmPassword The confirmation password
     * @throws PasswordMismatchException if passwords don't match
     * @throws PasswordValidationException if password doesn't meet requirements
     */
    public static void validatePassword(String password, String confirmPassword) {
        validatePasswordStrength(password);
        validatePasswordsMatch(password, confirmPassword);
    }
}