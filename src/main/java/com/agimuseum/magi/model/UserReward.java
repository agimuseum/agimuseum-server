package com.agimuseum.magi.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "user_rewards", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "reward_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReward {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reward_id", nullable = false)
    private Reward reward;

    @Column(name = "claimed_at", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date claimedAt;

    @Column(name = "redeemed")
    private Boolean redeemed;

    @Column(name = "redeemed_at")
    @Temporal(TemporalType.TIMESTAMP)
    private Date redeemedAt;

    @PrePersist
    protected void onCreate() {
        if (claimedAt == null) {
            claimedAt = new Date();
        }
        if (redeemed == null) {
            redeemed = false;
        }
    }
}