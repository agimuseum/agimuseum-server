package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.ClaimRewardRequest;
import com.agimuseum.magi.dto.RedeemRewardRequest;
import com.agimuseum.magi.dto.RewardDTO;
import com.agimuseum.magi.dto.UserRewardDTO;
import com.agimuseum.magi.service.RewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@Slf4j
public class RewardController {

    private final RewardService rewardService;

    /**
     * Get all available rewards for the current user
     */
    @GetMapping
    public ResponseEntity<List<RewardDTO>> getAllAvailableRewards() {
        log.info("Fetching all available rewards");
        return ResponseEntity.ok(rewardService.getAllAvailableRewards());
    }

    /**
     * Get all rewards claimed by the current user
     */
    @GetMapping("/claimed")
    public ResponseEntity<List<UserRewardDTO>> getUserRewards() {
        log.info("Fetching user's claimed rewards");
        return ResponseEntity.ok(rewardService.getUserRewards());
    }

    /**
     * Claim a reward
     */
    @PostMapping("/claim")
    public ResponseEntity<?> claimReward(@Valid @RequestBody ClaimRewardRequest request) {
        log.info("Processing claim request for reward ID: {}", request.getRewardId());

        try {
            UserRewardDTO claimedReward = rewardService.claimReward(
                    request.getRewardId(),
                    request.getRewardCode()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(claimedReward);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid reward claim request: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid request");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (IllegalStateException e) {
            log.warn("Reward claim failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Claim failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error processing reward claim", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "An unexpected error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Redeem a claimed reward
     */
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemReward(@Valid @RequestBody RedeemRewardRequest request) {
        log.info("Processing redeem request for user reward ID: {}", request.getUserRewardId());

        try {
            UserRewardDTO redeemedReward = rewardService.redeemReward(request.getUserRewardId());
            return ResponseEntity.ok(redeemedReward);
        } catch (IllegalStateException e) {
            log.warn("Reward redemption failed: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Redemption failed");
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        } catch (Exception e) {
            log.error("Error processing reward redemption", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Server error");
            error.put("message", "An unexpected error occurred while processing your request");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}