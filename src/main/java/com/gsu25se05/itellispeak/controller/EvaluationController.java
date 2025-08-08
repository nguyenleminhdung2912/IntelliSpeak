package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.ai_evaluation.EvaluationBatchResponseDto;
import com.gsu25se05.itellispeak.dto.ai_evaluation.EvaluationRequestDto;
import com.gsu25se05.itellispeak.dto.question.CompareRequestDTO;
import com.gsu25se05.itellispeak.service.EvaluationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/answer_compare")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class EvaluationController {

    private final RestTemplate restTemplate = new RestTemplate();

    private final EvaluationService evaluationService;

    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping("/evaluate-batch")
    public ResponseEntity<EvaluationBatchResponseDto> evaluateBatch(@RequestBody EvaluationRequestDto request) {
        EvaluationBatchResponseDto results = evaluationService.evaluateBatch(request);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<?> compareAnswer(@RequestBody CompareRequestDTO request) {
        String pythonApiUrl = "http://localhost:8000/similarity"
                + "?sentence1=" + encode(request.getSentence1())
                + "&sentence2=" + encode(request.getSentence2());

        ResponseEntity<String> response = restTemplate.getForEntity(pythonApiUrl, String.class);
        return ResponseEntity.ok(response.getBody());
    }

    private String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            return value;
        }
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

