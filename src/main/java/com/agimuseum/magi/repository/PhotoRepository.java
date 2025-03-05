package com.agimuseum.magi.repository;

import java.util.List;

import com.agimuseum.magi.model.Photo;
import com.agimuseum.magi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findByLocationId(Integer locationId);
    List<Photo> findByStopId(Integer stopId);
    List<Photo> findByUserAndLocationId(User user, Integer locationId);
    List<Photo> findByUserAndStopId(User user, Integer stopId);
    List<Photo> findByUser(User user);
    long countByUserAndLocationId(User user, Integer locationId);
    long countByUserAndStopId(User user, Integer stopId);
}