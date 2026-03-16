package com.project.app.repository;

import com.project.app.entity.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByActiveTrueOrderByOrder();
    Plan findByPlanId(Integer planId);
    Plan findByName(String name);
}
