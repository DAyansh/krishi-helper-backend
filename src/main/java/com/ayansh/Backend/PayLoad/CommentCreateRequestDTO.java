package com.ayansh.Backend.PayLoad;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDTO {

    @NotBlank
    private String author ;
    @NotBlank
    private String body ;
}
