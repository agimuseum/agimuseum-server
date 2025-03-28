package com.agimuseum.magi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "location_visits", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "location_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "visited_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date visitedAt;

    @Column(name = "has_photo_proof")
    private Boolean hasPhotoProof;

    @Column(name = "photo_id")
    private Integer photoId;

    @Column(name = "visit_method")
    @Enumerated(EnumType.STRING)
    private VisitMethod visitMethod;

    @PrePersist
    protected void onCreate() {
        if (visitedAt == null) {
            visitedAt = new Date();
        }

        if (visitMethod == null) {
            visitMethod = VisitMethod.MANUAL;
        }
    }

    // Visit method enum to track how the visit was registered
    public enum VisitMethod {
        MANUAL,         // Manually marked as visited through API
        PHOTO_UPLOAD,   // Visited by uploading a photo
        GPS_LOCATION,   // Visited by being physically at the location
        ADMIN_OVERRIDE  // Visit registered by an admin
    }
}