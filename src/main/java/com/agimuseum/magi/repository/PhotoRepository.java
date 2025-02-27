package com.agimuseum.magi.repository;

import java.util.List;

import com.agimuseum.magi.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PhotoRepository extends JpaRepository<Photo, Integer> {
    List<Photo> findByLocationId(Integer locationId);
    List<Photo> findByStopId(Integer stopId);
}
