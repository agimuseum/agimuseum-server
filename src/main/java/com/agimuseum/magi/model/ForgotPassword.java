package com.agimuseum.magi.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
@Table(name = "forgot_password")
public class ForgotPassword {

    @Id()
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer otp;

    @Column(nullable = true)
    private Date expirationTime;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

}
