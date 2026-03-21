package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.dto.UserProfileDto;
import com.project.app.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        return ResponseEntity.ok(ApiResponse.success("Dashboard data retrieved", userService.getProfile(getCurrentUserId())));
    }

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile() {
        try {
            return ResponseEntity.ok(Map.of("user", userService.getProfile(getCurrentUserId())));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to fetch profile"));
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDto profileDto) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Profile updated",
                "user", userService.updateProfile(getCurrentUserId(), profileDto)
            ));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update profile"));
        }
    }

    @GetMapping("/profile/{id}")
    public ResponseEntity<?> getUserName(@PathVariable Long id) {
        try {
            UserProfileDto profile = userService.getUserName(id);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "username", profile.getUsername()
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of(
                "success", false,
                "message", "User not found"
            ));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Server error"
            ));
        }
    }

    @PostMapping("/request-admin")
    public ResponseEntity<?> requestAdminAccess() {
        try {
            UserProfileDto user = userService.requestAdminAccess(getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "Admin request submitted successfully", "user", user));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to submit admin request", "error", exception.getMessage()));
        }
    }

    @GetMapping({"", "/"})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserProfileDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserProfileDto profileDto) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "User updated successfully",
                "user", userService.updateUser(id, profileDto, getCurrentUserId())
            ));
        } catch (IllegalArgumentException exception) {
            String message = "User not found".equals(exception.getMessage()) ? "User not found" : exception.getMessage();
            int status = "User not found".equals(message) ? 404 : 400;
            return ResponseEntity.status(status).body(Map.of("message", message));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Update failed", "error", exception.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id, getCurrentUserId());
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("message", "User not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Delete failed", "error", exception.getMessage()));
        }
    }

    @PutMapping("/approve-admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> approveAdminRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Admin request approved",
                "user", userService.approveAdminRequest(id, getCurrentUserId())
            ));
        } catch (IllegalArgumentException exception) {
            String message = "User not found".equals(exception.getMessage()) ? "User not found" : exception.getMessage();
            int status = "User not found".equals(message) ? 404 : 400;
            return ResponseEntity.status(status).body(Map.of("message", message));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to approve admin request", "error", exception.getMessage()));
        }
    }

    @PutMapping("/reject-admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rejectAdminRequest(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(Map.of(
                "message", "Admin request rejected",
                "user", userService.rejectAdminRequest(id, getCurrentUserId())
            ));
        } catch (IllegalArgumentException exception) {
            String message = "User not found".equals(exception.getMessage()) ? "User not found" : exception.getMessage();
            int status = "User not found".equals(message) ? 404 : 400;
            return ResponseEntity.status(status).body(Map.of("message", message));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to reject admin request", "error", exception.getMessage()));
        }
    }
}
