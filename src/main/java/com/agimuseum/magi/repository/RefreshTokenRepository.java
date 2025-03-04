package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.RefreshToken;
import com.agimuseum.magi.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Transactional
    void deleteByUser(User user);
    @Transactional
    void deleteByExpiryDateBefore(Date date);
}