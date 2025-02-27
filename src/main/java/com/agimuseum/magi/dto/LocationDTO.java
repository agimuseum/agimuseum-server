package com.agimuseum.magi.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {
    private Integer id;
    private String name;
    private String summary;
    private LocationDetailDTO location;
    private String weblink;
    private List<String> photos = new ArrayList<>();
    private List<StopDTO> stops = new ArrayList<>();
}
