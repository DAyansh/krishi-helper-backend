package com.ayansh.Backend.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.ayansh.Backend.Model.Comment;
import com.ayansh.Backend.Model.Post;
import com.ayansh.Backend.PayLoad.CommentCreateRequestDTO;
import com.ayansh.Backend.PayLoad.CommentResponseDTO;
import com.ayansh.Backend.PayLoad.PostCreateRequestDTO;
import com.ayansh.Backend.PayLoad.PostResponseDTO;
import com.ayansh.Backend.Repository.CommentRepository;
import com.ayansh.Backend.Repository.PostRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CommunityService {
    private final PostRepository postRepo;
    private final CommentRepository commentRepo;

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.baseUrl}")
    private String baseUrl;

    public CommunityService(PostRepository postRepo, CommentRepository commentRepo) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
    }

    public Page<PostResponseDTO> listPosts(String region, Pageable pageable) {
        Page<Post> page;
        if (region != null && !region.trim().isEmpty()) {
            page = postRepo.findByRegionOrderByCreatedAtDesc(region, pageable);
        } else {
            page = postRepo.findAllByOrderByCreatedAtDesc(pageable);
        }
        return page.map(this::mapToDto);
    }

    public PostResponseDTO createPost(PostCreateRequestDTO data, List<MultipartFile> images) {
        // File validation (as per section 10)
        if (images != null) {
            for (MultipartFile file : images) {
                if (file.getSize() > 5 * 1024 * 1024) { // 5MB max
                    throw new IllegalArgumentException("File size exceeds 5MB");
                }
                String contentType = file.getContentType();
                if (!List.of("image/png", "image/jpeg", "image/webp", "image/gif").contains(contentType)) {
                    throw new IllegalArgumentException("Invalid file type: " + contentType);
                }
            }
        }

        // Create and save post
        Post post = new Post(null, data.getAuthor(), data.getRegion(), data.getBody(), null, new ArrayList<>());
        post = postRepo.save(post); // Save to get ID

        // Handle file uploads
        List<String> imageUrls = new ArrayList<>();
        if (images != null) {
            for (MultipartFile file : images) {
                String sanitizedName = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
                Path filePath = Paths.get(uploadDir, sanitizedName);
                try {
                    Files.createDirectories(filePath.getParent()); // Ensure directory exists
                    Files.copy(file.getInputStream(), filePath);
                    imageUrls.add(baseUrl + "/" + sanitizedName); // Full URL
                } catch (IOException e) {
                    throw new RuntimeException("File upload failed", e);
                }
            }
        }
        post.setImageUrls(imageUrls);
        post = postRepo.save(post); // Update with image URLs
        return mapToDto(post);
    }

    public CommentResponseDTO addComment(Long postId, CommentCreateRequestDTO req) {
        // Validate post exists
        if (!postRepo.existsById(postId)) {
            throw new IllegalArgumentException("Post not found");
        }

        Comment comment = new Comment(null, postId, req.getAuthor(), req.getBody(), null);
        comment = commentRepo.save(comment);
        return mapToDto(comment);
    }

    private PostResponseDTO mapToDto(Post post) {
        return new PostResponseDTO(
                post.getId(),
                post.getAuthor(),
                post.getRegion(),
                post.getBody(),
                post.getCreatedAt(),
                post.getImageUrls()
        );
    }

    private CommentResponseDTO mapToDto(Comment comment) {
        return new CommentResponseDTO(
                comment.getId(),
                comment.getAuthor(),
                comment.getBody(),
                comment.getCreatedAt()
        );
    }

    private String getFileExtension(String filename) {
        return filename != null && filename.contains(".") ? filename.substring(filename.lastIndexOf(".")) : "";
    }
}