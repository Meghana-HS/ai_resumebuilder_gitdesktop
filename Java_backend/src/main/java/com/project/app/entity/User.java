package com.project.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private Boolean isAdmin = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdminRequestStatus adminRequestStatus = AdminRequestStatus.NONE;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private String plan = "Free";

    private LocalDateTime lastLogin;

    @Column
    private String fullName = "";

    @Column
    private String phone = "";

    @Column
    private String location = "";

    @Column(length = 1000)
    private String bio = "";

    @Column
    private String github = "";

    @Column
    private String linkedin = "";

    @Column(nullable = false)
    private Integer profileViews = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum AdminRequestStatus {
        NONE, PENDING, APPROVED, REJECTED
    }
}
