package com.ayansh.Backend.PayLoad;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SoilRequestDTO {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull @DecimalMin("0.0") @DecimalMax("999.0")
    private Double n;                  // kg/ha

    @NotNull @DecimalMin("0.0") @DecimalMax("999.0")
    private Double p;                  // kg/ha

    @NotNull @DecimalMin("0.0") @DecimalMax("999.0")
    private Double k;                  // kg/ha

    @NotNull @DecimalMin("0.0") @DecimalMax("14.0")
    private Double ph;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private Double moisture;           // % optional

    private Double ec;                 // dS/m, optional

    @NotNull @DecimalMin("0.01")
    private Double areaHectares;

    private String location;           // village name or "lat,lon"

    private String cropHistory;        // e.g. "rice,wheat"

    private String desiredCrop;        // optional hint from farmer

    private String language;           // "en", "hi", "mr" — default "en"
}