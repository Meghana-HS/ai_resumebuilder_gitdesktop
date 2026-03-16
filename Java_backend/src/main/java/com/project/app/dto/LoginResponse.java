package com.project.app.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private Boolean success;
    private String message;
    private String token;
    private Long userID;
    private Boolean isAdmin;
}
