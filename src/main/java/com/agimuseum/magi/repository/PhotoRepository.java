package com.agimuseum.magi.repository;

import java.util.List;
import java.util.Optional;

import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.PhotoType;
import com.agimuseum.magi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    // Find photos by location
    List<Photo> findByLocationId(Integer locationId);

    // Find photos by location ordered by upload date (newest first)
    List<Photo> findByLocationIdOrderByUploadedAtDesc(Integer locationId);

    // Find photos by approved status for a location
    List<Photo> findByLocationIdAndApprovedTrue(Integer locationId);

    // Find photos by type and location
    List<Photo> findByLocationIdAndPhotoType(Integer locationId, PhotoType photoType);

    // Find approved photos by type and location
    List<Photo> findByLocationIdAndPhotoTypeAndApprovedTrue(Integer locationId, PhotoType photoType);

    // Find photos by type and location ordered by upload date
    List<Photo> findByLocationIdAndPhotoTypeOrderByUploadedAtDesc(Integer locationId, PhotoType photoType);

    // Find approved photos by type and location ordered by upload date
    List<Photo> findByLocationIdAndPhotoTypeAndApprovedTrueOrderByUploadedAtDesc(Integer locationId, PhotoType photoType);

    // Find photos by stop
    List<Photo> findByStopId(Integer stopId);

    // Find photos by stop ordered by upload date (newest first)
    List<Photo> findByStopIdOrderByUploadedAtDesc(Integer stopId);

    // Find photos by approved status for a stop
    List<Photo> findByStopIdAndApprovedTrue(Integer stopId);

    // Find photos by type and stop
    List<Photo> findByStopIdAndPhotoType(Integer stopId, PhotoType photoType);

    // Find approved photos by type and stop
    List<Photo> findByStopIdAndPhotoTypeAndApprovedTrue(Integer stopId, PhotoType photoType);

    // Find photos by type and stop ordered by upload date
    List<Photo> findByStopIdAndPhotoTypeOrderByUploadedAtDesc(Integer stopId, PhotoType photoType);

    // Find approved photos by type and stop ordered by upload date
    List<Photo> findByStopIdAndPhotoTypeAndApprovedTrueOrderByUploadedAtDesc(Integer stopId, PhotoType photoType);

    // Find photos by user and location
    List<Photo> findByUserAndLocationId(User user, Integer locationId);

    // Find photos by user, location, and type
    List<Photo> findByUserAndLocationIdAndPhotoType(User user, Integer locationId, PhotoType photoType);

    // Find photos by user and stop
    List<Photo> findByUserAndStopId(User user, Integer stopId);

    // Find photos by user, stop, and type
    List<Photo> findByUserAndStopIdAndPhotoType(User user, Integer stopId, PhotoType photoType);

    // Find all photos by user
    List<Photo> findByUser(User user);

    // Find all photos by user and type
    List<Photo> findByUserAndPhotoType(User user, PhotoType photoType);

    // Count photos by user
    long countByUser(User user);

    // Count photos by user and type
    long countByUserAndPhotoType(User user, PhotoType photoType);

    // Count photos by user and location (for enforcing the 3-photo limit)
    long countByUserAndLocationId(User user, Integer locationId);

    // Count photos by user, location, and type
    long countByUserAndLocationIdAndPhotoType(User user, Integer locationId, PhotoType photoType);

    // Count photos by user and stop (for enforcing the 3-photo limit)
    long countByUserAndStopId(User user, Integer stopId);

    // Count photos by user, stop, and type
    long countByUserAndStopIdAndPhotoType(User user, Integer stopId, PhotoType photoType);

    // Find the most recent photo taken by a user at a specific location
    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.locationId = ?2 ORDER BY p.uploadedAt DESC")
    List<Photo> findByUserIdAndLocationIdOrderByUploadedAtDesc(Integer userId, Integer locationId);

    // Find the most recent photo of a specific type taken by a user at a specific location
    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.locationId = ?2 AND p.photoType = ?3 ORDER BY p.uploadedAt DESC")
    List<Photo> findByUserIdAndLocationIdAndPhotoTypeOrderByUploadedAtDesc(Integer userId, Integer locationId, PhotoType photoType);

    // Find the most recent photo taken by a user at a specific stop
    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.stopId = ?2 ORDER BY p.uploadedAt DESC")
    List<Photo> findByUserIdAndStopIdOrderByUploadedAtDesc(Integer userId, Integer stopId);

    // Find the most recent photo of a specific type taken by a user at a specific stop
    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.stopId = ?2 AND p.photoType = ?3 ORDER BY p.uploadedAt DESC")
    List<Photo> findByUserIdAndStopIdAndPhotoTypeOrderByUploadedAtDesc(Integer userId, Integer stopId, PhotoType photoType);

    // Find photos pending approval
    List<Photo> findByApprovedFalseOrderByUploadedAtAsc();

    // Find approved photos for a location ordered by uploaded date
    List<Photo> findByLocationIdAndApprovedTrueOrderByUploadedAtDesc(Integer locationId);

    // Find photos pending approval by type
    List<Photo> findByPhotoTypeAndApprovedFalseOrderByUploadedAtAsc(PhotoType photoType);
}