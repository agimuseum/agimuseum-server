package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.*;
import com.agimuseum.magi.service.RewardService;
import com.agimuseum.magi.service.UserService;
import com.agimuseum.magi.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserService userService;
    private final VisitService visitService;
    private final RewardService rewardService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        // Get current user information
        UserDTO currentUser = userService.getCurrentUser();

        // Get visit statistics
        VisitSummaryDTO visitSummary = visitService.getVisitSummary();

        // Get reward progress
        RewardProgressDTO rewardProgress = visitService.getRewardProgress();

        // Combine all information into a single response
        Map<String, Object> profileData = new HashMap<>();
        profileData.put("user", currentUser);
        profileData.put("visitStats", visitSummary);
        profileData.put("rewardProgress", rewardProgress);

        return ResponseEntity.ok(profileData);
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardInfo() {
        // Simplified response with visit counts, reward progress, and reward status
        VisitSummaryDTO visitSummary = visitService.getVisitSummary();
        RewardProgressDTO rewardProgress = visitService.getRewardProgress();

        // Get reward information
        List<RewardDTO> availableRewards = rewardService.getAllAvailableRewards();
        List<UserRewardDTO> claimedRewards = rewardService.getUserRewards();

        // Count claimable rewards
        long claimableRewards = availableRewards.stream()
                .filter(RewardDTO::getClaimable)
                .count();

        // Count unredeemed rewards
        long unredeemedRewards = claimedRewards.stream()
                .filter(r -> !r.getRedeemed() && !r.getExpired())
                .count();

        Map<String, Object> dashboardData = new HashMap<>();
        // Visit data
        dashboardData.put("totalVisited", visitSummary.getTotalVisitedLocations());
        dashboardData.put("totalUnvisited", visitSummary.getTotalUnvisitedLocations());
        dashboardData.put("locationsToGo", rewardProgress.getLocationsToGo());
        dashboardData.put("progressPercentage", rewardProgress.getProgressPercentage());
        dashboardData.put("rewardEligible", rewardProgress.isRewardEligible());

        // Reward data
        dashboardData.put("claimableRewards", claimableRewards);
        dashboardData.put("unredeemedRewards", unredeemedRewards);
        dashboardData.put("totalClaimedRewards", claimedRewards.size());

        return ResponseEntity.ok(dashboardData);
    }
}