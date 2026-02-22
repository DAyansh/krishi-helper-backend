package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Table(name="post_image")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PostImage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String url;

    @Column(name = "public_id", nullable = true)
    private String publicId;

    private LocalDateTime createdAt;

    public PostImage(Post post, String url, String publicId) {
        this.post = post;
        this.url = url;
        this.publicId = publicId;
    }

    public PostImage(Post post, String url) {
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
      }
    }

