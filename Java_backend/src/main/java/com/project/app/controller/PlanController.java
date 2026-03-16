package com.project.app.controller;

import com.project.app.dto.ApiResponse;
import com.project.app.entity.Plan;
import com.project.app.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @Autowired
    private PlanService planService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Plan>>> getAllActivePlans() {
        List<Plan> plans = planService.getAllActivePlans();
        return ResponseEntity.ok(ApiResponse.success("Plans retrieved", plans));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<Plan>>> getAllPlans() {
        List<Plan> plans = planService.getAllPlans();
        return ResponseEntity.ok(ApiResponse.success("All plans retrieved", plans));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Plan>> getPlanById(@PathVariable Long id) {
        Optional<Plan> plan = planService.getPlanById(id);
        return plan.map(value -> ResponseEntity.ok(ApiResponse.success("Plan retrieved", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/plan-id/{planId}")
    public ResponseEntity<ApiResponse<Plan>> getPlanByPlanId(@PathVariable Integer planId) {
        Optional<Plan> plan = planService.getPlanByPlanId(planId);
        return plan.map(value -> ResponseEntity.ok(ApiResponse.success("Plan retrieved", value)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Plan>> createPlan(@RequestBody Plan plan) {
        Plan createdPlan = planService.createPlan(plan);
        return ResponseEntity.status(201).body(ApiResponse.success("Plan created", createdPlan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Plan>> updatePlan(@PathVariable Long id, @RequestBody Plan planDetails) {
        try {
            Plan updatedPlan = planService.updatePlan(id, planDetails);
            return ResponseEntity.ok(ApiResponse.success("Plan updated", updatedPlan));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deletePlan(@PathVariable Long id) {
        try {
            planService.deletePlan(id);
            return ResponseEntity.ok(ApiResponse.success("Plan deleted"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
