package com.agimuseum.magi.model;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "location_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String address;
    private Double latitude;
    private Double longitude;
    @Column(name = "geo_fence_radius")
    private Integer geoFenceRadius;
}
