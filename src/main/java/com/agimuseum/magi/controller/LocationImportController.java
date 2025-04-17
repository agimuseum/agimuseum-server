package com.agimuseum.magi.controller;

import com.agimuseum.magi.service.impl.LocationImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling location data imports from CSV files
 * Restricted to ADMIN users only
 */
@RestController
@RequestMapping("/api/admin/import")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class LocationImportController {

    private final LocationImportService locationImportService;

    /**
     * Import locations from a CSV file
     */
    @PostMapping(value = "/locations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importLocations(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import locations from CSV file: {}", file.getOriginalFilename());

        try {
            // Validate file type
            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Only CSV files are supported");
            }

            Map<String, Object> result = locationImportService.importLocationsFromCsv(file);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error importing locations from CSV", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to import locations");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Import stops from a CSV file
     */
    @PostMapping(value = "/stops", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importStops(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import stops from CSV file: {}", file.getOriginalFilename());

        try {
            // Validate file type
            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Only CSV files are supported");
            }

            Map<String, Object> result = locationImportService.importStopsFromCsv(file);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error importing stops from CSV", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to import stops");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Import parking areas from a CSV file
     */
    @PostMapping(value = "/parking-areas", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> importParkingAreas(@RequestParam("file") MultipartFile file) {
        log.info("Received request to import parking areas from CSV file: {}", file.getOriginalFilename());

        try {
            // Validate file type
            if (!file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest().body("Only CSV files are supported");
            }

            Map<String, Object> result = locationImportService.importParkingAreasFromCsv(file);

            return ResponseEntity.ok(result);
        } catch (IOException e) {
            log.error("Error importing parking areas from CSV", e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to import parking areas");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}