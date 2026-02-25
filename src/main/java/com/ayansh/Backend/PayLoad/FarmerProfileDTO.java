package com.ayansh.Backend.PayLoad;

import com.ayansh.Backend.Model.FarmerCategory;
import com.ayansh.Backend.Model.LandType;
import lombok.Data;

@Data
public class FarmerProfileDTO {
    private String state ;
    private String district  ;
    private String cropType ;
    private Double landSize ;
    private FarmerCategory farmerCategory;
    private LandType landType;
}
