package com.ayansh.Backend.Controller;


import com.ayansh.Backend.Model.FarmerCategory;
import com.ayansh.Backend.Model.GovernmentScheme;
import com.ayansh.Backend.Model.LandType;
import com.ayansh.Backend.PayLoad.FarmerProfileDTO;
import com.ayansh.Backend.PayLoad.SchemeResponseDTO;
import com.ayansh.Backend.Service.SchemeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.net.URI;
import java.util.List;


@RestController
@RequestMapping("api/gov")
public class SchemeController {

    private final SchemeService service;

    public SchemeController(SchemeService service) {
        this.service = service;
    }

    @GetMapping("/schemes")
    public ResponseEntity<List<SchemeResponseDTO>> getSchemes(
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String crop,
            @RequestParam(required = false) Double landSize,
            @RequestParam(required = false) String farmerCategory,
            @RequestParam(required = false) String landType) {

        FarmerProfileDTO profile = new FarmerProfileDTO();
        profile.setState(state);
        profile.setCropType(crop);
        profile.setLandSize(landSize);

        try {
            if (farmerCategory != null) profile.setFarmerCategory(Enum.valueOf(FarmerCategory.class, farmerCategory.toUpperCase()));
        } catch (Exception ignored) {}

        try {
            if (landType != null) profile.setLandType(Enum.valueOf(LandType.class, landType.toUpperCase()));
        } catch (Exception ignored) {}

        List<SchemeResponseDTO> res = service.findEligibleSchemes(profile);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/admin/schemes")
    public ResponseEntity<List<GovernmentScheme>> all() {
        return ResponseEntity.ok(service.allSchemes());
    }

    @PostMapping("/admin/schemes")
    public ResponseEntity<GovernmentScheme> create(@RequestBody GovernmentScheme scheme) {
        GovernmentScheme saved = service.saveScheme(scheme);
        return ResponseEntity.created(java.net.URI.create("/admin/gov/schemes/" + saved.getId())).body(saved);
    }

    @PutMapping("/admin/schemes/{id}")
    public ResponseEntity<GovernmentScheme> update(@PathVariable Long id, @RequestBody GovernmentScheme scheme) {
        return service.findById(id)
                .map(existing -> {
                    scheme.setId(id);
                    GovernmentScheme updated = service.saveScheme(scheme);
                    return ResponseEntity.ok(updated);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/schemes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
