package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.jd.EvaluationRequest;
import com.gsu25se05.itellispeak.dto.jd.JDInputDTO;
import com.gsu25se05.itellispeak.entity.JD;
import com.gsu25se05.itellispeak.service.JDService;
import com.gsu25se05.itellispeak.utils.CooldownManager;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    public ResponseEntity<?> analyzeJD(@RequestBody JDInputDTO input) {
        try {
            JD jd = jdService.analyzeAndSaveJD(input);
            return ResponseEntity.ok(jd);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Lỗi hệ thống: " + e.getMessage()));
        }
    }
}
