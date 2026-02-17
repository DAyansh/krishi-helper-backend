package com.ayansh.Backend.PayLoad;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
    public class SoilRequestDTO {
        private Long userId;
        private Double n;
        private Double p;
        private Double k;
        private Double ph;
        private Double moisture;
        private Double areaHectares;
        private String location;
    }

