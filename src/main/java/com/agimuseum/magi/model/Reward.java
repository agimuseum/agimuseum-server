package com.agimuseum.magi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "rewards")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String description;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private Date expirationDate;

    @Column(nullable = false)
    private Integer requiredLocations;

    @Column(nullable = false)
    private Boolean requiresPhotoVerification;

    // Track if reward is active
    @Column(nullable = false)
    private Boolean active;
}