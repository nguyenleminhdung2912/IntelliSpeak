package com.gsu25se05.itellispeak.service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.auth.reponse.*;
import com.gsu25se05.itellispeak.dto.auth.request.*;
import com.gsu25se05.itellispeak.email.EmailDetail;
import com.gsu25se05.itellispeak.email.EmailService;
import com.gsu25se05.itellispeak.entity.InterviewHistory;
import com.gsu25se05.itellispeak.entity.InterviewHistoryDetail;
import com.gsu25se05.itellispeak.entity.Package;
import com.gsu25se05.itellispeak.entity.User;
import java.util.Objects;
import com.gsu25se05.itellispeak.entity.UserUsage;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.exception.auth.NotLoginException;
import com.gsu25se05.itellispeak.jwt.JWTService;
import com.gsu25se05.itellispeak.repository.InterviewHistoryRepository;
import com.gsu25se05.itellispeak.repository.PackageRepository;
import com.gsu25se05.itellispeak.repository.UserRepository;
//import com.gsu25se05.itellispeak.repository.WalletRepository;
import com.gsu25se05.itellispeak.repository.UserUsageRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthService implements UserDetailsService {

    private final static String defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/mentor-booking-3d46a.appspot.com/o/76f15d2d-9f0b-4051-8177-812d5ee785a1.jpg?alt=media";

    @Value("${BASE_FRONTEND_URL}")
    private String frontendBaseUrl;

    @Autowired
    @Lazy
    private AuthenticationManager authenticationManager;

    @Autowired
    private JWTService jwtService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AccountUtils accountUtils;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PackageRepository packageRepository;

    @Autowired
    private UserUsageRepository userUsageRepository;
    @Autowired
    private InterviewHistoryRepository interviewHistoryRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    private UserDTO convertToUserDTO(User user) {
        if (user == null) return null;

        String email = user.getEmail();
        String userName = (email != null && email.contains("@")) ? email.substring(0, email.indexOf('@')) : "";

        Long packageId = (user.getAPackage() != null) ? user.getAPackage().getPackageId() : null;

        int cvUsed = 0, jdUsed = 0, interviewUsed = 0;
        UserUsage usage = user.getUserUsage();
        if (usage == null) {

            usage = userUsageRepository.findByUser(user).orElse(null);
        }
        if (usage != null) {
            cvUsed = usage.getCvAnalyzeUsed();
            jdUsed = usage.getJdAnalyzeUsed();
            interviewUsed = usage.getInterviewUsed();
        }

        return UserDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(userName)
                .email(user.getEmail())
                .role(user.getRole())
                .packageId(packageId)
                .packageName(user.getAPackage() != null ? user.getAPackage().getPackageName() : null)
                .birthday(user.getBirthday())
                .avatar(user.getAvatar())
                .status(user.getStatus().name())
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
                .cvAnalyzeUsed(cvUsed)
                .jdAnalyzeUsed(jdUsed)
                .interviewUsed(interviewUsed)
                .build();
    }



    public Response<UserProfileDTO> getCurrentUserProfile() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) throw new NotLoginException("Please log in to continue");
        List<InterviewHistory> histories = interviewHistoryRepository.findByUser(user);

        // Calculate weekly counts
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfWeek = now.with(java.time.DayOfWeek.MONDAY).toLocalDate().atStartOfDay();
        LocalDateTime startOfLastWeek = startOfWeek.minusWeeks(1);
        LocalDateTime endOfLastWeek = startOfWeek.minusSeconds(1);

        long currentWeekCount = histories.stream()
                .filter(h -> h.getStartedAt() != null && !h.getStartedAt().isBefore(startOfWeek))
                .count();

        long lastWeekCount = histories.stream()
                .filter(h -> h.getStartedAt() != null && h.getStartedAt().isAfter(startOfLastWeek) && h.getStartedAt().isBefore(endOfLastWeek))
                .count();

        // Calculate average score
        double avgScore = histories.stream()
                .filter(h -> h.getAverageScore() != null)
                .mapToDouble(InterviewHistory::getAverageScore)
                .average()
                .orElse(0.0);

        String scoreEvaluate = avgScore >= 8
                ? "You are doing very well"
                : avgScore >= 5
                ? "You need to improve more"
                : "You should practice more";


        // Get all InterviewHistoryDetails for current user's histories
        List<InterviewHistoryDetail> allDetails = histories.stream()
                .flatMap(h -> h.getDetails() != null ? h.getDetails().stream() : java.util.stream.Stream.empty())
                .toList();

        // Filter details for current week
        List<InterviewHistoryDetail> currentWeekDetails = allDetails.stream()
                .filter(d -> d.getInterviewHistory().getStartedAt() != null &&
                        !d.getInterviewHistory().getStartedAt().isBefore(startOfWeek))
                .toList();

        // Filter details for last week
        List<InterviewHistoryDetail> lastWeekDetails = allDetails.stream()
                .filter(d -> d.getInterviewHistory().getStartedAt() != null &&
                        d.getInterviewHistory().getStartedAt().isAfter(startOfLastWeek) &&
                        d.getInterviewHistory().getStartedAt().isBefore(endOfLastWeek))
                .toList();

        // Count answered questions (answeredContent != "Không có câu trả lời")
        long currentWeekAnswered = currentWeekDetails.stream()
                .filter(d -> d.getAnsweredContent() != null && !d.getAnsweredContent().equals("No answer"))
                .count();

        long lastWeekAnswered = lastWeekDetails.stream()
                .filter(d -> d.getAnsweredContent() != null && !d.getAnsweredContent().equals("No answer"))
                .count();

        // Calculate percentage change
        String answeredQuestionCompared;
        if (lastWeekAnswered == 0) {
            answeredQuestionCompared = currentWeekAnswered > 0 ? "+100%" : "0%";
        } else {
            double percent = ((double) (currentWeekAnswered - lastWeekAnswered) / lastWeekAnswered) * 100;
            answeredQuestionCompared = (percent >= 0 ? "+" : "") + String.format("%.0f", percent) + "%";
        }

        // Calculate daily interview scores (last 10 days, oldest to newest)
        Map<LocalDate, List<InterviewHistory>> groupedByDate = histories.stream()
                .filter(h -> h.getStartedAt() != null && h.getAverageScore() != null)
                .collect(Collectors.groupingBy(h -> h.getStartedAt().toLocalDate()));

        List<DailyInterviewScoreDTO> dailyScores = groupedByDate.entrySet().stream()
                .map(entry -> {
                    double avg = entry.getValue().stream()
                            .mapToDouble(InterviewHistory::getAverageScore)
                            .average()
                            .orElse(0.0);
                    return DailyInterviewScoreDTO.builder()
                            .date(entry.getKey().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                            .averageScore(String.format("%.1f", avg))
                            .build();
                })
                .sorted(Comparator.comparing(dto -> LocalDate.parse(dto.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy")), Comparator.reverseOrder()))
                .limit(10)
                .sorted(Comparator.comparing(dto -> LocalDate.parse(dto.getDate(), DateTimeFormatter.ofPattern("dd/MM/yyyy"))))
                .toList();

        UserProfileStatisticDTO statistic = UserProfileStatisticDTO.builder()
                .interviewWeeklyCount(currentWeekCount + " sessions")
                .comparedToLastWeek(
                        (currentWeekCount >= lastWeekCount ? "+" : "-")
                                + Math.abs(currentWeekCount - lastWeekCount)
                                + " sessions this week compared to last week"
                )
                .averageInterviewScore(String.format("%.1f/10", avgScore))
                .scoreEvaluate(scoreEvaluate)
                .answeredQuestionCount(currentWeekAnswered + " questions")
                .answeredQuestionComparedToLastWeek(
                        answeredQuestionCompared + " compared to last week"
                )
                .dailyScores(dailyScores)
                .build();

        String email = user.getEmail();
        String userName = email != null && email.contains("@") ? email.split("@")[0] : "";

        int cvUsed = 0, jdUsed = 0, interviewUsed = 0;
        UserUsage usage = user.getUserUsage();
        if (usage == null) {
            usage = userUsageRepository.findByUser(user).orElse(null);
        }
        if (usage != null && user.getAPackage() != null) {
            Package currentPackage = user.getAPackage();
            cvUsed = (currentPackage.getCvAnalyzeCount() != null ? currentPackage.getCvAnalyzeCount() : 0) - usage.getCvAnalyzeUsed();
            jdUsed = (currentPackage.getJdAnalyzeCount() != null ? currentPackage.getJdAnalyzeCount() : 0) - usage.getJdAnalyzeUsed();
            interviewUsed = (currentPackage.getInterviewCount() != null ? currentPackage.getInterviewCount() : 0) - usage.getInterviewUsed();
        }

        UserProfileDTO profile = UserProfileDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(userName)
                .bio(user.getBio())
                .role(user.getRole())
                .phone(user.getPhone())
                .email(email)
                .packageName(user.getAPackage().getPackageName())
                .packageId(user.getAPackage().getPackageId())
                .avatar(user.getAvatar())
                .website(user.getWebsite())
                .github(user.getGithub())
                .linkedin(user.getLinkedin())
                .facebook(user.getFacebook())
                .youtube(user.getYoutube())
                .statistic(List.of(statistic))
                .jdAnalyzeUsed(jdUsed)
                .cvAnalyzeUsed(cvUsed)
                .interviewUsed(interviewUsed)
                .build();

        return new Response<>(200, "Profile information retrieved successfully", profile);
    }

    public Response<UserDTO> updateProfile(UpdateProfileRequestDTO request) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please log in to continue", null);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getPhone() != null) user.setPhone(request.getPhone());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getWebsite() != null) user.setWebsite(request.getWebsite());
        if (request.getGithub() != null) user.setGithub(request.getGithub());
        if (request.getLinkedin() != null) user.setLinkedin(request.getLinkedin());
        if (request.getFacebook() != null) user.setFacebook(request.getFacebook());
        if (request.getYoutube() != null) user.setYoutube(request.getYoutube());

        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);

        UserDTO userDTO = convertToUserDTO(user);

        return new Response<>(200, "Profile updated successfully", userDTO);

    }


    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Xóa cookie token
        Cookie tokenCookie = jwtService.clearTokenCookie();
        Cookie refreshTokenCookie = jwtService.clearRefreshTokenCookie();

        response.addCookie(tokenCookie);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.ok("Logout successfully");
    }

    private @NotNull User convertToUser(RegisterRequestDTO registerRequestDTO) {
        User account = new User(
                registerRequestDTO.getEmail()
        );
        String email = registerRequestDTO.getEmail();
        if (email != null && email.contains("@")) {
            String beforeAt = email.substring(0, email.indexOf("@"));
            account.setLastName(beforeAt);
        }
        account.setAvatar(defaultAvatar);

        account.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        return account;
    }

    private String getString(String token) {
        String email = jwtService.extractEmail(token);
        if (email == null || email.isEmpty()) {
            throw new AuthAppException(ErrorCode.TOKEN_INVALID);
        }
        return email;
    }

    public ResponseEntity<LoginResponseDTO> checkLogin(LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        try {
            User account = findUserByEmail(loginRequestDTO.getEmail());
            if (account == null) {
                throw new AuthAppException(ErrorCode.EMAIL_NOT_FOUND);
            }
            if (Boolean.TRUE.equals(account.getIsDeleted())) {
                throw new AuthAppException(ErrorCode.ACCOUNT_IS_DELETED);
            }

            Authentication authentication;
            try {
                authentication = authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                loginRequestDTO.getEmail(),
                                loginRequestDTO.getPassword()
                        )
                );
            } catch (Exception e) {
                throw new AuthAppException(ErrorCode.USERNAME_PASSWORD_NOT_CORRECT);
            }

            String status = (account.getStatus() == null) ? null : account.getStatus().toString();
            if (!"VERIFIED".equalsIgnoreCase(status)) {
                throw new AuthAppException(ErrorCode.EMAIL_WAIT_VERIFY);
            }

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtService.generateToken(loginRequestDTO.getEmail());
            Cookie tokenCookie = jwtService.createTokenCookie(token);
            response.addCookie(tokenCookie);
            response.addHeader("Set-Cookie",
                    String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            tokenCookie.getName(),
                            tokenCookie.getValue(),
                            tokenCookie.getPath(),
                            tokenCookie.getMaxAge()
                    )
            );

            String refreshToken = jwtService.generateRefreshToken(loginRequestDTO.getEmail());
            Cookie refreshTokenCookie = jwtService.createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);
            response.addHeader("Set-Cookie",
                    String.format("%s=%s; Path=%s; Max-Age=%d; HttpOnly; Secure; SameSite=None",
                            refreshTokenCookie.getName(),
                            refreshTokenCookie.getValue(),
                            refreshTokenCookie.getPath(),
                            refreshTokenCookie.getMaxAge()
                    )
            );

            UserDTO userDTO = convertToUserDTO(account);

            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setCode(200);
            loginResponseDTO.setMessage("Login successful");
            loginResponseDTO.setToken(token);
            loginResponseDTO.setRefreshToken(refreshToken);
            if (account.getAPackage() != null) {
                loginResponseDTO.setPackageId(account.getAPackage().getPackageId());
                loginResponseDTO.setPackageName(account.getAPackage().getPackageName());
            }
            loginResponseDTO.setUser(userDTO);

            return new ResponseEntity<>(loginResponseDTO, HttpStatus.OK);

        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();

            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setCode(errorCode.getCode());
            loginResponseDTO.setMessage(e.getMessage());
            loginResponseDTO.setError("Login failed");

            return new ResponseEntity<>(loginResponseDTO, errorCode.getHttpStatus());
        }
    }


    public ResponseEntity<RegisterResponseDTO> registerAccount(RegisterRequestDTO registerRequestDTO) {
        try {
            //Check if the email exist
            User tempAccount = findUserByEmail(registerRequestDTO.getEmail());
            if (tempAccount != null) {
                if (tempAccount.getStatus() == User.Status.VERIFIED
                        || "VERIFIED".equalsIgnoreCase(String.valueOf(tempAccount.getStatus()))) {
                    throw new AuthAppException(ErrorCode.EMAIL_EXISTED);
                }
                throw new AuthAppException(ErrorCode.EMAIL_WAIT_VERIFY);

            }

            if (!registerRequestDTO.getPassword().equals(registerRequestDTO.getConfirmPassword())) {
                throw new AuthAppException(ErrorCode.PASSWORD_REPEAT_INCORRECT);
            }

            User account = convertToUser(registerRequestDTO);
            account.setAvatar(defaultAvatar);
            account.setIsDeleted(false);
            account.setRole(User.Role.USER);
            account.setCreateAt(LocalDateTime.now());
            account.setAPackage(packageRepository.findById(1L).orElse(null));
            account.setStatus(User.Status.PENDING);
            User savedUser = userRepository.save(account);

            // Tạo UserUsage mặc định
            UserUsage usage = new UserUsage();
            usage.setUser(savedUser);
            usage.setCvAnalyzeUsed(0);
            usage.setJdAnalyzeUsed(0);
            usage.setInterviewUsed(0);
            usage.setUpdateAt(LocalDateTime.now());

            userUsageRepository.save(usage);

            String verifyToken = jwtService.generateEmailVerifyToken(savedUser.getEmail());
            String link = "bug-adapting-especially.ngrok-free.app/auth/verify" + verifyToken;
            String responseMessage = "Registration successful, please check your email to verify";
            RegisterResponseDTO response = new RegisterResponseDTO(responseMessage, null, 201, registerRequestDTO.getEmail());

            //Send email here
            EmailDetail emailDetail = EmailDetail.builder()
                    .recipient(account.getEmail())
                    .msgBody("Please verify your account to continue.")
                    .subject("Please verify your account!")
                    .name(account.getUsername())
                    .attachment(link)
                    .build();
            emailService.sendVerifyEmail(emailDetail);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            RegisterResponseDTO response = new RegisterResponseDTO(
                    errorCode.getCode(),
                    errorCode.getMessage(),
                    "Register failed"
            );

            return new ResponseEntity<>(response, errorCode.getHttpStatus());
        }
    }

    public boolean verifyAccount(String token) {
        try {
            String purpose = jwtService.extractPurpose(token);
            if (!"email_verify".equals(purpose)) {
                throw new TokenExpiredException("Invalid token purpose!", Instant.now());
            }

            String email = jwtService.extractEmail(token);
            User account = userRepository.findByEmail(email).orElse(null);
            if (account == null) {
                throw new AuthAppException(ErrorCode.EMAIL_NOT_FOUND);
            }

            if (account.getStatus() == User.Status.VERIFIED
                    || "VERIFIED".equalsIgnoreCase(String.valueOf(account.getStatus()))) {
                return true;
            }

            account.setStatus(User.Status.VERIFIED);
            userRepository.save(account);
            return true;

        } catch (Exception e) {
            throw new TokenExpiredException("Token is invalid or has expired!", Instant.now());
        }
    }


    public ResponseEntity<ForgotPasswordResponse> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        try {
            User account = userRepository.findByEmail(forgotPasswordRequest.getEmail())
                    .orElseThrow(() -> new AuthAppException(ErrorCode.EMAIL_NOT_FOUND));

            if (Boolean.TRUE.equals(account.getIsDeleted())) {
                throw new AuthAppException(ErrorCode.ACCOUNT_IS_DELETED);
            }

            String token = jwtService.generatePasswordResetToken(account.getEmail());

            account.setTokens(token);
            userRepository.save(account);

            String resetLink = String.format("%s/reset-password?token=%s", frontendBaseUrl, token);

            EmailDetail emailDetail = EmailDetail.builder()
                    .recipient(account.getEmail())
                    .name(account.getLastName() != null ? account.getLastName() : account.getUsername())
                    .subject("Password Reset Request")
                    .msgBody("We received a request to reset your password. Click the button to continue.")
                    .attachment(resetLink)
                    .build();

            emailService.sendForgotPasswordEmail(emailDetail);

            ForgotPasswordResponse resp = new ForgotPasswordResponse(
                    "Password reset link has been sent. Please check your email.",
                    null,
                    200
            );
            return new ResponseEntity<>(resp, HttpStatus.OK);

        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            ForgotPasswordResponse resp = new ForgotPasswordResponse(
                    "Password reset failed",
                    e.getMessage(),
                    errorCode.getCode()
            );
            return new ResponseEntity<>(resp, errorCode.getHttpStatus());
        }
    }


    public ResponseEntity<ResetPasswordResponse> resetPassword(ResetPasswordRequest resetPasswordRequest, String token) {
        try {

            if (token == null || token.isBlank()) {
                throw new AuthAppException(ErrorCode.TOKEN_INVALID);
            }
            if (!Objects.equals(resetPasswordRequest.getNew_password(), resetPasswordRequest.getRepeat_password())) {
                throw new AuthAppException(ErrorCode.PASSWORD_REPEAT_INCORRECT);
            }

            String purpose;
            String email;
            try {
                purpose = jwtService.extractPurpose(token);
                email = jwtService.extractEmail(token);
            } catch (Exception ex) {
                throw new AuthAppException(ErrorCode.TOKEN_INVALID);
            }
            if (!"password_reset".equals(purpose)) {
                throw new AuthAppException(ErrorCode.TOKEN_INVALID);
            }

            User account = userRepository.findByEmail(email)
                    .orElseThrow(() -> new AuthAppException(ErrorCode.EMAIL_NOT_FOUND));

            if (Boolean.TRUE.equals(account.getIsDeleted())) {
                throw new AuthAppException(ErrorCode.ACCOUNT_IS_DELETED);
            }

            account.setPassword(passwordEncoder.encode(resetPasswordRequest.getNew_password()));
            account.setTokens(null);
            userRepository.save(account);

            ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse(
                    "Password reset successfully.",
                    null,
                    200
            );
            return new ResponseEntity<>(resetPasswordResponse, HttpStatus.OK);

        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            ResetPasswordResponse resetPasswordResponse =
                    new ResetPasswordResponse("Password reset failed", e.getMessage(), errorCode.getCode());
            return new ResponseEntity<>(resetPasswordResponse, errorCode.getHttpStatus());
        } catch (Exception e) {

            ResetPasswordResponse resetPasswordResponse =
                    new ResetPasswordResponse("Password reset failed", "Unexpected error", 400);
            return new ResponseEntity<>(resetPasswordResponse, HttpStatus.BAD_REQUEST);
        }
    }

    public ResponseEntity<ChangePasswordResponse> changePassword(ChangePasswordRequest req) {
        try {
            User currentUser = accountUtils.getCurrentAccount();
            if (currentUser == null) {
                throw new AuthAppException(ErrorCode.NOT_LOGIN);
            }
            if (Boolean.TRUE.equals(currentUser.getIsDeleted())) {
                throw new AuthAppException(ErrorCode.ACCOUNT_IS_DELETED);
            }

            if (req.getCurrentPassword() == null || req.getNewPassword() == null || req.getRepeatPassword() == null) {
                throw new AuthAppException(ErrorCode.BAD_REQUEST);
            }
            if (!req.getNewPassword().equals(req.getRepeatPassword())) {
                throw new AuthAppException(ErrorCode.PASSWORD_REPEAT_INCORRECT);
            }
            if (passwordEncoder.matches(req.getNewPassword(), currentUser.getPassword())) {
                throw new AuthAppException(ErrorCode.PASSWORD_REPEAT_INCORRECT);
            }

            if (!passwordEncoder.matches(req.getCurrentPassword(), currentUser.getPassword())) {
                throw new AuthAppException(ErrorCode.USERNAME_PASSWORD_NOT_CORRECT);
            }

            currentUser.setPassword(passwordEncoder.encode(req.getNewPassword()));
            userRepository.save(currentUser);

            ChangePasswordResponse resp = new ChangePasswordResponse(
                    "Password changed successfully.", null, 200
            );
            return ResponseEntity.ok(resp);

        } catch (AuthAppException e) {
            ErrorCode ec = e.getErrorCode();
            ChangePasswordResponse resp = new ChangePasswordResponse(
                    "Change password failed", e.getMessage(), ec.getCode()
            );
            return new ResponseEntity<>(resp, ec.getHttpStatus());
        } catch (Exception e) {
            ChangePasswordResponse resp = new ChangePasswordResponse(
                    "Change password failed", "Unexpected error", 400
            );
            return ResponseEntity.badRequest().body(resp);
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
