package com.gsu25se05.itellispeak.utils;

import com.gsu25se05.itellispeak.entity.User;
import com.gsu25se05.itellispeak.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AccountUtils {

    private static final String COOKIE_NAME = "JWT_TOKEN";

    private final HttpServletRequest request;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;

    @Autowired
    public AccountUtils(HttpServletRequest request,
                        JwtUtils jwtUtils,
                        UserRepository userRepository) {
        this.request = request;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    public User getCurrentAccount() {
        // 1. Try SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof User user) {
                System.err.println("Found User in SecurityContext: " + user.getEmail());
                return user;
            }
            if (principal instanceof UserDetails ud) {
                System.err.println("Found UserDetails in SecurityContext: " + ud.getUsername());
                User user = userRepository.findByEmail(ud.getUsername()).orElse(null);
                System.err.println("User from repository: " + (user != null ? user.getEmail() : "null"));
                return user;
            }
        } else {
            System.err.println("SecurityContext: No authentication or not authenticated");
        }

        // 2. Try token from Cookie or Header
        String token = getTokenFromCookies();
        if (token == null) {
            token = getTokenFromHeader();
        }

        if (token != null) {
            try {
                String email = jwtUtils.extractUserId(token);
                User user = userRepository.findByEmail(email).orElse(null);
                return user;
            } catch (Exception e) {
                System.err.println("Failed to process token: " + e.getMessage());
            }
        } else {
            System.err.println("No token found in cookies or header");
        }
        return null;
    }

    private String getTokenFromCookies() {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            System.err.println("No cookies found in request");
            return null;
        }
        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        System.err.println("JWT_TOKEN cookie not found");
        return null;
    }

    private String getTokenFromHeader() {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            System.err.println("Found Authorization header token: " + token);
            return token;
        }
        System.err.println("Authorization header missing or invalid");
        return null;
    }
}