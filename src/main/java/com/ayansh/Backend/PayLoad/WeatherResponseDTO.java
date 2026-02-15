package com.ayansh.Backend.PayLoad;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class WeatherResponseDTO {
    private double temperature;
    private double humidity;
    private double rainProbability;
    private String alerts;

    // Constructors, getters, setters
    public WeatherResponseDTO() {
    }

    public WeatherResponseDTO(double temp, double hum, double rain, String alerts) {
        this.temperature = temp;
        this.humidity = hum;
        this.rainProbability = rain;
        this.alerts = alerts;
    }
}
