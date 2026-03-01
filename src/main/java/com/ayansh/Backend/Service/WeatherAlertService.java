package com.ayansh.Backend.Service;

import com.ayansh.Backend.PayLoad.WeatherResponseDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class WeatherAlertService {

    private static final Logger log = LoggerFactory.getLogger(WeatherAlertService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WeatherResponseDTO parseAndGenerate(String rawJson) {
        try {
            JsonNode root = objectMapper.readTree(rawJson);

            if (root.has("error")) {
                log.warn("Weather API returned error: {}", root.path("error").asText());
                return new WeatherResponseDTO(0.0, 0.0, 0.0, root.path("error").asText());
            }

            JsonNode current = root.path("current");
            double temperature    = current.path("temp").asDouble(0.0);
            double humidity       = current.path("humidity").asDouble(0.0);

            double rainProbability = 0.0;
            JsonNode daily = root.path("daily");
            if (daily.isArray() && !daily.isEmpty()) {
                rainProbability = daily.get(0).path("pop").asDouble(0.0) * 100.0;
            }

            String alerts = null;
            JsonNode alertsNode = root.path("alerts");
            if (alertsNode.isArray() && !alertsNode.isEmpty()) {
                StringBuilder sb = new StringBuilder();
                for (JsonNode alert : alertsNode) {
                    sb.append(alert.path("event").asText("Unknown"))
                            .append(": ")
                            .append(alert.path("description").asText())
                            .append("; ");
                }
                alerts = sb.toString().trim();
            }

            return new WeatherResponseDTO(temperature, humidity, rainProbability, alerts);

        } catch (Exception e) {
            log.error("Error parsing weather JSON", e);
            return new WeatherResponseDTO(0.0, 0.0, 0.0, null);
        }
    }
}