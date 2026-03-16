package com.project.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "plans")
public class Plan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer planId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column
    private String badge;

    @Column(nullable = false)
    private Integer price = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(length = 2000, nullable = false)
    private String description;

    @ElementCollection
    @CollectionTable(name = "plan_features", joinColumns = @JoinColumn(name = "plan_id"))
    private List<String> features;

    @Column(nullable = false)
    private Integer order;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
