package com.ayansh.Backend.PayLoad;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDTO {

    private Long id;
    private String author;
    private String region;
    private String body;
    private LocalDateTime createdAt;
    private List<String> imageUrls;
    private long commentCount;

}
