package org.cpi2.entities;

import java.time.LocalDate;

/**
 * Entity class representing a vehicle maintenance record
 */
public class Entretien {
    private int id;
    private long vehiculeId;
    private LocalDate dateEntretien;
    private String typeEntretien;
    private String description;
    private int kilometrageActuel;
    private double cout;
    private boolean maintenance;
    private String cheminFacture;
    private LocalDate dateProchainEntretien;
    private boolean statut;
    private LocalDate createdAt;

    // Default constructor
    public Entretien() {
        this.dateEntretien = LocalDate.now();
        this.createdAt = LocalDate.now();
    }

    public boolean isMaintenance() {
        return maintenance;
    }

    public void setMaintenance(boolean maintenance) {
        this.maintenance = maintenance;
    }

    public LocalDate getDateProchainEntretien() {
        return dateProchainEntretien;
    }

    public void setDateProchainEntretien(LocalDate dateProchainEntretien) {
        this.dateProchainEntretien = dateProchainEntretien;
    }

    // Constructor with required fields
    public Entretien(long vehiculeId, LocalDate dateEntretien, String typeEntretien,
                    int kilometrageActuel, double cout,String cheminFacture,boolean st,String description) {
        this.vehiculeId = vehiculeId;
        this.dateEntretien = dateEntretien;
        this.typeEntretien = typeEntretien;
        this.kilometrageActuel = kilometrageActuel;
        this.cout = cout;
        this.createdAt = LocalDate.now();
        this.cheminFacture = cheminFacture;
        this.statut = st;
        this.description = description;
    }

    public Entretien(LocalDate dateEntretien, String typeEntretien, double cout) {
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public LocalDate getDateEntretien() {
        return dateEntretien;
    }

    public void setDateEntretien(LocalDate dateEntretien) {
        this.dateEntretien = dateEntretien;
    }

    public String getTypeEntretien() {
        return typeEntretien;
    }

    public void setTypeEntretien(String typeEntretien) {
        this.typeEntretien = typeEntretien;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getKilometrageActuel() {
        return kilometrageActuel;
    }

    public void setKilometrageActuel(int kilometrageActuel) {
        this.kilometrageActuel = kilometrageActuel;
    }

    public double getCout() {
        return cout;
    }

    public void setCout(double cout) {
        this.cout = cout;
    }

    public boolean isDone() {
        return statut;
    }

    public void setStatut(boolean statut) {
        this.statut = statut;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public String getCheminFacture() {
        return cheminFacture;
    }

    public void setCheminFacture(String cheminFacture) {
        this.cheminFacture = cheminFacture;
    }

    @Override
    public String toString() {
        return String.format("Entretien du %s - %s (Kilométrage: %d km)", 
                           dateEntretien, typeEntretien, kilometrageActuel);
    }
}
