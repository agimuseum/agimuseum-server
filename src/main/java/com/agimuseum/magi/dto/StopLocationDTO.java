package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopLocationDTO {
    private Double latitude;
    private Double longitude;
    private Integer geoFenceRadius;
}