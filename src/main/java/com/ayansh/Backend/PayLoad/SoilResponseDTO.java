package com.ayansh.Backend.PayLoad;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class SoilResponseDTO {

    private List<String> recommendations  ;
    private Double confidence ;
}
