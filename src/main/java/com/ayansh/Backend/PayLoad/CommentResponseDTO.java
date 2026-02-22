package com.ayansh.Backend.PayLoad;

import lombok.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponseDTO {
    private Long id;
    private String author;
    private String body;
    private LocalDateTime createdAt;
}
