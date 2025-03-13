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
public class StopVisitDTO {
    private Integer stopId;
    private String stopName;
    private Integer locationId;
    private String locationName;
    private Date visitedAt;
}
