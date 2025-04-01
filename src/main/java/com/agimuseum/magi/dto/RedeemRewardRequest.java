package com.agimuseum.magi.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemRewardRequest {
    @NotNull(message = "User reward ID is required")
    private Integer userRewardId;
}