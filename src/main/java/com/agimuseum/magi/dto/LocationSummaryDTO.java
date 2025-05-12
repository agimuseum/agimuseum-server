package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location summary including visit status and stop statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSummaryDTO {
    private Integer id;
    private String name;
    private Boolean isVisited;
    private Integer totalNumberOfStops;
    private Integer totalNumberOfVisitedStops;
}