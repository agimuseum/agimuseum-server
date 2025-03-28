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
public class StopVisitDetailsDTO {
    // Base StopDTO fields
    private Integer id;
    private String name;
    private String summary;
    private String weblink;
    private StopLocationDTO location;
    private List<String> photos = new ArrayList<>();

    // Additional fields for visit status
    private boolean visited;
    private Date visitedAt;

    // New fields for photo evidence
    private boolean hasPhotoEvidence;
    private String visitPhotoUrl;
    private String visitMethod;
}