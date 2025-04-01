package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.RewardDTO;
import com.agimuseum.magi.dto.UserRewardDTO;
import com.agimuseum.magi.service.RewardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for displaying rewards in the user profile
 */
@RestController
@RequestMapping("/api/profile/rewards")
@RequiredArgsConstructor
@Slf4j
public class ProfileRewardsController {

    private final RewardService rewardService;

    /**
     * Get reward summary for user profile
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getRewardsSummary() {
        log.info("Fetching rewards summary for user profile");

        List<RewardDTO> availableRewards = rewardService.getAllAvailableRewards();
        List<UserRewardDTO> claimedRewards = rewardService.getUserRewards();

        // Count claimable rewards (ones the user is eligible for but hasn't claimed yet)
        long claimableCount = availableRewards.stream()
                .filter(RewardDTO::getClaimable)
                .count();

        // Count claimed but not yet redeemed rewards
        long unredeemedCount = claimedRewards.stream()
                .filter(r -> !r.getRedeemed() && !r.getExpired())
                .count();

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalAvailableRewards", availableRewards.size());
        summary.put("claimableRewards", claimableCount);
        summary.put("claimedRewards", claimedRewards.size());
        summary.put("unredeemedRewards", unredeemedCount);

        return ResponseEntity.ok(summary);
    }

    /**
     * Get detailed rewards info for the profile page
     */
    @GetMapping("/details")
    public ResponseEntity<Map<String, Object>> getRewardsDetails() {
        log.info("Fetching detailed rewards information for user profile");

        Map<String, Object> details = new HashMap<>();
        details.put("availableRewards", rewardService.getAllAvailableRewards());
        details.put("claimedRewards", rewardService.getUserRewards());

        return ResponseEntity.ok(details);
    }
}