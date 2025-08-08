package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.ai_evaluation.EvaluationBatchResponseDto;
import com.gsu25se05.itellispeak.service.InterviewHistoryService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
@CrossOrigin("**")
@SecurityRequirement(name = "api")
@RequestMapping("/interview-history")
public class InterviewSessionHistoryController {

    private final InterviewHistoryService interviewHistoryService;

    public InterviewSessionHistoryController(InterviewHistoryService interviewHistoryService) {
        this.interviewHistoryService = interviewHistoryService;
    }

    @GetMapping
    public ResponseEntity<List<EvaluationBatchResponseDto>> getAllInterviewHistories() {
        try {
            List<EvaluationBatchResponseDto> histories = interviewHistoryService.getAllInterviewHistories();
            return ResponseEntity.ok(histories);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(null);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EvaluationBatchResponseDto> getInterviewHistoryById(@PathVariable Long id) {
        try {
            EvaluationBatchResponseDto history = interviewHistoryService.getInterviewHistoryById(id);
            return ResponseEntity.ok(history);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(null);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(null);
        }
    }
}
