package com.agimuseum.magi.exception;

/**
 * Exception thrown when the provided passwords do not match
 */
public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}
