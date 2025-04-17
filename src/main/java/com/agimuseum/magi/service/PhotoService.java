package com.agimuseum.magi.service;

import com.agimuseum.magi.model.Photo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Service interface for managing location and stop photos
 * These are "stock" photos of locations, not proof-of-visit photos
 */
public interface PhotoService {

    /**
     * Upload a photo for a location
     * @param locationId The location ID
     * @param file The photo file to upload
     * @return The created photo entity
     * @throws IOException If there's an error processing the file
     */
    Photo uploadLocationPhoto(Integer locationId, MultipartFile file) throws IOException;

    /**
     * Upload a photo for a stop
     * @param stopId The stop ID
     * @param file The photo file to upload
     * @return The created photo entity
     * @throws IOException If there's an error processing the file
     */
    Photo uploadStopPhoto(Integer stopId, MultipartFile file) throws IOException;

    /**
     * Get all photos for a location
     * @param locationId The location ID
     * @return List of photos for the location
     */
    List<Photo> getLocationPhotos(Integer locationId);

    /**
     * Get all photos for a stop
     * @param stopId The stop ID
     * @return List of photos for the stop
     */
    List<Photo> getStopPhotos(Integer stopId);

    /**
     * Get all photos uploaded by the current user
     * @return List of photos uploaded by the current user
     */
    List<Photo> getUserPhotos();

    /**
     * Get the most recent photo taken by a user at a location
     * @param userId The user ID
     * @param locationId The location ID
     * @return The most recent photo or null if none exists
     */
    Photo getUserLocationPhoto(Integer userId, Integer locationId);

    /**
     * Get the most recent photo taken by a user at a stop
     * @param userId The user ID
     * @param stopId The stop ID
     * @return The most recent photo or null if none exists
     */
    Photo getUserStopPhoto(Integer userId, Integer stopId);

    /**
     * Delete a photo by ID
     * @param photoId The photo ID to delete
     * @throws IOException If there's an error deleting the file
     */
    void deletePhoto(Integer photoId) throws IOException;
}