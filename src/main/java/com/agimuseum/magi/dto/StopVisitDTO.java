package com.agimuseum.magi.dto;

import com.agimuseum.magi.model.StopVisit;
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
    private Boolean hasPhotoProof;
    private String visitMethod;
    private String photoUrl; // Added to include the photo URL if there's photo proof
}