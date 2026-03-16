package com.project.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Data
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
    
    public enum Actor {
        USER, ADMIN, SYSTEM
    }
}
