package com.ayansh.Backend.Controller;

import java.util.List;

import com.ayansh.Backend.PayLoad.CommentCreateRequestDTO;
import com.ayansh.Backend.PayLoad.CommentResponseDTO;
import com.ayansh.Backend.PayLoad.PostCreateRequestDTO;
import com.ayansh.Backend.PayLoad.PostResponseDTO;
import com.ayansh.Backend.Service.CommunityService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;


    @PostMapping(value = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostResponseDTO> createPost(
            @RequestParam String author,
            @RequestParam String region,
            @RequestParam String body,
            @RequestPart(name = "images", required = false) List<MultipartFile> images
    ) throws BadRequestException {
        PostCreateRequestDTO data = new PostCreateRequestDTO(author, region, body); // Assuming a constructor
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
    ) throws ChangeSetPersister.NotFoundException {
        CommentResponseDTO created = communityService.addComment(id, req);

        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}