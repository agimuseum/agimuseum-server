package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.PhotoService;
import com.agimuseum.magi.util.PhotoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
@Slf4j
public class PhotoController {

    private final PhotoService photoService;
    private final PhotoMapper photoMapper;
    private final UserRepository userRepository;

    public PhotoController(PhotoService photoService, PhotoMapper photoMapper, UserRepository userRepository) {
        this.photoService = photoService;
        this.photoMapper = photoMapper;
        this.userRepository = userRepository;
    }

    @PostMapping(value = "/locations/{locationId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLocationPhoto(
            @PathVariable Integer locationId,
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload photo for location ID: {}", locationId);
        log.info("File details - name: {}, size: {}, contentType: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty or null");
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Check file type (optional)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid content type: {}", contentType);
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            // Get current user for logging
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth != null ? auth.getName() : "unknown";

            log.info("Uploading file: {}, type: {}, size: {} bytes, by user: {}",
                    file.getOriginalFilename(), contentType, file.getSize(), username);

            Photo photo = photoService.uploadLocationPhoto(locationId, file);
            log.info("Photo uploaded successfully with ID: {}", photo.getId());

            return new ResponseEntity<>(photoMapper.toDTO(photo), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload photo", e);

            // Create detailed error response
            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Failed to upload photo");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("locationId", locationId);
            errorDetails.put("fileName", file != null ? file.getOriginalFilename() : "null");
            errorDetails.put("fileSize", file != null ? file.getSize() : 0);
            errorDetails.put("contentType", file != null ? file.getContentType() : "null");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorDetails);
        }
    }

    @PostMapping(value = "/stops/{stopId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadStopPhoto(
            @PathVariable Integer stopId,
            @RequestParam("file") MultipartFile file) {

        log.info("Received request to upload photo for stop ID: {}", stopId);

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                log.warn("File is empty or null");
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Check file type (optional)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                log.warn("Invalid content type: {}", contentType);
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            log.info("Uploading file: {}, type: {}, size: {} bytes",
                    file.getOriginalFilename(), contentType, file.getSize());

            Photo photo = photoService.uploadStopPhoto(stopId, file);
            log.info("Photo uploaded successfully with ID: {}", photo.getId());

            return new ResponseEntity<>(photoMapper.toDTO(photo), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            log.error("Resource not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("Bad request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            log.error("Failed to upload photo", e);

            Map<String, Object> errorDetails = new HashMap<>();
            errorDetails.put("message", "Failed to upload photo");
            errorDetails.put("error", e.getMessage());
            errorDetails.put("stopId", stopId);
            errorDetails.put("fileName", file != null ? file.getOriginalFilename() : "null");
            errorDetails.put("fileSize", file != null ? file.getSize() : 0);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorDetails);
        }
    }

    @GetMapping("/locations/{locationId}")
    public ResponseEntity<List<PhotoDTO>> getLocationPhotos(@PathVariable Integer locationId) {
        List<Photo> photos = photoService.getLocationPhotos(locationId);
        return ResponseEntity.ok(photoMapper.toDTOList(photos));
    }

    @GetMapping("/stops/{stopId}")
    public ResponseEntity<List<PhotoDTO>> getStopPhotos(@PathVariable Integer stopId) {
        List<Photo> photos = photoService.getStopPhotos(stopId);
        return ResponseEntity.ok(photoMapper.toDTOList(photos));
    }

    @GetMapping("/user")
    public ResponseEntity<List<PhotoDTO>> getUserPhotos() {
        List<Photo> photos = photoService.getUserPhotos();
        return ResponseEntity.ok(photoMapper.toDTOList(photos));
    }

    @DeleteMapping("/{photoId}")
    public ResponseEntity<?> deletePhoto(@PathVariable Integer photoId) {
        try {
            photoService.deletePhoto(photoId);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (IOException e) {
            log.error("Error deleting photo", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete photo: " + e.getMessage());
        }
    }
}