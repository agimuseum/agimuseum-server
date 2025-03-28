package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.LocationVisitDTO;
import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.dto.StopVisitDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.service.VisitPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller for handling visit photos (photos that serve as proof of visits)
 */
@RestController
@RequestMapping("/api/visits/photos")
@RequiredArgsConstructor
@Slf4j
public class VisitPhotoController {

    private final VisitPhotoService visitPhotoService;

    /**
     * Upload a photo for a location visit and mark the location as visited
     */
    @PostMapping(value = "/locations/{locationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLocationVisitPhoto(
            @PathVariable Integer locationId,
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload visit photo for location ID: {}", locationId);

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty or null");
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid content type: {}", contentType);
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            LocationVisitDTO visit = visitPhotoService.uploadLocationVisitPhoto(locationId, file);
            log.info("Location visit marked with photo proof for location ID: {}", locationId);

            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload visit photo", e);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Failed to upload visit photo");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("locationId", locationId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }

    /**
     * Upload a photo for a stop visit and mark the stop as visited
     */
    @PostMapping(value = "/stops/{stopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStopVisitPhoto(
            @PathVariable Integer stopId,
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload visit photo for stop ID: {}", stopId);

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty or null");
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid content type: {}", contentType);
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            StopVisitDTO visit = visitPhotoService.uploadStopVisitPhoto(stopId, file);
            log.info("Stop visit marked with photo proof for stop ID: {}", stopId);

            return ResponseEntity.status(HttpStatus.CREATED).body(visit);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload visit photo", e);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Failed to upload visit photo");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("stopId", stopId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }

    /**
     * Get the visit photo for a location (for the current user)
     */
    @GetMapping("/locations/{locationId}/proof")
    public ResponseEntity<?> getLocationVisitProof(@PathVariable Integer locationId) {
        try {
            PhotoDTO photo = visitPhotoService.getUserLocationVisitPhoto(locationId);
            return ResponseEntity.ok(photo);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving location visit proof", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve location visit proof: " + e.getMessage());
        }
    }

    /**
     * Get the visit photo for a stop (for the current user)
     */
    @GetMapping("/stops/{stopId}/proof")
    public ResponseEntity<?> getStopVisitProof(@PathVariable Integer stopId) {
        try {
            PhotoDTO photo = visitPhotoService.getUserStopVisitPhoto(stopId);
            return ResponseEntity.ok(photo);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving stop visit proof", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to retrieve stop visit proof: " + e.getMessage());
        }
    }
}