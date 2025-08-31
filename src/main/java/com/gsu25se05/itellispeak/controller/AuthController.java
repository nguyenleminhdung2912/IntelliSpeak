package com.gsu25se05.itellispeak.controller;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.auth.reponse.*;
import com.gsu25se05.itellispeak.dto.auth.request.*;
import com.gsu25se05.itellispeak.exception.ErrorCode;
import com.gsu25se05.itellispeak.exception.auth.AuthAppException;
import com.gsu25se05.itellispeak.jwt.JWTService;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/auth")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class AuthController {

    @Autowired
    AuthService authService;

    @Value("${BASE_FRONTEND_URL}")
    private String frontendBaseUrl;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @Operation(summary = "Lấy thông tin profile user")
    @GetMapping("/profile")
    public Response<UserProfileDTO> getProfile() {
        return authService.getCurrentUserProfile();
    }

    @Operation(summary = "Cập nhật profile cá nhân")
    @PutMapping("/profile")
    public ResponseEntity<Response<UserDTO>> updateProfile(@RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerAccount(@Valid @RequestBody RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
        String email = registerRequestDTO.getEmail();
        String domain = email.substring(email.indexOf("@") + 1);

        List<String> allowedDomains = List.of(
                "gmail.com", "outlook.com", "hotmail.com",
                "yahoo.com", "icloud.com", "protonmail.com",
                "fpt.edu.vn", "stu.edu.vn", "hust.edu.vn"
        );


        if (!allowedDomains.contains(domain)) {
            throw new AuthAppException(ErrorCode.DOMAIN_NOT_VALID);
        }
        return authService.registerAccount(registerRequestDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO loginRequestDTO, HttpServletResponse response) {
        return authService.checkLogin(loginRequestDTO, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        return authService.logout(response);
    }

    @GetMapping("/verify/{token}")
    public ResponseEntity<Void> activateAccount(@PathVariable String token) {
        try {
            if (authService.verifyAccount(token)) {
                return ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(frontendBaseUrl + "/login"))
                        .build();
            }
            return ResponseEntity.badRequest().build();
        } catch (TokenExpiredException ex) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendBaseUrl + "/verify?status=expired"))
                    .build();
        } catch (Exception ex) {
            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return authService.forgotPassword(forgotPasswordRequest);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest,
            @RequestParam("token") String token
    ) {
        return authService.resetPassword(resetPasswordRequest, token);
    }


//    @GetMapping("/cookies")
//    public ResponseEntity<Map<String, String>> getCookies(HttpServletRequest request) {
//        Map<String, String> cookieMap = new HashMap<>();
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null) {
//            for (Cookie cookie : cookies) {
//                cookieMap.put(cookie.getName(), cookie.getValue());
//            }
//        } else {
//            cookieMap.put("message", "No cookies found");
//        }
//        return ResponseEntity.ok(cookieMap);
//    }
}
