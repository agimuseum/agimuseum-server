package com.agimuseum.magi.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for creating standardized API error responses
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiErrorUtil {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    /**
     * Create a standard error response
     * @param message The error message
     * @param status The HTTP status code
     * @return A ResponseEntity with a standardized error structure
     */
    public static ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Create a standard error response with additional details
     * @param message The error message
     * @param status The HTTP status code
     * @param details Additional error details
     * @return A ResponseEntity with a standardized error structure
     */
    public static ResponseEntity<Map<String, Object>> createErrorResponse(String message, HttpStatus status, Map<String, Object> details) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);

        if (details != null && !details.isEmpty()) {
            errorResponse.put("details", details);
        }

        return ResponseEntity.status(status).body(errorResponse);
    }

    /**
     * Create a validation error response with field errors
     * @param message The error message
     * @param fieldErrors Map of field names to error messages
     * @return A ResponseEntity with a standardized validation error structure
     */
    public static ResponseEntity<Map<String, Object>> createValidationErrorResponse(String message, Map<String, String> fieldErrors) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMATTER));
        errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
        errorResponse.put("error", "Validation Error");
        errorResponse.put("message", message);
        errorResponse.put("fieldErrors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Create a not found error response
     * @param resourceType The type of resource that was not found
     * @param id The ID of the resource that was not found
     * @return A ResponseEntity with a standardized not found error structure
     */
    public static ResponseEntity<Map<String, Object>> createNotFoundResponse(String resourceType, Object id) {
        String message = resourceType + " not found with id: " + id;
        return createErrorResponse(message, HttpStatus.NOT_FOUND);
    }

    /**
     * Create an unauthorized error response
     * @param message The error message
     * @return A ResponseEntity with a standardized unauthorized error structure
     */
    public static ResponseEntity<Map<String, Object>> createUnauthorizedResponse(String message) {
        return createErrorResponse(message, HttpStatus.UNAUTHORIZED);
    }

    /**
     * Create a forbidden error response
     * @param message The error message
     * @return A ResponseEntity with a standardized forbidden error structure
     */
    public static ResponseEntity<Map<String, Object>> createForbiddenResponse(String message) {
        return createErrorResponse(message, HttpStatus.FORBIDDEN);
    }

    /**
     * Create a conflict error response (for duplicate resources)
     * @param message The error message
     * @return A ResponseEntity with a standardized conflict error structure
     */
    public static ResponseEntity<Map<String, Object>> createConflictResponse(String message) {
        return createErrorResponse(message, HttpStatus.CONFLICT);
    }

    /**
     * Create a bad request error response
     * @param message The error message
     * @return A ResponseEntity with a standardized bad request error structure
     */
    public static ResponseEntity<Map<String, Object>> createBadRequestResponse(String message) {
        return createErrorResponse(message, HttpStatus.BAD_REQUEST);
    }

    /**
     * Create an internal server error response
     * @param message The error message
     * @return A ResponseEntity with a standardized internal server error structure
     */
    public static ResponseEntity<Map<String, Object>> createInternalServerErrorResponse(String message) {
        return createErrorResponse(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}