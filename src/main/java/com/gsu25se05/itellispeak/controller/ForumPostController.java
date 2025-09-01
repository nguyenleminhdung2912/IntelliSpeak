package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.*;
import com.gsu25se05.itellispeak.entity.*;
import com.gsu25se05.itellispeak.exception.auth.NotFoundException;
import com.gsu25se05.itellispeak.repository.ForumPostRepository;
import com.gsu25se05.itellispeak.service.ForumPostService;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/forum-post")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ForumPostController {

    @Autowired
    AccountUtils accountUtils;

    @Autowired
    ForumPostRepository forumPostRepository;

    @Autowired
    ForumPostService forumPostService;

    // ForumPostController.java
    @GetMapping()
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getAllForumPosts() {
        Response<List<CreateResponseForumDTO>> resp = forumPostService.getAllPosts();
        return ResponseEntity.status(resp.getCode()).body(resp);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Response<CreateResponseForumDTO>> getForumPostById(@PathVariable Long id) {
        Response<CreateResponseForumDTO> resp = forumPostService.getPostById(id);
        return ResponseEntity.status(resp.getCode()).body(resp);
    }


    @GetMapping("/my-posts")
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getMyPosts() {
        return ResponseEntity.ok(forumPostService.getMyPosts());
    }

    @GetMapping("/by-topic/{topicId}")
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getByTopic(@PathVariable Long topicId) {
        Response<List<CreateResponseForumDTO>> resp = forumPostService.getPostsByTopic(topicId);
        return ResponseEntity.status(resp.getCode()).body(resp);
    }

    @PostMapping
    public ResponseEntity<Response<CreateResponseForumDTO>> createForumPost(@Valid @RequestBody CreateRequestForumPostDTO forumPostDTO) {
        Response<CreateResponseForumDTO> resp = forumPostService.createForumPost(forumPostDTO);
        return ResponseEntity.status(resp.getCode()).body(resp);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Response<UpdateResponsePostDTO>> updatePost(@PathVariable Long id, @RequestBody UpdateRequestPostDTO post) {
        Response<UpdateResponsePostDTO> resp = forumPostService.updateForumPost(id, post);
        return ResponseEntity.status(resp.getCode()).body(resp);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<String>> deletePost(@PathVariable Long id) {
        Response<String> resp = forumPostService.deletePost(id);
        return ResponseEntity.status(resp.getCode()).body(resp);
    }

    @DeleteMapping("/posts/{postId}/images/{imageId}")
    public ResponseEntity<Response<String>> deleteImageFromPost(
            @PathVariable Long postId,
            @PathVariable Long imageId
    ) {
        Response<String> response = forumPostService.deleteImageFromPost(postId, imageId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Sort posts by the highest number of comments")
    @GetMapping("/top-replied")
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getTopRepliedPosts(
            @RequestParam(defaultValue = "5") int limit) {
        Response<List<CreateResponseForumDTO>> response = forumPostService.getTopPostsByReplies(limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Response<String>> likeOrUnlikePost(
            @PathVariable Long postId,
            @RequestParam boolean liked
    ) {
        Response<String> response = forumPostService.likeOrUnlikePost(postId, liked);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{postId}/replies")
    public ResponseEntity<Response<List<ForumPostReplyWithUserDTO>>> getReplies(@PathVariable Long postId) {
        List<ForumPostReplyWithUserDTO> replies = forumPostService.getRepliesWithUserByPostId(postId);
        return ResponseEntity.ok(new Response<>(200, "Success", replies));
    }
}
