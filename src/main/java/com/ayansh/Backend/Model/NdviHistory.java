package com.ayansh.Backend.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
    @Table(name = "ndvi_history")
    @Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
    public class NdviHistory {
        @Id
        @GeneratedValue
        private Long id;
        private String agroPolygonId;
        private Instant date;
        private Double ndvi;
}
