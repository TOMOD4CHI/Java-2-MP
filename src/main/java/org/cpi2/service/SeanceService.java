package org.cpi2.service;

import org.cpi2.entities.Seance;
import org.cpi2.repository.SeanceRepository;
import org.cpi2.service.MoniteurService;
import org.cpi2.service.CandidatService;
import org.cpi2.service.VehiculeService;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeanceService {
    private static final Logger LOGGER = Logger.getLogger(SeanceService.class.getName());
    private final SeanceRepository seanceRepository = new SeanceRepository();

    /**
     * Save a new session to the database
     * Also updates vehicle kilometrage and tracks candidate presence
     */
    public boolean saveSeance(Seance seance) {
        
        if (seance.getStatus() == null || seance.getStatus().isEmpty()) {
            seance.setStatus("Planifiée");
        }

        
        if (seance.getMoniteurId() != null) {
            MoniteurService moniteurService = new MoniteurService();
            if (!moniteurService.getMoniteurById(seance.getMoniteurId()).isPresent()) {
                LOGGER.log(Level.WARNING, "Cannot save seance: Moniteur with ID " + seance.getMoniteurId() + " does not exist");
                return false;
            }
        } else {
            LOGGER.log(Level.WARNING, "Cannot save seance: Moniteur ID is null");
            return false;
        }

        
        CandidatService candidatService = new CandidatService();
        if (seance.getCandidatId() != null) {
            if (!candidatService.getCandidatById(seance.getCandidatId()).isPresent()) {
                LOGGER.log(Level.WARNING, "Cannot save seance: Candidat with ID " + seance.getCandidatId() + " does not exist");
                return false;
            }
        } else {
            LOGGER.log(Level.WARNING, "Cannot save seance: Candidat ID is null");
            return false;
        }

        
        VehiculeService vehiculeService = new VehiculeService();
        if (seance.getVehiculeId() != null) {
            if (!vehiculeService.getVehiculeById(seance.getVehiculeId()).isPresent()) {
                LOGGER.log(Level.WARNING, "Cannot save seance: Vehicule with ID " + seance.getVehiculeId() + " does not exist");
                return false;
            }
        }

        
        boolean saved = seanceRepository.save(seance);
        
        if (saved) {
            
            if (seance.getVehiculeId() != null && seance.getKilometrage() != null) {
                try {
                    
                    int currentKilometrage = vehiculeService.getVehiculeById(seance.getVehiculeId())
                            .map(v -> v.getKilometrageTotal())
                            .orElse(0);
                    
                    
                    int newKilometrage = currentKilometrage + seance.getKilometrage().intValue();
                    
                    
                    boolean updated = vehiculeService.updateKilometrage(seance.getVehiculeId(), newKilometrage);
                    if (!updated) {
                        LOGGER.log(Level.WARNING, "Failed to update vehicle kilometrage for vehicle ID " + seance.getVehiculeId());
                    } else {
                        LOGGER.log(Level.INFO, "Updated vehicle kilometrage for vehicle ID " + seance.getVehiculeId() + 
                                " from " + currentKilometrage + " to " + newKilometrage);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error updating vehicle kilometrage", e);
                }
            }

            if (seance.getCandidatId() != null && seance.getType() != null) {
                try {
                    PresenceService presenceService = new PresenceService();

                    if (seance.getId() == null) {
                        LOGGER.log(Level.WARNING, "Cannot record presence: Seance ID is null after save");
                    } else {
                        boolean presenceRecorded = presenceService.incrementPresence(
                                seance.getCandidatId(), 
                                seance.getType(), 
                                seance.getId());
                        
                        if (!presenceRecorded) {
                            LOGGER.log(Level.WARNING, "Failed to record presence for candidate ID " + seance.getCandidatId());
                        } else {
                            LOGGER.log(Level.INFO, "Recorded presence for candidate ID " + seance.getCandidatId() + 
                                    " in seance type " + seance.getType());
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error recording candidate presence", e);
                }
            }
        }
        
        return saved;
    }

    public boolean updateSeance(Seance seance) {
        if (seance == null || seance.getId() == null) {
            return false;
        }

        
        if (seance.getStatus() == null || seance.getStatus().isEmpty()) {
            seance.setStatus("Planifiée");
        }

        return seanceRepository.update(seance);
    }

    public boolean updateSessionStatus(Long id, String status, String commentaire) {
        if (id == null || status == null || status.isEmpty()) {
            return false;
        }
        return seanceRepository.updateStatus(id, status, commentaire);
    }

    public Optional<Seance> findSeanceById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return seanceRepository.findById(id);
    }


    public List<Seance> findAllSeances() {
        return seanceRepository.findAll();
    }

    public boolean deleteSeance(Long id) {
        if (id == null) {
            return false;
        }
        return seanceRepository.deleteById(id);
    }
}