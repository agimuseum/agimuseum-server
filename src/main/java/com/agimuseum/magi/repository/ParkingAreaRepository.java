package com.agimuseum.magi.repository;

import java.util.List;

import com.agimuseum.magi.model.ParkingArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ParkingAreaRepository extends JpaRepository<ParkingArea, Integer> {
    List<ParkingArea> findByLocationId(Integer locationId);
}
