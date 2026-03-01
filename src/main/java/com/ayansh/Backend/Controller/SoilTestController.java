package com.ayansh.Backend.Controller;

import com.ayansh.Backend.Model.SoilTest;
import com.ayansh.Backend.PayLoad.SoilRequestDTO;
import com.ayansh.Backend.Service.SoilTestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/soil")
@RequiredArgsConstructor
public class SoilTestController {

    private final SoilTestService soilTestService;

    // POST /api/soil/analyse
    @PostMapping("/analyse")
    public ResponseEntity<?> analyse(@Valid @RequestBody SoilRequestDTO dto) {
        return ResponseEntity.ok(soilTestService.analyzeSoil(dto));
    }

    // GET /api/soil/history/{userId}
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<SoilTest>> history(@PathVariable Long userId) {
        return ResponseEntity.ok(soilTestService.getHistory(userId));
    }
}