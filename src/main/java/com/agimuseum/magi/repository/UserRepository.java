package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByUsername(String username);


    @Transactional
    @Modifying
    @Query("UPDATE User u SET u.password = ?2 WHERE u.username = ?1")
    void updatePassword(String username, String password);
}
