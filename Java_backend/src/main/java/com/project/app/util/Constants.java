package com.project.app.util;

public class Constants {
    
    public static final String JWT_SECRET = "mySecretKey";
    public static final String ADMIN_EMAIL = "admin@example.com";
    
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int DEFAULT_PAGE_NUMBER = 0;
    
    public static final String COOKIE_TOKEN_NAME = "token";
    public static final int COOKIE_MAX_AGE_REMEMBER = 30 * 24 * 60 * 60; // 30 days
    public static final int COOKIE_MAX_AGE_SESSION = 2 * 60 * 60; // 2 hours
    
    public static final String UPLOADS_DIR = "uploads";
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    
    private Constants() {
        // Utility class
    }
}
