package com.agimuseum.magi.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stops")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stop {
    @Id
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "location_id")
    private Location location;

    private String name;

    @Column(columnDefinition = "LONGTEXT")
    private String summary;

    private String weblink;

    private Double latitude;
    private Double longitude;
    @Column(name = "geo_fence_radius")
    private Integer geoFenceRadius;

    @OneToMany(mappedBy = "stop", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Photo> photos = new ArrayList<>();
}