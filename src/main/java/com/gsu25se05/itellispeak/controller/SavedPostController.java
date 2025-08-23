package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.CreateResponseForumDTO;
import com.gsu25se05.itellispeak.dto.savedPost.ToggleSaveDTO;
import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.service.ForumPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/saved-post")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class SavedPostController {

    @Autowired
    ForumPostService forumPostService;

    @Operation(summary = "Lưu bài viết")
    @PostMapping("/{postId}")
    public ResponseEntity<Response<ToggleSaveDTO>> savePost(@PathVariable Long postId) {
        Response<ToggleSaveDTO> response = forumPostService.savePost(postId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Bỏ lưu bài viết")
    @DeleteMapping("/{postId}")
    public ResponseEntity<Response<ToggleSaveDTO>> unSavePost(@PathVariable Long postId) {
        Response<ToggleSaveDTO> response = forumPostService.unSavePost(postId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Lấy danh sách bài viết đã lưu")
    @GetMapping
    public ResponseEntity<Response<List<CreateResponseForumDTO>>> getSavedPosts() {
        Response<List<CreateResponseForumDTO>> response = forumPostService.getSavedPosts();
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
