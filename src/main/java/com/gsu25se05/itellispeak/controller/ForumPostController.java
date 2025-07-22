package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.category.UpdateCategoryRequest;
import com.gsu25se05.itellispeak.dto.category.UpdateCategoryResponse;
import com.gsu25se05.itellispeak.dto.forum.CreateRequestForumPostDTO;
import com.gsu25se05.itellispeak.dto.forum.CreateResponseForumDTO;
import com.gsu25se05.itellispeak.dto.forum.UpdateRequestPostDTO;
import com.gsu25se05.itellispeak.dto.forum.UpdateResponsePostDTO;
import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.service.ForumPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/forum-post")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ForumPostController {

    @Autowired
    ForumPostService forumPostService;

    @GetMapping
    public List<ForumPost> getAllPosts() {
        return forumPostService.getAllPosts();
    }

    @GetMapping("/{id}")
    public ForumPost getPostById(@PathVariable Long id) {
        return forumPostService.getPostById(id);
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

}
