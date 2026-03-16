package com.project.app.service;

import com.project.app.entity.Plan;
import com.project.app.repository.PlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PlanService {

    @Autowired
    private PlanRepository planRepository;

    public List<Plan> getAllActivePlans() {
        return planRepository.findByActiveTrueOrderByOrder();
    }

    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    public Optional<Plan> getPlanById(Long id) {
        return planRepository.findById(id);
    }

    public Optional<Plan> getPlanByPlanId(Integer planId) {
        Plan plan = planRepository.findByPlanId(planId);
        return Optional.ofNullable(plan);
    }

    public Optional<Plan> getPlanByName(String name) {
        Plan plan = planRepository.findByName(name);
        return Optional.ofNullable(plan);
    }

    public Plan createPlan(Plan plan) {
        return planRepository.save(plan);
    }

    public Plan updatePlan(Long id, Plan planDetails) {
        Optional<Plan> planOpt = planRepository.findById(id);
        if (planOpt.isEmpty()) {
            throw new RuntimeException("Plan not found");
        }

        Plan plan = planOpt.get();
        plan.setPlanId(planDetails.getPlanId());
        plan.setName(planDetails.getName());
        plan.setBadge(planDetails.getBadge());
        plan.setPrice(planDetails.getPrice());
        plan.setActive(planDetails.getActive());
        plan.setDescription(planDetails.getDescription());
        plan.setFeatures(planDetails.getFeatures());
        plan.setOrder(planDetails.getOrder());

        return planRepository.save(plan);
    }

    public void deletePlan(Long id) {
        if (!planRepository.existsById(id)) {
            throw new RuntimeException("Plan not found");
        }
        planRepository.deleteById(id);
    }
}
