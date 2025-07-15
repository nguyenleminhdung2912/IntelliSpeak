package com.gsu25se05.itellispeak.controller;

import com.gsu25se05.itellispeak.dto.Response;
import com.gsu25se05.itellispeak.dto.auth.reponse.*;
import com.gsu25se05.itellispeak.dto.auth.request.*;
import com.gsu25se05.itellispeak.jwt.JWTService;
import com.gsu25se05.itellispeak.repository.UserRepository;
import com.gsu25se05.itellispeak.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("**")
@SecurityRequirement(name = "api")
public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    private UserRepository accountRepository;

    @Autowired
    private JWTService jwtService;

    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;

    @GetMapping("/profile")
    public Response<UserProfileDTO> getProfile() {
        return authService.getCurrentUserProfile();
    }

    @Operation(summary = "Cập nhật profile cá nhân")
    @PutMapping("/profile")
    public ResponseEntity<Response<String>> updateProfile(@RequestBody UpdateProfileRequestDTO request) {
        return ResponseEntity.ok(authService.updateProfile(request));
    }

    @PostMapping("/register")
    public ResponseEntity<RegisterResponseDTO> registerAccount(@Valid @RequestBody RegisterRequestDTO registerRequestDTO, HttpServletResponse response) {
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
    public ResponseEntity<Void> activateAccount(@PathVariable String token) throws Exception {
        if (authService.verifyAccount(token)) {
            return ResponseEntity.status(HttpStatus.FOUND).location(URI.create("http://localhost:5173/login")).build();
        }
        return null;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ForgotPasswordResponse> forgotPassword(@RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        return authService.forgotPassword(forgotPasswordRequest);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResetPasswordResponse> resetPassword(@RequestParam("token") String token, @RequestBody ResetPasswordRequest resetPasswordRequest) {
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
