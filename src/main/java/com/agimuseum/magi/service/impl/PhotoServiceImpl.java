package com.agimuseum.magi.service.impl;

import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.PhotoType;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.StopRepository;
import com.agimuseum.magi.repository.UserRepository;
import com.agimuseum.magi.service.PhotoService;
import com.agimuseum.magi.service.S3StorageService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of the PhotoService for managing photos
 * Modified to automatically approve all photos
 */
@Service
@Slf4j
public class PhotoServiceImpl implements PhotoService {

    private final S3StorageService s3StorageService;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;

    public PhotoServiceImpl(S3StorageService s3StorageService,
                            PhotoRepository photoRepository,
                            UserRepository userRepository,
                            LocationRepository locationRepository,
                            StopRepository stopRepository) {
        this.s3StorageService = s3StorageService;
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.stopRepository = stopRepository;
    }

    /**
     * Upload a photo for a location
     * All photos are automatically approved
     */
    @Override
    @Transactional
    public Photo uploadLocationPhoto(Integer locationId, MultipartFile file) throws IOException {
        log.debug("Starting upload of location photo for locationId: {}", locationId);

        if (file == null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Get current user
        User currentUser = getCurrentUser();
        log.debug("Current user: {}", currentUser.getUsername());

        // Get location
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> {
                    log.error("Location not found with id: {}", locationId);
                    return new ResourceNotFoundException("Location not found with id: " + locationId);
                });
        log.debug("Found location: {}", location.getName());

        // Generate safe filename
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // Use a separate folder structure for photos
        String filePath = "photos/locations/" + locationId + "/";

        log.debug("Uploading file: {} with content type: {} to path: {}", originalFilename, contentType, filePath);

        try {
            // Upload to S3
            String url = s3StorageService.uploadFile(
                    filePath,
                    originalFilename,
                    file.getBytes(),
                    contentType
            );

            String s3Key = s3StorageService.extractKeyFromUrl(url);
            log.debug("File uploaded successfully, URL: {}", url);

            // Create and save photo entity - automatically approved
            Photo photo = Photo.builder()
                    .fileName(originalFilename)
                    .contentType(contentType)
                    .contentTypeField(contentType)
                    .url(url)
                    .photoUrl(url)
                    .s3Key(s3Key)
                    .s3Bucket("agimuseum-storage")
                    .referenceId(UUID.randomUUID().toString())
                    .locationId(locationId)
                    .locationName(location.getName())
                    .user(currentUser)
                    .uploadedAt(new Date())
                    .photoType(PhotoType.STOCK)
                    .approved(true)  // All photos are automatically approved
                    .description("Photo for location: " + location.getName())
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            log.debug("Photo entity saved successfully with ID: {}", savedPhoto.getId());

            return savedPhoto;
        } catch (Exception e) {
            log.error("Error uploading photo to S3", e);
            throw new IOException("Failed to upload photo: " + e.getMessage(), e);
        }
    }

    /**
     * Upload a photo for a stop
     * All photos are automatically approved
     */
    @Override
    @Transactional
    public Photo uploadStopPhoto(Integer stopId, MultipartFile file) throws IOException {
        log.debug("Starting upload of stop photo for stopId: {}", stopId);

        if (file == null || file.isEmpty()) {
            log.error("File is null or empty");
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Get current user
        User currentUser = getCurrentUser();
        log.debug("Current user: {}", currentUser.getUsername());

        // Get stop
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> {
                    log.error("Stop not found with id: {}", stopId);
                    return new ResourceNotFoundException("Stop not found with id: " + stopId);
                });
        log.debug("Found stop: {}", stop.getName());

        // Generate safe filename
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // Use a separate folder structure for photos
        String filePath = "photos/stops/" + stopId + "/";

        log.debug("Uploading file: {} with content type: {} to path: {}", originalFilename, contentType, filePath);

        try {
            // Upload to S3
            String url = s3StorageService.uploadFile(
                    filePath,
                    originalFilename,
                    file.getBytes(),
                    contentType
            );

            String s3Key = s3StorageService.extractKeyFromUrl(url);
            log.debug("File uploaded successfully, URL: {}", url);

            // Create and save photo entity - automatically approved
            Photo photo = Photo.builder()
                    .fileName(originalFilename)
                    .contentType(contentType)
                    .contentTypeField(contentType)
                    .url(url)
                    .photoUrl(url)
                    .s3Key(s3Key)
                    .s3Bucket("agimuseum-storage")
                    .referenceId(UUID.randomUUID().toString())
                    .locationId(stop.getLocation().getId())
                    .locationName(stop.getLocation().getName())
                    .stopId(stopId)
                    .stopName(stop.getName())
                    .user(currentUser)
                    .uploadedAt(new Date())
                    .photoType(PhotoType.STOCK)
                    .approved(true)  // All photos are automatically approved
                    .description("Photo for stop: " + stop.getName())
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            log.debug("Photo entity saved successfully with ID: {}", savedPhoto.getId());

            return savedPhoto;
        } catch (Exception e) {
            log.error("Error uploading photo to S3", e);
            throw new IOException("Failed to upload photo: " + e.getMessage(), e);
        }
    }

    /**
     * Get all photos for a location
     * Returns all photos including both STOCK and VISIT_PROOF types
     */
    @Override
    public List<Photo> getLocationPhotos(Integer locationId) {
        // Ensure the location exists
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found with id: " + locationId);
        }

        // Get all approved photos for this location
        return photoRepository.findByLocationIdAndApprovedTrueOrderByUploadedAtDesc(locationId);
    }



    /**
     * Get all photos for a stop
     * Returns all photos including both STOCK and VISIT_PROOF types
     */
    @Override
    public List<Photo> getStopPhotos(Integer stopId) {
        // Ensure the stop exists
        if (!stopRepository.existsById(stopId)) {
            throw new ResourceNotFoundException("Stop not found with id: " + stopId);
        }

        // Get all photos for this stop
        return photoRepository.findByStopIdOrderByUploadedAtDesc(stopId);
    }

    /**
     * Get all photos uploaded by the current user
     */
    @Override
    public List<Photo> getUserPhotos() {
        User currentUser = getCurrentUser();
        return photoRepository.findByUser(currentUser);
    }

    /**
     * Get the most recent photo taken by a user at a location
     */
    @Override
    public Photo getUserLocationPhoto(Integer userId, Integer locationId) {
        List<Photo> photos = photoRepository.findByUserIdAndLocationIdOrderByUploadedAtDesc(userId, locationId);
        return photos.isEmpty() ? null : photos.get(0);
    }

    /**
     * Get the most recent photo taken by a user at a stop
     */
    @Override
    public Photo getUserStopPhoto(Integer userId, Integer stopId) {
        List<Photo> photos = photoRepository.findByUserIdAndStopIdOrderByUploadedAtDesc(userId, stopId);
        return photos.isEmpty() ? null : photos.get(0);
    }

    /**
     * Delete a photo by ID
     */
    @Override
    @Transactional
    public void deletePhoto(Integer photoId) throws IOException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Check if current user is the owner or has admin role
        User currentUser = getCurrentUser();
        if (!photo.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().toString().equals("ADMIN")) {
            throw new IllegalStateException("You don't have permission to delete this photo");
        }

        // Delete from S3
        String s3Key = photo.getS3Key();
        if (s3Key != null) {
            boolean deleted = s3StorageService.deleteFile(s3Key);
            if (!deleted) {
                log.warn("Could not delete file from S3 with key: {}", s3Key);
            }
        } else {
            // If s3Key is not available, try to extract it from the URL
            String url = photo.getUrl();
            s3Key = s3StorageService.extractKeyFromUrl(url);
            if (s3Key != null) {
                boolean deleted = s3StorageService.deleteFile(s3Key);
                if (!deleted) {
                    log.warn("Could not delete file from S3 with extracted key: {}", s3Key);
                }
            }
        }

        // Delete from database
        photoRepository.delete(photo);
        log.info("Photo deleted successfully: ID={}, Type={}", photoId, photo.getPhotoType());
    }

    /**
     * Get current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}