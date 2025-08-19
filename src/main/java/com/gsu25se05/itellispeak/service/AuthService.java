package com.gsu25se05.itellispeak.service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.auth.reponse.*;
import com.gsu25se05.itellispeak.dto.auth.request.*;
import com.gsu25se05.itellispeak.email.EmailDetail;
import com.gsu25se05.itellispeak.email.EmailService;
import com.gsu25se05.itellispeak.entity.InterviewHistory;
import com.gsu25se05.itellispeak.entity.InterviewHistoryDetail;
import com.gsu25se05.itellispeak.entity.User;
//import com.gsu25se05.itellispeak.entity.Wallet;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AuthService implements UserDetailsService {

    private final static String defaultAvatar = "https://firebasestorage.googleapis.com/v0/b/mentor-booking-3d46a.appspot.com/o/76f15d2d-9f0b-4051-8177-812d5ee785a1.jpg?alt=media";

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

//    @Autowired
//    private WalletRepository walletRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    private UserDTO convertToUserDTO(User user) {
        if (user == null) return null;

        String email = user.getEmail();
        String userName = email != null && email.contains("@") ? email.split("@")[0] : "";

        //abcd
        return UserDTO.builder()
                .userId(user.getUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(userName)
                .email(user.getEmail())
                .role(user.getRole())
//                .packageId(user.getAPackage().getPackageId())
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
    }


    public Response<UserProfileDTO> getCurrentUserProfile() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) throw new NotLoginException("Please log in to continue");
        // Fetch interview histories
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
                .build();

        String email = user.getEmail();
        String userName = email != null && email.contains("@") ? email.split("@")[0] : "";

        UserProfileDTO profile = UserProfileDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .userName(userName)
                .bio(user.getBio())
                .role(user.getRole())
                .phone(user.getPhone())
                .email(email)
                .avatar(user.getAvatar())
                .website(user.getWebsite())
                .github(user.getGithub())
                .linkedin(user.getLinkedin())
                .facebook(user.getFacebook())
                .youtube(user.getYoutube())
                .statistic(List.of(statistic))
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
            // Validate email
            User account = findUserByEmail(loginRequestDTO.getEmail());
            if (account == null) {
                throw new AuthAppException(ErrorCode.EMAIL_NOT_FOUND);
            }
            if (Boolean.TRUE.equals(account.getIsDeleted())) {
                throw new AuthAppException(ErrorCode.ACCOUNT_IS_DELETED);
            }

            // Authenticate user
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

            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Generate JWT tokens and set cookies
            String token = jwtService.generateToken(loginRequestDTO.getEmail());
            // Sau khi tạo cookie
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

            // Build response
            UserDTO userDTO = convertToUserDTO(account); // Convert User -> UserDTO

            LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
            loginResponseDTO.setCode(200);
            loginResponseDTO.setMessage("Login successful");
            loginResponseDTO.setToken(token);
            loginResponseDTO.setRefreshToken(refreshToken);
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
            User savedUser = userRepository.save(account);

            // Tạo UserUsage mặc định
            UserUsage usage = new UserUsage();
            usage.setUser(savedUser);
            usage.setCvAnalyzeUsed(0);
            usage.setJdAnalyzeUsed(0);
            usage.setInterviewUsed(0);
            usage.setUpdateAt(LocalDateTime.now());

            userUsageRepository.save(usage);

            String responseMessage = "Registration successful, please check your email to verify";
            RegisterResponseDTO response = new RegisterResponseDTO(responseMessage, null, 201, registerRequestDTO.getEmail());

            //Send email here
            EmailDetail emailDetail = EmailDetail.builder()
                    .recipient(account.getEmail())
                    .msgBody("Please verify your account to continue.")
                    .subject("Please verify your account!")
                    .name(account.getUsername())
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
            String email = jwtService.extractEmail(token);

            User account = userRepository.findByEmail(email).orElse(null);
            if (account == null) {
                throw new AuthAppException(ErrorCode.EMAIL_NOT_FOUND);
            }
            account.setStatus("VERIFIED");
            userRepository.save(account);
            return true;
        } catch (Exception e) {
            throw new TokenExpiredException("Token is invalid or has expired!", Instant.now());
        }
    }

    public ResponseEntity<ForgotPasswordResponse> forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        try {
            // CHECK VALID EMAIL
            Optional<User> tempAccount = userRepository.findByEmail(forgotPasswordRequest.getEmail());

            User checkAccount = tempAccount.orElseThrow(() -> new AuthAppException(ErrorCode.EMAIL_NOT_FOUND));

            if (checkAccount.getEmail() == null || checkAccount.getEmail().isEmpty()) {
                throw new AuthAppException(ErrorCode.EMAIL_NOT_FOUND);
            }

            // GENERATE TOKEN FOR EMAIL FORGOT PASSWORD (ENSURE UNIQUE AND JUST ONLY EMAIL CAN USE)
            String token = jwtService.generateToken(forgotPasswordRequest.getEmail());
            User account = tempAccount.orElseThrow(() -> new AuthAppException(ErrorCode.USER_NOT_FOUND));
            account.setTokens(token);

            //SEND MAIL
            EmailDetail emailDetail = EmailDetail.builder()
                    .recipient(account.getEmail())
                    .msgBody("Hello " + account.getLastName() + ",\n\n" +
                            "We have received a request to reset the password for your account. To complete the process, please click the link below:\n\n" +
                            "<a href=\"https://circuit-project.vercel.app/forgotPassword?" + token + "\">Reset Password</a>\n\n" +
                            "If you did not request a password reset, please ignore this email or contact our support team if you have any questions.\n\n" +
                            "Best regards,\nSupport Team")
                    .subject("Password Reset Request - Action Required")


                    .name(account.getLastName())
                    .build();
            emailService.sendForgotPasswordEmail(emailDetail);

            userRepository.save(account);
            ForgotPasswordResponse forgotPasswordResponse = new ForgotPasswordResponse(
                    "Password reset code created successfully. Please check your email.",
                    null,
                    200
            );

            return new ResponseEntity<>(forgotPasswordResponse, HttpStatus.OK);
        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            ForgotPasswordResponse forgotPasswordResponse =
                    new ForgotPasswordResponse("Password reset failed", e.getMessage(), errorCode.getCode());
            return new ResponseEntity<>(forgotPasswordResponse, errorCode.getHttpStatus());
        }
    }


    public ResponseEntity<ResetPasswordResponse> resetPassword(ResetPasswordRequest resetPasswordRequest, String token) {
        try {
            // AFTER USER CLICK LINK FORGOT PASSWORD IN EMAIL THEN REDIRECT TO API HERE (RESET PASSWORD)
            // CHECK PASSWORD AND REPEAT PASSWORD
            if (!resetPasswordRequest.getNew_password().equals(resetPasswordRequest.getRepeat_password())) {
                throw new AuthAppException(ErrorCode.PASSWORD_REPEAT_INCORRECT);
            }
            // CALL FUNC
            String email = jwtService.extractEmail(token);

            // FIND EMAIL IN DATABASE AND UPDATE NEW PASSWORD
            Optional<User> accountOptional = userRepository.findByEmail(email);
            if (accountOptional.isPresent()) {
                User account = accountOptional.get();
                account.setPassword(passwordEncoder.encode(resetPasswordRequest.getNew_password()));
                userRepository.save(account);
            }

            ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse(
                    "Password reset successful.",
                    null,
                    200
            );

            return new ResponseEntity<>(resetPasswordResponse, HttpStatus.CREATED);
        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            ResetPasswordResponse resetPasswordResponse =
                    new ResetPasswordResponse("Password reset failed", e.getMessage(), errorCode.getCode());
            return new ResponseEntity<>(resetPasswordResponse, errorCode.getHttpStatus());
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
