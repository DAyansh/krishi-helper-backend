package com.ayansh.Backend.Controller;

import com.ayansh.Backend.Model.Polygon;
import com.ayansh.Backend.PayLoad.PolygonRequestDTO;
import com.ayansh.Backend.PayLoad.PolygonResponseDTO;
import com.ayansh.Backend.Repository.PolygonRepo;
import com.ayansh.Backend.Service.AgroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/polygons")
public class AgroController {

    @Autowired
    private AgroService agroService;

    @Autowired
    private PolygonRepo polygonRepository;

    // ---- Create Polygon ----
    @PostMapping("/create")
    public ResponseEntity<?> createPolygon(@RequestBody PolygonRequestDTO req) {
        try {
            Polygon entity = agroService.createPolygon(req);
            PolygonResponseDTO resp = new PolygonResponseDTO(entity.getId(), entity.getAgroPolygonId(), "Polygon created");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---- List All Polygons (local DB) ----
    @GetMapping
    public List<Polygon> listAll() {
        return polygonRepository.findAll();
    }

    // ---- NDVI History ----
    // GET /api/polygons/ndvi/history/{agroPolygonId}?start=1500336000&end=1508976000
    @GetMapping("/ndvi/history/{agroPolygonId}")
    public ResponseEntity<String> getNdviHistory(
            @PathVariable String agroPolygonId,
            @RequestParam long start,
            @RequestParam long end) {
        Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
        if (polygon == null) {
            return ResponseEntity.badRequest().body("Polygon not found: " + agroPolygonId);
        }
        return ResponseEntity.ok(agroService.getNdviHistory(agroPolygonId, start, end));
    }

    // ---- Satellite Image Search ----
    // GET /api/polygons/images/{agroPolygonId}?start=1500336000&end=1508976000
    @GetMapping("/images/{agroPolygonId}")
    public ResponseEntity<String> searchImages(
            @PathVariable String agroPolygonId,
            @RequestParam long start,
            @RequestParam long end) {
        Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
        if (polygon == null) {
            return ResponseEntity.badRequest().body("Polygon not found: " + agroPolygonId);
        }
        return ResponseEntity.ok(agroService.searchImages(agroPolygonId, start, end));
    }

    // ---- Current Weather for Polygon Center ----
    // GET /api/polygons/weather/{agroPolygonId}
    @GetMapping("/weather/{agroPolygonId}")
    public ResponseEntity<String> getWeather(@PathVariable String agroPolygonId) {
        // FIX: single clean lookup, no double query
        Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
        if (polygon == null) {
            return ResponseEntity.badRequest().body("Polygon not found: " + agroPolygonId);
        }
        String weather = agroService.getWeather(polygon.getCenterLat(), polygon.getCenterLon());
        return ResponseEntity.ok(weather);
    }

    // ---- Current Soil Data ----
    // GET /api/polygons/soil/{agroPolygonId}
    @GetMapping("/soil/{agroPolygonId}")
    public ResponseEntity<String> getSoilData(@PathVariable String agroPolygonId) {
        Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
        if (polygon == null) {
            return ResponseEntity.badRequest().body("Polygon not found: " + agroPolygonId);
        }
        return ResponseEntity.ok(agroService.getSoilData(agroPolygonId));
    }

    // ---- Current UVI Data ----
    // GET /api/polygons/uvi/{agroPolygonId}
    @GetMapping("/uvi/{agroPolygonId}")
    public ResponseEntity<String> getUviData(@PathVariable String agroPolygonId) {
        Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
        if (polygon == null) {
            return ResponseEntity.badRequest().body("Polygon not found: " + agroPolygonId);
        }
        return ResponseEntity.ok(agroService.getUviData(agroPolygonId));
    }

    // ---- Accumulated Temperature (uses polygon center lat/lon) ----
    // GET /api/polygons/accumulated-temp/{agroPolygonId}?threshold=284&start=1517502031&end=1519834831
    @GetMapping("/accumulated-temp/{agroPolygonId}")
    public ResponseEntity<String> getAccumulatedTemperature(
            @PathVariable String agroPolygonId,
            @RequestParam double threshold,
            @RequestParam long start,
            @RequestParam long end) {
        Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
        if (polygon == null) {
            return ResponseEntity.badRequest().body("Polygon not found: " + agroPolygonId);
        }
        String result = agroService.getAccumulatedTemperature(
                polygon.getCenterLat(), polygon.getCenterLon(), threshold, start, end);
        return ResponseEntity.ok(result);
    }
}