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
public class PhotoDTO {
    private Integer id;
    private String fileName;
    private String contentType;
    private String url;
    private Integer locationId;
    private String locationName;
    private Integer stopId;
    private String stopName;
    private String uploaderName;
    private Date uploadedAt;
}