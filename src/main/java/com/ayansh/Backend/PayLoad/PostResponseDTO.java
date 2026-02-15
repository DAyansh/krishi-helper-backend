package com.ayansh.Backend.PayLoad;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostResponseDTO {

    private Long id;
    private String author;
    private String region;
    private String body;
    private Instant createdAt;
    private List<String> imageUrls;
    private int commentCount;

    public PostResponseDTO(long id, String author, String region, String body, Instant createdAt, List<String> imageUrls) {
    }
}

