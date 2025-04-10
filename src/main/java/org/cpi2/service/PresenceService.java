package org.cpi2.service;

import org.cpi2.repository.PresenceRepository;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for managing candidate presence in sessions
 */
public class PresenceService {
    private static final Logger LOGGER = Logger.getLogger(PresenceService.class.getName());
    private final PresenceRepository presenceRepository;

    public PresenceService() {
        this.presenceRepository = new PresenceRepository();
    }

    /**
     * Records a candidate's presence in a code session
     * @param sessionCodeId The ID of the code session
     * @param candidatId The ID of the candidate
     * @param present Whether the candidate was present (true) or absent (false)
     * @return true if the record was created successfully, false otherwise
     */
    public boolean recordCodePresence(long sessionCodeId, long candidatId, boolean present) {
        return presenceRepository.recordCodePresence(sessionCodeId, candidatId, present);
    }

    /**
     * Records a candidate's presence in a driving session
     * @param sessionConduiteId The ID of the driving session
     * @param candidatId The ID of the candidate
     * @param present Whether the candidate was present (true) or absent (false)
     * @return true if the record was created successfully, false otherwise
     */
    public boolean recordConduitePresence(long sessionConduiteId, long candidatId, boolean present) {
        return presenceRepository.recordConduitePresence(sessionConduiteId, candidatId, present);
    }

    /**
     * Records a candidate's presence in a seance (generic session)
     * This method determines the type of session and calls the appropriate method
     * @param seanceId The ID of the seance
     * @param candidatId The ID of the candidate
     * @param seanceType The type of seance ("Code" or "Conduite")
     * @param present Whether the candidate was present (true) or absent (false)
     * @return true if the record was created successfully, false otherwise
     */
    public boolean recordPresenceForSeance(long seanceId, long candidatId, String seanceType, boolean present) {
        try {
            return presenceRepository.recordPresenceForSeance(seanceId, candidatId, seanceType, present);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording presence for seance", e);
            return false;
        }
    }

    /**
     * Increments a candidate's presence count for a specific seance type
     * @param candidatId The ID of the candidate
     * @param seanceType The type of seance ("Code" or "Conduite")
     * @param seanceId The ID of the seance
     * @return true if the presence was recorded successfully, false otherwise
     */
    public boolean incrementPresence(long candidatId, String seanceType, long seanceId) {
        // Mark the candidate as present (true)
        return recordPresenceForSeance(seanceId, candidatId, seanceType, true);
    }
}