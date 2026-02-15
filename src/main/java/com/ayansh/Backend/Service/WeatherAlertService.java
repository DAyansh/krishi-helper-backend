package com.ayansh.Backend.Service;

import com.ayansh.Backend.PayLoad.WeatherResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class WeatherAlertService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherResponseDTO parseAndGenerate(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);


            JsonNode current = root.path("current");
            double temperature = current.path("temp").asDouble(0.0);  // Default to 0.0 if missing
            double humidity = current.path("humidity").asDouble(0.0);

            JsonNode daily = root.path("daily");
            double rainProbability = 0.0;
            if (daily.isArray() && daily.size() > 0) {
                rainProbability = daily.get(0).path("pop").asDouble(0.0) * 100;  // Convert to percentage
            }


            JsonNode alertsNode = root.path("alerts");
            String alerts = null;
            if (alertsNode.isArray() && alertsNode.size() > 0) {

                StringBuilder alertBuilder = new StringBuilder();
                for (JsonNode alert : alertsNode) {
                    alertBuilder.append(alert.path("description").asText()).append("; ");
                }
                alerts = alertBuilder.toString().trim();
            }


            WeatherResponseDTO response = new WeatherResponseDTO();
            response.setTemperature(temperature);
            response.setHumidity(humidity);
            response.setRainProbability(rainProbability);
            response.setAlerts(alerts);
            return response;

        } catch (Exception e) {
            // Log error and return default
            System.err.println("Error parsing weather JSON: " + e.getMessage());
            return new WeatherResponseDTO(0.0, 0.0, 0.0, null);
        }
    }
}