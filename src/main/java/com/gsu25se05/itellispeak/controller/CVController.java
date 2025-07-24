package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.cv.CVAnalysisResponseDTO;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.cv.GetAllCvDTO;
import com.gsu25se05.itellispeak.entity.CVEvaluate;
import com.gsu25se05.itellispeak.service.CVService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
            Response<CVAnalysisResponseDTO> response = cvService.analyzeAndSaveFromFile(cvTitle ,file);
            return ResponseEntity.status(response.getCode() == 200 ? 200 : 400).body(response);
        } catch (Exception e) {
            Response<CVAnalysisResponseDTO> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("{id}")
    public ResponseEntity<Response<CVEvaluate>> getCV(@PathVariable Long id) {
        try {
            Response<CVEvaluate> response = cvService.getCV(id);
            return ResponseEntity.status(response.getCode() == 200 ? 200 : 400).body(response);
        } catch (Exception e) {
            Response<CVEvaluate> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Response<List<GetAllCvDTO>>> getAllCVs() {
        try {
            Response<List<GetAllCvDTO>> result = cvService.getAllCvDTOsByUser();

            Response<List<GetAllCvDTO>> response = Response.<List<GetAllCvDTO>>builder()
                    .code(200)
                    .message("Danh sách CV đã được lấy thành công")
                    .data(result.getData())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<GetAllCvDTO>> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
