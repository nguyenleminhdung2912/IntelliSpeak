package com.gsu25se05.itellispeak.controller;

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
    public ForumTopicType createTopicType(@RequestBody ForumTopicType topicType) {
        return topicTypeService.createTopicType(topicType);
    }

    @PutMapping("/{id}")
    public ForumTopicType updateTopicType(@PathVariable Long id, @RequestBody ForumTopicType topicType) {
        return topicTypeService.updateTopicType(id, topicType);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopicType(@PathVariable Long id) {
        topicTypeService.deleteTopicType(id);
        return ResponseEntity.noContent().build();
    }
}
