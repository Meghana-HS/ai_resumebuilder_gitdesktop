package com.project.app.controller;

import com.project.app.entity.Plan;
import com.project.app.service.PlanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        try {
            return ResponseEntity.ok(planService.getAllPlans());
        } catch (Exception exception) {
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(planService.getPlanByPlanId(id));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("message", "Plan not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to fetch plan", "error", exception.getMessage()));
        }
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateAllPlans(@Valid @RequestBody List<Plan> plans) {
        try {
            List<Plan> updatedPlans = planService.updateAllPlans(plans);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Plans updated successfully");
            response.put("plans", updatedPlans);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update plans", "error", exception.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updatePlan(@PathVariable Long id, @RequestBody Plan planDetails) {
        try {
            Plan updatedPlan = planService.updatePlan(id, planDetails);
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("message", "Plan updated successfully");
            response.put("plan", updatedPlan);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(409).body(Map.of("message", exception.getMessage()));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.status(404).body(Map.of("message", "Plan not found"));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to update plan", "error", exception.getMessage()));
        }
    }

    @PostMapping("/initialize")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> initializePlans() {
        try {
            List<Plan> plans = planService.initializePlans();
            return ResponseEntity.status(201).body(Map.of(
                "message", "Default plans initialized successfully",
                "plans", plans
            ));
        } catch (IllegalArgumentException exception) {
            return ResponseEntity.badRequest().body(Map.of("message", "Plans already initialized", "count", planService.getAllPlans().size()));
        } catch (Exception exception) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to initialize plans", "error", exception.getMessage()));
        }
    }
}
