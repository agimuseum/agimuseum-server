package com.agimuseum.magi.service;

import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.StopRepository;
import com.agimuseum.magi.repository.UserRepository;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Objects;

/**
 * Service for handling photo uploads, storage and retrieval
 */
@Service
@Slf4j
public class PhotoService {

    private final Storage storage;
    private final PhotoRepository photoRepository;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;

    @Value("${firebase.storage.bucket}")
    private String storageBucket;

    public PhotoService(Storage storage,
                        PhotoRepository photoRepository,
                        UserRepository userRepository,
                        LocationRepository locationRepository,
                        StopRepository stopRepository) {
        this.storage = storage;
        this.photoRepository = photoRepository;
        this.userRepository = userRepository;
        this.locationRepository = locationRepository;
        this.stopRepository = stopRepository;
    }

    /**
     * Upload a photo for a location
     */
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

        // Check if user has already uploaded 3 photos for this location
        long photoCount = photoRepository.countByUserAndLocationId(currentUser, locationId);
        if (photoCount >= 3) {
            log.error("User has already uploaded maximum number of photos for this location");
            throw new IllegalStateException("Maximum number of photos (3) already uploaded for this location");
        }

        // Generate safe filename
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String fileName = generateFileName(file);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String firebasePath = "locations/" + locationId + "/" + fileName;

        log.debug("Uploading file: {} with content type: {} to path: {}", fileName, contentType, firebasePath);

        try {
            // Add metadata to help with debugging
            Map<String, String> metadata = Map.of(
                    "uploadedBy", currentUser.getUsername(),
                    "originalFilename", originalFilename,
                    "uploadTimestamp", String.valueOf(System.currentTimeMillis())
            );

            BlobId blobId = BlobId.of(storageBucket, firebasePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setMetadata(metadata)
                    .build();

            // Log the upload attempt details
            log.debug("Uploading to bucket: {}, path: {}", storageBucket, firebasePath);

            // Try with Storage.BlobWriteOption to set predefined ACL
            Blob blob = storage.create(blobInfo, file.getBytes(),
                    Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

            if (blob == null) {
                log.error("Failed to create blob in Firebase Storage");
                throw new IOException("Failed to upload file to Firebase Storage - returned null blob");
            }

            String url = "https://storage.googleapis.com/" + storageBucket + "/" + firebasePath;
            log.debug("File uploaded successfully, URL: {}", url);

            // Create and save photo entity with explicit fileName set
            Photo photo = Photo.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .contentTypeField(contentType)
                    .url(url)
                    .photoUrl(url)
                    .firebasePath(firebasePath)
                    .referenceId(UUID.randomUUID().toString())
                    .locationId(locationId)
                    .locationName(location.getName())
                    .user(currentUser)
                    .uploadedAt(new Date())
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            log.debug("Photo entity saved successfully with ID: {}", savedPhoto.getId());

            return savedPhoto;
        } catch (Exception e) {
            log.error("Error uploading photo to Firebase Storage", e);
            throw new IOException("Failed to upload photo: " + e.getMessage(), e);
        }
    }

    /**
     * Upload a photo for a stop
     */
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

        // Check if user has already uploaded 3 photos for this stop
        long photoCount = photoRepository.countByUserAndStopId(currentUser, stopId);
        if (photoCount >= 3) {
            log.error("User has already uploaded maximum number of photos for this stop");
            throw new IllegalStateException("Maximum number of photos (3) already uploaded for this stop");
        }

        // Generate safe filename
        String originalFilename = Objects.requireNonNull(file.getOriginalFilename(), "Filename cannot be null");
        String fileName = generateFileName(file);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String firebasePath = "stops/" + stopId + "/" + fileName;

        log.debug("Uploading file: {} with content type: {} to path: {}", fileName, contentType, firebasePath);

        try {
            // Add metadata to help with debugging
            Map<String, String> metadata = Map.of(
                    "uploadedBy", currentUser.getUsername(),
                    "originalFilename", originalFilename,
                    "uploadTimestamp", String.valueOf(System.currentTimeMillis())
            );

            BlobId blobId = BlobId.of(storageBucket, firebasePath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(contentType)
                    .setMetadata(metadata)
                    .build();

            // Try with Storage.BlobWriteOption to set predefined ACL
            Blob blob = storage.create(blobInfo, file.getBytes(),
                    Storage.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ));

            if (blob == null) {
                log.error("Failed to create blob in Firebase Storage");
                throw new IOException("Failed to upload file to Firebase Storage - returned null blob");
            }

            String url = "https://storage.googleapis.com/" + storageBucket + "/" + firebasePath;
            log.debug("File uploaded successfully, URL: {}", url);

            // Create and save photo entity
            Photo photo = Photo.builder()
                    .fileName(fileName)
                    .contentType(contentType)
                    .contentTypeField(contentType)
                    .url(url)
                    .photoUrl(url)
                    .firebasePath(firebasePath)
                    .referenceId(UUID.randomUUID().toString())
                    .locationId(stop.getLocation().getId())
                    .locationName(stop.getLocation().getName())
                    .stopId(stopId)
                    .stopName(stop.getName())
                    .user(currentUser)
                    .uploadedAt(new Date())
                    .build();

            Photo savedPhoto = photoRepository.save(photo);
            log.debug("Photo entity saved successfully with ID: {}", savedPhoto.getId());

            return savedPhoto;
        } catch (Exception e) {
            log.error("Error uploading photo to Firebase Storage", e);
            throw new IOException("Failed to upload photo: " + e.getMessage(), e);
        }
    }

    /**
     * Get all photos for a location
     */
    public List<Photo> getLocationPhotos(Integer locationId) {
        return photoRepository.findByLocationIdOrderByUploadedAtDesc(locationId);
    }

    /**
     * Get all photos for a stop
     */
    public List<Photo> getStopPhotos(Integer stopId) {
        return photoRepository.findByStopIdOrderByUploadedAtDesc(stopId);
    }

    /**
     * Get all photos uploaded by the current user
     */
    public List<Photo> getUserPhotos() {
        User currentUser = getCurrentUser();
        return photoRepository.findByUser(currentUser);
    }

    /**
     * Get the most recent photo taken by a user at a location
     */
    public Photo getUserLocationPhoto(Integer userId, Integer locationId) {
        return photoRepository.findTopByUserIdAndLocationIdOrderByUploadedAtDesc(userId, locationId);
    }

    /**
     * Get the most recent photo taken by a user at a stop
     */
    public Photo getUserStopPhoto(Integer userId, Integer stopId) {
        return photoRepository.findTopByUserIdAndStopIdOrderByUploadedAtDesc(userId, stopId);
    }

    /**
     * Delete a photo by ID
     */
    public void deletePhoto(Integer photoId) throws IOException {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Check if current user is the owner or has admin role
        User currentUser = getCurrentUser();
        if (!photo.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().toString().equals("ADMIN")) {
            throw new IllegalStateException("You don't have permission to delete this photo");
        }

        // Delete from Firebase Storage
        BlobId blobId = BlobId.of(storageBucket, photo.getFirebasePath());
        boolean deleted = storage.delete(blobId);

        if (!deleted) {
            log.warn("Could not delete file from Firebase Storage at path: {}", photo.getFirebasePath());
        }

        // Delete from database
        photoRepository.delete(photo);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }

    private String generateFileName(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String extension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            extension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        return UUID.randomUUID().toString() + extension;
    }
}