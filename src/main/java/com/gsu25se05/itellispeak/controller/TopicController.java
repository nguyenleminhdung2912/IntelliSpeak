package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.interview_topic.TopicRequest;
import com.gsu25se05.itellispeak.entity.Tag;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.service.TopicService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/topic")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class TopicController {

    @Autowired
    private TopicService topicService;

    @PostMapping
    public ResponseEntity<Topic> createTopic(@Valid @RequestBody TopicRequest topicRequest) {
        Topic createdTopic = topicService.createTopic(topicRequest);
        return new ResponseEntity<>(createdTopic, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Topic>> getAllTopics() {
        List<Topic> topics = topicService.getAllTopics();
        return ResponseEntity.ok(topics);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Topic> getTopicById(@PathVariable Long id) {
        Topic topic = topicService.getTopicById(id);
        return ResponseEntity.ok(topic);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topic> updateTopic(@PathVariable Long id, @Valid @RequestBody TopicRequest topicRequest) {
        Topic updatedTopic = topicService.updateTopic(id, topicRequest);
        return ResponseEntity.ok(updatedTopic);
    }

    @PutMapping("/thumbnail/{id}")
    public ResponseEntity<Topic> updateTopicThumbnail(@PathVariable Long id, @RequestBody String thumbnailURL) {
        Topic updatedTopic = topicService.updateTopicThumbnail(id, thumbnailURL);
        return ResponseEntity.ok(updatedTopic);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{topicId}/tags/{tagId}")
    public ResponseEntity<Topic> addTagToTopic(@PathVariable Long topicId, @PathVariable Long tagId) {
        Topic updated = topicService.addTopicToTag(topicId, tagId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{topicId}/tags/{tagId}")
    public ResponseEntity<Topic> removeTagFromTopic(@PathVariable Long topicId, @PathVariable Long tagId) {
        Topic updated = topicService.removeTopicFromTag(topicId, tagId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{topicId}/tags")
    public ResponseEntity<Set<Tag>> getTagsOfTopic(@PathVariable Long topicId) {
        Set<Tag> tags = topicService.getTagsOfTopic(topicId);
        return ResponseEntity.ok(tags);
    }
}
