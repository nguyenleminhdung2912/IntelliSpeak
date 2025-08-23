package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.admin.CreateUserDTO;
import com.gsu25se05.itellispeak.dto.admin.UserWithPackageDTO;
import com.gsu25se05.itellispeak.dto.auth.reponse.UserDTO;
import com.gsu25se05.itellispeak.dto.hr.HRAdminResponseDTO;
import com.gsu25se05.itellispeak.email.EmailService;
import com.gsu25se05.itellispeak.entity.HR;
import com.gsu25se05.itellispeak.entity.HRStatus;
import com.gsu25se05.itellispeak.entity.Package;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.entity.UserUsage;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.repository.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

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
    private final UserUsageRepository userUsageRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;


    public AdminService(TransactionRepository transactionRepository, UserRepository userRepository, HRRepository hrRepository, PackageRepository packageRepository, UserUsageRepository userUsageRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.hrRepository = hrRepository;
        this.packageRepository = packageRepository;
        this.userUsageRepository = userUsageRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
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
        var packages = packageRepository.findByIsDeletedFalse();
        for (var p : packages) {
            Long count = userRepository.countUsersByPackage(p);
            result.put(p.getPackageName(), count != null ? count : 0L);
        }
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
                    hr.getCompany().getName(),
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


    @Transactional
    public void approveHR(Long hrId) {
        HR hr = hrRepository.findById(hrId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.ACCOUNT_NOT_FOUND));

        if (hr.getStatus() == HRStatus.APPROVED) {
            return;
        }

        User user = hr.getUser();
        hr.setStatus(HRStatus.APPROVED);
        hr.setApprovedAt(LocalDateTime.now());
        hrRepository.save(hr);

        user.setRole(User.Role.HR);
        userRepository.save(user);

        String email = user.getEmail();
        String name  = (user.getUsername() != null && !user.getUsername().isBlank())
                ? user.getUsername()
                : extractUsernameFromEmail(email);

        // Đăng ký gửi mail sau khi transaction commit
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendHrApprovalEmail(email, name);
            }
        });
    }

    @Transactional
    public void rejectHR(Long hrId, String reason) {
        HR hr = hrRepository.findById(hrId)
                .orElseThrow(() -> new AuthAppException(ErrorCode.ACCOUNT_NOT_FOUND));

        // Tránh set lại trạng thái giống hiện tại
        if (hr.getStatus() == HRStatus.REJECTED) {
            // vẫn có thể gửi lại mail nếu bạn muốn, hoặc return
            // return;
        }

        hr.setStatus(HRStatus.REJECTED);
        // nếu có field lưu reason: hr.setRejectionReason(reason);
        hrRepository.save(hr);

        User user = hr.getUser();
        String email = user.getEmail();
        String name  = (user.getUsername() != null && !user.getUsername().isBlank())
                ? user.getUsername()
                : extractUsernameFromEmail(email);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                emailService.sendHrRejectionEmail(email, name, reason);
            }
        });
    }

    private String extractUsernameFromEmail(String email) {
        if (email == null) return "there";
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
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
        var packages = packageRepository.findByIsDeletedFalse();

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

    @Transactional
    public UserDTO createUser(CreateUserDTO dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new AuthAppException(ErrorCode.EMAIL_WAIT_VERIFY);
        }

        Package welcomePackage = packageRepository.findByPackageName("Welcome").orElse(null);

        User user = User.builder()
                .email(dto.getEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .avatar("https://firebasestorage.googleapis.com/v0/b/mentor-booking-3d46a.appspot.com/o/76f15d2d-9f0b-4051-8177-812d5ee785a1.jpg?alt=media")
                .isDeleted(false)
                .status("VERIFIED")
                .createAt(LocalDateTime.now())
                .aPackage(welcomePackage)
                .build();

        User savedUser = userRepository.save(user);

        UserUsage usage = UserUsage.builder()
                .user(savedUser)
                .cvAnalyzeUsed(0)
                .jdAnalyzeUsed(0)
                .interviewUsed(0)
                .updateAt(LocalDateTime.now())
                .build();
        userUsageRepository.save(usage);

        return UserDTO.builder()
                .userId(savedUser.getUserId())
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .userName(savedUser.getEmail().split("@")[0])
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .avatar(savedUser.getAvatar())
                .status(savedUser.getStatus())
                .createAt(savedUser.getCreateAt())
                .isDeleted(savedUser.getIsDeleted())
                .packageId(welcomePackage.getPackageId())
                .build();
    }
}
