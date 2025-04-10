package org.cpi2.service;


import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.entities.Inscription;
import org.cpi2.entities.Paiement;
import org.cpi2.entities.PaiementExamen;
import org.cpi2.entities.PaiementInscription;
import org.cpi2.repository.PaiementRepository;
import org.cpi2.repository.CandidatRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CandidatService candidatService;
    private final InscriptionService inscriptionService;

    public PaiementService() {
        this.paiementRepository = new PaiementRepository();
        this.candidatService = new CandidatService();
        this.inscriptionService = new InscriptionService(this);
    }
    public PaiementService(InscriptionService inscriptionService) {
        this.paiementRepository = new PaiementRepository();
        this.candidatService = new CandidatService();
        this.inscriptionService = inscriptionService;
    }

    public boolean update(Paiement paiement) throws SQLException {
        if (paiement instanceof PaiementInscription) {
            if (paiement.getMontant() == calculerMontantRestant(((PaiementInscription) paiement).getInscription().getId())) {
                inscriptionService.updatePaymentStatus(((PaiementInscription) paiement).getInscription().getId(), true);
            }
            if (paiement.getMontant() < getPaiementById(paiement.getId()).get().getMontant()) {
                inscriptionService.updatePaymentStatus(((PaiementInscription) paiement).getInscription().getId(), false);
            }
        }
        return paiementRepository.update(paiement);
    }

    public boolean delete(Long id) {
        return paiementRepository.delete(id);
    }

    public List<Paiement> getAllPaiements() {
        return paiementRepository.findAll();
    }

    public Optional<Paiement> getPaiementById(Long id) {
        return paiementRepository.findById(id);
    }


    public List<Paiement> getPaiementsByCandidatId(Long candidatId) {
        return paiementRepository.findAllByCandidat(candidatId);
    }

    public boolean enregistrerPaiement(Paiement paiement) {
        if(paiement instanceof PaiementInscription){
            inscriptionService.updateNextPaymentDate(((PaiementInscription) paiement).getInscription());
        }
        return paiementRepository.save(paiement);
    }


    public double calculerMontantPayer(int inscriptionId) throws SQLException {
        List<PaiementInscription> paiements = paiementRepository.getTranches(inscriptionId);
        return paiements.stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double calculerMontantRestant(int inscriptionId) throws SQLException, DataNotFound {
        double montantPaye = calculerMontantPayer(inscriptionId);
        Optional<Inscription> inscriptionOpt = inscriptionService.getInscriptionById(inscriptionId);
        if (inscriptionOpt.isPresent()) {
            double montantTotal = inscriptionOpt.get().getAmount();
            return montantTotal - montantPaye;
        } else {
            throw new DataNotFound("Inscription not found for id: " + inscriptionId);
        }
    }

    public double calculateTotalPayments(String cin, LocalDate start, LocalDate end) {
        long id = candidatService.CinToId(cin);
        List<Paiement> paiements = paiementRepository.findAllByCandidat(id);
        return paiements.stream()
                .filter(p -> !p.getDatePaiement().isBefore(start) && !p.getDatePaiement().isAfter(end))
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double calculateRegistrationFees(String cin, LocalDate value, LocalDate value1) {
        long id = candidatService.CinToId(cin);
        List<Paiement> paiements = paiementRepository.findAllByCandidat(id);
        return paiements.stream()
                .filter(p -> p instanceof PaiementInscription)
                .filter(p -> !p.getDatePaiement().isBefore(value) && !p.getDatePaiement().isAfter(value1))
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double calculateExamFees(String cin, LocalDate value, LocalDate value1) {
        long id = candidatService.CinToId(cin);
        List<Paiement> paiements = paiementRepository.findAllByCandidat(id);
        return paiements.stream().filter(p -> p instanceof PaiementExamen)
                .filter(p -> !p.getDatePaiement().isBefore(value) && !p.getDatePaiement().isAfter(value1))
                .mapToDouble(Paiement::getMontant)
                .sum();
    }


    //Still many other other methods to implement based on the needs of the application
}