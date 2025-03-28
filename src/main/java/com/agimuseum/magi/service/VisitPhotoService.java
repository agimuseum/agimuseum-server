package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.dto.LocationVisitDTO;
import com.agimuseum.magi.dto.StopVisitDTO;
import com.agimuseum.magi.exception.ResourceNotFoundException;
import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.LocationVisit;
import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.model.StopVisit;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.repository.LocationRepository;
import com.agimuseum.magi.repository.PhotoRepository;
import com.agimuseum.magi.repository.StopRepository;
import com.agimuseum.magi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Service for handling photo submissions as proof of visits
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VisitPhotoService {

    private final PhotoService photoService;
    private final VisitService visitService;
    private final UserRepository userRepository;
    private final LocationRepository locationRepository;
    private final StopRepository stopRepository;
    private final PhotoRepository photoRepository;

    /**
     * Uploads a photo for a location visit and marks the location as visited
     *
     * @param locationId ID of the location being visited
     * @param file Photo file as proof of visit
     * @return The location visit info
     * @throws IOException If there's an error handling the file
     */
    @Transactional
    public LocationVisitDTO uploadLocationVisitPhoto(Integer locationId, MultipartFile file) throws IOException {
        // First, upload the photo
        Photo photo = photoService.uploadLocationPhoto(locationId, file);

        // Then mark the location as visited with photo proof
        LocationVisitDTO visit = visitService.markLocationVisited(
                locationId,
                photo.getId(),
                LocationVisit.VisitMethod.PHOTO_UPLOAD
        );

        log.info("User {} visited location {} with photo proof (photo ID: {})",
                getCurrentUser().getUsername(), locationId, photo.getId());

        return visit;
    }

    /**
     * Uploads a photo for a stop visit and marks the stop as visited
     *
     * @param stopId ID of the stop being visited
     * @param file Photo file as proof of visit
     * @return The stop visit info
     * @throws IOException If there's an error handling the file
     */
    @Transactional
    public StopVisitDTO uploadStopVisitPhoto(Integer stopId, MultipartFile file) throws IOException {
        // First, upload the photo
        Photo photo = photoService.uploadStopPhoto(stopId, file);

        // Then mark the stop as visited with photo proof
        StopVisitDTO visit = visitService.markStopVisited(
                stopId,
                photo.getId(),
                StopVisit.VisitMethod.PHOTO_UPLOAD
        );

        log.info("User {} visited stop {} with photo proof (photo ID: {})",
                getCurrentUser().getUsername(), stopId, photo.getId());

        return visit;
    }

    /**
     * Gets the most recent visit photo for a location taken by the current user
     *
     * @param locationId ID of the location
     * @return The photo DTO or null if not found
     */
    public PhotoDTO getUserLocationVisitPhoto(Integer locationId) {
        User user = getCurrentUser();
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        // Find the most recent photo by this user for this location
        Photo photo = photoRepository.findTopByUserIdAndLocationIdOrderByUploadedAtDesc(user.getId(), locationId);

        if (photo == null) {
            throw new ResourceNotFoundException("No visit photo found for location: " + locationId);
        }

        return mapPhotoToDTO(photo);
    }

    /**
     * Gets the most recent visit photo for a stop taken by the current user
     *
     * @param stopId ID of the stop
     * @return The photo DTO or null if not found
     */
    public PhotoDTO getUserStopVisitPhoto(Integer stopId) {
        User user = getCurrentUser();
        Stop stop = stopRepository.findById(stopId)
                .orElseThrow(() -> new ResourceNotFoundException("Stop not found with id: " + stopId));

        // Find the most recent photo by this user for this stop
        Photo photo = photoRepository.findTopByUserIdAndStopIdOrderByUploadedAtDesc(user.getId(), stopId);

        if (photo == null) {
            throw new ResourceNotFoundException("No visit photo found for stop: " + stopId);
        }

        return mapPhotoToDTO(photo);
    }

    /**
     * Check if a user has visited a location with photo proof
     *
     * @param userId User ID
     * @param locationId Location ID
     * @return true if the user has a photo proof for this location
     */
    public boolean hasLocationVisitWithPhoto(Integer userId, Integer locationId) {
        Photo photo = photoRepository.findTopByUserIdAndLocationIdOrderByUploadedAtDesc(userId, locationId);
        return photo != null;
    }

    /**
     * Check if a user has visited a stop with photo proof
     *
     * @param userId User ID
     * @param stopId Stop ID
     * @return true if the user has a photo proof for this stop
     */
    public boolean hasStopVisitWithPhoto(Integer userId, Integer stopId) {
        Photo photo = photoRepository.findTopByUserIdAndStopIdOrderByUploadedAtDesc(userId, stopId);
        return photo != null;
    }

    /**
     * Helper method to map a Photo entity to a DTO
     */
    private PhotoDTO mapPhotoToDTO(Photo photo) {
        return PhotoDTO.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .contentType(photo.getContentType())
                .url(photo.getUrl())
                .locationId(photo.getLocationId())
                .locationName(photo.getLocationName())
                .stopId(photo.getStopId())
                .stopName(photo.getStopName())
                .uploaderName(photo.getUser() != null ?
                        photo.getUser().getFirstname() + " " + photo.getUser().getLastname() : "Unknown")
                .uploadedAt(photo.getUploadedAt())
                .build();
    }

    /**
     * Helper method to get the current authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Current user not found"));
    }
}