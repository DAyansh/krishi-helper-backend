package com.ayansh.Backend.PayLoad;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PolygonResponseDTO {
    private Long id ;
    private String polygonId ;
    private String message ;
}
