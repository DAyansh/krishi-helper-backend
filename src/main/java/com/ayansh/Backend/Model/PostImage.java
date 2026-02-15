package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name="post_image")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id ;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post ;

    @Column(columnDefinition = "TEXT")
    private String url ;

    private Instant createdAt = Instant.now();

}
