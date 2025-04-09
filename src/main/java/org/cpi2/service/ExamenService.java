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

    public List<Examen> getExamensByResultat(Boolean resultat) {
        return examenRepository.findByResultat(resultat);
    }

    public boolean createExamen(Examen examen) {
        //need to check if the candidat complete all his sessions before proceeding
        //so need to check the number of session static in the course plan
        //using the inscription just check for an active inscription (meaning the candidat is still taking lessons)

        Candidat candidat = examen.getCandidat();
        if (candidat == null) {
            LOGGER.log(Level.WARNING, "Cannot create examen: Candidat is required");
            return false;
        }

        boolean candidatEligibility = inscriptionService.getInscriptionsByCin(candidat.getCin()).stream()
                .anyMatch(Inscription::isActive);
        if (!candidatEligibility) {
            try {
                return examenRepository.save(examen);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error creating examen", e);
                return false;
            }
        }
        else {
            LOGGER.log(Level.WARNING, "Cannot create examen: Candidat did not complete all sessions");
            return false;
        }
    }

    public boolean updateExamen(Examen examen) {
        try {
            if (examenRepository.findById(examen.getId()).isEmpty()) {
                LOGGER.log(Level.WARNING, "Cannot update examen: Examen not found");
                return false;
            }
            return examenRepository.update(examen);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating examen", e);
            return false;
        }
    }

    public boolean updateExamenResultat(Long examenId, Boolean resultat) {
        try {
            Optional<Examen> examenOpt = examenRepository.findById(examenId);
            if (examenOpt.isEmpty()) {
                LOGGER.log(Level.WARNING, "Cannot update examen resultat: Examen not found");
                return false;
            }

            Examen examen = examenOpt.get();
            examen.setResultat(resultat);

            return examenRepository.update(examen);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating examen resultat", e);
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

    public List<Examen> getUpcomingExamens(int days) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(days);
        return examenRepository.findByDateRange(today, endDate);
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