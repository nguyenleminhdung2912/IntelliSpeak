package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.category.CategoryRequest;
import com.gsu25se05.itellispeak.dto.category.CategoryResponse;
import com.gsu25se05.itellispeak.dto.reply.CreateReplyPostRequestDTO;
import com.gsu25se05.itellispeak.dto.reply.CreateReplyPostResponseDTO;
import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumPostReply;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.service.ForumPostReplyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reply")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ForumPostReplyController {

    @Autowired
    ForumPostReplyService forumPostReplyService;

    @GetMapping
    public List<ForumPostReply> getAllReplies() {
        return forumPostReplyService.getAllReplies();
    }

    @GetMapping("/{id}")
    public ForumPostReply getReplyById(@PathVariable Long id) {
        return forumPostReplyService.getReplyById(id);
    }

    @PostMapping
    public Response<CreateReplyPostResponseDTO> createReply(@Valid @RequestBody CreateReplyPostRequestDTO reply) {
        return forumPostReplyService.createReply(reply);
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteReply(@PathVariable Long id) {
        return forumPostReplyService.deleteReply(id);
    }
}
