package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name= "comment")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post ;

    private String author ;

    @Column(columnDefinition = "TEXT")
    private String body ;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public Comment(Object o, Long postId, @NotBlank String author, @NotBlank String body, Object o1) {
    }
}
