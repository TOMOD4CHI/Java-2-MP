package entitties;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class Session {
    protected Long id;
    protected long planId;
    protected LocalDate dateSession;
    protected LocalTime heureSession;
    protected long duree;
    protected Moniteur moniteur;
    protected StatutSession statut;

    protected Session() {
        this.statut = StatutSession.PLANIFIEE;
    }

    protected Session(long planId,LocalDate dateSession, LocalTime heureSession,long duree, Moniteur moniteur) {
        this();
        this.planId=planId;
        this.dateSession = dateSession;
        this.heureSession = heureSession;
        this.moniteur = moniteur;
        this.duree = duree;
    }

    public LocalDate getDateSession() {
        return dateSession;
    }

    public void setDateSession(LocalDate dateSession) {
        this.dateSession = dateSession;
    }

    public LocalTime getHeureSession() {
        return heureSession;
    }

    public void setHeureSession(LocalTime heureSession) {
        this.heureSession = heureSession;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Moniteur getMoniteur() {
        return moniteur;
    }

    public void setMoniteur(Moniteur moniteur) {
        this.moniteur = moniteur;
    }

    public long getPlanId() {
        return planId;
    }

    public void setPlanId(long planId) {
        this.planId = planId;
    }

    public long getDuree() {
        return duree;
    }

    public void setDuree(long duree) {
        this.duree = duree;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public void setStatut(StatutSession statut) {
        this.statut = statut;
    }

}
