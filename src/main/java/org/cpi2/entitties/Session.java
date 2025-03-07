package org.cpi2.entitties;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class Session {
    protected Long id;
    protected LocalDate dateSession;
    protected LocalTime heureSession;
    protected Moniteur moniteur;
    protected Double prix;
    protected StatutSession statut;
    protected TypeSession typeSession;

    protected Session() {
        this.statut = StatutSession.PLANIFIEE;
    }

    protected Session(LocalDate dateSession, LocalTime heureSession, Moniteur moniteur, Double prix, TypeSession typeSession) {
        this();
        this.dateSession = dateSession;
        this.heureSession = heureSession;
        this.moniteur = moniteur;
        this.prix = prix;
        this.typeSession = typeSession;
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

    public Double getPrix() {
        return prix;
    }

    public void setPrix(Double prix) {
        this.prix = prix;
    }

    public StatutSession getStatut() {
        return statut;
    }

    public void setStatut(StatutSession statut) {
        this.statut = statut;
    }

    public TypeSession getTypeSession() {
        return typeSession;
    }

    public void setTypeSession(TypeSession typeSession) {
        this.typeSession = typeSession;
    }
}
