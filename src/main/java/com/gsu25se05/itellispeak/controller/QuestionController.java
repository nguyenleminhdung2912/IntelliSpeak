package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.question.QuestionDTO;
import com.gsu25se05.itellispeak.dto.question.UpdateQuestionDTO;
import com.gsu25se05.itellispeak.entity.Question;
import com.gsu25se05.itellispeak.service.QuestionService;
import io.swagger.v3.oas.annotations.Operation;
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

    @PostMapping("/import-csv/{tagId}")
    public Response<List<QuestionDTO>> importQuestionsFromCsv(@RequestParam("file") MultipartFile file, @PathVariable Long tagId) {
        return questionService.importFromCsv(file, tagId);
    }

    @PostMapping("/{sessionId}/questions/{questionId}/remove")
    @Operation(summary = "Remove a question from an interview session")
    public ResponseEntity<Response<Void>> removeQuestion(
            @PathVariable Long sessionId,
            @PathVariable Long questionId) {
        questionService.removeQuestionFromSession(sessionId, questionId);
        return ResponseEntity.ok(new Response<>(200, "Question removed from session", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a question (soft delete)")
    public ResponseEntity<Response<Void>> delete(@PathVariable Long id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(new Response<>(200, "Question deleted successfully", null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a question")
    public ResponseEntity<Response<QuestionDTO>> update(@PathVariable Long id, @RequestBody UpdateQuestionDTO dto) {
        QuestionDTO questionDTO = questionService.updateQuestion(id, dto);
        Response<QuestionDTO> response = new Response<>(200, "Question updated successfully", questionDTO);
        return ResponseEntity.ok(response);
    }

}
