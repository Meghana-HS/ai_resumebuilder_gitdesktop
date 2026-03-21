package com.project.app.dto;


import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;


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
    private String adminRequestStatus;
    private Boolean isActive;
    private LocalDateTime lastLogin;
    private Integer profileViews;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Manual getters/setters to fix compilation issues
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("_id")
    public Long getMongoStyleId() { return id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public String getGithub() { return github; }
    public void setGithub(String github) { this.github = github; }

    public String getLinkedin() { return linkedin; }
    public void setLinkedin(String linkedin) { this.linkedin = linkedin; }

    public String getPlan() { return plan; }
    public void setPlan(String plan) { this.plan = plan; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }

    public String getAdminRequestStatus() { return adminRequestStatus; }
    public void setAdminRequestStatus(String adminRequestStatus) { this.adminRequestStatus = adminRequestStatus; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public LocalDateTime getLastLogin() { return lastLogin; }
    public void setLastLogin(LocalDateTime lastLogin) { this.lastLogin = lastLogin; }

    public Integer getProfileViews() { return profileViews; }
    public void setProfileViews(Integer profileViews) { this.profileViews = profileViews; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
