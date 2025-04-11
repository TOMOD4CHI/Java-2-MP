package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Notification {
    private Integer id;
    private String type; // ASSURANCE, ENTRETIEN, VIGNETTE, etc.
    private String titre;
    private String description;
    private Integer vehiculeId;
    private String vehiculeInfo; // Immatriculation + marque/modèle
    private LocalDate dateEcheance;
    private LocalDateTime dateCreation;
    private String statut; // ACTIVE, TRAITEE, A_VENIR
    private String priorite; // HAUTE, MOYENNE, BASSE

    public Notification() {
    }

    public Notification(Integer id, String type, String titre, String description, Integer vehiculeId, 
                       String vehiculeInfo, LocalDate dateEcheance, LocalDateTime dateCreation, 
                       String statut, String priorite) {
        this.id = id;
        this.type = type;
        this.titre = titre;
        this.description = description;
        this.vehiculeId = vehiculeId;
        this.vehiculeInfo = vehiculeInfo;
        this.dateEcheance = dateEcheance;
        this.dateCreation = dateCreation;
        this.statut = statut;
        this.priorite = priorite;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Integer vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public String getVehiculeInfo() {
        return vehiculeInfo;
    }

    public void setVehiculeInfo(String vehiculeInfo) {
        this.vehiculeInfo = vehiculeInfo;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }
    
    @Override
    public String toString() {
        return titre;
    }
}
