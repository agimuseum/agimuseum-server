package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.LocationDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface LocationDetailRepository extends JpaRepository<LocationDetail, Integer> {
    LocationDetail findByLocationId(Integer locationId);
}
