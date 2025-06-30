package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.forumtopic.ForumTopicRequest;
import com.gsu25se05.itellispeak.dto.forumtopic.ForumTopicResponse;
import com.gsu25se05.itellispeak.dto.forumtopic.UpdateForumTopicRequest;
import com.gsu25se05.itellispeak.dto.forumtopic.UpdateForumTopicResponse;
import com.gsu25se05.itellispeak.entity.ForumCategory;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.service.ForumTopicTypeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/topic-type")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ForumTopicTypeController {
    @Autowired
    private ForumTopicTypeService topicTypeService;

    @GetMapping
    public List<ForumTopicType> getAllTopicTypes() {
        return topicTypeService.getAllTopicTypes();
    }

    @GetMapping("/{id}")
    public ForumTopicType getTopicTypeById(@PathVariable Long id) {
        return topicTypeService.getTopicTypeById(id);
    }

    @PostMapping
    public Response<ForumTopicResponse> createTopicType(@RequestBody ForumTopicRequest topicType) {
        return topicTypeService.createTopicType(topicType);
    }

    @PutMapping("/{id}")
    public Response<UpdateForumTopicResponse> updateTopicType(@PathVariable Long id, @RequestBody UpdateForumTopicRequest topicType) {
        return topicTypeService.updateTopicType(id, topicType);
    }

    @DeleteMapping("/{id}")
    public Response<String> deleteTopicType(@PathVariable Long id) {
        return topicTypeService.deleteTopicType(id);
    }
}
