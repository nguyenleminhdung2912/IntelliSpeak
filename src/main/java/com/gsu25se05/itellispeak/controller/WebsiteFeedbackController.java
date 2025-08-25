package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackRequestDTO;
import com.gsu25se05.itellispeak.dto.website_feedback.WebsiteFeedbackResponseDTO;
import com.gsu25se05.itellispeak.service.WebsiteFeedbackService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity<Response<WebsiteFeedbackResponseDTO>> createWebsiteFeedback(@RequestBody WebsiteFeedbackRequestDTO websiteFeedbackRequestDTO) {
        Response<WebsiteFeedbackResponseDTO> createdFeedback = websiteFeedbackService.createWebsiteFeedback(websiteFeedbackRequestDTO);
        return ResponseEntity.ok(createdFeedback);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Response<WebsiteFeedbackResponseDTO>> getWebsiteFeedbackById(@PathVariable Long id) {
        Optional<WebsiteFeedbackResponseDTO> websiteFeedbackDTO = websiteFeedbackService.getWebsiteFeedbackById(id);
        return websiteFeedbackDTO.map(dto -> ResponseEntity.ok(new Response<>(200, "Website feedback fetched successfully", dto)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new Response<>(404, "Website feedback not found", null)));
    }

    @GetMapping("/user")
    public ResponseEntity<Response<List<WebsiteFeedbackResponseDTO>>> getWebsiteFeedbackByUser() {
        Response<List<WebsiteFeedbackResponseDTO>> websiteFeedbackDTO = websiteFeedbackService.getWebsiteFeedbackByUser();
        return ResponseEntity.ok(websiteFeedbackDTO);
    }

    @GetMapping
    public ResponseEntity<Response<List<WebsiteFeedbackResponseDTO>>> getAllWebsiteFeedbacks() {
        List<WebsiteFeedbackResponseDTO> websiteFeedbacks = websiteFeedbackService.getAllWebsiteFeedbacks();
        return ResponseEntity.ok(new Response<>(200, "Website feedbacks fetched successfully", websiteFeedbacks));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Response<Void>> deleteWebsiteFeedback(@PathVariable Long id) {
        websiteFeedbackService.deleteWebsiteFeedback(id);
        return ResponseEntity.ok(new Response<>(200, "Website feedback deleted successfully", null));
    }

    @PostMapping("/handle-reject/{websiteFeedbackId}")
    public ResponseEntity<Response<String>> handleRejectWebsiteFeedback(@PathVariable Long websiteFeedbackId, @RequestParam String reason) {
        String response = websiteFeedbackService.handleRejectWebsiteFeedback(websiteFeedbackId, reason);
        return ResponseEntity.ok(new Response<>(200, response, null));
    }

    @PostMapping("/handle-approve/{websiteFeedbackId}")
    public ResponseEntity<Response<String>> handleApproveWebsiteFeedback(@PathVariable Long websiteFeedbackId) {
        String response = websiteFeedbackService.handleApproveWebsiteFeedback(websiteFeedbackId);
        return ResponseEntity.ok(new Response<>(200, response, null));
    }
}
