package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forum.CreateRequestForumPostDTO;
import com.gsu25se05.itellispeak.dto.forum.CreateResponseForumDTO;
import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumPost;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.service.ForumPostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
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
    public Response<CreateResponseForumDTO> createForumPost(@RequestBody CreateRequestForumPostDTO forumPostDTO) {
        return forumPostService.createForumPost(forumPostDTO);
    }

}
