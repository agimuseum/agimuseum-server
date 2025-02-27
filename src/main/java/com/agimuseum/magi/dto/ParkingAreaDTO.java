package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingAreaDTO {
    private String name;
    private Double latitude;
    private Double longitude;
}
