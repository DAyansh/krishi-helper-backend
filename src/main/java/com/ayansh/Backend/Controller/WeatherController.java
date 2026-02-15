package com.ayansh.Backend.Controller;

import com.ayansh.Backend.PayLoad.WeatherResponseDTO;
import com.ayansh.Backend.Service.WeatherAlertService;
import com.ayansh.Backend.Service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
    @RequestMapping("/api/weather")
    public class WeatherController {

        @Autowired
        private WeatherService weatherService;

        @Autowired
        private WeatherAlertService alertService;

        @GetMapping
        public WeatherResponseDTO getWeather(@RequestParam double lat,
                                             @RequestParam double lon) {

            String raw = weatherService.fetchWeatherRaw(lat, lon);

            return alertService.parseAndGenerate(raw);
        }
    }

