package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.service.QuestionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    @GetMapping("/my-questions")
    public ResponseEntity<?> getMyQuestions() {
        return ResponseEntity.ok(questionService.getByCurrentUser());
    }

    @PostMapping("/import-csv")
    public Response<List<QuestionDTO>> importQuestionsFromCsv(@RequestParam("file") MultipartFile file) {
        return questionService.importFromCsv(file);
    }



}
