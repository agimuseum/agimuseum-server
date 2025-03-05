package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.UserDTO;
import com.agimuseum.magi.model.BlacklistedToken;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.RefreshToken;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.BlacklistedTokenRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.RefreshTokenRepository;
import com.agimuseum.magi.repository.UserRepository;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final PhotoRepository photoRepository;
    private final Storage storage;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    public UserService(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       BlacklistedTokenRepository blacklistedTokenRepository,
                       PhotoRepository photoRepository,
                       Storage storage) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.blacklistedTokenRepository = blacklistedTokenRepository;
        this.photoRepository = photoRepository;
        this.storage = storage;
    }

    public UserDTO getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        return mapToDTO(user);
    }

    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        return mapToDTO(user);
    }

    public UserDTO getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new EntityNotFoundException("No authenticated user found");
        }
        return getUserByUsername(authentication.getName());
    }

    @Transactional
    public UserDTO updateUser(Integer id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        updateUserFields(user, userDTO);
        User updatedUser = userRepository.save(user);
        return mapToDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Delete user photos from Firebase storage
        List<Photo> userPhotos = photoRepository.findByUser(user);
        for (Photo photo : userPhotos) {
            // Delete from Firebase storage
            BlobId blobId = BlobId.of(storageBucket, photo.getFirebasePath());
            storage.delete(blobId);
        }

        // Delete refresh tokens
        refreshTokenRepository.deleteByUser(user);

        // Delete user from database - cascade will handle related entities
        userRepository.delete(user);

        // Log out current session if deleting own account
        try {
            if (isCurrentUser(id)) {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            // If there's any issue checking the current user, just continue with the deletion
        }
    }

    @Transactional
    public void softDeleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        // Soft delete by deactivating the account and anonymizing personal data
        user.setActive(false);
        user.setFirstname("Deleted");
        user.setLastname("User");
        // Keep the username but append a timestamp to make it unique and unusable
        user.setUsername("deleted_" + user.getUsername() + "_" + System.currentTimeMillis());

        // Invalidate all sessions
        refreshTokenRepository.deleteByUser(user);

        // Save the updated user
        userRepository.save(user);

        // Log out current session if soft-deleting own account
        try {
            if (isCurrentUser(id)) {
                SecurityContextHolder.clearContext();
            }
        } catch (Exception e) {
            // If there's any issue checking the current user, just continue with the deletion
        }
    }

    public boolean isCurrentUser(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        return userRepository.findByUsername(authentication.getName())
                .map(user -> user.getId().equals(userId))
                .orElse(false);
    }

    private void updateUserFields(User user, UserDTO userDTO) {
        user.setFirstname(userDTO.getFirstname());
        user.setLastname(userDTO.getLastname());
        user.setZipCode(userDTO.getZipCode());
        user.setVisiting(userDTO.isVisiting());
        user.setNightInHotel(userDTO.isNightInHotel());

        // Handle hotel-related fields conditionally
        if (userDTO.isNightInHotel()) {
            user.setHotelName(userDTO.getHotelName());
            user.setNumberOfNights(userDTO.getNumberOfNights());
            user.setNumberOfRooms(userDTO.getNumberOfRooms());
        } else {
            // Clear hotel-related fields when not staying in hotel
            user.setHotelName(null);
            user.setNumberOfNights(null);
            user.setNumberOfRooms(null);
        }

        // Ensure numberOfPeople is always set
        if (userDTO.getNumberOfPeople() != null) {
            user.setNumberOfPeople(userDTO.getNumberOfPeople());
        } else {
            // If not provided in the DTO, keep the existing value or set a default
            if (user.getNumberOfPeople() == null) {
                user.setNumberOfPeople(1);
            }
        }

        // Don't update sensitive fields like password, role, etc.
    }

    private UserDTO mapToDTO(User user) {
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