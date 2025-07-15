package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackDTO;
import com.gsu25se05.itellispeak.service.WebsiteFeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/website-feedback")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class WebsiteFeedbackController {

    private final WebsiteFeedbackService websiteFeedbackService;

    public WebsiteFeedbackController(WebsiteFeedbackService websiteFeedbackService) {
        this.websiteFeedbackService = websiteFeedbackService;
    }

    @PostMapping
    public ResponseEntity<Response<WebsiteFeedbackDTO>> createWebsiteFeedback(@RequestBody WebsiteFeedbackDTO websiteFeedbackDTO) {
        WebsiteFeedbackDTO createdFeedback = websiteFeedbackService.createWebsiteFeedback(websiteFeedbackDTO);
        return ResponseEntity.ok(new Response<>(200, "Website feedback created successfully", createdFeedback));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<WebsiteFeedbackDTO>> getWebsiteFeedbackById(@PathVariable UUID id) {
        Optional<WebsiteFeedbackDTO> websiteFeedbackDTO = websiteFeedbackService.getWebsiteFeedbackById(id);
        return websiteFeedbackDTO.map(dto -> ResponseEntity.ok(new Response<>(200, "Website feedback fetched successfully", dto)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(404, "Website feedback not found", null)));
    }

    @GetMapping
    public ResponseEntity<Response<List<WebsiteFeedbackDTO>>> getAllWebsiteFeedbacks() {
        List<WebsiteFeedbackDTO> websiteFeedbacks = websiteFeedbackService.getAllWebsiteFeedbacks();
        return ResponseEntity.ok(new Response<>(200, "Website feedbacks fetched successfully", websiteFeedbacks));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteWebsiteFeedback(@PathVariable UUID id) {
        websiteFeedbackService.deleteWebsiteFeedback(id);
        return ResponseEntity.ok(new Response<>(200, "Website feedback deleted successfully", null));
    }
}
