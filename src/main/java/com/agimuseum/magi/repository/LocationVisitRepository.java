package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.Location;
import com.agimuseum.magi.model.LocationVisit;
import com.agimuseum.magi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationVisitRepository extends JpaRepository<LocationVisit, Integer> {

    List<LocationVisit> findByUser(User user);

    Optional<LocationVisit> findByUserAndLocation(User user, Location location);

    @Query("SELECT lv FROM LocationVisit lv WHERE lv.user.id = ?1 AND lv.location.id = ?2")
    Optional<LocationVisit> findByUserIdAndLocationId(Integer userId, Integer locationId);

    boolean existsByUserAndLocation(User user, Location location);

    @Query("SELECT COUNT(lv) FROM LocationVisit lv WHERE lv.user = ?1")
    long countVisitedLocationsByUser(User user);

    @Query("SELECT COUNT(lv) FROM LocationVisit lv WHERE lv.user = ?1 AND lv.hasPhotoProof = true")
    long countVisitedLocationsWithPhotoByUser(User user);

    @Query("SELECT l.id FROM Location l WHERE l.id NOT IN (SELECT lv.location.id FROM LocationVisit lv WHERE lv.user = ?1)")
    List<Integer> findUnvisitedLocationIdsByUser(User user);

    @Query("SELECT COUNT(l) FROM Location l WHERE l.id NOT IN (SELECT lv.location.id FROM LocationVisit lv WHERE lv.user = ?1)")
    long countUnvisitedLocationsByUser(User user);

    @Query("SELECT lv FROM LocationVisit lv WHERE lv.user = ?1 AND lv.hasPhotoProof = true")
    List<LocationVisit> findVisitsWithPhotoByUser(User user);
}