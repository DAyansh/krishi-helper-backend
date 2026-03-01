package com.ayansh.Backend.Service;

import com.ayansh.Backend.PayLoad.SoilResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AdviceTemplateService {


    public String buildSummary(SoilResponseDTO resp, String lang) {
        String topCrop  = resp.getTopCrops().isEmpty() ? "a suitable crop"
                : resp.getTopCrops().get(0).getCropName();
        String health   = resp.getSoilHealthLabel();
        double water    = resp.getWaterRequirementMmPerSeason();
        int    fertCount = resp.getFertilizerPlan().size();

        String en = String.format(
                "Your soil is currently %s. Based on the nutrient levels and pH provided, " +
                        "%s appears to be the most suitable crop for your field. " +
                        "%d fertilizer amendment(s) are recommended this season. " +
                        "Estimated water requirement is %.0f mm/season. " +
                        "Follow the fertilizer schedule carefully to optimise yield.",
                health, topCrop, fertCount, water);

        return switch (lang == null ? "en" : lang.toLowerCase()) {
            case "hi" -> buildHindi(health, topCrop, fertCount, water);
            // add "mr", "te", "ta" etc. as needed
            default   -> en;
        };
    }

    private String buildHindi(String health, String topCrop, int fertCount, double water) {
        return String.format(
                "आपकी मिट्टी वर्तमान में %s है। पोषक तत्वों और pH के आधार पर, " +
                        "%s आपके खेत के लिए सबसे उपयुक्त फसल प्रतीत होती है। " +
                        "इस सीजन में %d उर्वरक सुधार की सिफारिश की जाती है। " +
                        "अनुमानित जल आवश्यकता %.0f mm/सीजन है।",
                health, topCrop, fertCount, water);
    }

    public String soilHealthLabel(double ph) {
        if (ph < 5.5) return "Strongly Acidic";
        if (ph < 6.5) return "Mildly Acidic";
        if (ph <= 7.5) return "Neutral";
        if (ph <= 8.5) return "Mildly Alkaline";
        return "Strongly Alkaline";
    }
}