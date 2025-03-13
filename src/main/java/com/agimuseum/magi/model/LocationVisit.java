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

    @PrePersist
    protected void onCreate() {
        if (visitedAt == null) {
            visitedAt = new Date();
        }
    }
}