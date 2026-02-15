package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
    @Table(name="weather_snapshots")
    @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public class WeatherSnapshot {
        @Id
        @GeneratedValue
        private Long id;
        private String agroPolygonId;
        private Instant fetchedAt;
        @Column(columnDefinition = "TEXT")
        private String payload;
    }
