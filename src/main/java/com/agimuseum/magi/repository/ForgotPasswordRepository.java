package com.agimuseum.magi.repository;

import com.agimuseum.magi.model.ForgotPassword;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForgotPasswordRepository extends JpaRepository<ForgotPassword, Integer> {
}
