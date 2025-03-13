package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardProgressDTO {
    private int totalVisitedLocations;
    private int totalLocations;
    private int locationsToGo;
    private int progressPercentage;
    private boolean rewardEligible;
}
