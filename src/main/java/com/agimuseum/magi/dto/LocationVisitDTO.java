package com.agimuseum.magi.dto;

import com.agimuseum.magi.model.LocationVisit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationVisitDTO {
    private Integer locationId;
    private String locationName;
    private Date visitedAt;
    private Boolean hasPhotoProof;
    private String visitMethod;
    private String photoUrl; // Added to include the photo URL if there's photo proof
}