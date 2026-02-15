package com.ayansh.Backend.Controller;


import com.ayansh.Backend.Model.NdviHistory;
import com.ayansh.Backend.Model.Polygon;
import com.ayansh.Backend.PayLoad.PolygonRequestDTO;
import com.ayansh.Backend.PayLoad.PolygonResponseDTO;
import com.ayansh.Backend.Repository.NdviHistoryRepo;
import com.ayansh.Backend.Repository.PolygonRepo;
import com.ayansh.Backend.Service.AgroService;
import com.ayansh.Backend.Service.WeatherService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
    @RequestMapping("/api/polygons")
//    @CrossOrigin(origins = "*")
    public class AgroController {

        @Autowired
        private AgroService agroService;

        @Autowired
        private PolygonRepo polygonRepository;

        @Autowired
        private WeatherService weatherService;

        @Autowired
        private NdviHistoryRepo ndviRepo;

        @Autowired
        private ObjectMapper mapper = new ObjectMapper();

        @PostMapping("/create")
        public ResponseEntity<?> createPolygon(@RequestBody PolygonRequestDTO req) {
            try {
                Polygon entity = agroService.createPolygon(req);
                System.out.println("Agro JSON Payload = " + entity.getGeoJson());

                PolygonResponseDTO resp = new PolygonResponseDTO(entity.getId(), entity.getAgroPolygonId(), "Polygon created");


                return ResponseEntity.ok(resp);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }
        }

        @GetMapping
        public List<Polygon> listAll() {
            return polygonRepository.findAll();
        }

        @GetMapping("/ndvi/history/{polyId}")
        public ResponseEntity<String> getNdviHistory(@PathVariable String polyId, @RequestParam long start, @RequestParam long end) {
            Polygon polygon = polygonRepository.findByAgroPolygonId(polyId);
            if (polygon == null) {
                return ResponseEntity.badRequest().body("Polygon not found in local DB");
            }
            return ResponseEntity.ok(agroService.getNdviHistory(polyId, start, end));
        }


        @GetMapping("/ndvi/{agroPolygonId}")
        public ResponseEntity<String> getNdvi(@PathVariable String agroPolygonId) {
            Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
            if (polygon == null) {
                return ResponseEntity.badRequest().body("Polygon not found in local DB");
            }
            String resp = agroService.getNdvi(agroPolygonId);
            return ResponseEntity.ok(resp);
        }

            // 4) Weather for polygon center
            @GetMapping("/weather/{agroPolygonId}")
            public ResponseEntity<String> getWeather(@PathVariable String agroPolygonId) {
                Polygon p = polygonRepository.findByAgroPolygonId(agroPolygonId); // note: adapt if needed

                // better: store mapping of local id <-> agroPolygonId; here we assume agroPolygonId is passed
                // For demonstration we'll parse center from DB by agroPolygonId:
                Polygon pe = polygonRepository.findAll()
                        .stream().filter(x->x.getAgroPolygonId().equals(agroPolygonId)).findFirst().orElse(null);
                if (pe==null) return ResponseEntity.badRequest().body("Polygon not found");
                double lat = pe.getCenterLat();
                double lon = pe.getCenterLon();
                String w = weatherService.fetchWeatherRaw(lat, lon);
                return ResponseEntity.ok(w);
            }

            // 5) Satellite image search (by date range)






//        @GetMapping("/weather/{agroPolygonId}")
//        public ResponseEntity<String> getWeather(@PathVariable String agroPolygonId) {
//            Polygon pe = polygonRepository.findByAgroPolygonId(agroPolygonId).orElse(null);
//            if (pe == null) return ResponseEntity.badRequest().body("Polygon not found");
//            double lat = pe.getCenterLat();
//            double lon = pe.getCenterLon();
//            String w = weatherService.fetchWeatherRaw(lat, lon);
//            return ResponseEntity.ok(w);
//        }

        @GetMapping("/images/{agroPolygonId}")
        public ResponseEntity<String> searchImages(@PathVariable String agroPolygonId, @RequestParam long start, @RequestParam long end) {
            Polygon polygon = polygonRepository.findByAgroPolygonId(agroPolygonId);
            if (polygon == null) {
                return ResponseEntity.badRequest().body("Polygon not found in local DB");
            }
            String resp = agroService.searchImages(agroPolygonId, start, end);
            return ResponseEntity.ok(resp);
        }


    }


