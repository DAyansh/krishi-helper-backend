package com.ayansh.Backend.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class WeatherService {

    private final WebClient webClient;

    @Value("${openweather.api.key}")
    private String apiKey;

    public WeatherService(WebClient.Builder builder,
                          @Value("${openweather.api.url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Cacheable(value = "weather", key = "#lat + '-' + #lon")
    public String fetchWeatherRaw(double lat, double lon) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/data/2.5/onecall")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("exclude", "minutely")
                            .queryParam("units", "metric")
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 401) {
                return "{\"error\": \"Invalid or unactivated OpenWeatherMap API key.\"}";
            }
            throw new RuntimeException("Weather API error: " + e.getStatusCode() + " - " + e.getMessage(), e);
        }
    }
}