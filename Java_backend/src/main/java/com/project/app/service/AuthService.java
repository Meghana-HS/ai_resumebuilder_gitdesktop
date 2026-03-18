package com.project.app.service;

import com.project.app.dto.*;
import com.project.app.entity.Notification;
import com.project.app.entity.User;
import com.project.app.repository.NotificationRepository;
import com.project.app.repository.UserRepository;
import com.project.app.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private NotificationRepository notificationRepository;

    @Value("${admin.email}")
    private String adminEmail;

    public ApiResponse<String> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ApiResponse.error("User already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsAdmin(request.getEmail().equals(adminEmail));
        user.setIsActive(true);

        userRepository.save(user);

        return ApiResponse.success("User created successfully");
    }

    public ApiResponse<LoginResponse> login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOpt.get();
        if (!user.getIsActive()) {
            return ApiResponse.error("Your account is deactivated");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ApiResponse.error("Invalid credentials");
        }

        // Handle first login notifications
        if (user.getLastLogin() == null) {
            // Admin notification
            Notification adminNotification = new Notification();
            adminNotification.setType("FIRST_LOGIN");
            adminNotification.setMessage(user.getUsername() + " logged in for the first time");
            adminNotification.setUser(user);
            adminNotification.setActor(Notification.Actor.USER);
            notificationRepository.save(adminNotification);

            // User notification
            Notification userNotification = new Notification();
            userNotification.setType("FIRST_LOGIN");
            userNotification.setMessage("Welcome to UptoSkills AI Resume Builder 🎉");
            userNotification.setUser(user);
            userNotification.setActor(Notification.Actor.SYSTEM);
            notificationRepository.save(userNotification);
        }

        // Update last login
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        // Generate JWT token
        String token = jwtUtil.generateToken(user.getId(), user.getIsAdmin(), request.getRememberMe());

        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setMessage("Login successful");
        response.setToken(token);
        response.setUserID(user.getId());
        response.setIsAdmin(user.getIsAdmin());

        return ApiResponse.success("Login successful", response);
    }

    public ApiResponse<String> forgotPassword(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        // TODO: Implement email sending logic
        return ApiResponse.success("Password reset link sent (simulated)");
    }

    public ApiResponse<String> changePassword(Long userId, ChangePasswordRequest request) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ApiResponse.error("User not found");
        }

        User user = userOpt.get();

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ApiResponse.error("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return ApiResponse.success("Password changed successfully. Please login again.");
    }
}
