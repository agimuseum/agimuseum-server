package com.agimuseum.magi.service;

import com.agimuseum.magi.dto.LocationVisitDTO;
import com.agimuseum.magi.dto.PhotoDTO;
import com.agimuseum.magi.dto.StopVisitDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for handling photos uploaded as proof of visits
 * These are distinct from regular stock photos of locations and stops
 */
public interface VisitPhotoService {

    /**
     * Uploads a photo for a location visit and marks the location as visited
     * If user already has MAX_VISIT_PHOTOS (3), the oldest one will be replaced
     *
     * @param locationId ID of the location being visited
     * @param file Photo file as proof of visit
     * @param deleteExisting whether to delete existing photos for this location
     * @return The location visit info
     * @throws IOException If there's an error handling the file
     */
    LocationVisitDTO uploadLocationVisitPhoto(Integer locationId, MultipartFile file, boolean deleteExisting) throws IOException;

    /**
     * Legacy method without deleteExisting parameter
     */
    default LocationVisitDTO uploadLocationVisitPhoto(Integer locationId, MultipartFile file) throws IOException {
        return uploadLocationVisitPhoto(locationId, file, true);
    }

    /**
     * Uploads a photo for a stop visit and marks the stop as visited
     * If user already has MAX_VISIT_PHOTOS (3), the oldest one will be replaced
     *
     * @param stopId ID of the stop being visited
     * @param file Photo file as proof of visit
     * @param deleteExisting whether to delete existing photos for this stop
     * @return The stop visit info
     * @throws IOException If there's an error handling the file
     */
    StopVisitDTO uploadStopVisitPhoto(Integer stopId, MultipartFile file, boolean deleteExisting) throws IOException;

    /**
     * Legacy method without deleteExisting parameter
     */
    default StopVisitDTO uploadStopVisitPhoto(Integer stopId, MultipartFile file) throws IOException {
        return uploadStopVisitPhoto(stopId, file, true);
    }

    /**
     * Gets all visit photos for a location taken by the current user
     *
     * @param locationId ID of the location
     * @return List of photo DTOs
     */
    List<PhotoDTO> getUserLocationVisitPhotos(Integer locationId);

    /**
     * Gets all visit photos for a stop taken by the current user
     *
     * @param stopId ID of the stop
     * @return List of photo DTOs
     */
    List<PhotoDTO> getUserStopVisitPhotos(Integer stopId);

    /**
     * Delete a visit proof photo
     *
     * @param photoId ID of the photo to delete
     * @throws IOException If there's an error deleting the file
     */
    void deleteVisitPhoto(Integer photoId) throws IOException;

    /**
     * Delete all existing visit proof photos for a location
     *
     * @param locationId ID of the location
     * @throws IOException If there's an error deleting any of the files
     */
    void deleteExistingLocationVisitPhotos(Integer locationId) throws IOException;

    /**
     * Delete all existing visit proof photos for a stop
     *
     * @param stopId ID of the stop
     * @throws IOException If there's an error deleting any of the files
     */
    void deleteExistingStopVisitPhotos(Integer stopId) throws IOException;

    /**
     * Check if a user has visited a location with photo proof
     *
     * @param userId User ID
     * @param locationId Location ID
     * @return true if the user has a photo proof for this location
     */
    boolean hasLocationVisitWithPhoto(Integer userId, Integer locationId);

    /**
     * Check if a user has visited a stop with photo proof
     *
     * @param userId User ID
     * @param stopId Stop ID
     * @return true if the user has a photo proof for this stop
     */
    boolean hasStopVisitWithPhoto(Integer userId, Integer stopId);

    /**
     * Get the number of visit proof photos a user has for a location
     *
     * @param userId User ID
     * @param locationId Location ID
     * @return Number of visit proof photos
     */
    int getLocationVisitPhotoCount(Integer userId, Integer locationId);

    /**
     * Get the number of visit proof photos a user has for a stop
     *
     * @param userId User ID
     * @param stopId Stop ID
     * @return Number of visit proof photos
     */
    int getStopVisitPhotoCount(Integer userId, Integer stopId);
}