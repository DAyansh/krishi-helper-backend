package com.ayansh.Backend.PayLoad;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CropResult {
    private String cropName;
    private double suitabilityScore;   // 0.0 – 1.0
    private String suitabilityLabel;   // "Excellent / Good / Fair"
}