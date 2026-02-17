package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name = "polygons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Polygon {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;


        @Column(name = "agro_polygon_id", nullable = false, unique = true)
        private String agroPolygonId;

        private String name;


        @Column(columnDefinition = "TEXT")
        private String geoJson;

        private Double centerLat;
        private Double centerLon;

        private LocalDateTime createdAt;
    }