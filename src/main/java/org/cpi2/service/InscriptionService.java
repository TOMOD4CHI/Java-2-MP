package org.cpi2.service;

import org.cpi2.entities.CoursePlan;
import org.cpi2.entities.Inscription;
import org.cpi2.repository.InscriptionRepository;
import org.cpi2.repository.PlanRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Calendar;

public class InscriptionService {
    private final InscriptionRepository inscriptionRepository;
    private final PlanRepository planRepository;

    public InscriptionService() {
        this.inscriptionRepository = new InscriptionRepository();
        this.planRepository = new PlanRepository();
    }

    public Optional<Inscription> getInscriptionById(Integer id) {
        return inscriptionRepository.findById(id);
    }

    public List<Inscription> getAllInscriptions() {
        return inscriptionRepository.findAll();
    }

    public List<Inscription> getInscriptionsByCin(String cin) {
        return inscriptionRepository.findByCin(cin);
    }

    public List<Inscription> getInscriptionsByPaymentStatus(boolean isPaid) {
        return inscriptionRepository.findByPaymentStatus(isPaid);
    }

    public List<Inscription> getInscriptionsByStatus(String status) {
        return inscriptionRepository.findByStatus(status);
    }

    public boolean saveInscription(Inscription inscription) {
        updateNextPaymentDate(inscription);
        return inscriptionRepository.save(inscription);
    }

    public boolean updateInscription(Inscription inscription) {
        updateNextPaymentDate(inscription);
        return inscriptionRepository.update(inscription);
    }

    public boolean updatePaymentStatus(Integer inscriptionId, boolean isPaid) {
        Optional<Inscription> optInscription = inscriptionRepository.findById(inscriptionId);
        if (optInscription.isPresent()) {
            Inscription inscription = optInscription.get();
            inscription.setPaymentStatus(isPaid);

            updateNextPaymentDate(inscription);

            return inscriptionRepository.update(inscription);
        }
        return false;
    }

    private void updateNextPaymentDate(Inscription inscription) {
        // If not paid and has payment cycle, calculate next payment date
        if (!inscription.isPaymentStatus() && inscription.getPaymentCycle() != null) {
            Date nextPaymentDate = calculateNextPaymentDate(new Date(), inscription.getPaymentCycle());
            inscription.setnextPaymentDate(nextPaymentDate);
        } else if (inscription.isPaymentStatus()) {
            // If fully paid, there's no next payment
            inscription.setnextPaymentDate(null);
        }
    }

    public List<Inscription> getInscriptionsWithUpcomingPayments(int daysThreshold) {
        List<Inscription> allInscriptions = inscriptionRepository.findAll();
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_MONTH, daysThreshold);
        Date thresholdDate = calendar.getTime();

        return allInscriptions.stream()
                .filter(inscription -> !inscription.isPaymentStatus() &&
                        inscription.getnextPaymentDate() != null &&
                        inscription.getnextPaymentDate().before(thresholdDate))
                .collect(Collectors.toList());
    }

    public Date calculateNextPaymentDate(Date currentDate, String paymentCycle) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(currentDate);

        switch (paymentCycle.toLowerCase()) {
            case "weekly":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "monthly":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "quarterly":
                calendar.add(Calendar.MONTH, 3);
                break;
            case "annually":
                calendar.add(Calendar.YEAR, 1);
                break;
            default:
                calendar.add(Calendar.MONTH, 1); // Default to monthly
        }

        return calendar.getTime();
    }

    public List<CoursePlan> getAllPlans() {
        return planRepository.findAll();
    }

    public List<CoursePlan> getPlansByCategory(String category) {
        return planRepository.findByCategory(category);
    }

    public Optional<CoursePlan> getPlanById(Integer planId) {
        return planRepository.findById(planId);
    }

    public boolean isValidPaymentCycle(String cycle) {
        if (cycle == null) return false;

        String lowerCycle = cycle.toLowerCase();
        return lowerCycle.equals("weekly") ||
                lowerCycle.equals("monthly") ||
                lowerCycle.equals("quarterly") ||
                lowerCycle.equals("annually");
    }
}