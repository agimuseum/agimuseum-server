package com.agimuseum.magi.util;

import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Helper class for authentication-related operations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationHelper {

    private final UserRepository userRepository;

    /**
     * Get the current authenticated user entity
     * @return The user entity or empty if not authenticated
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username);
    }

    /**
     * Get the current authenticated user ID
     * @return The user ID or null if not authenticated
     */
    public Integer getCurrentUserId() {
        return getCurrentUser()
                .map(User::getId)
                .orElse(null);
    }

    /**
     * Check if the current user has a specific role
     * @param role The role to check (without the ROLE_ prefix)
     * @return true if the user has the role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        // Format the role string correctly
        String formattedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(formattedRole));
    }

    /**
     * Check if the current user is an admin
     * @return true if the user is an admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if the current user is the owner of a resource
     * @param userId The user ID of the resource owner
     * @return true if the current user is the owner or is an admin
     */
    public boolean isOwnerOrAdmin(Integer userId) {
        if (isAdmin()) {
            return true;
        }

        Integer currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * Get the current authentication details for debugging
     * @return Authentication details as a string
     */
    public String getAuthenticationDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null) {
            return "No authentication found";
        }

        StringBuilder details = new StringBuilder()
                .append("Authenticated: ").append(authentication.isAuthenticated())
                .append(", Principal: ").append(authentication.getName())
                .append(", Authorities: ").append(
                        authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.joining(", "))
                );

        return details.toString();
    }
}