package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.hr.HRResponseDTO;
import com.gsu25se05.itellispeak.dto.hr.HRRequestDTO;
import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.HRStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
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

    public Response<HRResponseDTO> applyHR(HRRequestDTO request) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);

        // Kiểm tra nếu user đã gửi yêu cầu HR
        if (hrRepository.findByUser(user).isPresent()) {
            throw new AuthAppException(ErrorCode.HR_ALREADY_APPLIED);
        }

        HR hrRequest = new HR();
        hrRequest.setUser(user);
        hrRequest.setCompany(request.getCompany());
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
                saved.getCompany(),
                saved.getPhone(),
                saved.getCountry(),
                saved.getExperienceYears(),
                saved.getLinkedinUrl(),
                saved.getCvUrl(),
                saved.getSubmittedAt(),
                saved.getStatus().name()
        );
        return new Response<>(200, "Gửi đơn ứng tuyển HR thành công", responseDTO);
    }

    public Response<HRResponseDTO> checkHRApplicationStatus() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) {
            return new Response<>(401, "Vui lòng đăng nhập để tiếp tục", null);
        }

        HR hrApplication = hrRepository.findByUser(user)
                .orElseThrow(() -> new AuthAppException(ErrorCode.HR_NOT_FOUND));

        HRResponseDTO responseDTO = new HRResponseDTO(
                hrApplication.getHrId(),
                hrApplication.getCompany(),
                hrApplication.getPhone(),
                hrApplication.getCountry(),
                hrApplication.getExperienceYears(),
                hrApplication.getLinkedinUrl(),
                hrApplication.getCvUrl(),
                hrApplication.getSubmittedAt(),
                hrApplication.getStatus().name()
        );

        return new Response<>(200, "Lấy trạng thái đơn ứng tuyển HR thành công", responseDTO);
    }


}
