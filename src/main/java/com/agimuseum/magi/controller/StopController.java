package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.StopDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.service.LocationService;
import com.agimuseum.magi.util.ApiErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling stop-related operations
 */
@RestController
@RequestMapping("/api/stops")
@RequiredArgsConstructor
@Slf4j
public class StopController {

    private final LocationService locationService;

    /**
     * Get all stops
     */
    @GetMapping
    public ResponseEntity<List<StopDTO>> getAllStops() {
        log.info("Fetching all stops");

        List<StopDTO> allStops = locationService.getAllLocations().stream()
                .flatMap(location -> location.getStops().stream())
                .collect(Collectors.toList());

        return ResponseEntity.ok(allStops);
    }

    /**
     * Get a stop by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getStopById(@PathVariable Integer id) {
        log.info("Fetching stop with ID: {}", id);

        try {
            // Search all locations for the stop with this ID
            StopDTO stop = locationService.getAllLocations().stream()
                    .flatMap(location -> location.getStops().stream())
                    .filter(s -> s.getId().equals(id))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + id));

            return ResponseEntity.ok(stop);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createNotFoundResponse("Stop", id);
        } catch (Exception e) {
            log.error("Error fetching stop with ID: {}", id, e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to fetch stop information");
        }
    }

    /**
     * Get stops for a specific location
     */
    @GetMapping("/location/{locationId}")
    public ResponseEntity<?> getStopsByLocationId(@PathVariable Integer locationId) {
        log.info("Fetching stops for location ID: {}", locationId);

        try {
            List<StopDTO> stops = locationService.getLocationById(locationId).getStops();
            return ResponseEntity.ok(stops);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createNotFoundResponse("Location", locationId);
        } catch (Exception e) {
            log.error("Error fetching stops for location ID: {}", locationId, e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to fetch stops for the location");
        }
    }

    /**
     * Endpoint for searching stops by name
     */
    @GetMapping("/search")
    public ResponseEntity<List<StopDTO>> searchStops(@RequestParam String query) {
        log.info("Searching stops with query: {}", query);

        String lowerCaseQuery = query.toLowerCase();

        List<StopDTO> matchingStops = locationService.getAllLocations().stream()
                .flatMap(location -> location.getStops().stream())
                .filter(stop ->
                        stop.getName().toLowerCase().contains(lowerCaseQuery) ||
                                (stop.getSummary() != null && stop.getSummary().toLowerCase().contains(lowerCaseQuery))
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(matchingStops);
    }
}