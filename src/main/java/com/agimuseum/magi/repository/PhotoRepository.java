package com.agimuseum.magi.repository;

import java.util.List;

import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    // Find photos by location
    List<Photo> findByLocationId(Integer locationId);

    // Find photos by stop
    List<Photo> findByStopId(Integer stopId);

    // Find photos by user and location
    List<Photo> findByUserAndLocationId(User user, Integer locationId);

    // Find photos by user and stop
    List<Photo> findByUserAndStopId(User user, Integer stopId);

    // Find all photos by user
    List<Photo> findByUser(User user);

    // Count photos by user and location (for enforcing the 3-photo limit)
    long countByUserAndLocationId(User user, Integer locationId);

    // Count photos by user and stop (for enforcing the 3-photo limit)
    long countByUserAndStopId(User user, Integer stopId);

    // Find the most recent photo taken by a user at a specific location
    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.locationId = ?2 ORDER BY p.uploadedAt DESC")
    Photo findTopByUserIdAndLocationIdOrderByUploadedAtDesc(Integer userId, Integer locationId);

    // Find the most recent photo taken by a user at a specific stop
    @Query("SELECT p FROM Photo p WHERE p.user.id = ?1 AND p.stopId = ?2 ORDER BY p.uploadedAt DESC")
    Photo findTopByUserIdAndStopIdOrderByUploadedAtDesc(Integer userId, Integer stopId);

    // Find all photos by location ID sorted by upload date (newest first)
    List<Photo> findByLocationIdOrderByUploadedAtDesc(Integer locationId);

    // Find all photos by stop ID sorted by upload date (newest first)
    List<Photo> findByStopIdOrderByUploadedAtDesc(Integer stopId);
}