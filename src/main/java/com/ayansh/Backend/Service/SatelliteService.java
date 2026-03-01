package com.ayansh.Backend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class SatelliteService {

    private final WebClient agroClient;
    private final WebClient plainClient;
    private final String apiKey;

    public SatelliteService(WebClient.Builder builder,
                            @Value("${agro.api.url}") String baseUrl,
                            @Value("${agro.api.key}") String apiKey) {
        this.agroClient  = builder.baseUrl(baseUrl).build();
        this.plainClient = WebClient.create();
        this.apiKey = apiKey;
    }

    public JsonNode searchImages(String polygonId, long fromTs, long toTs) {
        try {
            return agroClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/agro/1.0/image/search")
                            .queryParam("polyid", polygonId)
                            .queryParam("start", fromTs)
                            .queryParam("end", toTs)
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Satellite search error: " + e.getStatusCode() + " - " + e.getMessage(), e);
        }
    }

    public JsonNode getImageStats(String statsUrl) {
        try {
            String separator = statsUrl.contains("?") ? "&" : "?";
            String fullUrl = statsUrl + separator + "appid=" + apiKey;

            return plainClient.get()
                    .uri(fullUrl)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Satellite stats error: " + e.getStatusCode() + " - " + e.getMessage(), e);
        }
    }
}