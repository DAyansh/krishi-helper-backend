package com.ayansh.Backend.PayLoad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDTO {
    private double temperature;
    private double humidity;
    private double rainProbability;
    private String alerts;
}