package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.ForgotPassword;
import com.agimuseum.magi.model.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {
    @Modifying
    @Transactional
    @Query("DELETE FROM ForgotPassword fp WHERE fp.user = ?1")
    void deleteByUser(User user);


    @Query("select fp from ForgotPassword fp where fp.otp = ?1 and fp.user = ?2")
    Optional<ForgotPassword> findByOtpAndUser(Integer otp, User user);

    @Query("select fp from ForgotPassword fp where fp.user = ?1")
    Optional<ForgotPassword> findByUser(User user);
}
