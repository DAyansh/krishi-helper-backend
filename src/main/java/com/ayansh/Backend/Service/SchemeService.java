package com.ayansh.Backend.Service;

import com.ayansh.Backend.Model.FarmerCategory;
import com.ayansh.Backend.Model.GovernmentScheme;
import com.ayansh.Backend.Model.LandType;
import com.ayansh.Backend.PayLoad.FarmerProfileDTO;
import com.ayansh.Backend.PayLoad.SchemeResponseDTO;
import com.ayansh.Backend.Repository.GovernmentSchemeRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class SchemeService {

    @Autowired
    private GovernmentSchemeRepo governmentSchemeRepo;

    public List<SchemeResponseDTO> findEligibleSchemes(FarmerProfileDTO farmerProfileDTO) {
        List<GovernmentScheme> schemes = governmentSchemeRepo.findByIsActiveTrue() ;

        return schemes.stream()
                .filter(s->matchesState(s, farmerProfileDTO.getState()))
                .filter(s->matchesCrop(s,farmerProfileDTO.getCropType()))
                .filter(s->matchesLandSize(s,farmerProfileDTO.getLandSize()))
                .filter(s->matchesFarmerCategory(s,farmerProfileDTO.getFarmerCategory()))
                .filter(s->matchesLandType(s,farmerProfileDTO.getLandType()))
                .map(this::toDTO)
                .collect(Collectors.toList()) ;
    }

    private boolean matchesState(GovernmentScheme s, String state) {
        if (s.getState() == null || s.getState().isBlank()) return true; // central scheme
        if (state == null || state.isBlank()) return true; // no filter applied
        return s.getState().equalsIgnoreCase(state);
    }

    private boolean matchesLandSize(GovernmentScheme s, Double landSize) {
        if (landSize == null) return true;
        if (s.getMinLandSize() != null && landSize < s.getMinLandSize()) return false; // ✅ already correct
        if (s.getMaxLandSize() != null && landSize > s.getMaxLandSize()) return false; // ✅ fixed
        return true;
    }

    private boolean matchesCrop(GovernmentScheme s, String crop) {
        if (s.getCropTypes() == null || s.getCropTypes().isBlank()) return true;
        if (crop == null || crop.isBlank()) return true; // ✅ no filter applied
        String[] parts = s.getCropTypes().split(",");
        return Arrays.stream(parts)
                .map(String::trim)
                .anyMatch(c -> c.equalsIgnoreCase(crop));
    }

    private boolean matchesFarmerCategory(GovernmentScheme s, FarmerCategory cat) {
        if (s.getFarmerCategory() == null || s.getFarmerCategory() == FarmerCategory.ALL) return true;
        if (cat == null) return true; // ✅ no filter applied
        return s.getFarmerCategory() == cat;
    }

    private boolean matchesLandType(GovernmentScheme s, LandType landType) {
        if (s.getLandType() == null || s.getLandType() == LandType.BOTH) return true;
        if (landType == null) return true; // ✅ same fix for consistency
        return s.getLandType() == landType;
    }

    private SchemeResponseDTO toDTO(GovernmentScheme s) {
        return new SchemeResponseDTO(
                s.getId(),
                s.getSchemeName(),
                s.getDescription(),
                s.getEligibilityText(),
                s.getRequiredDocuments(),
                s.getApplicationLink(),
                s.getState()
        ); }

        public GovernmentScheme saveScheme(GovernmentScheme scheme) {
            return governmentSchemeRepo.save(scheme);
        }

        public Optional<GovernmentScheme> findById(Long id) {
            return governmentSchemeRepo.findById(id);
        }

        public void deleteById(Long id) {
            governmentSchemeRepo.deleteById(id);
        }

        public List<GovernmentScheme> allSchemes() {
            return governmentSchemeRepo.findAll();
        }

}
