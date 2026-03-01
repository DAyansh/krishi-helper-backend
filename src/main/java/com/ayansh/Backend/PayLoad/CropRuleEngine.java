package com.ayansh.Backend.PayLoad;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Rule-based agronomy engine.
 * Each crop has: ideal pH range, N/P/K minimums, water need.
 * Scoring: 1 point per satisfied condition → normalised to 0–1.
 */
@Component
public class CropRuleEngine {

    // ── Crop profiles (expandable to DB/YAML later) ──────────────────────────
    private record CropProfile(
            String name,
            double phMin, double phMax,
            double nMin,  double pMin, double kMin,
            double moistureMin,              // % soil moisture
            double waterMmPerSeason          // mm/season estimate
    ) {}

    private static final List<CropProfile> PROFILES = List.of(
            new CropProfile("Rice",       5.5, 7.0, 280, 20, 110, 60, 1200),
            new CropProfile("Wheat",      6.0, 7.5, 260, 15, 100, 30,  450),
            new CropProfile("Maize",      5.8, 7.0, 250, 18,  90, 35,  500),
            new CropProfile("Soybean",    6.0, 7.0, 180, 20, 100, 40,  450),
            new CropProfile("Cotton",     6.0, 8.0, 240, 15,  80, 25,  700),
            new CropProfile("Sugarcane",  6.0, 7.5, 300, 25, 120, 55, 1800),
            new CropProfile("Groundnut",  5.5, 7.0, 200, 20,  80, 30,  500),
            new CropProfile("Chickpea",   6.0, 9.0, 160, 15,  80, 20,  300),
            new CropProfile("Mustard",    6.0, 7.5, 220, 15,  80, 20,  300),
            new CropProfile("Potato",     5.0, 6.5, 250, 25, 150, 45,  500)
    );

    // ── Public API ────────────────────────────────────────────────────────────

    /** Returns crops sorted by suitability, top N. */
    public List<CropResult> rankCrops(double n, double p, double k,
                                      double ph, double moisture, int topN) {
        return PROFILES.stream()
                .map(cp -> new CropResult(
                        cp.name(),
                        score(cp, n, p, k, ph, moisture),
                        label(score(cp, n, p, k, ph, moisture))
                ))
                .sorted(Comparator.comparingDouble(CropResult::getSuitabilityScore).reversed())
                .limit(topN)
                .toList();
    }

    /** Water requirement for given crop (or best crop). */
    public double waterMmPerSeason(String cropName) {
        return PROFILES.stream()
                .filter(cp -> cp.name().equalsIgnoreCase(cropName))
                .mapToDouble(CropProfile::waterMmPerSeason)
                .findFirst()
                .orElse(600);   // generic default
    }

    /** Build fertilizer plan based on deficiencies, scaled to area. */
    public List<FertilizerRecommendation> buildFertilizerPlan(
            double n, double p, double k, double ph, double areaHa) {

        List<FertilizerRecommendation> plan = new ArrayList<>();

        // pH correction first — affects nutrient availability
        if (ph < 5.5) {
            double dose = 400;   // kg/ha lime
            plan.add(new FertilizerRecommendation(
                    "pH Correction", "Agricultural Lime (CaCO₃)",
                    dose, dose * areaHa, "kg",
                    "Apply 4–6 weeks before sowing. Incorporate into topsoil by ploughing."));
        } else if (ph > 8.5) {
            double dose = 250;
            plan.add(new FertilizerRecommendation(
                    "pH Correction", "Gypsum (CaSO₄)",
                    dose, dose * areaHa, "kg",
                    "Broadcast and irrigate immediately."));
        }

        // Nitrogen
        if (n < 140) {
            double dose = 120;   // kg urea/ha
            plan.add(new FertilizerRecommendation(
                    "Nitrogen", "Urea (46% N)",
                    dose, dose * areaHa, "kg",
                    "Split into 3 doses: basal 40%, tillering 30%, panicle 30%."));
        } else if (n < 280) {
            double dose = 65;
            plan.add(new FertilizerRecommendation(
                    "Nitrogen", "Urea (46% N)",
                    dose, dose * areaHa, "kg",
                    "Apply half at sowing, half at 30 DAS."));
        }

        // Phosphorus
        if (p < 10) {
            double dose = 100;   // kg DAP/ha
            plan.add(new FertilizerRecommendation(
                    "Phosphorus", "DAP (18% N, 46% P₂O₅)",
                    dose, dose * areaHa, "kg",
                    "Apply full dose as basal before sowing."));
        } else if (p < 20) {
            double dose = 50;
            plan.add(new FertilizerRecommendation(
                    "Phosphorus", "SSP (16% P₂O₅)",
                    dose, dose * areaHa, "kg",
                    "Incorporate into soil at time of field preparation."));
        }

        // Potassium
        if (k < 110) {
            double dose = 80;    // kg MOP/ha
            plan.add(new FertilizerRecommendation(
                    "Potassium", "MOP / Muriate of Potash (60% K₂O)",
                    dose, dose * areaHa, "kg",
                    "Apply full dose basally or split 50:50 at sowing and flowering."));
        }

        if (plan.isEmpty()) {
            plan.add(new FertilizerRecommendation(
                    "Maintenance", "Balanced NPK 10:26:26",
                    100, 100 * areaHa, "kg",
                    "Soil is in good condition. Apply maintenance dose at sowing."));
        }

        return plan;
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private double score(CropProfile cp, double n, double p, double k,
                         double ph, double moisture) {
        int met = 0, total = 5;
        if (ph >= cp.phMin() && ph <= cp.phMax()) met++;
        if (n  >= cp.nMin())   met++;
        if (p  >= cp.pMin())   met++;
        if (k  >= cp.kMin())   met++;
        if (moisture != 0 && moisture >= cp.moistureMin()) met++;
        else if (moisture == 0) { total = 4; }   // moisture not provided
        return Math.round((double) met / total * 100.0) / 100.0;
    }

    private String label(double score) {
        if (score >= 0.80) return "Excellent";
        if (score >= 0.60) return "Good";
        if (score >= 0.40) return "Fair";
        return "Not Recommended";
    }
}