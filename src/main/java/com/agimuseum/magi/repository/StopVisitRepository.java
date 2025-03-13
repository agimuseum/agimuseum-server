package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.Stop;
import com.agimuseum.magi.model.StopVisit;
import com.agimuseum.magi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StopVisitRepository extends JpaRepository<StopVisit, Integer> {

    List<StopVisit> findByUser(User user);

    Optional<StopVisit> findByUserAndStop(User user, Stop stop);

    boolean existsByUserAndStop(User user, Stop stop);

    @Query("SELECT COUNT(sv) FROM StopVisit sv WHERE sv.user = ?1")
    long countVisitedStopsByUser(User user);

    @Query("SELECT s.id FROM Stop s WHERE s.id NOT IN (SELECT sv.stop.id FROM StopVisit sv WHERE sv.user = ?1)")
    List<Integer> findUnvisitedStopIdsByUser(User user);

    @Query("SELECT COUNT(s) FROM Stop s WHERE s.id NOT IN (SELECT sv.stop.id FROM StopVisit sv WHERE sv.user = ?1)")
    long countUnvisitedStopsByUser(User user);

    List<StopVisit> findByUserAndStop_Location_Id(User user, Integer locationId);

    @Query("SELECT COUNT(sv) FROM StopVisit sv WHERE sv.user = ?1 AND sv.stop.location.id = ?2")
    long countVisitedStopsByUserAndLocationId(User user, Integer locationId);
}