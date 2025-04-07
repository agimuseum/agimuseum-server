package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationVisitDetailsDTO {
    // Base LocationDTO fields
    private Integer id;
    private String name;
    private String summary;
    private LocationDetailDTO location;
    private String weblink;
    private List<String> photos = new ArrayList<>();

    // Additional fields for visit status
    private boolean visited;
    private Date visitedAt;
    private List<StopVisitDetailsDTO> stops = new ArrayList<>();

    // New fields for photo evidence
    @Builder.Default
    private boolean hasPhotoEvidence = false;
    private String visitPhotoUrl;
    private String visitMethod;
}