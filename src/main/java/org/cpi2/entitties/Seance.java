package org.cpi2.entitties;

import java.time.LocalDateTime;
import java.util.Objects;

public class Seance {
    private Long id;
    private LocalDateTime date;
    private int duree;
    private String type; // "conduite" or "code"
    private String statut; // "planifiée", "en cours", "complétée", "annulée"
    private Moniteur moniteur;
    private Candidat candidat;
    private Vehicule vehicule; // Can be null for code sessions
    
    public Seance() {
        this.statut = "planifiée";
    }
    
    public Seance(Long id, LocalDateTime date, int duree, String type, 
                Moniteur moniteur, Candidat candidat, Vehicule vehicule) {
        this();
        this.id = id;
        this.date = date;
        this.duree = duree;
        this.type = type;
        this.moniteur = moniteur;
        this.candidat = candidat;
        this.vehicule = vehicule;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public int getDuree() {
        return duree;
    }
    
    public void setDuree(int duree) {
        this.duree = duree;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getStatut() {
        return statut;
    }
    
    public void setStatut(String statut) {
        this.statut = statut;
    }
    
    public Moniteur getMoniteur() {
        return moniteur;
    }
    
    public void setMoniteur(Moniteur moniteur) {
        this.moniteur = moniteur;
    }
    
    public Candidat getCandidat() {
        return candidat;
    }
    
    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }
    
    public Vehicule getVehicule() {
        return vehicule;
    }
    
    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seance seance = (Seance) o;
        return Objects.equals(id, seance.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Séance de " + type + " (" + date + ", durée: " + duree + " min)";
    }
}