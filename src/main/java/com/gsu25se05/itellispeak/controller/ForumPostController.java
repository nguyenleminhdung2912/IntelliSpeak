package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.category.UpdateCategoryResponse;
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

    public CreateResponseForumDTO mapToDTO(ForumPost post) {
        String email = post.getUser().getEmail();
        String username = email.substring(0, email.indexOf('@'));

        List<String> imageUrls = post.getPictures().stream()
                .filter(p -> !Boolean.TRUE.equals(p.isDeleted()))
                .map(ForumPostPicture::getUrl)
                .collect(Collectors.toList());

        int readTime = (post.getContent() == null) ? 1 : Math.max(1, post.getContent().length() / 500);

        return new CreateResponseForumDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                imageUrls,
                username,
                post.getForumTopicType(),
                post.getCreateAt(),
                post.getLikeCount(),
                readTime
        );
    }


    @GetMapping()
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getAllForumPosts() {
        List<ForumPost> posts = forumPostRepository.findByIsDeletedFalse();
        List<CreateResponseForumDTO> response = posts.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new Response<>(200, "Success", response));
    }


    @GetMapping("/{id}")
    public ResponseEntity<Response<CreateResponseForumDTO>> getForumPostById(@PathVariable Long id) {
        ForumPost post = forumPostRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết với ID: " + id));
        return ResponseEntity.ok(new Response<>(200, "Success", mapToDTO(post)));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getMyPosts() {
        return ResponseEntity.ok(forumPostService.getMyPosts());
    }


    @PostMapping
    public Response<CreateResponseForumDTO> createForumPost(@Valid @RequestBody CreateRequestForumPostDTO forumPostDTO) {
        return forumPostService.createForumPost(forumPostDTO);
    }

    @PutMapping("/{id}")
    public Response<UpdateResponsePostDTO> updatePost(@PathVariable Long id, @RequestBody UpdateRequestPostDTO post) {
        return forumPostService.updateForumPost(id, post);
    }

    @DeleteMapping("/{id}")
    public Response<String> deletePost(@PathVariable Long id) {
        return forumPostService.deletePost(id);
    }

    @DeleteMapping("/posts/{postId}/images/{imageId}")
    @SecurityRequirement(name = "api")
    public ResponseEntity<Response<String>> deleteImageFromPost(
            @PathVariable Long postId,
            @PathVariable Long imageId
    ) {
        Response<String> response = forumPostService.deleteImageFromPost(postId, imageId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Sắp xếp bài viết có lượt comment từ cao xuống thấp")
    @GetMapping("/top-replied")
    public ResponseEntity<Response<List<ForumPost>>> getTopRepliedPosts(
            @RequestParam(defaultValue = "5") int limit) {
        Response<List<ForumPost>> response = forumPostService.getTopPostsByReplies(limit);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<Response<String>> likeOrUnlikePost(
            @PathVariable Long postId,
            @RequestParam boolean liked
    ) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null));
        }

        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài viết"));

        if (post.getLikeCount() == null) post.setLikeCount(0);

        if (liked) {
            post.setLikeCount(post.getLikeCount() + 1);
        } else {
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        }

        forumPostRepository.save(post);

        return ResponseEntity.ok(new Response<>(200, "Cập nhật like thành công", "Tổng số like: " + post.getLikeCount()));
    }

    @GetMapping("/{postId}/replies")
    public ResponseEntity<Response<List<ForumPostReplyWithUserDTO>>> getReplies(@PathVariable Long postId) {
        List<ForumPostReplyWithUserDTO> replies = forumPostService.getRepliesWithUserByPostId(postId);
        return ResponseEntity.ok(new Response<>(200, "Success", replies));
    }

}
