package org.cpi2.entitties;

import java.time.LocalDate;

/**
 * Entity class representing a vehicle maintenance record
 */
public class Entretien {
    private int id;
    private int vehiculeId;
    private LocalDate dateEntretien;
    private String typeEntretien;
    private String description;
    private int kilometrageActuel;
    private double cout;
    private String prestataire;
    private LocalDate createdAt;

    // Default constructor
    public Entretien() {
        this.dateEntretien = LocalDate.now();
        this.createdAt = LocalDate.now();
    }

    // Constructor with required fields
    public Entretien(int vehiculeId, LocalDate dateEntretien, String typeEntretien, 
                    int kilometrageActuel, double cout) {
        this.vehiculeId = vehiculeId;
        this.dateEntretien = dateEntretien;
        this.typeEntretien = typeEntretien;
        this.kilometrageActuel = kilometrageActuel;
        this.cout = cout;
        this.createdAt = LocalDate.now();
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

    public int getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(int vehiculeId) {
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

    public String getPrestataire() {
        return prestataire;
    }

    public void setPrestataire(String prestataire) {
        this.prestataire = prestataire;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Entretien du %s - %s (Kilométrage: %d km)", 
                           dateEntretien, typeEntretien, kilometrageActuel);
    }
}
