package org.cpi2.service;

import org.cpi2.entities.CoursePlan;
import org.cpi2.entities.Inscription;
import org.cpi2.repository.InscriptionRepository;
import org.cpi2.repository.PlanRepository;

import java.util.*;
import java.util.stream.Collectors;

public class InscriptionService {
    private final InscriptionRepository inscriptionRepository;
    private final PlanRepository planRepository;
    private final PaiementService paiementService;

    public InscriptionService() {
        this.inscriptionRepository = new InscriptionRepository();
        this.planRepository = new PlanRepository();
        this.paiementService = new PaiementService();
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
            case "hebdomadaire":
                calendar.add(Calendar.WEEK_OF_YEAR, 1);
                break;
            case "mensuel":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "trimestriel":
                calendar.add(Calendar.MONTH, 3);
                break;
            default:
                calendar.add(Calendar.MONTH, 1); // Default to monthly
        }

        return calendar.getTime();
    }

    public List<CoursePlan> getAllPlans() {
        return planRepository.findAll();
    }

    public List<CoursePlan> getPlansByTypePermis(String category) {
        return planRepository.findByTypePermis(category);
    }

    public Optional<CoursePlan> getPlanById(Integer planId) {
        return planRepository.findById(planId);
    }

    public List<Inscription> getActifInscription() {
        List<Inscription> res = new ArrayList<>(getAllInscriptions().stream()
                .filter(inscription -> inscription.getStatus().equals("En Cours"))
                .toList());
        res.sort(Comparator.comparing(Inscription::getInscriptioDate));
        return res ;
    }

    public List<Inscription> getActifandPayedInscription() {
        return getActifInscription().stream()
                .filter(Inscription::isPaymentStatus)
                .toList();
    }
    public List<Inscription> getUnpaidInscription() {
        return getActifInscription().stream()
                .filter(inscription -> !inscription.isPaymentStatus())
                .toList();
    }
    public List<Inscription> getActifInscirptionBycin(String cin) {
        return getActifInscription().stream()
                .filter(inscription -> inscription.getCin().equals(cin))
                .toList();
    }

    public List<Inscription> getActifandPayedInscriptionBycin(String cin) {
        return getActifandPayedInscription().stream()
                .filter(inscription -> inscription.getCin().equals(cin))
                .toList();
    }

    public boolean haveActifInscription(String cin) {
        return !getActifInscirptionBycin(cin).isEmpty();
    }

    public boolean haveActifandUnpayedInscription(String cin) {
        return getUnpaidInscription().stream().anyMatch(inscription -> inscription.getCin().equals(cin) && inscription.getStatus().equals("En Cours"));
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