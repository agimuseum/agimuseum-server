package com.agimuseum.magi.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClaimRewardRequest {

    private Integer rewardId;

    @NotBlank(message = "Reward code is required")
    private String rewardCode;
}