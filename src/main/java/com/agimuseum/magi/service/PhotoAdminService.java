package com.agimuseum.magi.service;

import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.PhotoType;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service interface for administrative photo operations
 */
public interface PhotoAdminService {

    /**
     * Approve a photo
     * @param photoId The photo ID to approve
     * @return The approved photo
     */
    Photo approvePhoto(Integer photoId);

    /**
     * Reject a photo (delete it)
     * @param photoId The photo ID to reject
     * @throws IOException If there's an error deleting the file
     */
    void rejectPhoto(Integer photoId) throws IOException;

    /**
     * Change a photo's type
     * @param photoId The photo ID
     * @param newType The new photo type
     * @return The updated photo
     */
    Photo changePhotoType(Integer photoId, PhotoType newType);

    /**
     * Bulk approve photos
     * @param photoIds List of photo IDs to approve
     * @return Map with operation results
     */
    Map<String, Object> bulkApprovePhotos(List<Integer> photoIds);

    /**
     * Get all photos pending approval
     * @return List of pending photos
     */
    List<Photo> getPendingPhotos();

    /**
     * Get photos pending approval of a specific type
     * @param photoType The photo type
     * @return List of pending photos of the specified type
     */
    List<Photo> getPendingPhotosByType(PhotoType photoType);

    /**
     * Add a description to a photo
     * @param photoId The photo ID
     * @param description The description to add
     * @return The updated photo
     */
    Photo addPhotoDescription(Integer photoId, String description);
}