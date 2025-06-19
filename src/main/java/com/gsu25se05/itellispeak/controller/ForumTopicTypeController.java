package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.topic.TopicRequest;
import com.gsu25se05.itellispeak.dto.topic.TopicResponse;
import com.gsu25se05.itellispeak.dto.topic.UpdateTopicRequest;
import com.gsu25se05.itellispeak.dto.topic.UpdateTopicResponse;
import com.gsu25se05.itellispeak.entity.ForumTopicType;
import com.gsu25se05.itellispeak.service.ForumTopicTypeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topic-type")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class ForumTopicTypeController {
    @Autowired
    private ForumTopicTypeService topicTypeService;

    @GetMapping("/{id}")
    public ForumTopicType getTopicTypeById(@PathVariable Long id) {
        return topicTypeService.getTopicTypeById(id);
    }

    @PostMapping
    public Response<TopicResponse> createTopicType(@RequestBody TopicRequest topicType) {
        return topicTypeService.createTopicType(topicType);
    }

    @PutMapping("/{id}")
    public Response<UpdateTopicResponse> updateTopicType(@PathVariable Long id, @RequestBody UpdateTopicRequest topicType) {
        return topicTypeService.updateTopicType(id, topicType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopicType(@PathVariable Long id) {
        topicTypeService.deleteTopicType(id);
        return ResponseEntity.noContent().build();
    }
}
