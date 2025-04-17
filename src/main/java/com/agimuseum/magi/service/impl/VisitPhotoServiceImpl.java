package com.agimuseum.magi.service.impl;

import com.agimuseum.magi.dto.LocationVisitDTO;
import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.dto.StopVisitDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.*;
import com.agimuseum.magi.repository.*;
import com.agimuseum.magi.service.S3StorageService;
import com.agimuseum.magi.service.VisitPhotoService;
import com.agimuseum.magi.util.PhotoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of VisitPhotoService to handle photos used as proof of visits
 * Modified to automatically approve all photos
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitPhotoServiceImpl implements VisitPhotoService {

    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;
    private final PhotoRepository photoRepository;
    private final LocationVisitRepository locationVisitRepository;
    private final StopVisitRepository stopVisitRepository;
    private final S3StorageService s3StorageService;
    private final PhotoMapper photoMapper;

    /**
     * Upload a photo as visit proof for a location
     * Automatically approves the photo
     */
    @Override
    @Transactional
    public LocationVisitDTO uploadLocationVisitPhoto(Integer locationId, MultipartFile file) throws IOException {
        log.info("Uploading location visit proof photo for locationId: {}", locationId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Get current user
        User currentUser = getCurrentUser();
        log.info("Current user for location photo upload: {}", currentUser.getUsername());

        // Get location
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Delete existing visit proof photos for this location and user
        List<Photo> existingPhotos = photoRepository.findByUserAndLocationIdAndPhotoType(
                currentUser, locationId, PhotoType.VISIT_PROOF);

        log.info("Found {} existing proof photos to delete for location {}", existingPhotos.size(), locationId);

        for (Photo existingPhoto : existingPhotos) {
            try {
                log.info("Deleting existing photo with ID: {} and S3 key: {}", existingPhoto.getId(), existingPhoto.getS3Key());
                if (existingPhoto.getS3Key() != null) {
                    boolean deleted = s3StorageService.deleteFile(existingPhoto.getS3Key());
                    log.info("S3 file deletion result: {}", deleted);
                }
                photoRepository.delete(existingPhoto);
                log.info("Successfully deleted existing photo from database");
            } catch (Exception e) {
                log.error("Error deleting existing photo: {}", e.getMessage(), e);
            }
        }

        // Upload new photo to S3
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "visit_proof.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String filePath = "visits/locations/" + locationId + "/" + currentUser.getId() + "/";

        log.info("Uploading new photo to S3 path: {}", filePath);
        String url = s3StorageService.uploadFile(
                filePath,
                originalFilename,
                file.getBytes(),
                contentType
        );
        log.info("Successfully uploaded photo to S3, URL: {}", url);

        String s3Key = s3StorageService.extractKeyFromUrl(url);
        log.info("Extracted S3 key: {}", s3Key);

        // Create a photo record marked as visit proof - automatically approved
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
                .photoType(PhotoType.VISIT_PROOF)
                .approved(true)  // All photos are automatically approved
                .description("Visit proof for location: " + location.getName())
                .build();

        Photo savedPhoto = photoRepository.save(photo);
        log.info("Saved photo entity to database with ID: {}", savedPhoto.getId());

        // Check if location is already visited
        Optional<LocationVisit> existingVisit = locationVisitRepository.findByUserAndLocation(currentUser, location);

        LocationVisit visit;
        if (existingVisit.isPresent()) {
            // Update existing visit
            log.info("Updating existing location visit record");
            visit = existingVisit.get();
            visit.setHasPhotoProof(true);
            visit.setPhotoId(savedPhoto.getId());
            visit.setVisitMethod(LocationVisit.VisitMethod.PHOTO_UPLOAD);
        } else {
            // Create new visit
            log.info("Creating new location visit record");
            visit = LocationVisit.builder()
                    .user(currentUser)
                    .location(location)
                    .visitedAt(new Date())
                    .hasPhotoProof(true)
                    .photoId(savedPhoto.getId())
                    .visitMethod(LocationVisit.VisitMethod.PHOTO_UPLOAD)
                    .build();
        }

        LocationVisit savedVisit = locationVisitRepository.save(visit);
        log.info("Saved location visit record with ID: {}", savedVisit.getId());

        return LocationVisitDTO.builder()
                .locationId(savedVisit.getLocation().getId())
                .locationName(savedVisit.getLocation().getName())
                .visitedAt(savedVisit.getVisitedAt())
                .hasPhotoProof(true)
                .visitMethod(savedVisit.getVisitMethod().name())
                .photoUrl(savedPhoto.getUrl())
                .build();
    }

    /**
     * Upload a photo as visit proof for a stop
     * Automatically approves the photo
     */
    @Override
    @Transactional
    public StopVisitDTO uploadStopVisitPhoto(Integer stopId, MultipartFile file) throws IOException {
        log.info("Uploading stop visit proof photo for stopId: {}", stopId);

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be null or empty");
        }

        // Get current user
        User currentUser = getCurrentUser();
        log.info("Current user for stop photo upload: {}", currentUser.getUsername());

        // Get stop
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Delete existing visit proof photos for this stop and user
        List<Photo> existingPhotos = photoRepository.findByUserAndStopIdAndPhotoType(
                currentUser, stopId, PhotoType.VISIT_PROOF);

        log.info("Found {} existing proof photos to delete for stop {}", existingPhotos.size(), stopId);

        for (Photo existingPhoto : existingPhotos) {
            try {
                log.info("Deleting existing photo with ID: {} and S3 key: {}", existingPhoto.getId(), existingPhoto.getS3Key());
                if (existingPhoto.getS3Key() != null) {
                    boolean deleted = s3StorageService.deleteFile(existingPhoto.getS3Key());
                    log.info("S3 file deletion result: {}", deleted);
                }
                photoRepository.delete(existingPhoto);
                log.info("Successfully deleted existing photo from database");
            } catch (Exception e) {
                log.error("Error deleting existing photo: {}", e.getMessage(), e);
            }
        }

        // Upload new photo to S3
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "visit_proof.jpg";
        String contentType = file.getContentType() != null ? file.getContentType() : "image/jpeg";
        String filePath = "visits/stops/" + stopId + "/" + currentUser.getId() + "/";

        log.info("Uploading new photo to S3 path: {}", filePath);
        String url = s3StorageService.uploadFile(
                filePath,
                originalFilename,
                file.getBytes(),
                contentType
        );
        log.info("Successfully uploaded photo to S3, URL: {}", url);

        String s3Key = s3StorageService.extractKeyFromUrl(url);
        log.info("Extracted S3 key: {}", s3Key);

        // Create a photo record marked as visit proof - automatically approved
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
                .photoType(PhotoType.VISIT_PROOF)
                .approved(true)  // All photos are automatically approved
                .description("Visit proof for stop: " + stop.getName())
                .build();

        Photo savedPhoto = photoRepository.save(photo);
        log.info("Saved photo entity to database with ID: {}", savedPhoto.getId());

        // Check if stop is already visited
        Optional<StopVisit> existingVisit = stopVisitRepository.findByUserAndStop(currentUser, stop);

        StopVisit visit;
        if (existingVisit.isPresent()) {
            // Update existing visit
            log.info("Updating existing stop visit record");
            visit = existingVisit.get();
            visit.setHasPhotoProof(true);
            visit.setPhotoId(savedPhoto.getId());
            visit.setVisitMethod(StopVisit.VisitMethod.PHOTO_UPLOAD);
        } else {
            // Create new visit
            log.info("Creating new stop visit record");
            visit = StopVisit.builder()
                    .user(currentUser)
                    .stop(stop)
                    .visitedAt(new Date())
                    .hasPhotoProof(true)
                    .photoId(savedPhoto.getId())
                    .visitMethod(StopVisit.VisitMethod.PHOTO_UPLOAD)
                    .build();
        }

        StopVisit savedVisit = stopVisitRepository.save(visit);
        log.info("Saved stop visit record with ID: {}", savedVisit.getId());

        // Mark the parent location as visited if not already
        Location parentLocation = stop.getLocation();
        if (!locationVisitRepository.existsByUserAndLocation(currentUser, parentLocation)) {
            log.info("Auto-marking parent location as visited: {}", parentLocation.getId());
            LocationVisit locationVisit = LocationVisit.builder()
                    .user(currentUser)
                    .location(parentLocation)
                    .visitedAt(new Date())
                    .hasPhotoProof(false) // No direct photo for the location
                    .visitMethod(LocationVisit.VisitMethod.MANUAL)
                    .build();
            locationVisitRepository.save(locationVisit);
            log.info("Created parent location visit record");
        }

        return StopVisitDTO.builder()
                .stopId(savedVisit.getStop().getId())
                .stopName(savedVisit.getStop().getName())
                .locationId(savedVisit.getStop().getLocation().getId())
                .locationName(savedVisit.getStop().getLocation().getName())
                .visitedAt(savedVisit.getVisitedAt())
                .hasPhotoProof(true)
                .visitMethod(savedVisit.getVisitMethod().name())
                .photoUrl(savedPhoto.getUrl())
                .build();
    }

    // Other methods from VisitPhotoService interface...
    @Override
    public List<PhotoDTO> getUserLocationVisitPhotos(Integer locationId) {
        User user = getCurrentUser();

        // Check if location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Find all visit proof photos for this location by the current user
        List<Photo> photos = photoRepository.findByUserAndLocationIdAndPhotoType(
                user, locationId, PhotoType.VISIT_PROOF);

        if (photos.isEmpty()) {
            throw new ResourceNotFoundException("No visit proof photos found for this location");
        }

        return photoMapper.toDTOList(photos);
    }

    @Override
    public PhotoDTO getUserLocationVisitPhoto(Integer locationId) {
        User user = getCurrentUser();

        // Check if location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Find visit photos specifically with VISIT_PROOF type
        List<Photo> photos = photoRepository.findByUserIdAndLocationIdAndPhotoTypeOrderByUploadedAtDesc(
                user.getId(), locationId, PhotoType.VISIT_PROOF);

        if (photos.isEmpty()) {
            throw new ResourceNotFoundException("No visit proof photo found for this location");
        }

        // Get the most recent one (first in the list)
        return photoMapper.toDTO(photos.get(0));
    }

    @Override
    public List<PhotoDTO> getUserStopVisitPhotos(Integer stopId) {
        User user = getCurrentUser();

        // Check if stop exists
        stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Find all visit proof photos for this stop by the current user
        List<Photo> photos = photoRepository.findByUserAndStopIdAndPhotoType(
                user, stopId, PhotoType.VISIT_PROOF);

        if (photos.isEmpty()) {
            throw new ResourceNotFoundException("No visit proof photos found for this stop");
        }

        return photoMapper.toDTOList(photos);
    }

    @Override
    public PhotoDTO getUserStopVisitPhoto(Integer stopId) {
        User user = getCurrentUser();

        // Check if stop exists
        stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Find visit photos specifically with VISIT_PROOF type
        List<Photo> photos = photoRepository.findByUserIdAndStopIdAndPhotoTypeOrderByUploadedAtDesc(
                user.getId(), stopId, PhotoType.VISIT_PROOF);

        if (photos.isEmpty()) {
            throw new ResourceNotFoundException("No visit proof photo found for this stop");
        }

        // Get the most recent one (first in the list)
        return photoMapper.toDTO(photos.get(0));
    }

    @Override
    @Transactional
    public void deleteVisitPhoto(Integer photoId) throws IOException {
        User currentUser = getCurrentUser();

        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo not found with id: " + photoId));

        // Verify it's a visit proof photo
        if (photo.getPhotoType() != PhotoType.VISIT_PROOF) {
            throw new IllegalArgumentException("Photo is not a visit proof photo");
        }

        // Verify it belongs to the current user
        if (!photo.getUser().getId().equals(currentUser.getId()) && !currentUser.getRole().toString().equals("ADMIN")) {
            throw new IllegalStateException("You don't have permission to delete this photo");
        }

        // Delete from S3
        if (photo.getS3Key() != null) {
            boolean deleted = s3StorageService.deleteFile(photo.getS3Key());
            log.info("S3 file deletion result: {}", deleted);
        }

        // Delete from database
        photoRepository.delete(photo);
        log.info("Deleted photo entity from database");

        // Update location visit if this was the only photo proof
        if (photo.getLocationId() != null) {
            updateLocationVisitAfterPhotoDeletion(currentUser, photo.getLocationId());
        }

        // Update stop visit if this was the only photo proof
        if (photo.getStopId() != null) {
            updateStopVisitAfterPhotoDeletion(currentUser, photo.getStopId());
        }
    }

    @Override
    public boolean hasLocationVisitWithPhoto(Integer userId, Integer locationId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Check if there's a LocationVisit with hasPhotoProof=true
        Optional<LocationVisit> visit = locationVisitRepository.findByUserIdAndLocationId(userId, locationId);
        if (visit.isPresent() && visit.get().getHasPhotoProof() != null && visit.get().getHasPhotoProof()) {
            return true;
        }

        // If no direct visit record with photo proof, check if there's any VISIT_PROOF type photos
        List<Photo> photos = photoRepository.findByUserIdAndLocationIdAndPhotoTypeOrderByUploadedAtDesc(
                userId, locationId, PhotoType.VISIT_PROOF);

        return !photos.isEmpty();
    }

    @Override
    public boolean hasStopVisitWithPhoto(Integer userId, Integer stopId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if stop exists
        stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Check if there's a StopVisit with hasPhotoProof=true
        Optional<StopVisit> visit = stopVisitRepository.findByUserIdAndStopId(userId, stopId);
        if (visit.isPresent() && visit.get().getHasPhotoProof() != null && visit.get().getHasPhotoProof()) {
            return true;
        }

        // If no direct visit record with photo proof, check if there's any VISIT_PROOF type photos
        List<Photo> photos = photoRepository.findByUserIdAndStopIdAndPhotoTypeOrderByUploadedAtDesc(
                userId, stopId, PhotoType.VISIT_PROOF);

        return !photos.isEmpty();
    }

    @Override
    public int getLocationVisitPhotoCount(Integer userId, Integer locationId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if location exists
        locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Count visit proof photos
        List<Photo> photos = photoRepository.findByUserAndLocationIdAndPhotoType(
                user, locationId, PhotoType.VISIT_PROOF);

        return photos.size();
    }

    @Override
    public int getStopVisitPhotoCount(Integer userId, Integer stopId) {
        // Check if user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        // Check if stop exists
        stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Count visit proof photos
        List<Photo> photos = photoRepository.findByUserAndStopIdAndPhotoType(
                user, stopId, PhotoType.VISIT_PROOF);

        return photos.size();
    }

    /**
     * Update location visit after photo deletion
     */
    private void updateLocationVisitAfterPhotoDeletion(User user, Integer locationId) {
        // Check if there are any remaining visit proof photos
        List<Photo> remainingPhotos = photoRepository.findByUserAndLocationIdAndPhotoType(
                user, locationId, PhotoType.VISIT_PROOF);

        if (remainingPhotos.isEmpty()) {
            log.info("No remaining proof photos for location {}. Updating visit record.", locationId);
            // No more proof photos, update the visit record
            locationVisitRepository.findByUserIdAndLocationId(user.getId(), locationId)
                    .ifPresent(visit -> {
                        visit.setHasPhotoProof(false);
                        visit.setPhotoId(null);
                        locationVisitRepository.save(visit);
                        log.info("Updated location visit record: removed photo proof reference");
                    });
        }
    }

    /**
     * Update stop visit after photo deletion
     */
    private void updateStopVisitAfterPhotoDeletion(User user, Integer stopId) {
        // Check if there are any remaining visit proof photos
        List<Photo> remainingPhotos = photoRepository.findByUserAndStopIdAndPhotoType(
                user, stopId, PhotoType.VISIT_PROOF);

        if (remainingPhotos.isEmpty()) {
            log.info("No remaining proof photos for stop {}. Updating visit record.", stopId);
            // No more proof photos, update the visit record
            stopVisitRepository.findByUserIdAndStopId(user.getId(), stopId)
                    .ifPresent(visit -> {
                        visit.setHasPhotoProof(false);
                        visit.setPhotoId(null);
                        stopVisitRepository.save(visit);
                        log.info("Updated stop visit record: removed photo proof reference");
                    });
        }
    }

    /**
     * Get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}