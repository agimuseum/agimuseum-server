package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.dto.LocationVisitDTO;
import com.agimuseum.magi.dto.StopVisitDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.service.VisitPhotoService;
import com.agimuseum.magi.util.ApiErrorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller for handling visit photos (photos that serve as proof of visits)
 * Modified to simplify photo upload and retrieval
 */
@RestController
@RequestMapping("/api/visits/photos")
@RequiredArgsConstructor
@Slf4j
public class VisitPhotoController {

    private final VisitPhotoService visitPhotoService;
    private static final int MAX_VISIT_PHOTOS = 3; // Maximum photos per location/stop

    /**
     * Upload one or multiple photos for a location visit and mark the location as visited
     * IMPORTANT: Deletes all existing visit proof photos for this location and user first
     */
    @PostMapping(value = "/locations/{locationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLocationVisitPhotos(
            @PathVariable Integer locationId,
            @RequestParam("files") MultipartFile[] files) {

        log.info("Received request to upload {} visit photo(s) for location ID: {}", files.length, locationId);

        try {
            // Validate files
            if (files == null || files.length == 0) {
                log.warn("No files provided");
                return ApiErrorUtil.createBadRequestResponse("No files provided");
            }

            // Check number of files
            if (files.length > MAX_VISIT_PHOTOS) {
                log.warn("Too many files provided: {}", files.length);
                return ApiErrorUtil.createBadRequestResponse("Maximum of " + MAX_VISIT_PHOTOS + " photos allowed");
            }

            // Check file types
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    log.warn("File is empty");
                    return ApiErrorUtil.createBadRequestResponse("One or more files are empty");
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    log.warn("Invalid content type: {}", contentType);
                    return ApiErrorUtil.createBadRequestResponse("Only image files are allowed");
                }
            }

            // Process files
            List<LocationVisitDTO> visits = new ArrayList<>();
            LocationVisitDTO finalVisit = null;

            // Upload all photos one by one
            // Each upload will automatically delete existing photos for this location and user
            for (MultipartFile file : files) {
                finalVisit = visitPhotoService.uploadLocationVisitPhoto(locationId, file);
                visits.add(finalVisit);
            }

            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("location", finalVisit != null ? finalVisit.getLocationName() : null);
            result.put("locationId", locationId);
            result.put("uploadedPhotos", visits.size());
            result.put("visitedAt", finalVisit != null ? finalVisit.getVisitedAt() : null);
            result.put("hasPhotoProof", true);
            result.put("message", "Successfully uploaded " + visits.size() + " visit proof photo(s)");

            log.info("Successfully uploaded {} photos for location ID: {}", visits.size(), locationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ApiErrorUtil.createNotFoundResponse("Location", locationId);
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ApiErrorUtil.createBadRequestResponse(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload visit photos", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to upload visit photos: " + e.getMessage());
        }
    }

    /**
     * Upload a single photo for a location visit and mark the location as visited
     * Backward compatibility with single file upload API
     * Deletes all existing visit proof photos for this location and user first
     */
    @PostMapping(value = "/location/{locationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSingleLocationVisitPhoto(
            @PathVariable Integer locationId,
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload single visit photo for location ID: {}", locationId);

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty or null");
                return ApiErrorUtil.createBadRequestResponse("File cannot be empty");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid content type: {}", contentType);
                return ApiErrorUtil.createBadRequestResponse("Only image files are allowed");
            }

            // Process file (this will delete any existing photos)
            LocationVisitDTO visit = visitPhotoService.uploadLocationVisitPhoto(locationId, file);

            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("location", visit.getLocationName());
            result.put("locationId", locationId);
            result.put("visitedAt", visit.getVisitedAt());
            result.put("hasPhotoProof", true);
            result.put("photoUrl", visit.getPhotoUrl());
            result.put("message", "Successfully uploaded visit proof photo");

            log.info("Successfully uploaded photo for location ID: {}", locationId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ApiErrorUtil.createNotFoundResponse("Location", locationId);
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ApiErrorUtil.createBadRequestResponse(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload visit photo", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to upload visit photo: " + e.getMessage());
        }
    }

    /**
     * Upload one or multiple photos for a stop visit and mark the stop as visited
     * IMPORTANT: Deletes all existing visit proof photos for this stop and user first
     */
    @PostMapping(value = "/stops/{stopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStopVisitPhotos(
            @PathVariable Integer stopId,
            @RequestParam("files") MultipartFile[] files) {

        log.info("Received request to upload {} visit photo(s) for stop ID: {}", files.length, stopId);

        try {
            // Validate files
            if (files == null || files.length == 0) {
                log.warn("No files provided");
                return ApiErrorUtil.createBadRequestResponse("No files provided");
            }

            // Check number of files
            if (files.length > MAX_VISIT_PHOTOS) {
                log.warn("Too many files provided: {}", files.length);
                return ApiErrorUtil.createBadRequestResponse("Maximum of " + MAX_VISIT_PHOTOS + " photos allowed");
            }

            // Check file types
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    log.warn("File is empty");
                    return ApiErrorUtil.createBadRequestResponse("One or more files are empty");
                }

                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    log.warn("Invalid content type: {}", contentType);
                    return ApiErrorUtil.createBadRequestResponse("Only image files are allowed");
                }
            }

            // Process files
            List<StopVisitDTO> visits = new ArrayList<>();
            StopVisitDTO finalVisit = null;

            // Upload all photos one by one
            // Each upload will automatically delete existing photos for this stop and user
            for (MultipartFile file : files) {
                finalVisit = visitPhotoService.uploadStopVisitPhoto(stopId, file);
                visits.add(finalVisit);
            }

            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("stop", finalVisit != null ? finalVisit.getStopName() : null);
            result.put("stopId", stopId);
            result.put("location", finalVisit != null ? finalVisit.getLocationName() : null);
            result.put("locationId", finalVisit != null ? finalVisit.getLocationId() : null);
            result.put("uploadedPhotos", visits.size());
            result.put("visitedAt", finalVisit != null ? finalVisit.getVisitedAt() : null);
            result.put("hasPhotoProof", true);
            result.put("message", "Successfully uploaded " + visits.size() + " visit proof photo(s)");

            log.info("Successfully uploaded {} photos for stop ID: {}", visits.size(), stopId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ApiErrorUtil.createNotFoundResponse("Stop", stopId);
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ApiErrorUtil.createBadRequestResponse(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload visit photos", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to upload visit photos: " + e.getMessage());
        }
    }

    /**
     * Upload a single photo for a stop visit and mark the stop as visited
     * Backward compatibility with single file upload API
     * Deletes all existing visit proof photos for this stop and user first
     */
    @PostMapping(value = "/stop/{stopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadSingleStopVisitPhoto(
            @PathVariable Integer stopId,
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload single visit photo for stop ID: {}", stopId);

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty or null");
                return ApiErrorUtil.createBadRequestResponse("File cannot be empty");
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid content type: {}", contentType);
                return ApiErrorUtil.createBadRequestResponse("Only image files are allowed");
            }

            // Process file (this will delete any existing photos)
            StopVisitDTO visit = visitPhotoService.uploadStopVisitPhoto(stopId, file);

            // Return result
            Map<String, Object> result = new HashMap<>();
            result.put("stop", visit.getStopName());
            result.put("stopId", stopId);
            result.put("location", visit.getLocationName());
            result.put("locationId", visit.getLocationId());
            result.put("visitedAt", visit.getVisitedAt());
            result.put("hasPhotoProof", true);
            result.put("photoUrl", visit.getPhotoUrl());
            result.put("message", "Successfully uploaded visit proof photo");

            log.info("Successfully uploaded photo for stop ID: {}", stopId);
            return ResponseEntity.status(HttpStatus.CREATED).body(result);

        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ApiErrorUtil.createNotFoundResponse("Stop", stopId);
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ApiErrorUtil.createBadRequestResponse(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload visit photo", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to upload visit photo: " + e.getMessage());
        }
    }

    /**
     * Get the visit proof photos for a location (for the current user)
     */
    @GetMapping("/locations/{locationId}/proof")
    public ResponseEntity<?> getLocationVisitProof(@PathVariable Integer locationId) {
        try {
            List<PhotoDTO> photos = visitPhotoService.getUserLocationVisitPhotos(locationId);
            return ResponseEntity.ok(photos);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createNotFoundResponse("Visit proof photo for location", locationId);
        } catch (Exception e) {
            log.error("Error retrieving location visit proof", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to retrieve location visit proof");
        }
    }

    /**
     * Get the visit proof photos for a stop (for the current user)
     */
    @GetMapping("/stops/{stopId}/proof")
    public ResponseEntity<?> getStopVisitProof(@PathVariable Integer stopId) {
        try {
            List<PhotoDTO> photos = visitPhotoService.getUserStopVisitPhotos(stopId);
            return ResponseEntity.ok(photos);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createNotFoundResponse("Visit proof photo for stop", stopId);
        } catch (Exception e) {
            log.error("Error retrieving stop visit proof", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to retrieve stop visit proof");
        }
    }

    /**
     * Delete a visit proof photo
     */
    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deleteVisitPhoto(@PathVariable Integer photoId) {
        try {
            visitPhotoService.deleteVisitPhoto(photoId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Visit proof photo deleted successfully");

            return ResponseEntity.ok(response);
        } catch (ResourceNotFoundException e) {
            return ApiErrorUtil.createNotFoundResponse("Photo", photoId);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ApiErrorUtil.createBadRequestResponse(e.getMessage());
        } catch (IOException e) {
            log.error("Error deleting visit photo", e);
            return ApiErrorUtil.createInternalServerErrorResponse("Failed to delete visit photo");
        }
    }
}