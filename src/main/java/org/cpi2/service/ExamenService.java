package org.cpi2.service;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.Examen;
import org.cpi2.entities.Inscription;
import org.cpi2.entities.TypeExamen;
import org.cpi2.repository.CandidatRepository;
import org.cpi2.repository.ExamenRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExamenService {
    private static final Logger LOGGER = Logger.getLogger(ExamenService.class.getName());
    private final ExamenRepository examenRepository;
    private final CandidatService candidatService; ;
    private final InscriptionService inscriptionService;
    private final TypeExamenService typeExamenService;

    public ExamenService() {
        this.examenRepository = new ExamenRepository();
        this.candidatService = new CandidatService();
        this.typeExamenService = new TypeExamenService();
        this.inscriptionService = new InscriptionService();
    }

    public List<Examen> getAllExamens() {
        return examenRepository.findAll();
    }

    public Optional<Examen> getExamenById(Long id) {
        return examenRepository.findById(id);
    }

    public List<Examen> getExamensByCandidatId(Long candidatId) {
        return examenRepository.findByCandidatId(candidatId);
    }

    public List<Examen> getExamensByTypeExamen(Integer typeExamenId) {
        return examenRepository.findByTypeExamen(typeExamenId);
    }

    public List<Examen> getExamensByDateRange(LocalDate startDate, LocalDate endDate) {
        return examenRepository.findByDateRange(startDate, endDate);
    }

    public List<Examen> getExamensAfter(LocalDate date) {
        return examenRepository.findAll().stream()
                .filter(examen -> examen.getDate().isAfter(date))
                .toList();
    }

    public List<Examen> getExamensByResultat(Boolean resultat) {
        return examenRepository.findByResultat(resultat);
    }

    public boolean verifyCandidatEligibilite(String ExamenType,String cin) {
        Optional<Candidat> candidatOpt = candidatService.findByCin(cin);
        if (candidatOpt.isEmpty()) {
            LOGGER.log(Level.WARNING, "Candidat not found with CIN: " + cin);
            return false;
        }
        Candidat candidat = candidatOpt.get();
        Inscription inscription = inscriptionService.getActifInscirptionBycin(cin).get(0);
        if (inscription == null) {
            LOGGER.log(Level.WARNING, "Candidat has no active inscription");
            return false;
        }
        
        if (ExamenType.equalsIgnoreCase("code")) {
            if (inscriptionService.isInscriptionCodeDone(inscription.getId())) {
                LOGGER.log(Level.INFO, "Candidat has completed all code sessions");
            } else {
                LOGGER.log(Level.WARNING, "Candidat has not completed all code sessions");
                return false;
            }
            if (getExamensAfter(inscription.getInscriptioDate().toLocalDate()).stream().filter(examen -> examen.getTypeExamen().name().equalsIgnoreCase("code")).anyMatch(
                    examen -> examen.getResultat() && examen.getDate().isAfter(inscription.getInscriptioDate().toLocalDate()))) {
                LOGGER.log(Level.WARNING, "Candidat have finished this exam already");
                return false;}
            } else {
                if (inscriptionService.isInscriptionConduiteDone(inscription.getId())) {
                    LOGGER.log(Level.INFO, "Candidat has completed all conduite sessions");
                } else {
                    LOGGER.log(Level.WARNING, "Candidat has not completed all conduite sessions");
                    return false;
                }
            if (getExamensAfter(inscription.getInscriptioDate().toLocalDate()).stream().filter(examen -> examen.getTypeExamen().name().equalsIgnoreCase("conduite")).anyMatch(
                    examen -> examen.getResultat() && examen.getDate().isAfter(inscription.getInscriptioDate().toLocalDate()))) {
                LOGGER.log(Level.WARNING, "Candidat have finished this exam already");
                return false;}
            }

            return true;
        }


    public boolean createExamen(Examen examen) {
        
        
        

        Candidat candidat = examen.getCandidat();
        if (candidat == null) {
            LOGGER.log(Level.WARNING, "Cannot create examen: Candidat is required");
            return false;
        }
        try {
            return examenRepository.save(examen);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating examen", e);
            return false;
        }
    }

    public boolean hasSucceded(Inscription inscription) {
        List<Examen> examens = getExamensByCandidat(inscription.getCin());
        boolean hasSuccededCode = false;
        boolean hasSuccededConduite = false;
        for (Examen examen : examens) {
            if (examen.getResultat() != null && examen.getResultat() && examen.getDate().isAfter(inscription.getInscriptioDate().toLocalDate())) {
                if(examen.getTypeExamen().name().equalsIgnoreCase("code")) {
                    hasSuccededCode = true;
                } else if (examen.getTypeExamen().name().equalsIgnoreCase("conduite")) {
                    hasSuccededConduite = true;
                }
            }
        }
        return hasSuccededCode && hasSuccededConduite;
    }
    public boolean updateExamen(Examen examen) {
        try {
            if (examenRepository.findById(examen.getId()).isEmpty()) {
                LOGGER.log(Level.WARNING, "Cannot update examen: Examen not found");
                return false;
            }
            if(examenRepository.update(examen)){
                Inscription inscription = inscriptionService.getActifInscirptionBycin(examen.getCandidat().getCin()).get(0);
                if(hasSucceded(inscription)){
                    inscription.setStatus("Terminé");
                    inscriptionService.updateInscription(inscription);
                    LOGGER.log(Level.INFO, "Candidat has succeded in all exams and finished his course");
                }
                return true;
            }
            LOGGER.log(Level.WARNING, "Cannot update examen: Error updating examen");
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating examen", e);
            return false;
        }
    }

    public boolean deleteExamen(Long examenId) {
        try {
            if (examenRepository.findById(examenId).isEmpty()) {
                LOGGER.log(Level.WARNING, "Cannot delete examen: Examen not found");
                return false;
            }

            return examenRepository.delete(examenId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting examen", e);
            return false;
        }
    }

    public List<Examen> getExamensByCandidat(String cin) {
        Optional<Candidat> candidat = candidatService.findByCin(cin);
        if (candidat.isEmpty()) {
            return List.of();
        }
        return examenRepository.findByCandidatId(candidat.get().getId());
    }

    public double calculateSuccessRate(TypeExamen typeExamen) {
        try {
            int typeExamenId = examenRepository.getTypeExamenId(typeExamen);
            List<Examen> examens = examenRepository.findByTypeExamen(typeExamenId);
            if (examens.isEmpty()) {
                return 0.0;
            }
            long passedCount = examens.stream()
                    .filter(e -> e.getResultat() != null && e.getResultat())
                    .count();

            return (double) passedCount / examens.size() * 100;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting type examen id", e);
            return -1;
        }
    }
    public HashMap<String,Double> getType_Price() {
        HashMap<String,Double> type_price = new HashMap<>();
        List<String> typeExamenList = typeExamenService.getAllTypeExamens();
        for (String typeExamen : typeExamenList) {
            type_price.put(typeExamen,typeExamenService.getExamenCostByLibelle(typeExamen));
        }
        return type_price;
    }
    public boolean hasPendingExamens(String cin) {
        return getExamensByCandidat(cin).stream().anyMatch(examen -> examen.getResultat() == null);
    }

    public Examen getPendingExamen(String cin) {
        return getExamensByCandidat(cin).stream()
                .filter(examen -> examen.getResultat() == null)
                .findFirst()
                .orElse(null);
    }
    public List<Examen> getAllPendingExamens() {
        return examenRepository.findAll().stream()
                .filter(examen -> examen.getResultat() == null)
                .toList();
    }
}