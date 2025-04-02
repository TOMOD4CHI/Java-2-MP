package org.cpi2.service;


import org.cpi2.entities.Paiement;
import org.cpi2.entities.PaiementInscription;
import org.cpi2.repository.PaiementRepository;
import org.cpi2.repository.CandidatRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CandidatRepository candidatRepository;
    private final InscriptionService inscriptionService;

    public PaiementService() {
        this.paiementRepository = new PaiementRepository();
        this.candidatRepository = new CandidatRepository();
        this.inscriptionService = new InscriptionService();
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
        return paiementRepository.save(paiement);
    }


    public double calculerMontantPayer(int inscriptionId) throws SQLException {
        List<PaiementInscription> paiements = paiementRepository.getTranches(inscriptionId);
        return paiements.stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double calculerMontantRestant(int inscriptionId) throws SQLException {
        double montantPaye = calculerMontantPayer(inscriptionId);
        double montantTotal = inscriptionService.getInscriptionById(inscriptionId).get().getAmount();
        return montantTotal - montantPaye;
    }


    //Still many other other methods to implement based on the needs of the application
}