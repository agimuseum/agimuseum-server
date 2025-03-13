package com.agimuseum.magi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "stop_visits", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "stop_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StopVisit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", nullable = false)
    private Stop stop;

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