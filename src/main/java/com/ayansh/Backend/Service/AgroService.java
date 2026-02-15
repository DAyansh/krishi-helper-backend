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
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))  // Increase to 1 MB
                .build();
        this.objectMapper = objectMapper;
        this.polygonRepository = polygonRepository;
    }

    @Transactional
    public Polygon createPolygon(PolygonRequestDTO req) throws Exception {
        if (req == null) throw new IllegalArgumentException("Request cannot be null");
        if (req.getName() == null || req.getName().trim().isEmpty())
            throw new IllegalArgumentException("name is required");

        // 1) Resolve coordinates: prefer geo_json if provided, otherwise convert coordinates field
        List<List<Double>> ring = null;
        if (req.getGeo_json() != null && !req.getGeo_json().isEmpty(null)) {
            ring = extractFirstRingFromGeoJson(req.getGeo_json());
        } else if (req.getCoordinates() != null) {
            ring = deepCopyCoordinates(req.getCoordinates());
        } else {
            throw new IllegalArgumentException("Either geo_json or coordinates must be provided");
        }

        // 2) Validate minimum points (need at least 4 points including closing point)
        if (ring.size() < 3) {
            throw new IllegalArgumentException("At least 3 unique points required (will be auto-closed)");
        }

        // 3) Normalize & detect coordinate order (try to auto-detect)
        // Ensure each inner list has exactly 2 numbers
        normalizeRingPoints(ring);

        // 4) Ensure ring is closed (first == last)
        if (!pointsEqual(ring.get(0), ring.get(ring.size() - 1))) {
            // add a copy of first point to close polygon
            ring.add(new ArrayList<>(ring.get(0)));
        }

        // 5) Validate lat/lon ranges and ensure we send Agro's expected [lon, lat].
        // Our ring is currently in the form as received. We will convert to [lon, lat] now.
        List<List<Double>> lonLatRing = ensureLonLatOrder(ring);

        // After this, lonLatRing contains points like [lon, lat], closed.

        // 6) Build final GeoJSON ObjectNode for Agro API
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

        // 7) Create request root with name & geo_json (Agro expects "geo_json")
        ObjectNode root = objectMapper.createObjectNode();
        root.put("name", req.getName());
        root.set("geo_json", geoJson);

        String body = objectMapper.writeValueAsString(root);

        // 8) Call AgroMonitoring API (handle 422 and other errors)
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
            // include response body when possible for debugging
            String respBody = e.getResponseBodyAsString();
            String msg = String.format("Agro API call failed: message=%s, body=%s", e.getMessage(), respBody);
            throw new RuntimeException(msg, e);
        }

        JsonNode agroResponse = objectMapper.readTree(agroResponseStr);
        if (agroResponse == null || !agroResponse.has("id")) {
            throw new RuntimeException("Agro API did not return polygon id: " + agroResponseStr);
        }
        String agroId = agroResponse.get("id").asText();

        // 9) Compute centroid (simple average as approximation)
        double sumLat = 0.0, sumLon = 0.0;
        int count = 0;
        // exclude the duplicated closing point when computing centroid
        for (int i = 0; i < lonLatRing.size() - 1; i++) {
            List<Double> p = lonLatRing.get(i);
            sumLon += p.get(0);
            sumLat += p.get(1);
            count++;
        }
        double centerLat = sumLat / count;
        double centerLon = sumLon / count;

        // 10) Persist entity (store geoJson string)
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

    // ----------------- Helper Methods -----------------

    private List<List<Double>> extractFirstRingFromGeoJson(JsonNode geoJson) {
        if (!geoJson.has("geometry")) {
            // maybe frontend sent geometry directly
            if (geoJson.has("coordinates") && geoJson.has("type") && "Polygon".equalsIgnoreCase(geoJson.path("type").asText())) {
                // handle geometry object
                return jsonCoordinatesToList(geoJson.get("coordinates"));
            }
            throw new IllegalArgumentException("geo_json must contain a 'geometry' object");
        }
        JsonNode geometry = geoJson.get("geometry");
        if (!geometry.has("coordinates")) {
            throw new IllegalArgumentException("geo_json.geometry must contain 'coordinates'");
        }
        return jsonCoordinatesToList(geometry.get("coordinates"));
    }

    private List<List<Double>> jsonCoordinatesToList(JsonNode coordinatesNode) {
        if (!coordinatesNode.isArray() || coordinatesNode.size() == 0) {
            throw new IllegalArgumentException("coordinates must be a non-empty array");
        }
        // pick first ring
        JsonNode firstRing = coordinatesNode.get(0);
        if (!firstRing.isArray()) {
            throw new IllegalArgumentException("first element of coordinates must be an array (ring)");
        }
        List<List<Double>> ring = new ArrayList<>();
        for (JsonNode point : firstRing) {
            if (!point.isArray() || point.size() < 2) {
                throw new IllegalArgumentException("each coordinate must be an array of two numbers");
            }
            double a = point.get(0).asDouble();
            double b = point.get(1).asDouble();
            List<Double> p = new ArrayList<>();
            p.add(a);
            p.add(b);
            ring.add(p);
        }
        return ring;
    }

    private List<List<Double>> deepCopyCoordinates(List<List<Double>> input) {
        List<List<Double>> out = new ArrayList<>(input.size());
        for (List<Double> pt : input) {
            if (pt == null || pt.size() < 2)
                throw new IllegalArgumentException("each coordinate must contain two numbers");
            List<Double> copy = new ArrayList<>(2);
            copy.add(pt.get(0));
            copy.add(pt.get(1));
            out.add(copy);
        }
        return out;
    }

    private void normalizeRingPoints(List<List<Double>> ring) {
        for (List<Double> p : ring) {
            if (p.size() != 2) throw new IllegalArgumentException("each coordinate must contain exactly two numbers");
            if (p.get(0) == null || p.get(1) == null) throw new IllegalArgumentException("coordinate contains null");
        }
    }

    private boolean pointsEqual(List<Double> a, List<Double> b) {
        return a != null && b != null && Objects.equals(a.get(0), b.get(0)) && Objects.equals(a.get(1), b.get(1));
    }

    private List<List<Double>> ensureLonLatOrder(List<List<Double>> ring) {
        boolean firstLooksLikeLon = false;
        boolean secondLooksLikeLon = false;

        for (List<Double> p : ring) {
            double a = p.get(0);
            double b = p.get(1);
            if (Math.abs(a) > 90 && Math.abs(a) <= 180 && Math.abs(b) <= 90) firstLooksLikeLon = true;
            if (Math.abs(b) > 90 && Math.abs(b) <= 180 && Math.abs(a) <= 90) secondLooksLikeLon = true;
        }

        List<List<Double>> out = new ArrayList<>();
        if (secondLooksLikeLon && !firstLooksLikeLon) {
            // input looks like [lat, lon] -> swap
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
            // assume input is [lon, lat]
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
        if (lat < -90 || lat > 90) throw new IllegalArgumentException("latitude out of range: " + lat);
        if (lon < -180 || lon > 180) throw new IllegalArgumentException("longitude out of range: " + lon);
    }

    public String getNdvi(String polyId) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/ndvi")
                            .queryParam("polyid", polyId)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"NDVI data not found for polygon " + polyId + ". Ensure the polygon exists and has satellite data.\"}";
            }
            throw new RuntimeException("Agro API error: " + e.getMessage(), e);
        }
    }

    public String getNdviHistory(String polyId, long startTs, long endTs) {
        try {
            return webClient.get().uri(uriBuilder -> uriBuilder.path("/agro/1.0/ndvi/history")
                            .queryParam("polyid", polyId)
                            .queryParam("start", startTs)
                            .queryParam("end", endTs)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"NDVI data not found for polygon " + polyId + ". Ensure the polygon exists and has satellite data.\"}";
            }
            throw new RuntimeException("Agro API error: " + e.getMessage(), e);
        }
    }

    public String searchImages(String polyId, long start, long end) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/agro/1.0/image/search")  // Add /agro/1.0/
                            .queryParam("polyid", polyId)
                            .queryParam("start", start)
                            .queryParam("end", end)
                            .queryParam("appid", apiKey).build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 404) {
                return "{\"error\": \"No satellite images found for the polygon and date range.\"}";
            }
            throw new RuntimeException("Agro API error: " + e.getMessage(), e);
        }
    }
}
