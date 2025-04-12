package org.cpi2.service;

import org.cpi2.entities.Inscription;
import org.cpi2.repository.PresenceRepository;
import org.cpi2.repository.SeanceRepository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PresenceService {
    private static final Logger LOGGER = Logger.getLogger(PresenceService.class.getName());
    private final PresenceRepository presenceRepository;
    private final SessionService sessionService;

    public PresenceService() {
        this.presenceRepository = new PresenceRepository();
        this.sessionService = new SessionService();
    }

    public boolean recordCodePresence(long sessionCodeId, long candidatId, boolean present) {
        return presenceRepository.recordCodePresence(sessionCodeId, candidatId, present);
    }

    public boolean recordConduitePresence(long sessionConduiteId, long candidatId, boolean present) {
        return presenceRepository.recordConduitePresence(sessionConduiteId, candidatId, present);
    }

    public boolean recordPresenceForSeance(long seanceId, long candidatId, String seanceType, boolean present) {
        try {
            return presenceRepository.recordPresenceForSeance(seanceId, candidatId, seanceType, present);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error recording presence for seance", e);
            return false;
        }
    }

    public boolean incrementPresence(long candidatId, String seanceType, long seanceId) {
        return recordPresenceForSeance(seanceId, candidatId, seanceType, true);
    }

    public int getCountInscriptioinPerSession(long candidatId, String seanceType, Date date) {
        if(seanceType == null || seanceType.isEmpty()) {
            LOGGER.log(Level.WARNING, "Seance type is null or empty");
            return 0;
        }
        if(seanceType.equalsIgnoreCase("code")) {
            return presenceRepository.getAllPresencesCode().stream().filter(presence ->sessionService.viewSessionCode(presence.getSessionId())
                    .getDateSession().isAfter(date.toLocalDate()) && presence.getCandidatId() == candidatId).toList().size();
        } else if(seanceType.equalsIgnoreCase("conduite")) {
            return presenceRepository.getAllPresencesConduite().stream().filter(presence ->sessionService.viewSessionConduite(presence.getSessionId())
                    .getDateSession().isAfter(date.toLocalDate()) && presence.getCandidatId() == candidatId).toList().size();
        } else {
            LOGGER.log(Level.WARNING, "Invalid seance type: " + seanceType);
            return 0;
        }
    }
}