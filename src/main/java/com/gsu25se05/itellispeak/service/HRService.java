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
import java.util.Optional;

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

        Optional<HR> existingOpt = hrRepository.findByUser(user);

        Company company;
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
            company = companyRepository.findById(request.getCompanyId()).orElse(null);
            if (company == null) {
                return new Response<>(500, "The company with this ID does not exist, please check again!", null);
            }
        }

        HR saved;

        if (existingOpt.isPresent()) {
            HR existing = existingOpt.get();
            HRStatus status = existing.getStatus();

            if (status == HRStatus.PENDING) {
                // Đang chờ duyệt -> không cho nộp lại
                throw new AuthAppException(ErrorCode.HR_ALREADY_APPLIED);
            } else if (status == HRStatus.APPROVED) {
                // Đã được duyệt -> không cần nộp
                return new Response<>(400, "Your HR application has already been approved.", null);
            } else if (status == HRStatus.REJECTED) {
                // Bị từ chối -> cho phép nộp lại bằng cách cập nhật hồ sơ cũ
                existing.setCompany(company);
                existing.setPhone(request.getPhone());
                existing.setCountry(request.getCountry());
                existing.setExperienceYears(request.getExperienceYears());
                existing.setLinkedinUrl(request.getLinkedinUrl());
                existing.setCvUrl(request.getCvUrl());
                existing.setStatus(HRStatus.PENDING);
                existing.setSubmittedAt(LocalDateTime.now());
                saved = hrRepository.save(existing);
            } else {
                return new Response<>(400, "Invalid HR application status.", null);
            }
        } else {
            // Chưa từng nộp -> tạo mới
            HR hrRequest = new HR();
            hrRequest.setUser(user);
            hrRequest.setCompany(company);
            hrRequest.setPhone(request.getPhone());
            hrRequest.setCountry(request.getCountry());
            hrRequest.setExperienceYears(request.getExperienceYears());
            hrRequest.setLinkedinUrl(request.getLinkedinUrl());
            hrRequest.setCvUrl(request.getCvUrl());
            hrRequest.setStatus(HRStatus.PENDING);
            hrRequest.setSubmittedAt(LocalDateTime.now());

            saved = hrRepository.save(hrRequest);
        }

        String firstName = saved.getUser().getFirstName();
        String lastName  = saved.getUser().getLastName();
        String fullName = (firstName == null || firstName.isBlank())
                ? lastName
                : firstName + " " + lastName;

        HRResponseDTO responseDTO = new HRResponseDTO(
                saved.getHrId(),
                saved.getCompany().getName(),
                fullName,
                saved.getPhone(),
                saved.getCountry(),
                saved.getExperienceYears(),
                saved.getLinkedinUrl(),
                saved.getCvUrl(),
                saved.getSubmittedAt(),
                saved.getStatus().name()
        );
        String message = (existingOpt.isPresent() && existingOpt.get().getStatus() == HRStatus.REJECTED)
                ? "Resubmitted HR application successfully"
                : "HR application submitted successfully";

        return new Response<>(200, message, responseDTO);
    }

    public Response<HRResponseDTO> checkHRApplicationStatus() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Please log in to continue", null);
        }

        HR hrApplication = hrRepository.findByUser(user)
                .orElse(null);
        if (hrApplication == null ){
            return new Response<>(200, "You have not submitted for applying HR yet", null);
        }

        String firstName = hrApplication.getUser().getFirstName();
        String lastName  = hrApplication.getUser().getLastName();

        String fullName = (firstName == null || firstName.isBlank())
                ? lastName
                : firstName + " " + lastName;

        HRResponseDTO responseDTO = new HRResponseDTO(
                hrApplication.getHrId(),
                hrApplication.getCompany().getName(),
                fullName,
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
