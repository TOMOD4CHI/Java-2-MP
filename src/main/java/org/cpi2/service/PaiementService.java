package org.cpi2.service;


import org.cpi2.entitties.Inscription;
import org.cpi2.entitties.Paiement;
import org.cpi2.entitties.PaiementInscription;
import org.cpi2.entitties.StatutPaiement;
import org.cpi2.repository.PaiementRepository;
import org.cpi2.repository.CandidatRepository;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;


public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CandidatRepository candidatRepository;

    public PaiementService() {
        this.paiementRepository = new PaiementRepository();
        this.candidatRepository = new CandidatRepository();
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

    public double calculerMontantRestant(int inscriptionId, double montantTotal) throws SQLException {
        double montantPaye = calculerMontantPayer(inscriptionId);
        return montantTotal - montantPaye;
    }



    public boolean verifierSiCandidatExiste(Long candidatId) {
        return candidatRepository.findById(candidatId).isPresent();
    }

    //Still many other other methods to implement based on the needs of the application
}