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
public class UserRewardDTO {
    private Integer id;
    private String rewardName;
    private String rewardDescription;
    private String rewardCode;
    private Date claimedAt;
    private Boolean redeemed;
    private Date redeemedAt;
    private Date expirationDate;
    private Boolean expired;
}