package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.company.CreateCompanyRequestDTO;
import com.gsu25se05.itellispeak.dto.company.GetCompanyDetailResponseDTO;
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
    public ResponseEntity<Response<GetCompanyDetailResponseDTO>> getCompanyById(@PathVariable Long company_id) {
        GetCompanyDetailResponseDTO companyDetail = companyService.getCompanyDetailById(company_id);
        if (companyDetail == null) {
            return ResponseEntity.status(404).body(new Response<>(404, "Company not found", null));
        }
        return ResponseEntity.ok(new Response<>(200, "Get company by id successfully", companyDetail));
    }

    @PostMapping
    public ResponseEntity<Response<Company>> createCompanies(@RequestBody CreateCompanyRequestDTO createCompanyRequestDTO) {
        Company companies = companyService.createCompany(createCompanyRequestDTO);
        return ResponseEntity.ok(new Response<>(200, "Get all companies successfully", companies));
    }

}
