package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id ;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String region;

    @Column(columnDefinition = "TEXT")
    private String body ;

    @ElementCollection
    @CollectionTable(name = "post_images", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> imageUrls;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    public <E> Post(Object o, @NotBlank String author, @NotBlank String region, @NotBlank String body, Object o1, ArrayList<E> es) {
    }

//    public void addImage(PostImage img){
//        image.add(img) ;
//        img.setPost(this) ;
//    }

}
