package org.cpi2.service;

import org.cpi2.entities.Seance;
import org.cpi2.repository.SeanceRepository;

import java.util.List;
import java.util.Optional;

public class SeanceService {
    private final SeanceRepository seanceRepository = new SeanceRepository();

    /**
     * Save a new session to the database
     */
    public boolean saveSeance(Seance seance) {
        return seanceRepository.save(seance);
    }
    
    /**
     * Update an existing session in the database
     */
    public boolean updateSeance(Seance seance) {
        if (seance == null || seance.getId() == null) {
            return false;
        }
        return seanceRepository.update(seance);
    }
    
    /**
     * Update the attendance status of a session
     */
    public boolean updateSessionStatus(Long id, String status, String commentaire) {
        if (id == null || status == null || status.isEmpty()) {
            return false;
        }
        return seanceRepository.updateStatus(id, status, commentaire);
    }
    
    /**
     * Find a session by its ID
     */
    public Optional<Seance> findSeanceById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return seanceRepository.findById(id);
    }
    
    /**
     * Get all sessions from the database
     */
    public List<Seance> findAllSeances() {
        return seanceRepository.findAll();
    }
    
    /**
     * Delete a session by its ID
     */
    public boolean deleteSeance(Long id) {
        if (id == null) {
            return false;
        }
        return seanceRepository.deleteById(id);
    }
} 