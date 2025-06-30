package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.service.QuestionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/question")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class QuestionController {
    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionDTO> create(@RequestBody QuestionDTO dto) {
        return ResponseEntity.ok(questionService.save(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionDTO> getById(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<QuestionDTO>> getAll() {
        return ResponseEntity.ok(questionService.findAll());
    }
}
