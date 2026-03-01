package com.ayansh.Backend.Service;

import com.ayansh.Backend.Model.SoilRecommendation;
import com.ayansh.Backend.Model.SoilTest;
import com.ayansh.Backend.PayLoad.*;
import com.ayansh.Backend.Repository.SoilRecommendationRepo;
import com.ayansh.Backend.Repository.SoilTestRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SoilTestService {

    private final SoilTestRepo          soilRepo;
    private final SoilRecommendationRepo recRepo;
    private final CropRuleEngine         ruleEngine;
    private final AdviceTemplateService  adviceService;
    private final ObjectMapper           objectMapper;        // Spring Boot auto-configures this

    // ── Analyse ──────────────────────────────────────────────────────────────

    @Transactional
    public SoilResponseDTO analyzeSoil(SoilRequestDTO dto) {

        validate(dto);

        // 1. Persist raw test
        SoilTest saved = soilRepo.save(toEntity(dto));

        // 2. Rank crops
        List<CropResult> topCrops = ruleEngine.rankCrops(
                dto.getN(), dto.getP(), dto.getK(),
                dto.getPh(), nullSafe(dto.getMoisture()), 3);

        // 3. Build fertilizer plan (scaled to area)
        List<FertilizerRecommendation> plan = ruleEngine.buildFertilizerPlan(
                dto.getN(), dto.getP(), dto.getK(), dto.getPh(), dto.getAreaHectares());

        // 4. Water estimate — use best crop's profile
        String bestCrop = topCrops.isEmpty() ? "" : topCrops.get(0).getCropName();
        double waterMm  = ruleEngine.waterMmPerSeason(bestCrop);

        // 5. Confidence — based on how many fields were provided
        double confidence = calculateConfidence(dto);

        // 6. Assemble DTO
        SoilResponseDTO resp = new SoilResponseDTO();
        resp.setSoilTestId(saved.getSoilTestId());
        resp.setTopCrops(topCrops);
        resp.setFertilizerPlan(plan);
        resp.setWaterRequirementMmPerSeason(waterMm);
        resp.setConfidence(confidence);
        resp.setConfidenceLabel(confidence >= 0.85 ? "High" : confidence >= 0.70 ? "Medium" : "Low");
        resp.setSoilHealthLabel(adviceService.soilHealthLabel(dto.getPh()));
        resp.setAdviceSummary(adviceService.buildSummary(resp, dto.getLanguage()));

        // 7. Persist recommendation (JSON snapshot)
        persistRecommendation(saved.getSoilTestId(), resp, confidence);

        return resp;
    }

    // ── History ───────────────────────────────────────────────────────────────

    public List<SoilTest> getHistory(Long userId) {
        return soilRepo.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void validate(SoilRequestDTO dto) {
        if (dto.getN() < 0)   throw new IllegalArgumentException("N must be ≥ 0");
        if (dto.getP() < 0)   throw new IllegalArgumentException("P must be ≥ 0");
        if (dto.getK() < 0)   throw new IllegalArgumentException("K must be ≥ 0");
        if (dto.getPh() < 0 || dto.getPh() > 14)
            throw new IllegalArgumentException("pH must be between 0 and 14");
        if (dto.getAreaHectares() == null || dto.getAreaHectares() <= 0)
            throw new IllegalArgumentException("Area must be a positive value");
    }

    private SoilTest toEntity(SoilRequestDTO dto) {
        SoilTest t = new SoilTest();
        t.setUserId(dto.getUserId());
        t.setN(dto.getN());
        t.setP(dto.getP());
        t.setK(dto.getK());
        t.setPh(dto.getPh());
        t.setMoisture(dto.getMoisture());
        t.setEc(dto.getEc());
        t.setAreaHectares(dto.getAreaHectares());
        t.setLocation(dto.getLocation());
        t.setCropHistory(dto.getCropHistory());
        t.setDesiredCrop(dto.getDesiredCrop());
        t.setCreatedAt(LocalDateTime.now());
        return t;
    }

    private double calculateConfidence(SoilRequestDTO dto) {
        // Start at 0.60 (minimum 3 core fields always present)
        // Each extra quality signal adds weight
        double score = 0.60;
        if (dto.getMoisture() != null)    score += 0.10;
        if (dto.getEc()       != null)    score += 0.05;
        if (dto.getLocation() != null && !dto.getLocation().isBlank()) score += 0.10;
        if (dto.getCropHistory() != null) score += 0.10;
        if (dto.getDesiredCrop() != null) score += 0.05;
        return Math.min(score, 0.95);
    }

    private void persistRecommendation(Long soilTestId, SoilResponseDTO resp, double confidence) {
        try {
            SoilRecommendation rec = new SoilRecommendation();
            rec.setSoilTestId(soilTestId);
            rec.setRecommendationJson(objectMapper.writeValueAsString(resp));
            rec.setConfidence(confidence);
            rec.setCreatedAt(LocalDateTime.now());
            recRepo.save(rec);
        } catch (Exception e) {
            log.error("Failed to persist soil recommendation for testId={}", soilTestId, e);
        }
    }

    private double nullSafe(Double val) {
        return val == null ? 0.0 : val;
    }
}