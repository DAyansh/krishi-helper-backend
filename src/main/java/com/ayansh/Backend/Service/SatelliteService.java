package com.ayansh.Backend.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SatelliteService {

    private final WebClient agroClient;
    private final String apiKey;
    private final ObjectMapper mapper = new ObjectMapper();

    public SatelliteService(WebClient.Builder builder, @Value("${agro.api.url}") String baseUrl,
                            @Value("${agro.api.key}") String apiKey) {
        this.agroClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    // Step 1: search
    public JsonNode searchImages(String polygonId, long fromTs, long toTs) {
        return agroClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/agro/1.0/image/search")
                        .queryParam("polyid", polygonId)
                        .queryParam("start", fromTs)
                        .queryParam("end", toTs)
                        .queryParam("appid", apiKey).build())
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }

    // Step 2: get stats (zonal statistics) or image tile
    public JsonNode getImageStats(String statsUrl) {
        // statsUrl might be absolute; append appid if missing
        String separator = statsUrl.contains("?") ? "&" : "?";
        return agroClient.get().uri(statsUrl + separator + "appid=" + apiKey)
                .retrieve().bodyToMono(JsonNode.class).block();
    }
}
