package com.project.app.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserProfileDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String location;
    private String bio;
    private String github;
    private String linkedin;
    private String plan;
    private Boolean isAdmin;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private Integer profileViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
