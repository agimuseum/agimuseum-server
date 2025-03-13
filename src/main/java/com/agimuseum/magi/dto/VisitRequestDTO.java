package com.agimuseum.magi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VisitRequestDTO {
    private Integer id;  // Location or Stop ID
    private boolean visited;  // true to mark as visited, false to unmark
}
