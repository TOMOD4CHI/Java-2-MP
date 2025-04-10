package org.cpi2.service;


import org.cpi2.entities.SessionCode;
import org.cpi2.entities.SessionConduite;
import org.cpi2.repository.SessionRepository;

import java.util.List;

public class SessionService {
    private final SessionRepository sessionRepo;
    
    public SessionService() {
        this.sessionRepo = new SessionRepository();
    }
    
    public boolean saveSessionConduite(SessionConduite session) {
        return sessionRepo.saveConduiteSession(session);
    }
    public boolean saveSessionCode(SessionCode session) {
        return sessionRepo.saveCodeSession(session);
    }
    
    /**
     * Affecte un candidat à une séance de code
     * @param sessionId L'ID de la session code
     * @param candidatId L'ID du candidat
     * @return true si l'affectation a réussi, false sinon
     */
    public boolean affecterCandidatASessionCode(long sessionId, long candidatId) {
        return sessionRepo.affecterCandidatASessionCode(sessionId, candidatId);
    }
    
    /**
     * Affecte un candidat à une séance de conduite
     * @param seanceId L'ID de la séance de conduite
     * @param candidatId L'ID du candidat
     * @return true si l'affectation a réussi, false sinon
     */
    public boolean affecterCandidatASeanceConduite(long seanceId, long candidatId) {
        return sessionRepo.affecterCandidatASeanceConduite(seanceId, candidatId);
    }
    public boolean modifySessionConduite(SessionConduite session) {
        return sessionRepo.updateConduiteSession(session);
    }
    public boolean modifySessionCode(SessionCode session) {
        return sessionRepo.updateCodeSession(session);
    }
    public boolean deleteSessionConduite(long id) {
        return sessionRepo.deleteConduiteSession(id);
    }
    public boolean deleteSessionCode(long id) {
        return sessionRepo.deleteCodeSession(id);
    }
    public SessionConduite viewSessionConduite(long id) {
        return sessionRepo.findConduiteSessionById(id).orElse(null);
    }
    public SessionCode viewSessionCode(long id) {
        return sessionRepo.findCodeSessionById(id).orElse(null);
    }
    public List<SessionConduite> viewAllSessionConduite() {
        return sessionRepo.findAllConduiteSessions();
    }
    public List<SessionCode> viewAllSessionCode() {
        return sessionRepo.findAllCodeSessions();
    }
    public List<SessionConduite> viewSessionConduiteByMoniteur(long moniteurId) {
        return sessionRepo.findAllConduiteSessionsByMoniteur(moniteurId);
    }
    public List<SessionCode> viewSessionCodeByMoniteur(long moniteurId) {
        return sessionRepo.findAllCodeSessionsByMoniteur(moniteurId);
    }
    public List<SessionConduite> viewSessionConduiteByVehicule(long vehiculeId) {
        return viewAllSessionConduite().stream().filter(session -> session.getVehicule().getId() == vehiculeId).toList();
    }
    public List<SessionCode> viewSessionCodeByCandidat(long candidatId) {
        return viewAllSessionCode().stream().filter(session -> session.getParticipants().stream().anyMatch(candidat -> candidat.getId() == candidatId)).toList();
    }
    public List<SessionCode> viewSessionCodeByPlan(long planId) {
        return viewAllSessionCode().stream().filter(session -> session.getPlanId() == planId).toList();
    }
    public List<SessionConduite> viewSessionConduiteByPlan(long planId) {
        return viewAllSessionConduite().stream().filter(session -> session.getPlanId() == planId).toList();
    }
    public List<SessionConduite> viewSessionConduiteByDate(String date) {
        return viewAllSessionConduite().stream().filter(session -> session.getDateSession().toString().equals(date)).toList();
    }
    public List<SessionCode> viewSessionCodeByDate(String date) {
        return viewAllSessionCode().stream().filter(session -> session.getDateSession().toString().equals(date)).toList();
    }
    public List<SessionConduite> viewSessionConduiteByMoniteurAndDate(long moniteurId, String date) {
        return viewSessionConduiteByMoniteur(moniteurId).stream().filter(session -> session.getDateSession().toString().equals(date)).toList();
    }
    public List<SessionCode> viewSessionCodeByMoniteurAndDate(long moniteurId, String date) {
        return viewSessionCodeByMoniteur(moniteurId).stream().filter(session -> session.getDateSession().toString().equals(date)).toList();
    }
    public List<SessionConduite> viewSessionConduiteByVehiculeAndDate(long vehiculeId, String date) {
        return viewSessionConduiteByVehicule(vehiculeId).stream().filter(session -> session.getDateSession().toString().equals(date)).toList();
    }
    //more to add based on the requirements

}
