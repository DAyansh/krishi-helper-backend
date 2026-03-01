package com.ayansh.Backend.Controller;

import com.ayansh.Backend.Service.SatelliteService;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/satellite")
@CrossOrigin(origins = "*")
public class SatelliteController {

    private final SatelliteService satelliteService;

    public SatelliteController(SatelliteService satelliteService) {
        this.satelliteService = satelliteService;
    }

    @GetMapping("/search")
    public ResponseEntity<JsonNode> searchImages(
            @RequestParam String polygonId,
            @RequestParam long fromTs,
            @RequestParam long toTs) {
        return ResponseEntity.ok(satelliteService.searchImages(polygonId, fromTs, toTs));
    }

    @GetMapping("/stats")
    public ResponseEntity<JsonNode> getStats(@RequestParam String statsUrl) {
        return ResponseEntity.ok(satelliteService.getImageStats(statsUrl));
    }
}