package com.gsu25se05.itellispeak.controller;


import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.hr.HRRequestDTO;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
import com.gsu25se05.itellispeak.service.HRService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hr")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class HRController {
    @Autowired
    private HRService hrService;

    @PostMapping("/apply")
    public Response<HRResponseDTO> applyHR(@RequestBody @Valid HRRequestDTO request) {
        return hrService.applyHR(request);
    }
}
