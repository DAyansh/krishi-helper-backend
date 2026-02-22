package com.ayansh.Backend.Controller;

import com.ayansh.Backend.PayLoad.CommentCreateRequestDTO;
import com.ayansh.Backend.PayLoad.CommentResponseDTO;
import com.ayansh.Backend.PayLoad.PostCreateRequestDTO;
import com.ayansh.Backend.PayLoad.PostResponseDTO;
import com.ayansh.Backend.Service.CommunityService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@RestController
@RequestMapping("api/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    private static final Logger log = LoggerFactory.getLogger(CommunityController.class);

    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestParam String author,
            @RequestParam String region,
            @RequestParam String body,
            @RequestParam(name = "images", required = false) List<MultipartFile> images
    ) {
        PostCreateRequestDTO data = new PostCreateRequestDTO(author, region, body);
        PostResponseDTO created = communityService.createPost(data, images);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/posts")
    public Page<PostResponseDTO> listPosts(
            @RequestParam(required = false) String region,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return communityService.listPosts(region, PageRequest.of(page, size));
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long id,
            @RequestBody @Valid CommentCreateRequestDTO req
    ) {
        CommentResponseDTO created = communityService.addComment(id, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable long postId){
        communityService.deletePost(postId);
        //log.info("Deleted post with id {}", postId);
        return ResponseEntity.status(HttpStatus.OK).body("Post with ID: " + postId + " has been deleted");
    }
}
