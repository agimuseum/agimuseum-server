package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.Reward;
import com.agimuseum.magi.model.User;
import com.agimuseum.magi.model.UserReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRewardRepository extends JpaRepository<UserReward, Integer> {

    // Find all rewards claimed by a user
    List<UserReward> findByUser(User user);

    // Check if a user has already claimed a specific reward
    boolean existsByUserAndReward(User user, Reward reward);

    // Find a specific user-reward combination
    Optional<UserReward> findByUserAndReward(User user, Reward reward);

    // Find all claimed but unredeemed rewards for a user
    @Query("SELECT ur FROM UserReward ur WHERE ur.user = ?1 AND ur.redeemed = false")
    List<UserReward> findUnredeemedRewardsByUser(User user);

    // Find all redeemed rewards for a user
    @Query("SELECT ur FROM UserReward ur WHERE ur.user = ?1 AND ur.redeemed = true")
    List<UserReward> findRedeemedRewardsByUser(User user);
}