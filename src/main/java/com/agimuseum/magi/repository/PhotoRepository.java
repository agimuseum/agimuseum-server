package com.agimuseum.magi.repository;

import java.util.List;

import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
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
}