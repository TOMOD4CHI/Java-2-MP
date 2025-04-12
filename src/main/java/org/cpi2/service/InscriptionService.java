package org.cpi2.service;

import org.cpi2.entities.CoursePlan;
import org.cpi2.entities.Inscription;
import org.cpi2.repository.InscriptionRepository;
import org.cpi2.repository.PlanRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class InscriptionService {
    private final InscriptionRepository inscriptionRepository;
    private final PlanRepository planRepository;
    private final PresenceService presenceService = new PresenceService();
    private final CandidatService candidatService;


    

    

    public InscriptionService() {
        this.inscriptionRepository = new InscriptionRepository();
        this.planRepository = new PlanRepository();
        
        this.candidatService = new CandidatService();
    }
    public InscriptionService(PaiementService paiementService) {
        this.inscriptionRepository = new InscriptionRepository();
        this.planRepository = new PlanRepository();
        
        this.candidatService = new CandidatService();
    }
    public InscriptionService(CandidatService candidatService) {
        this.inscriptionRepository = new InscriptionRepository();
        this.planRepository = new PlanRepository();
        this.candidatService = candidatService;
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
        return inscriptionRepository.save(inscription);
    }

    public boolean updateInscription(Inscription inscription) {
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

    public void updateNextPaymentDate(Inscription inscription) {
        if (!Objects.equals(inscription.getPaymentCycle(), "Totale")) {
            if (!inscription.isPaymentStatus() && inscription.getPaymentCycle() != null) {
                Date nextPaymentDate = calculateNextPaymentDate(inscription.getnextPaymentDate()==null ? java.sql.Date.valueOf(LocalDate.now()):inscription.getnextPaymentDate(), inscription.getPaymentCycle());
                inscription.setnextPaymentDate((java.sql.Date) nextPaymentDate);
            } else if (inscription.isPaymentStatus() ) {
                inscription.setnextPaymentDate(null);
            }
        } else {
            if(inscription.isPaymentStatus()) {
                inscription.setnextPaymentDate(null);
            } else {
                inscription.setnextPaymentDate(java.sql.Date.valueOf(LocalDate.now()));
            }
        }
        inscriptionRepository.update(inscription);
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

        switch (paymentCycle.toLowerCase().trim()) {
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
                calendar.add(Calendar.MONTH, 1); 
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
    public boolean isInscriptionCodeDone(int id){
        Inscription inscription = getInscriptionById(id).orElse(null);
        if(inscription == null) return false;
        return inscription.getPlan().getGetNbreSeanceCode() == presenceService.getCountInscriptioinPerSession(candidatService.CinToId(inscription.getCin()), "code", inscription.getInscriptioDate());
    }
    public boolean isInscriptionConduiteDone(int id){
        Inscription inscription = getInscriptionById(id).orElse(null);
        if(inscription == null) return false;
        return inscription.getPlan().getNbreSeanceConduite() == presenceService.getCountInscriptioinPerSession(candidatService.CinToId(inscription.getCin()), "conduite", inscription.getInscriptioDate());
    }
}