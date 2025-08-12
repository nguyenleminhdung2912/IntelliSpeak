package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.admin.UserWithPackageDTO;
import com.gsu25se05.itellispeak.dto.auth.reponse.UserDTO;
import com.gsu25se05.itellispeak.dto.hr.HRAdminResponseDTO;
import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.HRStatus;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.repository.HRRepository;
import com.gsu25se05.itellispeak.repository.PackageRepository;
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
    private final PackageRepository packageRepository;


    public AdminService(TransactionRepository transactionRepository, UserRepository userRepository, HRRepository hrRepository, PackageRepository packageRepository) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.hrRepository = hrRepository;
        this.packageRepository = packageRepository;
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
            String firstName = hr.getUser().getFirstName();
            String lastName = hr.getUser().getLastName();
            String email = hr.getUser().getEmail();

            String fullName;
            if (firstName == null || lastName == null || firstName.isBlank() || lastName.isBlank()) {

                fullName = email != null && email.contains("@")
                        ? email.substring(0, email.indexOf("@"))
                        : "Unknown";
            } else {
                fullName = firstName + " " + lastName;
            }

            return new HRAdminResponseDTO(
                    hr.getHrId(),
                    fullName,
                    email,
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

    public List<UserWithPackageDTO> getAllUsersWithPackage() {
        return userRepository.findAll().stream().map(user -> {
            UserWithPackageDTO dto = new UserWithPackageDTO();
            dto.setUserId(user.getUserId());
            dto.setFirstName(user.getFirstName());
            dto.setLastName(user.getLastName());
            dto.setEmail(user.getEmail());
            dto.setAvatar(user.getAvatar());
            dto.setStatus(user.getStatus());
            if (user.getAPackage() != null) {
                dto.setPackageId(user.getAPackage().getPackageId());
                dto.setPackageName(user.getAPackage().getPackageName());
            }
            return dto;
        }).toList();
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            String email = user.getEmail();
            String userName = email != null && email.contains("@") ? email.split("@")[0] : "";
            return UserDTO.builder()
                    .userId(user.getUserId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .userName(userName)
                    .email(user.getEmail())
                    .role(user.getRole())
                    .packageId(user.getAPackage() != null ? user.getAPackage().getPackageId() : null)
                    .birthday(user.getBirthday())
                    .avatar(user.getAvatar())
                    .status(user.getStatus())
                    .phone(user.getPhone())
                    .bio(user.getBio())
                    .website(user.getWebsite())
                    .github(user.getGithub())
                    .linkedin(user.getLinkedin())
                    .facebook(user.getFacebook())
                    .youtube(user.getYoutube())
                    .createAt(user.getCreateAt())
                    .updateAt(user.getUpdateAt())
                    .isDeleted(user.getIsDeleted())
                    .build();
        }).toList();
    }

    public List<Map<String, Object>> getPackageSubscriptionStats(int year) {
        List<Map<String, Object>> result = new ArrayList<>();

        // Lấy tất cả gói
        var packages = packageRepository.findAll();

        for (var p : packages) {
            Map<String, Object> packageData = new HashMap<>();
            packageData.put("name", p.getPackageName());

            List<Long> monthlyCounts = new ArrayList<>();
            for (int month = 1; month <= 12; month++) {
                LocalDateTime start = YearMonth.of(year, month).atDay(1).atStartOfDay();
                LocalDateTime end = YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59);

                Long count = userRepository.countByPackageIdAndCreateAtBetween(
                        p.getPackageId(), start, end
                );

                monthlyCounts.add(count != null ? count : 0L);
            }
            packageData.put("data", monthlyCounts);

            result.add(packageData);
        }
        return result;
    }



}
