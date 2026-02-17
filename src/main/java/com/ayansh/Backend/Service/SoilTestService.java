package com.ayansh.Backend.Service;

import com.ayansh.Backend.Model.SoilTest;
import com.ayansh.Backend.PayLoad.SoilRequestDTO;
import com.ayansh.Backend.PayLoad.SoilResponseDTO;
import com.ayansh.Backend.Repository.SoilTestRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;


@Service
public class SoilTestService {

        @Autowired
        private SoilTestRepo soilRepo;


        public SoilResponseDTO analyzeSoil(SoilRequestDTO dto) {

            validate(dto);

            SoilTest test = saveSoilTest(dto);

            List<String> recommendations = applyRules(dto);

            double confidence = calculateConfidence(dto, recommendations);

            SoilResponseDTO response = new SoilResponseDTO();
            response.setRecommendations(recommendations);
            response.setConfidence(confidence);

            return response;
        }

        private void validate(SoilRequestDTO dto) {
            if (dto.getN() < 0 || dto.getPh() > 14)
                throw new RuntimeException("Invalid soil values");
        }

        private SoilTest saveSoilTest(SoilRequestDTO dto) {
            SoilTest test = new SoilTest();
            test.setUserId(dto.getUserId());
            test.setN(dto.getN());
            test.setP(dto.getP());
            test.setK(dto.getK());
            test.setPh(dto.getPh());
            test.setMoisture(dto.getMoisture());
            test.setAreaHectares(dto.getAreaHectares());
            test.setLocation(dto.getLocation());
            test.setCreatedAt(LocalDateTime.now());

            return soilRepo.save(test);
        }

        private List<String> applyRules(SoilRequestDTO dto) {

            List<String> rec = new ArrayList<>();

            if (dto.getPh() < 5.5) {
                rec.add("Soil is acidic. Apply agricultural lime: 400 kg/ha");
            }

            if (dto.getN() < 280) {
                rec.add("Nitrogen deficiency. Apply urea: 75 kg/ha");
            }

            if (dto.getP() < 10) {
                rec.add("Low phosphorus. Apply DAP fertilizer");
            }

            if (dto.getK() < 110) {
                rec.add("Low potassium. Apply MOP fertilizer");
            }

            if (rec.isEmpty()) {
                rec.add("Soil health is good. Maintain current practices.");
            }

            return rec;
        }

        private double calculateConfidence(SoilRequestDTO dto, List<String> rec) {
            if (rec.size() <= 1) return 0.95;
            if (rec.size() <= 3) return 0.85;
            return 0.75;
        }
    }
