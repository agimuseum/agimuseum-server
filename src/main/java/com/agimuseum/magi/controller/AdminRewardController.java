package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.RewardDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Reward;
import com.agimuseum.magi.repository.RewardRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/rewards")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminRewardController {

    private final RewardRepository rewardRepository;

    /**
     * Get all rewards (including inactive)
     */
    @GetMapping
    public ResponseEntity<List<RewardDTO>> getAllRewards() {
        log.info("Admin fetching all rewards");
        Date now = new Date();

        return ResponseEntity.ok(rewardRepository.findAll().stream()
                .map(reward -> RewardDTO.builder()
                        .id(reward.getId())
                        .name(reward.getName())
                        .description(reward.getDescription())
                        .code(reward.getCode())
                        .expirationDate(reward.getExpirationDate())
                        .requiredLocations(reward.getRequiredLocations())
                        .requiresPhotoVerification(reward.getRequiresPhotoVerification())
                        .claimable(reward.getActive() && reward.getExpirationDate().after(now))
                        .build())
                .collect(Collectors.toList()));
    }

    /**
     * Get a specific reward by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RewardDTO> getRewardById(@PathVariable Integer id) {
        log.info("Admin fetching reward with ID: {}", id);

        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + id));

        Date now = new Date();

        return ResponseEntity.ok(RewardDTO.builder()
                .id(reward.getId())
                .name(reward.getName())
                .description(reward.getDescription())
                .code(reward.getCode())
                .expirationDate(reward.getExpirationDate())
                .requiredLocations(reward.getRequiredLocations())
                .requiresPhotoVerification(reward.getRequiresPhotoVerification())
                .claimable(reward.getActive() && reward.getExpirationDate().after(now))
                .build());
    }

    /**
     * Create a new reward
     */
    @PostMapping
    public ResponseEntity<?> createReward(@Valid @RequestBody Reward rewardRequest) {
        log.info("Admin creating new reward: {}", rewardRequest.getName());

        try {
            // Validate inputs
            if (rewardRequest.getName() == null || rewardRequest.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Reward name is required");
            }

            if (rewardRequest.getCode() == null || rewardRequest.getCode().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Reward code is required");
            }

            if (rewardRepository.findByCode(rewardRequest.getCode()).isPresent()) {
                return ResponseEntity.badRequest().body("Reward code must be unique");
            }

            // Set default values if not provided
            if (rewardRequest.getActive() == null) {
                rewardRequest.setActive(true);
            }

            Reward savedReward = rewardRepository.save(rewardRequest);

            return ResponseEntity.status(HttpStatus.CREATED).body(RewardDTO.builder()
                    .id(savedReward.getId())
                    .name(savedReward.getName())
                    .description(savedReward.getDescription())
                    .code(savedReward.getCode())
                    .expirationDate(savedReward.getExpirationDate())
                    .requiredLocations(savedReward.getRequiredLocations())
                    .requiresPhotoVerification(savedReward.getRequiresPhotoVerification())
                    .claimable(savedReward.getActive())
                    .build());
        } catch (Exception e) {
            log.error("Error creating reward", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "An unexpected error occurred while creating the reward");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Update an existing reward
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateReward(@PathVariable Integer id, @Valid @RequestBody Reward rewardRequest) {
        log.info("Admin updating reward with ID: {}", id);

        try {
            Reward existingReward = rewardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + id));

            // Check if code is being changed and if it's unique
            if (!existingReward.getCode().equals(rewardRequest.getCode()) &&
                    rewardRepository.findByCode(rewardRequest.getCode()).isPresent()) {
                return ResponseEntity.badRequest().body("Reward code must be unique");
            }

            // Update fields
            existingReward.setName(rewardRequest.getName());
            existingReward.setDescription(rewardRequest.getDescription());
            existingReward.setCode(rewardRequest.getCode());
            existingReward.setExpirationDate(rewardRequest.getExpirationDate());
            existingReward.setRequiredLocations(rewardRequest.getRequiredLocations());
            existingReward.setRequiresPhotoVerification(rewardRequest.getRequiresPhotoVerification());
            existingReward.setActive(rewardRequest.getActive());

            Reward updatedReward = rewardRepository.save(existingReward);

            return ResponseEntity.ok(RewardDTO.builder()
                    .id(updatedReward.getId())
                    .name(updatedReward.getName())
                    .description(updatedReward.getDescription())
                    .code(updatedReward.getCode())
                    .expirationDate(updatedReward.getExpirationDate())
                    .requiredLocations(updatedReward.getRequiredLocations())
                    .requiresPhotoVerification(updatedReward.getRequiresPhotoVerification())
                    .claimable(updatedReward.getActive())
                    .build());

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating reward", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "An unexpected error occurred while updating the reward");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a reward
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteReward(@PathVariable Integer id) {
        log.info("Admin deleting reward with ID: {}", id);

        try {
            Reward reward = rewardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + id));

            rewardRepository.delete(reward);

            Map<String, String> response = new HashMap<>();
            response.put("message", "Reward deleted successfully");

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error deleting reward", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "An unexpected error occurred while deleting the reward");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Activate or deactivate a reward
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateRewardStatus(@PathVariable Integer id, @RequestParam boolean active) {
        log.info("Admin updating reward status for ID: {} to active={}", id, active);

        try {
            Reward reward = rewardRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + id));

            reward.setActive(active);
            Reward updatedReward = rewardRepository.save(reward);

            Map<String, Object> response = new HashMap<>();
            response.put("id", updatedReward.getId());
            response.put("name", updatedReward.getName());
            response.put("active", updatedReward.getActive());
            response.put("message", "Reward status updated successfully");

            return ResponseEntity.ok(response);

        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating reward status", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "An unexpected error occurred while updating the reward status");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}