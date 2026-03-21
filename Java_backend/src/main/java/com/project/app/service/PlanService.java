package com.project.app.service;

import com.project.app.entity.Plan;
import com.project.app.repository.PlanRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public List<Plan> getAllPlans() {
        return planRepository.findAllByOrderByOrderAsc();
    }

    public Plan getPlanByPlanId(Long planId) {
        Plan plan = planRepository.findByPlanId(planId);
        if (plan == null) {
            throw new IllegalArgumentException("Plan not found");
        }
        return plan;
    }

    @Transactional
    public List<Plan> updateAllPlans(List<Plan> incomingPlans) {
        if (incomingPlans == null || incomingPlans.isEmpty()) {
            throw new IllegalArgumentException("Invalid data format. Expected a non-empty array of plans.");
        }

        Set<Long> incomingPlanIds = new HashSet<>();
        List<Plan> updatedPlans = new ArrayList<>();

        for (Plan incoming : incomingPlans) {
            validatePlan(incoming);

            if (incomingPlanIds.contains(incoming.getPlanId())) {
                throw new IllegalArgumentException("Duplicate planId found: " + incoming.getPlanId());
            }
            incomingPlanIds.add(incoming.getPlanId());

            if (planRepository.existsByNameIgnoreCaseAndPlanIdNot(incoming.getName(), incoming.getPlanId())) {
                throw new IllegalStateException("Plan name cannot be same , Change Plan Name : " + incoming.getName());
            }

            Plan existing = planRepository.findByPlanId(incoming.getPlanId());
            Plan plan = existing != null ? existing : new Plan();

            plan.setPlanId(incoming.getPlanId());
            plan.setName(incoming.getName());
            plan.setBadge(incoming.getBadge());
            plan.setPrice(incoming.getPrice());
            plan.setActive(Boolean.TRUE.equals(incoming.getActive()));
            plan.setDescription(incoming.getDescription());
            plan.setFeatures(incoming.getFeatures());
            plan.setOrder(incoming.getOrder());

            updatedPlans.add(planRepository.save(plan));
        }

        planRepository.findAll().stream()
            .filter(plan -> !incomingPlanIds.contains(plan.getPlanId()))
            .forEach(planRepository::delete);

        updatedPlans.sort(Comparator.comparing(Plan::getOrder, Comparator.nullsLast(Integer::compareTo)));
        return updatedPlans;
    }

    public Plan updatePlan(Long planId, Plan planDetails) {
        Plan plan = getPlanByPlanId(planId);

        if (planDetails.getName() != null && planRepository.existsByNameIgnoreCaseAndPlanIdNot(planDetails.getName(), planId)) {
            throw new IllegalStateException("Plan name cannot be same , Change Plan Name : " + planDetails.getName());
        }

        if (planDetails.getName() != null) plan.setName(planDetails.getName());
        if (planDetails.getBadge() != null) plan.setBadge(planDetails.getBadge());
        if (planDetails.getPrice() != null) plan.setPrice(planDetails.getPrice());
        if (planDetails.getActive() != null) plan.setActive(planDetails.getActive());
        if (planDetails.getDescription() != null) plan.setDescription(planDetails.getDescription());
        if (planDetails.getFeatures() != null) plan.setFeatures(planDetails.getFeatures());
        if (planDetails.getOrder() != null) plan.setOrder(planDetails.getOrder());

        return planRepository.save(plan);
    }

    public List<Plan> initializePlans() {
        if (!planRepository.findAll().isEmpty()) {
            throw new IllegalArgumentException("Plans already initialized");
        }

        List<Plan> defaults = new ArrayList<>();
        defaults.add(buildPlan(1, "Free", 0, true, 1, "For testing & basic usage",
            List.of("1 Resume Template", "Limited AI Suggestions", "Watermark on Resume", "Community Support")));
        defaults.add(buildPlan(2, "Pro", 299, true, 2, "Best for students & professionals",
            List.of("Unlimited Templates", "Full AI Resume Writing", "No Watermark", "PDF & DOCX Export", "Priority Support")));
        defaults.add(buildPlan(3, "Ultra Pro", 999, true, 3, "One-time payment",
            List.of("All Pro Features", "Lifetime Access", "Priority Support", "Future Updates")));
        return planRepository.saveAll(defaults);
    }

    private Plan buildPlan(long planId, String name, int price, boolean active, int order, String description, List<String> features) {
        Plan plan = new Plan();
        plan.setPlanId(planId);
        plan.setName(name);
        plan.setPrice(price);
        plan.setActive(active);
        plan.setOrder(order);
        plan.setDescription(description);
        plan.setFeatures(features);
        return plan;
    }

    private void validatePlan(Plan plan) {
        if (plan == null || plan.getPlanId() == null || isBlank(plan.getName()) || plan.getPrice() == null) {
            throw new IllegalArgumentException("Each plan must have planId, name, and price");
        }
        if (isBlank(plan.getDescription())) {
            throw new IllegalArgumentException("Each plan must have description");
        }
        if (plan.getOrder() == null) {
            throw new IllegalArgumentException("Each plan must have order");
        }
        if (Objects.requireNonNullElse(plan.getFeatures(), List.<String>of()).isEmpty()) {
            throw new IllegalArgumentException("Each plan must have at least one feature");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
