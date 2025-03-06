package com.agimuseum.magi.controller;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.PhotoService;
import com.agimuseum.magi.util.PhotoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    private final PhotoService photoService;
    private final PhotoMapper photoMapper;
    private final UserRepository userRepository;

    public PhotoController(PhotoService photoService, PhotoMapper photoMapper, UserRepository userRepository) {
        this.photoService = photoService;
        this.photoMapper = photoMapper;
        this.userRepository = userRepository;
    }

    @PostMapping("/locations/{locationId}")
    public ResponseEntity<?> uploadLocationPhoto(
            @PathVariable Integer locationId,
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Check file type (optional)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            Photo photo = photoService.uploadLocationPhoto(locationId, file);
            return new ResponseEntity<>(photoMapper.toDTO(photo), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload photo: " + e.getMessage());
        }
    }

    @PostMapping("/stops/{stopId}")
    public ResponseEntity<?> uploadStopPhoto(
            @PathVariable Integer stopId,
            @RequestParam("file") MultipartFile file) {

        try {
            // Validate file
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Check file type (optional)
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Only image files are allowed");
            }

            Photo photo = photoService.uploadStopPhoto(stopId, file);
            return new ResponseEntity<>(photoMapper.toDTO(photo), HttpStatus.CREATED);
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to upload photo: " + e.getMessage());
        }
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
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete photo: " + e.getMessage());
        }
    }

    // Get photos uploaded by the current user for a location
    @GetMapping("/my-photos/locations/{locationId}")
    public ResponseEntity<List<PhotoDTO>> getMyLocationPhotos(@PathVariable Integer locationId) {
        List<Photo> photos = photoService.getUserPhotosForLocation(locationId);
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(photoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(photoDTOs);
    }

    // Get photos uploaded by the current user for a stop
    @GetMapping("/my-photos/stops/{stopId}")
    public ResponseEntity<List<PhotoDTO>> getMyStopPhotos(@PathVariable Integer stopId) {
        List<Photo> photos = photoService.getUserPhotosForStop(stopId);
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(photoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(photoDTOs);
    }

    // Get all photos for a location (public)
    @GetMapping("/locations/{locationId}")
    public ResponseEntity<List<PhotoDTO>> getAllLocationPhotos(@PathVariable Integer locationId) {
        List<Photo> photos = photoService.getAllLocationPhotos(locationId);
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(photoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(photoDTOs);
    }

    // Get all photos for a stop (public)
    @GetMapping("/stops/{stopId}")
    public ResponseEntity<List<PhotoDTO>> getAllStopPhotos(@PathVariable Integer stopId) {
        List<Photo> photos = photoService.getAllStopPhotos(stopId);
        List<PhotoDTO> photoDTOs = photos.stream()
                .map(photoMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(photoDTOs);
    }

    // Get count of photos uploaded by current user for a location
    @GetMapping("/my-photos/locations/{locationId}/count")
    public ResponseEntity<Long> getMyLocationPhotoCount(@PathVariable Integer locationId) {
        User currentUser = getCurrentUser();
        long count = photoService.getLocationPhotoCount(currentUser, locationId);
        return ResponseEntity.ok(count);
    }

    // Get count of photos uploaded by current user for a stop
    @GetMapping("/my-photos/stops/{stopId}/count")
    public ResponseEntity<Long> getMyStopPhotoCount(@PathVariable Integer stopId) {
        User currentUser = getCurrentUser();
        long count = photoService.getStopPhotoCount(currentUser, stopId);
        return ResponseEntity.ok(count);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}