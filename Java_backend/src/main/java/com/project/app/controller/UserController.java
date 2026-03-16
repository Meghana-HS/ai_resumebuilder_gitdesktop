package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.UserProfileDto;
import com.project.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Long.parseLong(authentication.getName());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<UserProfileDto>> getDashboardData() {
        Long userId = getCurrentUserId();
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved", profile));
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile() {
        Long userId = getCurrentUserId();
        UserProfileDto profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success("Profile retrieved", profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateProfile(@RequestBody UserProfileDto profileDto) {
        Long userId = getCurrentUserId();
        UserProfileDto updatedProfile = userService.updateProfile(userId, profileDto);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", updatedProfile));
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getUserName(@PathVariable Long id) {
        UserProfileDto profile = userService.getUserName(id);
        return ResponseEntity.ok(ApiResponse.success("User profile retrieved", profile));
    }

    @PostMapping("/request-admin")
    public ResponseEntity<ApiResponse<String>> requestAdminAccess() {
        Long userId = getCurrentUserId();
        userService.requestAdminAccess(userId);
        return ResponseEntity.ok(ApiResponse.success("Admin access requested"));
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserProfileDto>>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<UserProfileDto> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved", users));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> updateUser(@PathVariable Long id, 
                                                                 @RequestBody UserProfileDto profileDto) {
        UserProfileDto updatedUser = userService.updateUser(id, profileDto);
        return ResponseEntity.ok(ApiResponse.success("User updated", updatedUser));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deleted"));
    }

    @PutMapping("/approve-admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> approveAdminRequest(@PathVariable Long id) {
        UserProfileDto user = userService.approveAdminRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Admin request approved", user));
    }

    @PutMapping("/reject-admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserProfileDto>> rejectAdminRequest(@PathVariable Long id) {
        UserProfileDto user = userService.rejectAdminRequest(id);
        return ResponseEntity.ok(ApiResponse.success("Admin request rejected", user));
    }
}
