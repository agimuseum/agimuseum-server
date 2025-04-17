package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.LocationVisitDTO;
import com.agimuseum.magi.dto.RewardProgressDTO;
import com.agimuseum.magi.dto.StopVisitDTO;
import com.agimuseum.magi.dto.VisitRequestDTO;
import com.agimuseum.magi.dto.VisitSummaryDTO;
import com.agimuseum.magi.service.VisitService;
import com.agimuseum.magi.util.ApiErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling user visits to locations and stops
 */
@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Slf4j
public class VisitController {

    private final VisitService visitService;

    /**
     * Mark a location as visited (or unvisited)
     */
    @PostMapping("/locations/{locationId}")
    public ResponseEntity<?> markLocationVisited(
            @PathVariable Integer locationId,
            @RequestBody(required = false) VisitRequestDTO request) {

        try {
            boolean visited = request != null && request.isVisited();
            log.info("Received request to mark location {} as {}", locationId, visited ? "visited" : "unvisited");

            if (visited) {
                LocationVisitDTO visit = visitService.markLocationVisited(locationId);
                return ResponseEntity.ok(visit);
            } else {
                visitService.unmarkLocationVisited(locationId);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Location marked as unvisited");
                response.put("locationId", locationId);
                response.put("visited", false);

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error handling location visit request", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to process visit request: " + e.getMessage());
        }
    }

    /**
     * Mark a stop as visited (or unvisited)
     */
    @PostMapping("/stops/{stopId}")
    public ResponseEntity<?> markStopVisited(
            @PathVariable Integer stopId,
            @RequestBody(required = false) VisitRequestDTO request) {

        try {
            boolean visited = request != null && request.isVisited();
            log.info("Received request to mark stop {} as {}", stopId, visited ? "visited" : "unvisited");

            if (visited) {
                StopVisitDTO visit = visitService.markStopVisited(stopId);
                return ResponseEntity.ok(visit);
            } else {
                visitService.unmarkStopVisited(stopId);

                Map<String, Object> response = new HashMap<>();
                response.put("message", "Stop marked as unvisited");
                response.put("stopId", stopId);
                response.put("visited", false);

                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            log.error("Error handling stop visit request", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to process visit request: " + e.getMessage());
        }
    }

    /**
     * Get all visited locations
     */
    @GetMapping("/locations")
    public ResponseEntity<List<LocationVisitDTO>> getVisitedLocations() {
        return ResponseEntity.ok(visitService.getVisitedLocations());
    }

    /**
     * Get all visited stops
     */
    @GetMapping("/stops")
    public ResponseEntity<List<StopVisitDTO>> getVisitedStops() {
        return ResponseEntity.ok(visitService.getVisitedStops());
    }

    /**
     * Get summary of visited locations and stops
     */
    @GetMapping("/summary")
    public ResponseEntity<VisitSummaryDTO> getVisitSummary() {
        return ResponseEntity.ok(visitService.getVisitSummary());
    }

    /**
     * Get user's reward progress
     * This endpoint is public and doesn't require authentication
     */
    @GetMapping("/rewards/progress")
    public ResponseEntity<?> getRewardProgress() {
        try {
            RewardProgressDTO progress = visitService.getRewardProgress();
            return ResponseEntity.ok(progress);
        } catch (IllegalStateException e) {
            // Return anonymous progress for unauthenticated users
            RewardProgressDTO anonymousProgress = RewardProgressDTO.builder()
                    .totalVisitedLocations(0)
                    .totalVisitedLocationsWithPhoto(0)
                    .totalLocations(10) // Assuming 10 total locations
                    .locationsToGo(10)
                    .progressPercentage(0)
                    .rewardEligible(false)
                    .photoVerificationEligible(false)
                    .build();

            return ResponseEntity.ok(anonymousProgress);
        } catch (Exception e) {
            log.error("Error getting reward progress", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving reward progress: " + e.getMessage());
        }
    }
}