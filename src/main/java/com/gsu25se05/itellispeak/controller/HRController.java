package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.hr.HRRequestDTO;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
import com.gsu25se05.itellispeak.dto.interview_session.InterviewSessionDTO;
import com.gsu25se05.itellispeak.entity.InterviewSession;
import com.gsu25se05.itellispeak.service.HRService;
import com.gsu25se05.itellispeak.service.InterviewSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class HRController {

    private final HRService hrService;
    private final InterviewSessionService interviewSessionService;

    public HRController(HRService hrService, InterviewSessionService interviewSessionService) {
        this.hrService = hrService;
        this.interviewSessionService = interviewSessionService;
    }

    @PostMapping("/apply")
    public Response<HRResponseDTO> applyHR(@RequestBody @Valid HRRequestDTO request) {
        return hrService.applyHR(request);
    }

    @GetMapping("/application/status")
    public Response<HRResponseDTO> getApplicationStatus() {
        return hrService.checkHRApplicationStatus();
    }

    @PostMapping("/create")
    @Operation(summary = "Tạo interview session")
    public ResponseEntity<Response<InterviewSession>> create(@RequestBody InterviewSessionDTO interviewSessionDTO) {
        InterviewSession session = interviewSessionService.saveFromHR(interviewSessionDTO);
        return ResponseEntity.ok(new Response<>(200, "Interview session created", session));
    }
}
