package com.gsu25se05.itellispeak.service;

import com.gsu25se05.itellispeak.dto.auth.reponse.LoginResponseDTO;
import com.gsu25se05.itellispeak.dto.auth.reponse.RegisterResponseDTO;
import com.gsu25se05.itellispeak.dto.auth.request.LoginRequestDTO;
import com.gsu25se05.itellispeak.dto.auth.request.RegisterRequestDTO;
//import com.gsu25se05.itellispeak.email.EmailDetail;
//import com.gsu25se05.itellispeak.email.EmailService;
import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.jwt.JWTService;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.utils.AccountUtils;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

//    @Autowired
//    private EmailService emailService;

    @Autowired
    private AccountUtils accountUtils;

    @Autowired
    private UserRepository userRepository;

    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    private @NotNull User convertToUser(RegisterRequestDTO registerRequestDTO) {
        User account = new User(
                registerRequestDTO.getFirstName(),
                registerRequestDTO.getLastName(),
                registerRequestDTO.getEmail(),
                registerRequestDTO.getRole()
        );
        account.setAvatar(defaultAvatar);

        account.setPassword(passwordEncoder.encode(registerRequestDTO.getPassword()));
        return account;
    }

    public ResponseEntity<LoginResponseDTO> checkLogin(LoginRequestDTO loginRequestDTO) {
        try {
            // GET EMAIL BY REQUEST DTO AND VALIDATION EMAIL
            User account = findUserByEmail(loginRequestDTO.getEmail());

            if (account == null) {
                throw new AuthAppException(ErrorCode.EMAIL_NOT_FOUND);
            }
            if (account.getIsDeleted().equals(true)) {
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

            User returnAccount = (User) authentication.getPrincipal();
            // CALL FUNC || GENERATE TOKEN (1DAY) AND REFRESH TOKEN (7DAYS)
            account.setTokens(jwtService.generateToken(account.getEmail()));
            account.setRefreshToken(jwtService.generateRefreshToken(account.getEmail()));

            String responseString = "Login successful";
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
                    200,
                    responseString,
                    null,
                    account.getRole(),
                    returnAccount.getTokens(),
                    returnAccount.getRefreshToken()
            );
            return new ResponseEntity<>(loginResponseDTO, HttpStatus.OK);


        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            String errorResponse = "Login failed";
            LoginResponseDTO loginResponseDTO = new LoginResponseDTO(
                    400,
                    e.getMessage(),
                    errorResponse,
                    null,
                    null,
                    null
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
            User account = convertToUser(registerRequestDTO);
            userRepository.save(account);

            String responseMessage = "Successful registration, please check your email for verification";
            RegisterResponseDTO response = new RegisterResponseDTO(responseMessage, null, 201, registerRequestDTO.getEmail());

//            //Send email here
//            EmailDetail emailDetail = EmailDetail.builder()
//                    .recipient(account.getEmail())
//                    .msgBody("Please verify your account to continue.")
//                    .subject("Please verify your account!")
//                    .name(account.getUsername())
//                    .build();
//            emailService.sendVerifyEmail(emailDetail);

            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (AuthAppException e) {
            ErrorCode errorCode = e.getErrorCode();
            String errorMessage = "Register failed";
            RegisterResponseDTO response = new RegisterResponseDTO(errorMessage, errorCode.getMessage(), errorCode.getCode(), null);
            return new ResponseEntity<>(response, errorCode.getHttpStatus());
        }
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

}
