package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.hr.HRAdminResponseDTO;
import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.HRStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.repository.HRRepository;
import com.gsu25se05.itellispeak.repository.TransactionRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final HRRepository hrRepository;


    public AdminService(TransactionRepository transactionRepository, UserRepository userRepository, HRRepository hrRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.hrRepository = hrRepository;
    }

    public Double getMonthlyRevenue(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);
        Double revenue = transactionRepository.sumAmountByCreateAtBetween(start, end);
        return revenue != null ? revenue : 0.0;
    }

    public Map<String, Long> getPlanCounts() {
        Map<String, Long> result = new HashMap<>();
//        result.put("PROFESSIONAL", userRepository.countByPlanType(PlanType.PROFESSIONAL));
//        result.put("BUSINESS", userRepository.countByPlanType(PlanType.BUSINESS));
        return result;
    }

    public List<Map<String, String>> getYearlyRevenueFormatted(int year) {
        List<Map<String, String>> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            Double amount = getMonthlyRevenue(year, month);
            String formattedAmount = String.format("%,.0f Đồng", amount);
            Map<String, String> entry = new HashMap<>();
            entry.put("Month", "Thg " + month);
            entry.put("Amount", formattedAmount);
            result.add(entry);
        }
        return result;
    }


    public List<HRAdminResponseDTO> getAllHRApplications() {
        return hrRepository.findAll().stream().map(hr -> {
            return new HRAdminResponseDTO(
                    hr.getHrId(),
                    hr.getUser().getFirstName() + " " + hr.getUser().getLastName(),
                    hr.getUser().getEmail(),
                    hr.getCompany(),
                    hr.getPhone(),
                    hr.getCountry(),
                    hr.getExperienceYears(),
                    hr.getLinkedinUrl(),
                    hr.getCvUrl(),
                    hr.getStatus(),
                    hr.getSubmittedAt()
            );
        }).toList();
    }

    public void approveHR(Long hrId) {
        HR hr = hrRepository.findById(hrId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.ACCOUNT_NOT_FOUND));

        User user = hr.getUser();
        hr.setStatus(HRStatus.APPROVED);
        hr.setApprovedAt(LocalDateTime.now());
        hrRepository.save(hr);
        user.setRole(User.Role.HR);
        userRepository.save(user);
    }

    public void rejectHR(Long hrId) {
        HR hr = hrRepository.findById(hrId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.ACCOUNT_NOT_FOUND));
        hr.setStatus(HRStatus.REJECTED);
        hrRepository.save(hr);
    }
}
