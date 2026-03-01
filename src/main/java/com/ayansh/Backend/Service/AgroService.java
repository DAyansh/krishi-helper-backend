package com.ayansh.Backend.Service;

import com.ayansh.Backend.Model.Polygon;
import com.ayansh.Backend.PayLoad.PolygonRequestDTO;
import com.ayansh.Backend.Repository.PolygonRepo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AgroService {

    @Value("${agro.api.key}")
    private String apiKey;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final PolygonRepo polygonRepository;

    @Autowired
    public AgroService(WebClient.Builder builder,
                       @Value("${agro.api.url}") String baseUrl,
                       ObjectMapper objectMapper,
                       PolygonRepo polygonRepository) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
        this.objectMapper = objectMapper;
        this.polygonRepository = polygonRepository;
    }

    @Transactional
    public Polygon createPolygon(PolygonRequestDTO req) throws Exception {
        if (req == null) throw new IllegalArgumentException("Request cannot be null");
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new IllegalArgumentException("name is required");

        // FIX: correct null/empty check on JsonNode
        List<List<Double>> ring;
        if (req.getGeo_json() != null && !req.getGeo_json().isNull() && !req.getGeo_json().isEmpty()) {
            ring = extractFirstRingFromGeoJson(req.getGeo_json());
        } else if (req.getCoordinates() != null && !req.getCoordinates().isEmpty()) {
            ring = deepCopyCoordinates(req.getCoordinates());
        } else {
            throw new IllegalArgumentException("Either geo_json or coordinates must be provided");
        }

        if (ring.size() < 3) {
            throw new IllegalArgumentException("At least 3 unique points required");
        }

        normalizeRingPoints(ring);

        // Ensure ring is closed (first == last)
        if (!pointsEqual(ring.get(0), ring.get(ring.size() - 1))) {
            ring.add(new ArrayList<>(ring.get(0)));
        }

        // Ensure [lon, lat] order for Agro API
        List<List<Double>> lonLatRing = ensureLonLatOrder(ring);

        // Build GeoJSON for Agro API
        ObjectNode geometry = objectMapper.createObjectNode();
        geometry.put("type", "Polygon");

        ArrayNode coordinatesOuter = objectMapper.createArrayNode();
        ArrayNode ringNode = objectMapper.createArrayNode();
        for (List<Double> pt : lonLatRing) {
            ArrayNode pointNode = objectMapper.createArrayNode();
            pointNode.add(pt.get(0)); // lon
            pointNode.add(pt.get(1)); // lat
            ringNode.add(pointNode);
        }
        coordinatesOuter.add(ringNode);
        geometry.set("coordinates", coordinatesOuter);

        ObjectNode geoJson = objectMapper.createObjectNode();
        geoJson.put("type", "Feature");
        geoJson.set("properties", objectMapper.createObjectNode());
        geoJson.set("geometry", geometry);

        ObjectNode root = objectMapper.createObjectNode();
        root.put("name", req.getName());
        root.set("geo_json", geoJson);

        String body = objectMapper.writeValueAsString(root);

        // Call AgroMonitoring Polygons API
        String agroResponseStr;
        try {
            agroResponseStr = webClient.post()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/polygons")
                            .queryParam("appid", apiKey).build())
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            String respBody = e.getResponseBodyAsString();
            throw new RuntimeException("Agro API call failed [" + e.getStatusCode() + "]: " + respBody, e);
        }

        JsonNode agroResponse = objectMapper.readTree(agroResponseStr);
        if (agroResponse == null || !agroResponse.has("id")) {
            throw new RuntimeException("Agro API did not return polygon id: " + agroResponseStr);
        }
        String agroId = agroResponse.get("id").asText();

        // Compute centroid (exclude closing duplicate point)
        double sumLat = 0.0, sumLon = 0.0;
        int count = lonLatRing.size() - 1; // exclude closing point
        for (int i = 0; i < count; i++) {
            sumLon += lonLatRing.get(i).get(0);
            sumLat += lonLatRing.get(i).get(1);
        }
        double centerLat = sumLat / count;
        double centerLon = sumLon / count;

        Polygon entity = Polygon.builder()
                .agroPolygonId(agroId)
                .name(req.getName())
                .geoJson(geoJson.toString())
                .centerLat(centerLat)
                .centerLon(centerLon)
                .createdAt(LocalDateTime.now())
                .build();

        polygonRepository.save(entity);
        return entity;
    }

    // ---- NDVI History (correct endpoint per docs) ----
    // GET /agro/1.0/ndvi/history?polyid=...&start=...&end=...&appid=...
    public String getNdviHistory(String polyId, long startTs, long endTs) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/ndvi/history")
                            .queryParam("polyid", polyId)
                            .queryParam("start", startTs)
                            .queryParam("end", endTs)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"NDVI data not found for polygon " + polyId + "\"}";
            }
            throw new RuntimeException("Agro API error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        }
    }

    // ---- Satellite Image Search ----
    // GET /agro/1.0/image/search?polyid=...&start=...&end=...&appid=...
    public String searchImages(String polyId, long start, long end) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/image/search")
                            .queryParam("polyid", polyId)
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"No satellite images found for polygon " + polyId + " in the given date range.\"}";
            }
            throw new RuntimeException("Agro API error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        }
    }

    // ---- Current Soil Data ----
    // GET /agro/1.0/soil?polyid=...&appid=...
    public String getSoilData(String polyId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/soil")
                            .queryParam("polyid", polyId)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"Soil data not found for polygon " + polyId + "\"}";
            }
            throw new RuntimeException("Agro API error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        }
    }

    // ---- Current UVI Data ----
    // GET /agro/1.0/uvi?polyid=...&appid=...
    public String getUviData(String polyId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/uvi")
                            .queryParam("polyid", polyId)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"UVI data not found for polygon " + polyId + "\"}";
            }
            throw new RuntimeException("Agro API error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        }
    }

    // ---- Current Weather (by lat/lon) ----
    // GET /agro/1.0/weather?lat=...&lon=...&appid=...
    public String getWeather(double lat, double lon) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/weather")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            throw new RuntimeException("Agro weather API error [" + e.getStatusCode() + "]: " + e.getResponseBodyAsString(), e);
        }
    }

    // ---- Accumulated Temperature ----
    // GET /agro/1.0/weather/history/accumulated_temperature?lat=...&lon=...&threshold=...&start=...&end=...&appid=...
// Add this constant at the top of AgroService
    private static final String PLAN_RESTRICTED =
            "{\"error\":\"This endpoint requires a Corporate plan. " +
                    "Upgrade at https://agromonitoring.com/price\"}";

    // Fix getAccumulatedTemperature
    public String getAccumulatedTemperature(double lat, double lon, double threshold, long start, long end) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/agro/1.0/weather/history/accumulated_temperature")
                            .queryParam("lat", lat)
                            .queryParam("lon", lon)
                            .queryParam("threshold", threshold)
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 401) return PLAN_RESTRICTED;
            if (e.getStatusCode().value() == 404)
                return "{\"error\":\"No data found for the given coordinates and date range.\"}";
            throw new RuntimeException("Agro API error [" + e.getStatusCode() + "]: "
                    + e.getResponseBodyAsString(), e);
        }
    }

    // ----------------- Helper Methods -----------------

    private List<List<Double>> extractFirstRingFromGeoJson(JsonNode geoJson) {
        JsonNode geometry;
        if (geoJson.has("geometry")) {
            geometry = geoJson.get("geometry");
        } else if (geoJson.has("type") && "Polygon".equalsIgnoreCase(geoJson.path("type").asText())) {
            // geometry object sent directly
            geometry = geoJson;
        } else {
            throw new IllegalArgumentException("geo_json must contain a 'geometry' object or be a Polygon geometry");
        }
        if (!geometry.has("coordinates")) {
            throw new IllegalArgumentException("geometry must contain 'coordinates'");
        }
        return jsonCoordinatesToList(geometry.get("coordinates"));
    }

    private List<List<Double>> jsonCoordinatesToList(JsonNode coordinatesNode) {
        if (!coordinatesNode.isArray() || coordinatesNode.size() == 0) {
            throw new IllegalArgumentException("coordinates must be a non-empty array");
        }
        JsonNode firstRing = coordinatesNode.get(0);
        if (!firstRing.isArray()) {
            throw new IllegalArgumentException("First element of coordinates must be a ring array");
        }
        List<List<Double>> ring = new ArrayList<>();
        for (JsonNode point : firstRing) {
            if (!point.isArray() || point.size() < 2) {
                throw new IllegalArgumentException("Each coordinate must be an array of two numbers");
            }
            List<Double> p = new ArrayList<>();
            p.add(point.get(0).asDouble());
            p.add(point.get(1).asDouble());
            ring.add(p);
        }
        return ring;
    }

    private List<List<Double>> deepCopyCoordinates(List<List<Double>> input) {
        List<List<Double>> out = new ArrayList<>(input.size());
        for (List<Double> pt : input) {
            if (pt == null || pt.size() < 2)
                throw new IllegalArgumentException("Each coordinate must contain two numbers");
            List<Double> copy = new ArrayList<>(2);
            copy.add(pt.get(0));
            copy.add(pt.get(1));
            out.add(copy);
        }
        return out;
    }

    private void normalizeRingPoints(List<List<Double>> ring) {
        for (List<Double> p : ring) {
            if (p.size() != 2) throw new IllegalArgumentException("Each coordinate must contain exactly two numbers");
            if (p.get(0) == null || p.get(1) == null) throw new IllegalArgumentException("Coordinate contains null value");
        }
    }

    private boolean pointsEqual(List<Double> a, List<Double> b) {
        return a != null && b != null
                && Objects.equals(a.get(0), b.get(0))
                && Objects.equals(a.get(1), b.get(1));
    }

    private List<List<Double>> ensureLonLatOrder(List<List<Double>> ring) {
        boolean firstCouldBeLon = false;
        boolean secondCouldBeLon = false;

        for (List<Double> p : ring) {
            double a = p.get(0);
            double b = p.get(1);
            // If first value > 90 it can't be latitude → must be longitude
            if (Math.abs(a) > 90 && Math.abs(a) <= 180 && Math.abs(b) <= 90) firstCouldBeLon = true;
            // If second value > 90 it can't be latitude → input is [lat, lon]
            if (Math.abs(b) > 90 && Math.abs(b) <= 180 && Math.abs(a) <= 90) secondCouldBeLon = true;
        }

        List<List<Double>> out = new ArrayList<>();
        if (secondCouldBeLon && !firstCouldBeLon) {
            // Input is [lat, lon] → swap to [lon, lat]
            for (List<Double> p : ring) {
                double lat = p.get(0);
                double lon = p.get(1);
                validateLatLon(lat, lon);
                List<Double> q = new ArrayList<>();
                q.add(lon);
                q.add(lat);
                out.add(q);
            }
        } else {
            // Input is already [lon, lat]
            for (List<Double> p : ring) {
                double lon = p.get(0);
                double lat = p.get(1);
                validateLatLon(lat, lon);
                List<Double> q = new ArrayList<>();
                q.add(lon);
                q.add(lat);
                out.add(q);
            }
        }
        return out;
    }

    private void validateLatLon(double lat, double lon) {
        if (lat < -90 || lat > 90) throw new IllegalArgumentException("Latitude out of range: " + lat);
        if (lon < -180 || lon > 180) throw new IllegalArgumentException("Longitude out of range: " + lon);
    }
}