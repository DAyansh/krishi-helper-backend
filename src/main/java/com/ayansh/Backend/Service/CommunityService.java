package com.ayansh.Backend.Service;

import com.ayansh.Backend.Model.Comment;
import com.ayansh.Backend.Model.Post;
import com.ayansh.Backend.Model.PostImage;
import com.ayansh.Backend.PayLoad.CommentCreateRequestDTO;
import com.ayansh.Backend.PayLoad.CommentResponseDTO;
import com.ayansh.Backend.PayLoad.PostCreateRequestDTO;
import com.ayansh.Backend.PayLoad.PostResponseDTO;
import com.ayansh.Backend.Repository.CommentRepository;
import com.ayansh.Backend.Repository.PostRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CommunityService {

    private final PostRepository postRepo;
    private final CommentRepository commentRepo;
    private final FileStorageService fileStorageService;


    public CommunityService(PostRepository postRepo,
                            CommentRepository commentRepo,
                            FileStorageService fileStorageService) {
        this.postRepo = postRepo;
        this.commentRepo = commentRepo;
        this.fileStorageService = fileStorageService;
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

    @Transactional
    public PostResponseDTO createPost(PostCreateRequestDTO data, List<MultipartFile> images) {
        Post post = new Post(data.getAuthor(), data.getRegion(), data.getBody());
        post = postRepo.save(post);

        if (images != null && !images.isEmpty()) {
            List<PostImage> postImages = new ArrayList<>();
            for (MultipartFile file : images) {
                try {
                    Map<String, String> uploadResult =
                            fileStorageService.upload(file, "posts/" + post.getId());

                    String url = uploadResult.get("url");
                    String publicId = uploadResult.get("publicId");
                    PostImage pi = new PostImage(post, url,publicId);
                    postImages.add(pi);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload image: " + e.getMessage(), e);
                }
            }
            post.getImages().addAll(postImages);
            post = postRepo.save(post);
        }

        return mapToDto(post);
    }

    @Transactional
    public CommentResponseDTO addComment(Long postId, CommentCreateRequestDTO req) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found with id: " + postId));

        Comment comment = new Comment(post, req.getAuthor(), req.getBody());
        comment = commentRepo.save(comment);
        return mapToDto(comment);
    }

    private PostResponseDTO mapToDto(Post post) {
        List<String> imageUrls = new ArrayList<>();
        if (post.getImages() != null) {
            for (PostImage img : post.getImages()) {
                imageUrls.add(img.getUrl());
            }
        }
        long commentCount = commentRepo.countByPost_Id(post.getId());
        return new PostResponseDTO(
                post.getId(),
                post.getAuthor(),
                post.getRegion(),
                post.getBody(),
                post.getCreatedAt(),
                imageUrls,
                commentCount
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

    @Transactional
    public void deletePost(Long postId) {

        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        if (post.getImages() != null) {
            for (PostImage img : post.getImages()) {
                try {
                    fileStorageService.delete(img.getPublicId());
                } catch (IOException e) {
                    throw new RuntimeException("Failed to delete image from Cloudinary", e);
                }
            }
        }

        commentRepo.deleteCommentByPost(post) ;
        postRepo.delete(post);
    }
}
