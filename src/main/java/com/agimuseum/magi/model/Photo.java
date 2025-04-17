package com.agimuseum.magi.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
@Table(name = "photos")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "contentType", nullable = false)
    private String contentTypeField;

    @Column(nullable = false, length = 1024)
    private String url;

    @Column(name = "photo_url", nullable = false, length = 1024)
    private String photoUrl;

    // S3-specific fields
    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "s3_bucket")
    private String s3Bucket;

    @Column(name = "reference_id", nullable = false)
    private String referenceId;

    @Column(name = "location_id")
    private Integer locationId;

    @Column(name = "location_name")
    private String locationName;

    @Column(name = "stop_id")
    private Integer stopId;

    @Column(name = "stop_name")
    private String stopName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", insertable = false, updatable = false)
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stop_id", insertable = false, updatable = false)
    private Stop stop;

    @Column(name = "uploaded_at", nullable = false)
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date uploadedAt;

    /**
     * Type of photo (stock, visit proof, etc.)
     */
    @Column(name = "photo_type")
    @Enumerated(EnumType.STRING)
    private PhotoType photoType;

    /**
     * Description of the photo (optional)
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Whether the photo is approved for display
     * All photos are approved by default
     */
    @Column(name = "approved")
    private Boolean approved;

    @PrePersist
    protected void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = new Date();
        }

        // Set default photo type if not specified
        if (photoType == null) {
            photoType = PhotoType.STOCK;
        }

        // All photos are approved by default
        if (approved == null) {
            approved = true;
        }
    }
}