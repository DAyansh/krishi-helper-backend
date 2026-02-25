package com.ayansh.Backend.PayLoad;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemeResponseDTO {
    private Long id ;
    private String schemeName ;
    private String description ;
    private String eligibilityText ;
    private String requiredDocuments ;
    private String applicationLink ;
    private String state  ;

}
