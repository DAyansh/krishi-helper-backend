package com.ayansh.Backend.PayLoad;

import lombok.Data;
import java.util.List;

@Data
public class SoilResponseDTO {

    private Long soilTestId;

    // Core outputs
    private List<CropResult> topCrops;                          // top 3
    private List<FertilizerRecommendation> fertilizerPlan;
    private double waterRequirementMmPerSeason;
    private double confidence;                                  // 0–1
    private String confidenceLabel;                             // "High/Medium/Low"

    // Human-readable summary
    private String adviceSummary;                               // one paragraph

    // Echo back computed intermediates (useful for UI)
    private String soilHealthLabel;                             // "Acidic / Neutral / Alkaline"
    private String organicCarbonStatus;                         // if OC provided
}