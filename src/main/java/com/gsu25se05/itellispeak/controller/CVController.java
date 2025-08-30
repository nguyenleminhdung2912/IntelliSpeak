package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.cv.*;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.entity.CVEvaluate;
import com.gsu25se05.itellispeak.service.CVService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/cv")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class CVController {
    private final CVService cvService;

    public CVController(CVService cvService) {
        this.cvService = cvService;
    }

    @PostMapping("/upload/{cvTitle}")
    public ResponseEntity<Response<CVAnalysisResponseDTO>> uploadCV(@PathVariable("cvTitle") String cvTitle,
                                                                    @RequestParam("file") MultipartFile file) {
        try {
            Response<CVAnalysisResponseDTO> response = cvService.analyzeAndSaveFromFile(cvTitle, file);
            return ResponseEntity.status(response.getCode() == 200 ? 200 : 400).body(response);
        } catch (Exception e) {
            Response<CVAnalysisResponseDTO> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<Response<CVEvaluateResponseDTO>> getCV(@PathVariable Long id) {
        try {
            Response<CVEvaluateResponseDTO> response = cvService.getCV(id);

            HttpStatus status = switch (response.getCode()) {
                case 200 -> HttpStatus.OK;
                case 401 -> HttpStatus.UNAUTHORIZED;
                case 404 -> HttpStatus.NOT_FOUND;
                default -> HttpStatus.BAD_REQUEST;
            };

            return ResponseEntity.status(status).body(response);
        } catch (Exception e) {
            Response<CVEvaluateResponseDTO> errorResponse =
                    new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Response<List<GetAllCvDTO>>> getAllCVs() {
        try {
            Response<List<GetAllCvDTO>> result = cvService.getAllCvDTOsByUser();

            Response<List<GetAllCvDTO>> response = Response.<List<GetAllCvDTO>>builder()
                    .code(200)
                    .message("List of CVs successfully retrieved!")
                    .data(result.getData())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<GetAllCvDTO>> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/submit-for-company")
    public ResponseEntity<Response<String>> submitCvToCompany(
            @RequestParam Long companyId) {
        try {
            // Assume you have a method to get the current user
            String result = cvService.submitCvToCompany(companyId);
            return ResponseEntity.ok(new Response<>(200, "CV submitted successfully!", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/candidate/view-submitted-cv")
    @Operation(summary = "Người dùng xem danh sách CV đã nộp, isViewed = null là công ty đó chưa xem, isViewed = false là công ty từ chối, isViewed = true là công ty chấp nhận và sẽ liên lạc sớm")
    public ResponseEntity<Response<List<CandidateSubmittedCvDTO>>> candidateViewSubmittedCV() {
        try {
            List<CandidateSubmittedCvDTO> dtos = cvService.getSubmittedCvsForCurrentUser();
            return ResponseEntity.ok(new Response<>(200, "Submitted CVs fetched successfully!", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/hr/view-submitted-cv")
    @Operation(summary = "HR xem danh sách CV đã nộp, isViewed = null là công ty đó chưa xem, isViewed = false là công ty đã từ chối, isViewed = true là công ty đã chấp nhận và sẽ liên lạc sớm")
    public ResponseEntity<Response<List<HRViewSubmittedCvDTO>>> hrViewSubmittedCV() {
        try {
            List<HRViewSubmittedCvDTO> dtos = cvService.getSubmittedCvsForCompany();
            return ResponseEntity.ok(new Response<>(200, "Submitted CVs fetched successfully!", dtos));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response<>(400, "Error: " + e.getMessage(), null));
        }
    }

    @PutMapping("/hr/submission/{submissionId}/approve")
    @Operation(summary = "HR duyệt CV của ứng viên (đặt isViewed = true)")
    public ResponseEntity<Response<Void>> approveCvSubmission(@PathVariable Long submissionId) {
        cvService.approveCvSubmission(submissionId);
        return ResponseEntity.ok(new Response<>(200, "CV submission approved successfully.", null));
    }

    @PutMapping("/hr/submission/{submissionId}/reject")
    @Operation(summary = "HR từ chối CV của ứng viên (đặt isViewed = false)")
    public ResponseEntity<Response<Void>> rejectCvSubmission(@PathVariable Long submissionId) {
        cvService.rejectCvSubmission(submissionId);
        return ResponseEntity.ok(new Response<>(200, "CV submission rejected successfully.", null));
    }

    @PutMapping("/{cvId}/set-active")
    @Operation(summary = "Người dùng đặt một CV làm CV chính (active), các CV khác sẽ bị vô hiệu hóa")
    public ResponseEntity<Response<Void>> setActiveCv(@PathVariable Long cvId) {
        cvService.setActiveCv(cvId);
        return ResponseEntity.ok(new Response<>(200, "CV has been set as active successfully.", null));
    }
}
