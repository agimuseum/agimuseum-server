package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitSummaryDTO {
    private long totalVisitedLocations;
    private long totalUnvisitedLocations;
    private long totalVisitedStops;
    private long totalUnvisitedStops;
}
