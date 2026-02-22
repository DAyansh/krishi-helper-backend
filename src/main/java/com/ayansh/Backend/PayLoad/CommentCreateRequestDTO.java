package com.ayansh.Backend.PayLoad;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentCreateRequestDTO {

    @NotBlank
    private String author;
    @NotBlank
    private String body;
}
