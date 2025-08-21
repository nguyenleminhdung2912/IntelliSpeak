package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
import com.gsu25se05.itellispeak.dto.hr.HRRequestDTO;
import com.gsu25se05.itellispeak.entity.Company;
import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.HRStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.repository.CompanyRepository;
import com.gsu25se05.itellispeak.repository.HRRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class HRService {
    @Autowired
    private AccountUtils accountUtils;

    @Autowired
    private HRRepository hrRepository;
    @Autowired
    private CompanyRepository companyRepository;

    public Response<HRResponseDTO> applyHR(HRRequestDTO request) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Please log in to continue.", null);
        }

        // Kiểm tra nếu user đã gửi yêu cầu HR
        if (hrRepository.findByUser(user).isPresent()) {
            throw new AuthAppException(ErrorCode.HR_ALREADY_APPLIED);
        }

        Company company = new Company();
        if (request.getCompanyId() == null) {
            company = new Company();
            company.setName(request.getCompanyNameIfNotExist());
            company.setDescription("");
            company.setWebsite("");
            company.setLogoUrl("");
            company.setCreateAt(LocalDateTime.now());
            company.setUpdateAt(LocalDateTime.now());
            company.setIsDeleted(false);
            company = companyRepository.save(company);
        } else {
            company = companyRepository.findById(company.getCompanyId()).orElse(null);
            if (company == null) {
                return new Response<>(500, "The company with this ID does not exist, please check again!", null);
            }
        }


        HR hrRequest = new HR();
        hrRequest.setUser(user);
        hrRequest.setCompany(company);
        hrRequest.setCompany(null);
        hrRequest.setPhone(request.getPhone());
        hrRequest.setCountry(request.getCountry());
        hrRequest.setExperienceYears(request.getExperienceYears());
        hrRequest.setLinkedinUrl(request.getLinkedinUrl());
        hrRequest.setCvUrl(request.getCvUrl());
        hrRequest.setStatus(HRStatus.PENDING);
        hrRequest.setSubmittedAt(LocalDateTime.now());

        HR saved = hrRepository.save(hrRequest);

        HRResponseDTO responseDTO = new HRResponseDTO(
                saved.getHrId(),
                saved.getCompany().getName(),
                saved.getPhone(),
                saved.getCountry(),
                saved.getExperienceYears(),
                saved.getLinkedinUrl(),
                saved.getCvUrl(),
                saved.getSubmittedAt(),
                saved.getStatus().name()
        );
        return new Response<>(200, "HR application submitted successfully", responseDTO);
    }

    public Response<HRResponseDTO> checkHRApplicationStatus() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Please log in to continue", null);
        }

        HR hrApplication = hrRepository.findByUser(user)
                .orElseThrow(() -> new AuthAppException(ErrorCode.HR_NOT_FOUND));

        HRResponseDTO responseDTO = new HRResponseDTO(
                hrApplication.getHrId(),
                hrApplication.getCompany().getName(),
                hrApplication.getPhone(),
                hrApplication.getCountry(),
                hrApplication.getExperienceYears(),
                hrApplication.getLinkedinUrl(),
                hrApplication.getCvUrl(),
                hrApplication.getSubmittedAt(),
                hrApplication.getStatus().name()
        );

        return new Response<>(200, "Get HR application status successfully", responseDTO);
    }
}
