package com.project.app.dto;




public class LoginResponse {
    private Boolean success;
    private String message;
    private String token;
    private Long userID;
    private Boolean isAdmin;
    
    // Manual getters/setters to fix compilation issues
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public Long getUserID() { return userID; }
    public void setUserID(Long userID) { this.userID = userID; }
    
    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean isAdmin) { this.isAdmin = isAdmin; }
}
