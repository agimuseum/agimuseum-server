package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.UserDTO;
import com.agimuseum.magi.model.Role;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.UserService;
import com.agimuseum.magi.util.ApiErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for admin-only user management operations
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final UserService userService;

    /**
     * Get all users
     */
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        log.info("Admin fetching all users");

        List<User> users = userRepository.findAll();
        List<UserDTO> userDTOs = users.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(userDTOs);
    }

    /**
     * Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        log.info("Admin fetching user with ID: {}", id);

        try {
            return ResponseEntity.ok(userService.getUserById(id));
        } catch (Exception e) {
            log.error("Error fetching user with ID: {}", id, e);
            return ApiErrorUtil.createNotFoundResponse("User", id);
        }
    }

    /**
     * Update user role (promote/demote)
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Integer id,
            @RequestParam Role role) {

        log.info("Admin updating role for user ID: {} to {}", id, role);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            user.setRole(role);
            User updatedUser = userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("role", updatedUser.getRole());
            response.put("message", "User role updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating user role", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to update user role: " + e.getMessage());
        }
    }

    /**
     * Disable/enable user account
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Integer id,
            @RequestParam boolean active) {

        log.info("Admin updating status for user ID: {} to active={}", id, active);

        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

            user.setActive(active);
            User updatedUser = userRepository.save(user);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedUser.getId());
            response.put("username", updatedUser.getUsername());
            response.put("active", updatedUser.isActive());
            response.put("message", "User status updated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error updating user status", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to update user status: " + e.getMessage());
        }
    }

    /**
     * Delete user (hard delete)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Integer id) {
        log.info("Admin deleting user with ID: {}", id);

        try {
            userService.deleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User deleted successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error deleting user", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to delete user: " + e.getMessage());
        }
    }

    /**
     * Soft delete user (deactivate account)
     */
    @PostMapping("/{id}/soft-delete")
    public ResponseEntity<?> softDeleteUser(@PathVariable Integer id) {
        log.info("Admin soft-deleting user with ID: {}", id);

        try {
            userService.softDeleteUser(id);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User account deactivated successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error soft-deleting user", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to deactivate user account: " + e.getMessage());
        }
    }

    /**
     * Get user registration statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        log.info("Admin fetching user statistics");

        List<User> allUsers = userRepository.findAll();

        // Count active/inactive users
        long activeUsers = allUsers.stream().filter(User::isActive).count();
        long inactiveUsers = allUsers.size() - activeUsers;

        // Count users by role
        long adminCount = allUsers.stream().filter(u -> Role.ADMIN.equals(u.getRole())).count();
        long userCount = allUsers.stream().filter(u -> Role.USER.equals(u.getRole())).count();

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", allUsers.size());
        statistics.put("activeUsers", activeUsers);
        statistics.put("inactiveUsers", inactiveUsers);
        statistics.put("adminUsers", adminCount);
        statistics.put("regularUsers", userCount);

        return ResponseEntity.ok(statistics);
    }

    /**
     * Helper method to convert User entity to UserDTO
     */
    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .zipCode(user.getZipCode())
                .isVisiting(user.isVisiting())
                .isNightInHotel(user.isNightInHotel())
                .hotelName(user.getHotelName())
                .numberOfNights(user.getNumberOfNights())
                .numberOfRooms(user.getNumberOfRooms())
                .numberOfPeople(user.getNumberOfPeople())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .active(user.isActive())
                .lastLogin(user.getLastLogin())
                .build();
    }
}