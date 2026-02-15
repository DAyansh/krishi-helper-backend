package com.ayansh.Backend.Model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "soil_tests")
public class SoilTest {

        @Id
        @GeneratedValue
        private Long soilTestId;

        private Long userId;

        private Double n;

        private Double p;

        private Double k;

        private Double ph;

        private Double moisture;

        private Double areaHectares;

        private String location; // as village or latlon

        private LocalDateTime createdAt;
    }

