package com.ayansh.Backend.Controller;


import com.ayansh.Backend.PayLoad.SoilRequestDTO;
import com.ayansh.Backend.Service.SoilTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/soil")
public class SoilTestController {

    @Autowired
    SoilTestService soilTestService;


    @PostMapping("/analyse")
    public ResponseEntity<?> analyse(@RequestBody SoilRequestDTO soilRequestDTO) {

        return ResponseEntity.ok(soilTestService.analyzeSoil(soilRequestDTO)) ;
    }
}
