package com.agimuseum.magi.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agimuseum.magi.dto.LocationDTO;
import com.agimuseum.magi.dto.LocationSummaryDTO;
import com.agimuseum.magi.service.LocationService;
import com.agimuseum.magi.service.VisitService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;
    private final VisitService visitService;

    @GetMapping
    public ResponseEntity<List<LocationDTO>> getAllLocations() {
        return ResponseEntity.ok(locationService.getAllLocations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDTO> getLocationById(@PathVariable Integer id) {
        return ResponseEntity.ok(locationService.getLocationById(id));
    }

    /**
     * Get all locations with visit summary information for the current user
     * Returns an array of locations including visit status and stop statistics
     */
    @GetMapping("/summary")
    public ResponseEntity<List<LocationSummaryDTO>> getLocationSummaries() {
        return ResponseEntity.ok(visitService.getLocationSummaries());
    }
}