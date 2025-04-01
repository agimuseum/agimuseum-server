package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, Integer> {

    // Find all active rewards that haven't expired
    @Query("SELECT r FROM Reward r WHERE r.active = true AND r.expirationDate > ?1")
    List<Reward> findAllActiveRewards(Date currentDate);

    // Find a reward by its unique code
    Optional<Reward> findByCode(String code);

    // Find rewards that match the criteria (for location visits)
    @Query("SELECT r FROM Reward r WHERE r.active = true AND r.expirationDate > ?1 AND r.requiredLocations <= ?2 " +
            "AND (r.requiresPhotoVerification = false OR (r.requiresPhotoVerification = true AND ?3 >= r.requiredLocations))")
    List<Reward> findEligibleRewards(Date currentDate, int totalVisitedLocations, int totalVisitedLocationsWithPhoto);
}