package com.ayansh.Backend.PayLoad;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.bind.annotation.ModelAttribute;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class PostCreateRequestDTO {

    @NotBlank
    private String author ;
    @NotBlank
    private String region ;
    @NotBlank
    private String body ;

}
