package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "soil_recommendations")
public class SoilRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long soilRecId;

    private Long   soilTestId;

    @Column(columnDefinition = "TEXT")    // store full JSON snapshot
    private String recommendationJson;

    private Double confidence;
    private LocalDateTime createdAt;
}