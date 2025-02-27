package com.agimuseum.magi.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StopDTO {
    private Integer id;
    private String name;
    private String summary;
    private String weblink;
    private StopLocationDTO location;
    private List<String> photos = new ArrayList<>();
}
