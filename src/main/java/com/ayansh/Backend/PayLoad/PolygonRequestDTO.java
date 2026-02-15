package com.ayansh.Backend.PayLoad;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import java.util.List;

@Data
public class PolygonRequestDTO {
    // name of polygon (required)
    private String name;

    // Optional: full GeoJSON object sent by frontend (preferred)
    // Example: { "type":"Feature", "properties":{}, "geometry": { "type":"Polygon", "coordinates":[ [ [lon,lat], ... ] ] } }
    private JsonNode geo_json;

    // Optional: simplified coordinates array (array of [lon,lat] pairs)
    // Example: [[77.6, 12.9], [77.61, 12.9], [77.61, 12.91], [77.6, 12.91], [77.6, 12.9]]
    private List<List<Double>> coordinates;
}
