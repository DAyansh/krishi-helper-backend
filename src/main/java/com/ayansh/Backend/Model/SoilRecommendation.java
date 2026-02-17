package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
    @Table(name = "soil_recommendations")
    public class SoilRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long soilRecId ;

    private long soilTestId ;

    private String recommendationJson ;

    private Double confidence ;

    private LocalDateTime createdAt ;
}
