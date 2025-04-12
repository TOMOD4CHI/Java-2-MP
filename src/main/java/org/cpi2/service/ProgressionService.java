package org.cpi2.service;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.TypePermis;
import org.cpi2.repository.ProgressionRepository;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

public class ProgressionService {
    private static final Logger LOGGER = Logger.getLogger(ProgressionService.class.getName());
    private final ProgressionRepository progressionRepository;
    private final CandidatService candidatService;

    public ProgressionService() {
        this.progressionRepository = new ProgressionRepository();
        this.candidatService = new CandidatService();
    }
    
    /**
     * Get all candidates for the combo box
     * @return Map of candidat ID to display name (nom + prenom)
     */
    public Map<Long, String> getAllCandidatsForComboBox() {
        Map<Long, String> candidats = new HashMap<>();
        
        List<Candidat> allCandidats = candidatService.getAllCandidats();
        for (Candidat candidat : allCandidats) {
            String displayName = candidat.getNom() + " " + candidat.getPrenom();
            candidats.put(candidat.getId(), displayName);
        }
        
        return candidats;
    }

    /**
     * Get a candidate's overall progression information
     */
    public Map<String, Object> getCandidatProgression(long candidatId) {
        Map<String, Object> progressionData = new HashMap<>();

        Optional<Candidat> candidatOpt = candidatService.getCandidatById(candidatId);
        if (candidatOpt.isEmpty()) {
            return progressionData;
        }
        
        Candidat candidat = candidatOpt.get();
        progressionData.put("nom", candidat.getNom());
        progressionData.put("prenom", candidat.getPrenom());

        Optional<Map<String, Object>> inscriptionDataOpt = progressionRepository.getCandidatInscription(candidatId);
        if (inscriptionDataOpt.isEmpty()) {
            return progressionData;
        }
        
        Map<String, Object> inscriptionData = inscriptionDataOpt.get();
        Date dateInscription = (Date) inscriptionData.get("dateInscription");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        
        String statut = (String) inscriptionData.get("statut");
        int totalSeancesCode = (int) inscriptionData.get("heuresCode");
        int totalSeancesConduite = (int) inscriptionData.get("heuresConduite");
        int typePermisId = (int) inscriptionData.get("typePermisId");

        int seancesCodeCompletes = progressionRepository.getCompletedCodeSessions(candidatId);
        int seancesConduiteCompletes = progressionRepository.getCompletedDrivingSessions(candidatId);

        double progressionTotale = 0.0;
        if ((totalSeancesCode + totalSeancesConduite) > 0) {
            progressionTotale = (double) (seancesCodeCompletes + seancesConduiteCompletes) / 
                               (totalSeancesCode + totalSeancesConduite);
        }

        String typePermis;
        switch (typePermisId) {
            case 1: typePermis = "A"; break;
            case 2: typePermis = "B"; break;
            case 3: typePermis = "C"; break;
            default: typePermis = "B"; break;
        }

        progressionData.put("typePermis", typePermis);
        progressionData.put("dateInscription", dateFormat.format(dateInscription));
        progressionData.put("statut", statut);
        progressionData.put("totalSeancesCode", totalSeancesCode);
        progressionData.put("totalSeancesConduite", totalSeancesConduite);
        progressionData.put("seancesCodeCompletes", seancesCodeCompletes);
        progressionData.put("seancesConduiteCompletes", seancesConduiteCompletes);
        progressionData.put("progressionTotale", progressionTotale);
        
        return progressionData;
    }
    
    /**
     * Get data for the progression charts
     */
    public Map<String, Object> getProgressionChartData(long candidatId) {
        Map<String, Object> chartData = new HashMap<>();

        Map<String, Integer> monthlyCodeData = progressionRepository.getMonthlyCodeSessions(candidatId, 6);
        Map<String, Integer> monthlyDrivingData = progressionRepository.getMonthlyDrivingSessions(candidatId, 6);
        
        chartData.put("monthlyCode", monthlyCodeData);
        chartData.put("monthlyDriving", monthlyDrivingData);

        Map<String, Object> examStats = progressionRepository.getExamStatistics(candidatId);
        chartData.put("examStats", examStats);
        
        return chartData;
    }
    
    /**
     * Get sessions data by time period
     */
    public Map<String, Integer> getSessionsByPeriod(long candidatId, String period) {
        return progressionRepository.getSessionsByPeriod(candidatId, period);
    }
    
    /**
     * Search for a candidate by ID
     */
    public Optional<Candidat> searchCandidatById(long candidatId) {
        return candidatService.getCandidatById(candidatId);
    }
}
