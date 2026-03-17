package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name="government_scheme")
@NoArgsConstructor
@AllArgsConstructor

public class GovernmentScheme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    private String schemeName ;
    private String description ;
    private String state ;

    @Column(length = 1000)
    private String cropTypes ;
    private Double minLandSize ;
    private Double maxLandSize ;

    @Enumerated(EnumType.STRING)
    private FarmerCategory farmerCategory ;

    @Enumerated(EnumType.STRING)
    private LandType landType ;

    private String eligibilityText ;

    private String requiredDocuments ;

    private String applicationLink ;

    private Boolean isActive = true ;

    private String sourceUrl ;
    private LocalDateTime scrapedAt ;
    private Integer scrapeConfidence ;

    private Boolean lockedByAdmin = true;


}
