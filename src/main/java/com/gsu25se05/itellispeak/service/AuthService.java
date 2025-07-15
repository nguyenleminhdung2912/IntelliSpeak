package com.gsu25se05.itellispeak.service;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.auth.reponse.*;
import com.gsu25se05.itellispeak.dto.auth.request.*;
import com.gsu25se05.itellispeak.email.EmailDetail;
import com.gsu25se05.itellispeak.email.EmailService;
import com.gsu25se05.itellispeak.entity.User;
//import com.gsu25se05.itellispeak.entity.Wallet;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.jwt.JWTService;
import com.gsu25se05.itellispeak.repository.UserRepository;
//import com.gsu25se05.itellispeak.repository.WalletRepository;
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

//    @Autowired
//    private WalletRepository walletRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


    public Response<UserProfileDTO> getCurrentUserProfile() {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        UserProfileDTO profile = UserProfileDTO.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .bio(user.getBio())
                .avatar(user.getAvatar())
                .website(user.getWebsite())
                .github(user.getGithub())
                .linkedin(user.getLinkedin())
                .facebook(user.getFacebook())
                .youtube(user.getYoutube())
                .build();

        return new Response<>(200, "Get profile success", profile);
    }


    public Response<String> updateProfile(UpdateProfileRequestDTO request) {
        User user = accountUtils.getCurrentAccount();
        if (user == null) return new Response<>(401, "Please login first", null);

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getAvatar() != null) user.setAvatar(request.getAvatar());
        if (request.getWebsite() != null) user.setWebsite(request.getWebsite());
        if (request.getGithub() != null) user.setGithub(request.getGithub());
        if (request.getLinkedin() != null) user.setLinkedin(request.getLinkedin());
        if (request.getFacebook() != null) user.setFacebook(request.getFacebook());
        if (request.getYoutube() != null) user.setYoutube(request.getYoutube());

        user.setUpdateAt(LocalDateTime.now());
        userRepository.save(user);

        return new Response<>(200, "Update profile success", null);
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
                registerRequestDTO.getFirstName(),
                registerRequestDTO.getLastName(),
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
            if (account.getIsDeleted().equals(true)) {
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

            // Generate token and set it in a cookie
            String token = jwtService.generateToken(loginRequestDTO.getEmail());
            Cookie cookie = jwtService.createTokenCookie(token);
            response.addCookie(cookie);

            String refreshToken = jwtService.generateRefreshToken(loginRequestDTO.getEmail());
            Cookie refreshTokenCookie = jwtService.createRefreshTokenCookie(refreshToken);
            response.addCookie(refreshTokenCookie);

            // Build response
            String responseString = "Login successful";
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
                    200,
                    responseString,
                    null
            );
            return new ResponseEntity<>(loginResponseDTO, HttpStatus.OK);

        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            String errorResponse = "Login failed";
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
                    400,
                    e.getMessage(),
                    errorResponse
            );
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
            account.setIsDeleted(false);
            account.setRole(User.Role.USER);
            userRepository.save(account);

//            Wallet wallet = new Wallet();
//            wallet.setTotal(0D);
//            wallet.setUser(account);
//            walletRepository.save(wallet);

            String responseMessage = "Successful registration, please check your email for verification";
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
            String errorMessage = "Register failed";
            RegisterResponseDTO response = new RegisterResponseDTO(errorMessage, errorCode.getMessage(), errorCode.getCode(), null);
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
            throw new TokenExpiredException("Invalid or expired token!", Instant.now());
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
            User account = tempAccount.orElseThrow(() -> new UsernameNotFoundException("User not found"));
            account.setTokens(token);

            //SEND MAIL
            EmailDetail emailDetail = EmailDetail.builder()
                    .recipient(account.getEmail())
                    .msgBody("Dear " + account.getLastName() + ",\n\n" +
                            "We received a request to reset the password for your account. To complete the process, please click the link below:\n\n" +
                            "<a href=\"https://circuit-project.vercel.app/forgotPassword?" + token + "\">Reset My Password</a>\n\n" +
                            "If you did not request a password reset, please ignore this email or contact support if you have any concerns.\n\n" +
                            "Thank you,\nThe Support Team")
                    .subject("Password Reset Request - Action Required")
                    .name(account.getLastName())
                    .build();
            emailService.sendForgotPasswordEmail(emailDetail);

            userRepository.save(account);
            ForgotPasswordResponse forgotPasswordResponse = new ForgotPasswordResponse("Password reset token generated successfully. Please check your email", null, 200);
            return new ResponseEntity<>(forgotPasswordResponse, HttpStatus.OK);
        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            ForgotPasswordResponse forgotPasswordResponse = new ForgotPasswordResponse("Password reset failed", e.getMessage(), errorCode.getCode());
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
            String email = getString(token);
            // FIND EMAIL IN DATABASE AND UPDATE NEW PASSWORD
            Optional<User> accountOptional = userRepository.findByEmail(email);
            if (accountOptional.isPresent()) {
                User account = accountOptional.get();
                account.setPassword(passwordEncoder.encode(resetPasswordRequest.getNew_password()));
                userRepository.save(account);
            }

            ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse("Password reset token generated successfully.", null, 200);
            return new ResponseEntity<>(resetPasswordResponse, HttpStatus.CREATED);
        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            ResetPasswordResponse resetPasswordResponse = new ResetPasswordResponse("Password reset failed", e.getMessage(), errorCode.getCode());
            return new ResponseEntity<>(resetPasswordResponse, errorCode.getHttpStatus());
        }

    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
