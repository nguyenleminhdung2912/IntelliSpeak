package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.jd.GetAllJdDTO;
import com.gsu25se05.itellispeak.entity.JD;
import com.gsu25se05.itellispeak.service.JDService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jd")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class JDController {

    private final JDService jdService;

    public JDController(JDService jdService) {
        this.jdService = jdService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeJD(@RequestParam("file") MultipartFile file) {
        try {
            JD jd = jdService.analyzeAndSaveJD(file);
            return ResponseEntity.ok(jd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "System error: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getJDById(@PathVariable Long id) {
        try {
            JD jd = jdService.getJDById(id);
            return ResponseEntity.ok(jd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<Response<List<GetAllJdDTO>>> getJDList() {
        try {
            Response<List<GetAllJdDTO>> result = jdService.getAllJDsByUser();

            Response<List<GetAllJdDTO>> response = Response.<List<GetAllJdDTO>>builder()
                    .code(200)
                    .message("The CV list has been retrieved successfully.")
                    .data(result.getData())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Response<List<GetAllJdDTO>> errorResponse = new Response<>(400, "Error: " + e.getMessage(), null);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
