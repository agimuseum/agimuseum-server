package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationVisitDTO {
    private Integer locationId;
    private String locationName;
    private Date visitedAt;
}



