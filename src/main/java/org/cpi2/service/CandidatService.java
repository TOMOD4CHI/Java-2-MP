package org.cpi2.service;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.entities.*;
import org.cpi2.repository.CandidatRepository;
import org.cpi2.repository.InscriptionRepository;
import org.cpi2.utils.InvoiceGenerator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class CandidatService {
    private static final Logger LOGGER = Logger.getLogger(CandidatService.class.getName());
    private final CandidatRepository candidatRepository;
    private final InscriptionRepository inscriptionRepository;
    private final InscriptionService inscriptionService;
    private final DossierService dossierService;

    public CandidatService() {
        this.candidatRepository = new CandidatRepository();
        this.inscriptionRepository = new InscriptionRepository();
        this.dossierService = new DossierService(this);
        this.inscriptionService = new InscriptionService(this);
    }

    public CandidatService(DossierService dossierService) {
        this.candidatRepository = new CandidatRepository();
        this.inscriptionRepository = new InscriptionRepository();
        this.dossierService = dossierService;
        this.inscriptionService = new InscriptionService();
    }

    public CandidatService(InscriptionService inscriptionService){
        this.candidatRepository = new CandidatRepository();
        this.inscriptionRepository = new InscriptionRepository();
        this.dossierService = new DossierService(this);
        this.inscriptionService = inscriptionService;
    }

    public List<Candidat> getAllCandidats() {
        return candidatRepository.findAll();
    }

    public Optional<Candidat> getCandidatById(Long id) {
        return candidatRepository.findById(id);
    }
    public Candidat getCandidatByCin(String cin) {
        return candidatRepository.findByCin(cin).orElseThrow(() -> new DataNotFound("Candidat with cin "+cin+" not found"));
    }

    public long CinToId(String cin) {
        if(candidatRepository.findByCin(cin).isPresent()){
            return candidatRepository.findByCin(cin).get().getId();
        }
        return -1;
    }

    public String IdToCin(Long id) {
        if(candidatRepository.findById(id).isPresent()){
            return candidatRepository.findById(id).get().getCin();
        }
        return "";
    }

    public boolean addCandidat(Candidat candidat) {
        if(candidatRepository.findByCin(candidat.getCin()).isPresent()) {
            LOGGER.info("Candidat already exists");
            return false;
        }
        return candidatRepository.save(candidat) && dossierService.creerDossier(new Dossier(), candidat.getId());
    }

    public boolean updateCandidat(Candidat candidat) {
        return candidatRepository.update(candidat);
    }


    public List<String> findCandidatsByTypePermis(String typePermis) {
        return dossierService.getAllDossiers().stream()
                .filter(dossier -> dossier.getDocuments().containsKey(TypeDocument.valueOf("PERMIS_"+typePermis)))
                .map(dossier ->IdToCin(dossier.getCandidatId())).toList();
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

    public Optional<Candidat> findByCin(String cin) {
        return candidatRepository.findByCin(cin);
    }
    public List<Candidat> findByActifInscription(){
        List<Candidat> candidats = new ArrayList<>();
        for(Candidat candidat : candidatRepository.findAll()){
            if(inscriptionService.haveActifInscription(candidat.getCin())){
                candidats.add(candidat);
            }
        }
        return candidats;
    }
    public List<Candidat> findByActifAndUnpayedInscription(){
        List<Candidat> candidats = new ArrayList<>();
        for(Candidat candidat : candidatRepository.findAll()){
            if(inscriptionService.haveActifandUnpayedInscription(candidat.getCin())){
                candidats.add(candidat);
            }
        }
        return candidats;
    }

    public void generateInvoice(String cin, String typeFacture, LocalDate dateDebut, 
                              LocalDate dateFin, double montant, String note) {
        try {
            Candidat candidat = getCandidatByCin(cin);
            
            
            List<Inscription> inscriptions = inscriptionRepository.findByCin(cin);
            Inscription latestInscription = inscriptions.stream()
                .max(Comparator.comparing(Inscription::getInscriptioDate))
                .orElseThrow(() -> new DataNotFound("No inscription found for candidate " + cin));
            
            
            CoursePlan coursePlan = latestInscription.getPlan();
            
            
            InvoiceGenerator.generatePDF(
                candidat,
                typeFacture,
                dateDebut,
                dateFin,
                montant,
                note
            );
            

            
            LOGGER.info("Invoice generated successfully for candidate: " + cin);
        } catch (Exception e) {
            LOGGER.severe("Error generating invoice: " + e.getMessage());
            throw new RuntimeException("Error generating invoice", e);
        }
    }
    public List<Candidat> getAllWithActifInscription() {
        List<Candidat> candidats = new ArrayList<>();
        for (Candidat candidat : candidatRepository.findAll()) {
            if (inscriptionService.haveActifInscription(candidat.getCin())) {
                candidats.add(candidat);
            }
        }
        return candidats;
    }
    public List<Candidat> getAllWithActifByPlan(int planId) {
        List<Candidat> candidats = new ArrayList<>();
        for (Candidat candidat : candidatRepository.findAll()) {
            if (inscriptionService.haveActifInscription(candidat.getCin())) {
                List<Inscription> inscriptions = inscriptionRepository.findByCin(candidat.getCin());
                for (Inscription inscription : inscriptions) {
                    if (inscription.getStatus().equalsIgnoreCase("En Cours") && inscription.getPlan().getId() == planId) {
                        candidats.add(candidat);
                        break;
                    }
                }
            }
        }
        return candidats;

    }
}