package com.ayansh.Backend.Controller;

import com.ayansh.Backend.PayLoad.WeatherResponseDTO;
import com.ayansh.Backend.Service.WeatherAlertService;
import com.ayansh.Backend.Service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")   // tighten to your frontend origin in production
public class WeatherController {

    private final WeatherService weatherService;
    private final WeatherAlertService alertService;

    // Constructor injection — preferred over @Autowired field injection
    public WeatherController(WeatherService weatherService,
                             WeatherAlertService alertService) {
        this.weatherService = weatherService;
        this.alertService   = alertService;
    }

    @GetMapping
    public ResponseEntity<WeatherResponseDTO> getWeather(
            @RequestParam double lat,
            @RequestParam double lon) {

        String raw = weatherService.fetchWeatherRaw(lat, lon);
        WeatherResponseDTO dto = alertService.parseAndGenerate(raw);
        return ResponseEntity.ok(dto);
    }
}