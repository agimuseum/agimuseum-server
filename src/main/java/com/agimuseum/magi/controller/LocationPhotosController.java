package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.service.LocationService;
import com.agimuseum.magi.service.PhotoService;
import com.agimuseum.magi.util.PhotoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for handling location photos display
 */
@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationPhotosController {

    private final LocationService locationService;
    private final PhotoService photoService;
    private final PhotoMapper photoMapper;

    /**
     * Get all photos for a location
     */
    @GetMapping("/{locationId}/photos")
    public ResponseEntity<?> getLocationPhotos(@PathVariable Integer locationId) {
        try {
            List<PhotoDTO> photos = photoService.getLocationPhotos(locationId)
                    .stream()
                    .map(photoMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(photos);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving location photos", e);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Failed to retrieve location photos");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("locationId", locationId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }

    /**
     * Get all photos for a stop
     */
    @GetMapping("/stops/{stopId}/photos")
    public ResponseEntity<?> getStopPhotos(@PathVariable Integer stopId) {
        try {
            List<PhotoDTO> photos = photoService.getStopPhotos(stopId)
                    .stream()
                    .map(photoMapper::toDTO)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(photos);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            log.error("Error retrieving stop photos", e);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Failed to retrieve stop photos");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("stopId", stopId);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDetails);
        }
    }

    /**
     * Get URLs of all photos for a location (for use in mobile apps that just need the image URLs)
     */
    @GetMapping("/{locationId}/photo-urls")
    public ResponseEntity<?> getLocationPhotoUrls(@PathVariable Integer locationId) {
        try {
            List<String> photoUrls = locationService.getLocationPhotos(locationId);
            return ResponseEntity.ok(photoUrls);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * Get URLs of all photos for a stop (for use in mobile apps that just need the image URLs)
     */
    @GetMapping("/stops/{stopId}/photo-urls")
    public ResponseEntity<?> getStopPhotoUrls(@PathVariable Integer stopId) {
        try {
            List<String> photoUrls = locationService.getStopPhotos(stopId);
            return ResponseEntity.ok(photoUrls);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}