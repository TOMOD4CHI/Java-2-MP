package org.cpi2.service;

import org.cpi2.entitties.Paiement;
import org.cpi2.entitties.StatutPaiement;
import org.cpi2.entitties.Tranche;
import org.cpi2.repository.PaiementRepository;
import org.cpi2.repository.CandidatRepository;

import java.util.List;
import java.util.Optional;

public class PaiementService {
    private final PaiementRepository paiementRepository;
    private final CandidatRepository candidatRepository;

    public PaiementService() {
        this.paiementRepository = new PaiementRepository();
        this.candidatRepository = new CandidatRepository();
    }

    /* 7ata lin ta7dher paiment ,  this should be the starting base :>>

    public Optional<Paiement> getPaiementById(Long id) {
        return paiementRepository.findById(id);
    }

    public List<Paiement> getPaiementsByCandidatId(Long candidatId) {
        return paiementRepository.findAllByCandidat(candidatId);
    }

    public boolean enregistrerPaiement(Paiement paiement) {
        return paiementRepository.save(paiement);
    }

    public boolean mettreAJourStatutTranche(Long trancheId, StatutPaiement nouveauStatut) {
        return paiementRepository.updateTrancheStatus(trancheId, nouveauStatut);
    }

    public double calculerMontantTotal(Long candidatId) {
        List<Paiement> paiements = paiementRepository.findAllByCandidat(candidatId);
        return paiements.stream()
                .mapToDouble(Paiement::getMontant)
                .sum();
    }

    public double calculerMontantRestant(Long candidatId, double montantTotal) {
        double montantPaye = calculerMontantTotal(candidatId);
        return montantTotal - montantPaye;
    }

    public List<Tranche> getTranchesPaiement(Long paiementId) {
        Optional<Paiement> paiement = paiementRepository.findById(paiementId);
        return paiement.map(Paiement::getTranches).orElse(List.of());
    }



    public List<Tranche> getTranchesPaiementEnRetard(Long candidatId) {
        List<Paiement> paiements = getPaiementsByCandidatId(candidatId);
        return paiements.stream()
                .flatMap(p -> p.getTranches().stream())
                .filter(this::verifierPaiementEnRetard)
                .toList();
    }


    public boolean verifierPaiementEnRetard(Tranche tranche) {
        return tranche.getStatut() != StatutPaiement.PAYE &&
                tranche.getDateEcheance().isBefore(java.time.LocalDate.now());
    }

    public boolean verifierSiCandidatExiste(Long candidatId) {
        return candidatRepository.findById(candidatId).isPresent();
    }

    */
}