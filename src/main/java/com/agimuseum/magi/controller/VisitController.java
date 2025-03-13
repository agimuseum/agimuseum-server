package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.*;
import com.agimuseum.magi.service.VisitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @GetMapping("/summary")
    public ResponseEntity<VisitSummaryDTO> getVisitSummary() {
        return ResponseEntity.ok(visitService.getVisitSummary());
    }

    @GetMapping("/locations/visited")
    public ResponseEntity<List<LocationVisitDTO>> getVisitedLocations() {
        return ResponseEntity.ok(visitService.getVisitedLocations());
    }

    @GetMapping("/stops/visited")
    public ResponseEntity<List<StopVisitDTO>> getVisitedStops() {
        return ResponseEntity.ok(visitService.getVisitedStops());
    }

    @PostMapping("/locations/{locationId}/visited")
    public ResponseEntity<LocationVisitDTO> markLocationVisited(@PathVariable Integer locationId) {
        return ResponseEntity.ok(visitService.markLocationVisited(locationId));
    }

    @DeleteMapping("/locations/{locationId}/visited")
    public ResponseEntity<Void> unmarkLocationVisited(@PathVariable Integer locationId) {
        visitService.unmarkLocationVisited(locationId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/stops/{stopId}/visited")
    public ResponseEntity<StopVisitDTO> markStopVisited(@PathVariable Integer stopId) {
        return ResponseEntity.ok(visitService.markStopVisited(stopId));
    }

    @DeleteMapping("/stops/{stopId}/visited")
    public ResponseEntity<Void> unmarkStopVisited(@PathVariable Integer stopId) {
        visitService.unmarkStopVisited(stopId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<LocationVisitDetailsDTO> getLocationWithVisitStatus(@PathVariable Integer locationId) {
        return ResponseEntity.ok(visitService.getLocationWithVisitStatus(locationId));
    }

    @GetMapping("/locations/all")
    public ResponseEntity<List<LocationVisitDetailsDTO>> getAllLocationsWithVisitStatus() {
        return ResponseEntity.ok(visitService.getAllLocationsWithVisitStatus());
    }

    @GetMapping("/rewards/progress")
    public ResponseEntity<RewardProgressDTO> getRewardProgress() {
        return ResponseEntity.ok(visitService.getRewardProgress());
    }
}