package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.company.CreateCompanyRequestDTO;
import com.gsu25se05.itellispeak.entity.Company;
import com.gsu25se05.itellispeak.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id).orElse(null);
    }

    public Company createCompany(CreateCompanyRequestDTO createCompanyRequestDTO) {
        Company company = Company.builder()
                .name(createCompanyRequestDTO.getName())
                .shortName(createCompanyRequestDTO.getShortName())
                .description(createCompanyRequestDTO.getDescription())
                .logoUrl(createCompanyRequestDTO.getLogoUrl())
                .website(createCompanyRequestDTO.getWebsite())
                .createAt(LocalDateTime.now())
                .updateAt(LocalDateTime.now())
                .isDeleted(false)
                .build();
        company = companyRepository.save(company);
        return company;
    }

    public Company updateCompany(Long id, Company updatedCompany) {
        return companyRepository.findById(id)
                .map(company -> {
                    company.setName(updatedCompany.getName());
                    company.setDescription(updatedCompany.getDescription());
                    // set other fields as needed
                    return companyRepository.save(company);
                })
                .orElseThrow(() -> new EntityNotFoundException("Company not found"));
    }

    public void deleteCompany(Long id) {
        companyRepository.deleteById(id);
    }
}
