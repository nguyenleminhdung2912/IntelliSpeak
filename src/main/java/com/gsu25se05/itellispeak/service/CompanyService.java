package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.ai_evaluation.InterviewSessionDto;
import com.gsu25se05.itellispeak.dto.company.CreateCompanyRequestDTO;
import com.gsu25se05.itellispeak.dto.company.GetCompanyDetailResponseDTO;
import com.gsu25se05.itellispeak.dto.company.InterviewSessionUserDto;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
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

    public GetCompanyDetailResponseDTO getCompanyDetailById(Long companyId) {
        Company company = companyRepository.findById(companyId).orElse(null);
        if (company == null) return null;

        GetCompanyDetailResponseDTO dto = new GetCompanyDetailResponseDTO();
        dto.setCompanyId(company.getCompanyId());
        dto.setName(company.getName());
        dto.setShortName(company.getShortName());
        dto.setDescription(company.getDescription());
        dto.setLogoUrl(company.getLogoUrl());
        dto.setWebsite(company.getWebsite());
        dto.setCreateAt(company.getCreateAt());
        dto.setUpdateAt(company.getUpdateAt());
        dto.setIsDeleted(company.getIsDeleted());

        // Map HRs
        List<HRResponseDTO> hrDTOs = company.getHrList().stream().map(hr -> {
            HRResponseDTO hrDto = new HRResponseDTO();
            hrDto.setHrId(hr.getHrId());
            hrDto.setCompany(company.getName());
            hrDto.setPhone(hr.getPhone());
            hrDto.setCountry(hr.getCountry());
            hrDto.setExperienceYears(hr.getExperienceYears());
            hrDto.setLinkedinUrl(hr.getLinkedinUrl());
            hrDto.setCvUrl(hr.getCvUrl());
            hrDto.setSubmittedAt(hr.getSubmittedAt());
            hrDto.setHrStatus(hr.getStatus() != null ? hr.getStatus().name() : null);
            return hrDto;
        }).toList();
        dto.setHrList(hrDTOs);

        // Map InterviewSessions
        List<InterviewSessionDto> sessionDTOs = company.getInterviewSessions().stream().map(session -> {
            InterviewSessionUserDto createdByDto = null;
            if (session.getCreatedBy() != null) {
                createdByDto = new InterviewSessionUserDto(
                        session.getCreatedBy().getUserId(),
                        session.getCreatedBy().getFirstName() + " " + session.getCreatedBy().getLastName(),
                        session.getCreatedBy().getAvatar()
                );
            }
            InterviewSessionDto sessionDto = new InterviewSessionDto();
            sessionDto.setInterviewSessionId(session.getInterviewSessionId());
            sessionDto.setTitle(session.getTitle());
            sessionDto.setDescription(session.getDescription());
            sessionDto.setTotalQuestion(session.getTotalQuestion());
            sessionDto.setDurationEstimate(session.getDurationEstimate() != null ? session.getDurationEstimate().toString() : null);
            sessionDto.setCreatedBy(createdByDto);
            return sessionDto;
        }).toList();
        dto.setInterviewTemplateList(sessionDTOs);

        return dto;
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
