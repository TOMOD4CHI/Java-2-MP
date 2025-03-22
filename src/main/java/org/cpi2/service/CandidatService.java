package org.cpi2.service;

import org.cpi2.entitties.*;
import org.cpi2.repository.CandidatRepository;
import org.cpi2.repository.InscriptionRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class CandidatService {
    private static final Logger LOGGER = Logger.getLogger(CandidatService.class.getName());
    private final CandidatRepository candidatRepository;
    private final InscriptionRepository inscriptionRepository = new InscriptionRepository();

    public CandidatService() {
        this.candidatRepository = new CandidatRepository();
    }

    public List<Candidat> getAllCandidats() {
        return candidatRepository.findAll();
    }

    public Optional<Candidat> getCandidatById(Long id) {
        return candidatRepository.findById(id);
    }

    public boolean addCandidat(Candidat candidat) {
        if(candidatRepository.findByCin(candidat.getCin()).isEmpty()) {
            LOGGER.info("Candidat already exists");
            return false;
        }
        return candidatRepository.save(candidat);
    }

    public boolean updateCandidat(Candidat candidat) {
        return candidatRepository.update(candidat);
    }

    public List<Candidat> findCandidatsByTypePermis(TypePermis typePermis) {
        //waiting until we implement the method in the repository of the dossier and document
        return null;
    }

    public List<Candidat> findCandidatsBySubscriptionStatus(String status) {
        return inscriptionRepository.findAll().stream()
                .filter(inscription -> inscription.getStatus().equals(status))
                .map(Inscription::getCin).map(cin -> candidatRepository.findByCin(cin).get())
                .toList();
    }
    public List<Candidat> findCandidatsBySubscription(CoursePlan plan) {
        return inscriptionRepository.findAll().stream()
                .filter(inscription -> inscription.getPlan().getId()==(plan.getId()))
                .map(Inscription::getCin).map(cin -> candidatRepository.findByCin(cin).get())
                .toList();
    }
    public List<Candidat> findCandidatsInscritAfter(Date date) {
        return inscriptionRepository.findAll().stream().filter(inscription -> inscription.getInscriptioDate().after(date))
                .map(Inscription::getCin).map(cin -> candidatRepository.findByCin(cin).get())
                .toList();
    }
    public List<Candidat> findCandidatsInscritOn(Date date) {
        return inscriptionRepository.findAll().stream().filter(inscription -> inscription.getInscriptioDate().equals(date))
                .map(Inscription::getCin).map(cin -> candidatRepository.findByCin(cin).get())
                .toList();
    }
    public boolean updateDossier(Candidat candidat, Dossier dossier) {
        candidat.setDossier(dossier);
        return candidatRepository.update(candidat);
    }

}