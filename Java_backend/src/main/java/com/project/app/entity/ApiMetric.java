package com.project.app.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity

@Table(name = "api_metrics")
public class ApiMetric {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String endpoint;
    
    @Column(nullable = false)
    private String method;
    
    private Long responseTime;
    
    private Integer statusCode;
    
    private String userAgent;
    
    private String ipAddress;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    // Manual getters/setters to fix compilation issues
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    
    public Long getResponseTime() { return responseTime; }
    public void setResponseTime(Long responseTime) { this.responseTime = responseTime; }
    
    public Integer getStatusCode() { return statusCode; }
    public void setStatusCode(Integer statusCode) { this.statusCode = statusCode; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public enum Actor {
        USER, ADMIN, SYSTEM
    }
}
