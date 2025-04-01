package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RewardDTO {
    private Integer id;
    private String name;
    private String description;
    private String code;
    private Date expirationDate;
    private Integer requiredLocations;
    private Boolean requiresPhotoVerification;
    private Boolean claimable;   // Can the user claim this reward?
    private Boolean claimed;     // Has the user already claimed this reward?
}