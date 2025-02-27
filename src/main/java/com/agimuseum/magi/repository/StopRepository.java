package com.agimuseum.magi.repository;

import java.util.List;

import com.agimuseum.magi.model.Stop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface StopRepository extends JpaRepository<Stop, Integer> {
    List<Stop> findByLocationId(Integer locationId);
}
