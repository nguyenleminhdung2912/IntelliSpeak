package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.company.CreateCompanyRequestDTO;
import com.gsu25se05.itellispeak.entity.Company;
import com.gsu25se05.itellispeak.service.CompanyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/company")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping
    public ResponseEntity<Response<List<Company>>> getAllCompanies() {
        try {
            List<Company> companies = companyService.getAllCompanies();
            return ResponseEntity.ok(new Response<>(200, "Get all companies successfully", companies));
        } catch (Exception e) {
            return ResponseEntity.ok(new Response<>(500, "Something went wrong, please try again!", null));
        }
    }

    @PostMapping
    public ResponseEntity<Response<Company>> createCompanies(CreateCompanyRequestDTO createCompanyRequestDTO) {
        try {
            Company companies = companyService.createCompany(createCompanyRequestDTO);
            return ResponseEntity.ok(new Response<>(200, "Get all companies successfully", companies));
        } catch (Exception e) {
            return ResponseEntity.ok(new Response<>(500, "Something went wrong, please try again!", null));
        }
    }

}
