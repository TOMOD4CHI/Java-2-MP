package org.cpi2.entities;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entity class representing a vehicle in the auto school system
 */
public class Vehicule {
    private int id;
    private String immatriculation;
    private TypePermis typePermis;
    private String marque;
    private String modele;
    private Integer annee;
    private LocalDate dateMiseEnService;
    private int kilometrageTotal;
    private int kilometrageProchainEntretien;
    private LocalDate dateProchainEntretien;
    private LocalDate dateDerniereVisiteTechnique;
    private LocalDate dateProchaineVisiteTechnique;
    private LocalDate dateExpirationAssurance;
    private String statut;
    private LocalDate createdAt;
    private List<Entretien> entretiens;

    // Default constructor
    public Vehicule() {
        this.kilometrageTotal = 0;
        this.kilometrageProchainEntretien = 10000;
        this.statut = "Disponible";
        this.entretiens = new ArrayList<>();
    }

    // Constructor with required fields
    public Vehicule(String immatriculation, String marque, String modele,int annee, TypePermis typePermis,
                   LocalDate dateMiseEnService,int kilometrageTotal, int kilometrageProchainEntretien) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.typePermis = typePermis;
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageTotal = kilometrageTotal ;
        this.kilometrageProchainEntretien = kilometrageProchainEntretien;
        this.statut = "Disponible";
        this.entretiens = new ArrayList<>();
    }

    // Full constructor
    public Vehicule(String immatriculation, String marque, String modele, Integer annee, 
                   TypePermis typePermis, LocalDate dateMiseEnService, Integer kilometrageTotal, 
                   Integer kilometrageProchainEntretien, LocalDate dateProchainEntretien,
                   LocalDate dateDerniereVisiteTechnique, LocalDate dateProchaineVisiteTechnique,
                   LocalDate dateExpirationAssurance, String statut) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.typePermis = typePermis;
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageTotal = kilometrageTotal;
        this.kilometrageProchainEntretien = kilometrageProchainEntretien;
        this.dateProchainEntretien = dateProchainEntretien;
        this.dateDerniereVisiteTechnique = dateDerniereVisiteTechnique;
        this.dateProchaineVisiteTechnique = dateProchaineVisiteTechnique;
        this.dateExpirationAssurance = dateExpirationAssurance;
        this.statut = statut;
        this.entretiens = new ArrayList<>();
    }
    
    // Helper property for JavaFX TableView to display marque + modele
    public StringProperty marqueModeleProperty() {
        return new SimpleStringProperty(marque + " " + modele);
    }

    // Equals and Hashcode based on immatriculation (license plate)
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Vehicule)) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(immatriculation, vehicule.immatriculation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(immatriculation);
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public TypePermis getTypePermis() {
        return typePermis;
    }

    public void setTypePermis(TypePermis typePermis) {
        this.typePermis = typePermis;
    }

    public String getMarque() {
        return marque;
    }

    public void setMarque(String marque) {
        this.marque = marque;
    }

    public String getModele() {
        return modele;
    }

    public void setModele(String modele) {
        this.modele = modele;
    }

    public Integer getAnnee() {
        return annee;
    }

    public void setAnnee(Integer annee) {
        this.annee = annee;
    }

    public LocalDate getDateMiseEnService() {
        return dateMiseEnService;
    }

    public void setDateMiseEnService(LocalDate dateMiseEnService) {
        this.dateMiseEnService = dateMiseEnService;
    }

    public int getKilometrageTotal() {
        return kilometrageTotal;
    }

    public void setKilometrageTotal(int kilometrageTotal) {
        this.kilometrageTotal = kilometrageTotal;
    }

    public int getKilometrageProchainEntretien() {
        return kilometrageProchainEntretien;
    }

    public void setKilometrageProchainEntretien(int kilometrageProchainEntretien) {
        this.kilometrageProchainEntretien = kilometrageProchainEntretien;
    }

    public LocalDate getDateProchainEntretien() {
        return dateProchainEntretien;
    }

    public void setDateProchainEntretien(LocalDate dateProchainEntretien) {
        this.dateProchainEntretien = dateProchainEntretien;
    }

    public LocalDate getDateDerniereVisiteTechnique() {
        return dateDerniereVisiteTechnique;
    }

    public void setDateDerniereVisiteTechnique(LocalDate dateDerniereVisiteTechnique) {
        this.dateDerniereVisiteTechnique = dateDerniereVisiteTechnique;
    }

    public LocalDate getDateProchaineVisiteTechnique() {
        return dateProchaineVisiteTechnique;
    }

    public void setDateProchaineVisiteTechnique(LocalDate dateProchaineVisiteTechnique) {
        this.dateProchaineVisiteTechnique = dateProchaineVisiteTechnique;
    }

    public LocalDate getDateExpirationAssurance() {
        return dateExpirationAssurance;
    }

    public void setDateExpirationAssurance(LocalDate dateExpirationAssurance) {
        this.dateExpirationAssurance = dateExpirationAssurance;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public List<Entretien> getEntretiens() {
        return entretiens;
    }

    public void setEntretiens(List<Entretien> entretiens) {
        this.entretiens = entretiens;
    }

    public void addEntretien(Entretien entretien) {
        this.entretiens.add(entretien);
    }

    @Override
    public String toString() {
        return immatriculation + " - " + marque + " " + modele;
    }

    public String getTypeVehicule() {
        return typePermis.getTypeVehicule();
    }
}

