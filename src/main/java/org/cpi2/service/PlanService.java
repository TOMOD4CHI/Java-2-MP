package org.cpi2.service;

import org.cpi2.entities.CoursePlan;
import org.cpi2.repository.PlanRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PlanService {
    private final PlanRepository planRepository;

    public PlanService() {
        this.planRepository = new PlanRepository();
    }

    public Optional<CoursePlan> findPlanById(Integer id) {
        return planRepository.findById(id);
    }

    public List<CoursePlan> getAllPlans() {
        return planRepository.findAll();
    }

    public List<CoursePlan> getPlansByTypePermis(String category) {
        return planRepository.findByTypePermis(category);
    }

    public Optional<CoursePlan> getPlanByName(String name) {
        return planRepository.findByName(name);
    }
}