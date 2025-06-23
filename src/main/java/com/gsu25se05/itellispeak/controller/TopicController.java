package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.interview_topic.TopicRequest;
import com.gsu25se05.itellispeak.entity.Topic;
import com.gsu25se05.itellispeak.service.TopicService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTopic(@PathVariable Long id) {
        topicService.deleteTopic(id);
        return ResponseEntity.noContent().build();
    }
}
