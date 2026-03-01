package com.ayansh.Backend.Repository;

import com.ayansh.Backend.Model.SoilRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoilRecommendationRepo extends JpaRepository<SoilRecommendation, Long> {
}
