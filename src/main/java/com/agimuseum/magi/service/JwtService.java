package com.agimuseum.magi.service;

import com.agimuseum.magi.model.Role;
import com.agimuseum.magi.model.User;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.function.Function;

/**
 * Service interface for JWT token operations
 */
public interface JwtService {

    /**
     * Duration of access tokens in milliseconds
     */
    long ACCESS_TOKEN_DURATION = 30 * 60 * 1000; // 30 mins

    /**
     * Extract username from token
     * @param token The JWT token
     * @return The username
     */
    String extractUsername(String token);

    /**
     * Validate the token for a user
     * @param token The JWT token
     * @param user The user details
     * @return true if the token is valid for the user
     */
    boolean isValid(String token, UserDetails user);

    /**
     * Extract token expiration date
     * @param token The JWT token
     * @return The expiration date
     */
    Date extractExpiration(String token);

    /**
     * Extract a specific claim from the token
     * @param token The JWT token
     * @param resolver Function to extract the desired claim
     * @return The extracted claim value
     */
    <T> T extractClaim(String token, Function<Claims, T> resolver);

    /**
     * Generate a token for a user
     * @param user The user
     * @return The generated JWT token
     */
    String generateToken(User user);

    /**
     * Extract user ID from token
     * @param token The JWT token
     * @return The user ID
     */
    Integer extractUserId(String token);

    /**
     * Extract user role from token
     * @param token The JWT token
     * @return The user role
     */
    Role extractUserRole(String token);
}