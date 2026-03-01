package com.ayansh.Backend.PayLoad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FertilizerRecommendation {
    private String nutrient;           // "Nitrogen", "Phosphorus", etc.
    private String fertilizerName;     // "Urea", "DAP", "MOP", "Lime"
    private double dosagePerHectare;   // kg/ha
    private double totalDosage;        // dosagePerHectare × area
    private String unit;               // "kg"
    private String applicationTip;
}