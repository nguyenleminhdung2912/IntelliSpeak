package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.hr.HRRequestDTO;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
import com.gsu25se05.itellispeak.service.HRService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class HRController {

    private final HRService hrService;

    public HRController(HRService hrService) {
        this.hrService = hrService;
    }

    @PostMapping("/apply")
    public Response<HRResponseDTO> applyHR(@RequestBody @Valid HRRequestDTO request) {
        return hrService.applyHR(request);
    }

    @GetMapping("/application/status")
    public Response<HRResponseDTO> getApplicationStatus() {
        return hrService.checkHRApplicationStatus();
    }
}
