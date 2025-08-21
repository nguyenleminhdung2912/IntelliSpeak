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
        List<Company> companies = companyService.getAllCompanies();
        return ResponseEntity.ok(new Response<>(200, "Get all companies successfully", companies));
    }

    @GetMapping("/{company_id}")
    public ResponseEntity<Response<Company>> getCompanyById(@PathVariable Long company_id) {
        Company companies = companyService.getCompanyById(company_id);
        return ResponseEntity.ok(new Response<>(200, "Get company by id successfully", companies));
    }

    @PostMapping
    public ResponseEntity<Response<Company>> createCompanies(@RequestBody CreateCompanyRequestDTO createCompanyRequestDTO) {
        Company companies = companyService.createCompany(createCompanyRequestDTO);
        return ResponseEntity.ok(new Response<>(200, "Get all companies successfully", companies));
    }

}
