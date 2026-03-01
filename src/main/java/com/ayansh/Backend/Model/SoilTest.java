package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "soil_tests")
public class SoilTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long soilTestId;

    private Long   userId;
    private Double n;
    private Double p;
    private Double k;
    private Double ph;
    private Double ec;               // NEW
    private Double moisture;
    private Double areaHectares;
    private String location;
    private String cropHistory;      // NEW  e.g. "rice,wheat"
    private String desiredCrop;      // NEW
    private LocalDateTime createdAt;
}