package com.project.app.controller;

import com.project.app.dto.*;
import com.project.app.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@Valid @RequestBody RegisterRequest request) {
        ApiResponse<String> response = authService.register(request);
        return ResponseEntity.status(response.getSuccess() ? 201 : 400).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request, 
                                                          HttpServletResponse response) {
        ApiResponse<LoginResponse> authResponse = authService.login(request);
        
        if (authResponse.getSuccess() && authResponse.getData() != null) {
            // Set cookie if login successful
            Cookie cookie = new Cookie("token", authResponse.getData().getToken());
            cookie.setHttpOnly(true);
            cookie.setSecure(false); // Set to true in production with HTTPS
            cookie.setPath("/");
            cookie.setMaxAge(request.getRememberMe() ? 30 * 24 * 60 * 60 : 2 * 60 * 60); // 30 days or 2 hours
            response.addCookie(cookie);
        }
        
        return ResponseEntity.status(authResponse.getSuccess() ? 200 : 401).body(authResponse);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestParam String email) {
        ApiResponse<String> response = authService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<ApiResponse<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            @RequestHeader("X-User-Id") Long userId,
                                                            HttpServletResponse httpResponse) {
        ApiResponse<String> response = authService.changePassword(userId, request);
        
        if (response.getSuccess()) {
            // Clear token cookie on password change
            Cookie cookie = new Cookie("token", "");
            cookie.setHttpOnly(true);
            cookie.setSecure(false);
            cookie.setPath("/");
            cookie.setMaxAge(0);
            httpResponse.addCookie(cookie);
        }
        
        return ResponseEntity.ok(response);
    }
}
