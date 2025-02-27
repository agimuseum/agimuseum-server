package com.agimuseum.magi.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailDTO {
    private String address;
    private Double latitude;
    private Double longitude;
    private Integer geoFenceRadius;
    private List<ParkingAreaDTO> nearbyParkingAreas = new ArrayList<>();
}
