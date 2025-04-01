package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.RewardDTO;
import com.agimuseum.magi.dto.UserRewardDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Reward;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.model.UserReward;
import com.agimuseum.magi.repository.RewardRepository;
import com.agimuseum.magi.repository.UserRewardRepository;
import com.agimuseum.magi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardService {

    private final RewardRepository rewardRepository;
    private final UserRewardRepository userRewardRepository;
    private final UserRepository userRepository;
    private final VisitService visitService;

    /**
     * Get all available rewards for the current user
     */
    public List<RewardDTO> getAllAvailableRewards() {
        User currentUser = getCurrentUser();
        Date now = new Date();

        // Get current user's visit stats to determine eligibility
        int totalVisitedLocations = (int) visitService.getVisitSummary().getTotalVisitedLocations();
        int totalVisitedLocationsWithPhoto = (int) visitService.getVisitSummary().getTotalVisitedLocationsWithPhotoProof();

        // Get all active rewards that haven't expired
        List<Reward> activeRewards = rewardRepository.findAllActiveRewards(now);

        return activeRewards.stream()
                .map(reward -> {
                    // Check if user has already claimed this reward
                    boolean claimed = userRewardRepository.existsByUserAndReward(currentUser, reward);

                    // Check if user is eligible to claim this reward
                    boolean claimable = !claimed &&
                            totalVisitedLocations >= reward.getRequiredLocations() &&
                            (!reward.getRequiresPhotoVerification() ||
                                    totalVisitedLocationsWithPhoto >= reward.getRequiredLocations());

                    return RewardDTO.builder()
                            .id(reward.getId())
                            .name(reward.getName())
                            .description(reward.getDescription())
                            .code(claimed ? reward.getCode() : null) // Only show code if claimed
                            .expirationDate(reward.getExpirationDate())
                            .requiredLocations(reward.getRequiredLocations())
                            .requiresPhotoVerification(reward.getRequiresPhotoVerification())
                            .claimable(claimable)
                            .claimed(claimed)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Get all rewards claimed by the current user
     */
    public List<UserRewardDTO> getUserRewards() {
        User currentUser = getCurrentUser();
        Date now = new Date();

        return userRewardRepository.findByUser(currentUser).stream()
                .map(userReward -> UserRewardDTO.builder()
                        .id(userReward.getId())
                        .rewardName(userReward.getReward().getName())
                        .rewardDescription(userReward.getReward().getDescription())
                        .rewardCode(userReward.getReward().getCode())
                        .claimedAt(userReward.getClaimedAt())
                        .redeemed(userReward.getRedeemed())
                        .redeemedAt(userReward.getRedeemedAt())
                        .expirationDate(userReward.getReward().getExpirationDate())
                        .expired(userReward.getReward().getExpirationDate().before(now))
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Claim a reward for the current user
     */
    @Transactional
    public UserRewardDTO claimReward(Integer rewardId, String rewardCode) {
        User currentUser = getCurrentUser();
        Date now = new Date();

        // Find the reward
        Reward reward = rewardRepository.findById(rewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward not found with id: " + rewardId));

        // Verify the reward code
        if (!reward.getCode().equals(rewardCode)) {
            throw new IllegalArgumentException("Invalid reward code");
        }

        // Check if reward is active and not expired
        if (!reward.getActive() || now.after(reward.getExpirationDate())) {
            throw new IllegalStateException("Reward is no longer available");
        }

        // Check if user has already claimed this reward
        if (userRewardRepository.existsByUserAndReward(currentUser, reward)) {
            throw new IllegalStateException("You have already claimed this reward");
        }

        // Check if user has visited enough locations
        int totalVisitedLocations = (int) visitService.getVisitSummary().getTotalVisitedLocations();
        int totalVisitedLocationsWithPhoto = (int) visitService.getVisitSummary().getTotalVisitedLocationsWithPhotoProof();

        if (totalVisitedLocations < reward.getRequiredLocations()) {
            throw new IllegalStateException("You haven't visited enough locations to claim this reward");
        }

        if (reward.getRequiresPhotoVerification() && totalVisitedLocationsWithPhoto < reward.getRequiredLocations()) {
            throw new IllegalStateException("You need photo verification for all required locations");
        }

        // Create new user reward
        UserReward userReward = UserReward.builder()
                .user(currentUser)
                .reward(reward)
                .claimedAt(now)
                .redeemed(false)
                .build();

        UserReward savedUserReward = userRewardRepository.save(userReward);
        log.info("User {} claimed reward: {}", currentUser.getUsername(), reward.getName());

        return UserRewardDTO.builder()
                .id(savedUserReward.getId())
                .rewardName(reward.getName())
                .rewardDescription(reward.getDescription())
                .rewardCode(reward.getCode())
                .claimedAt(savedUserReward.getClaimedAt())
                .redeemed(false)
                .expirationDate(reward.getExpirationDate())
                .expired(reward.getExpirationDate().before(now))
                .build();
    }

    /**
     * Mark a claimed reward as redeemed
     */
    @Transactional
    public UserRewardDTO redeemReward(Integer userRewardId) {
        User currentUser = getCurrentUser();
        Date now = new Date();

        // Find the user reward
        UserReward userReward = userRewardRepository.findById(userRewardId)
                .orElseThrow(() -> new ResourceNotFoundException("Claimed reward not found with id: " + userRewardId));

        // Check if this reward belongs to the current user
        if (!userReward.getUser().getId().equals(currentUser.getId())) {
            throw new IllegalStateException("This reward doesn't belong to you");
        }

        // Check if reward is already redeemed
        if (userReward.getRedeemed()) {
            throw new IllegalStateException("This reward has already been redeemed");
        }

        // Check if reward is expired
        if (now.after(userReward.getReward().getExpirationDate())) {
            throw new IllegalStateException("This reward has expired");
        }

        // Mark as redeemed
        userReward.setRedeemed(true);
        userReward.setRedeemedAt(now);

        UserReward updatedUserReward = userRewardRepository.save(userReward);
        log.info("User {} redeemed reward: {}", currentUser.getUsername(), userReward.getReward().getName());

        return UserRewardDTO.builder()
                .id(updatedUserReward.getId())
                .rewardName(updatedUserReward.getReward().getName())
                .rewardDescription(updatedUserReward.getReward().getDescription())
                .rewardCode(updatedUserReward.getReward().getCode())
                .claimedAt(updatedUserReward.getClaimedAt())
                .redeemed(true)
                .redeemedAt(updatedUserReward.getRedeemedAt())
                .expirationDate(updatedUserReward.getReward().getExpirationDate())
                .expired(updatedUserReward.getReward().getExpirationDate().before(now))
                .build();
    }

    /**
     * Helper method to get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}