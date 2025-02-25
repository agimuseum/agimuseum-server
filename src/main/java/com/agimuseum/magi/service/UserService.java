package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.UserDTO;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
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

    public boolean isCurrentUser(Integer userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Current user not found"));
        return currentUser.getId().equals(userId);
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

        // Number of people is always required
        user.setNumberOfPeople(userDTO.getNumberOfPeople());

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