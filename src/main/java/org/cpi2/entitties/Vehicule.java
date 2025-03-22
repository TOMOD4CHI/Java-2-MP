package org.cpi2.entitties;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.util.*;

public class Vehicule {
    private Long id;
    private String immatriculation;
    private String marque;
    private String modele;
    private Integer annee;
    private TypePermis typePermis;
    private LocalDate dateMiseEnService;
    private Integer kilometrageTotal;
    private Integer kilometrageProchainEntretien;
    private Map<LocalDate, Entretien> historique;
    private LocalDate dateProchainEntretien;
    private LocalDate dateDerniereVisiteTechnique;
    private LocalDate dateProchaineVisiteTechnique;
    private LocalDate dateExpirationAssurance;
    private String statut;
    
    /**
     * No-argument constructor for Vehicule class
     */
    public Vehicule() {
        this.kilometrageTotal = 0;
        this.historique = new TreeMap<>();
        this.statut = "Disponible";
    }

    /**
     * Full constructor for Vehicule class
     */
    public Vehicule(String immatriculation, String marque, String modele, Integer annee, TypePermis typePermis,
                    LocalDate dateMiseEnService, Integer kilometrageProchainEntretien,
                    LocalDate dateProchainEntretien, LocalDate dateDerniereVisiteTechnique,
                    LocalDate dateProchaineVisiteTechnique, LocalDate dateExpirationAssurance) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.annee = annee;
        this.typePermis = typePermis;
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageProchainEntretien = kilometrageProchainEntretien;
        this.kilometrageTotal = 0;
        this.dateProchainEntretien = dateProchainEntretien;
        this.dateDerniereVisiteTechnique = dateDerniereVisiteTechnique;
        this.dateProchaineVisiteTechnique = dateProchaineVisiteTechnique;
        this.dateExpirationAssurance = dateExpirationAssurance;
        this.historique = new TreeMap<>();
        this.statut = "Disponible";
    }
    
    /**
     * Property for JavaFX TableView to display marque + modele as a single column
     */
    public StringProperty marqueModeleProperty() {
        return new SimpleStringProperty(marque + " " + modele);
    }

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

    public Map<LocalDate, Entretien> getHistorique() {
        return historique;
    }

    public void setHistorique(Map<LocalDate, Entretien> historique) {
        this.historique = historique;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImmatriculation() {
        return immatriculation;
    }

    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }

    public Integer getKilometrageProchainEntretien() {
        return kilometrageProchainEntretien;
    }

    public void setKilometrageProchainEntretien(Integer kilometrageProchainEntretien) {
        this.kilometrageProchainEntretien = kilometrageProchainEntretien;
    }

    public Integer getKilometrageTotal() {
        return kilometrageTotal;
    }

    public void setKilometrageTotal(Integer kilometrageTotal) {
        this.kilometrageTotal = kilometrageTotal;
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

    public TypePermis getTypePermis() {
        return typePermis;
    }

    public void setTypePermis(TypePermis typePermis) {
        this.typePermis = typePermis;
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
    
    /**
     * Add a new entretien to the history
     */
    public void addEntretien(Entretien entretien) {
        this.historique.put(entretien.getDateEntretien(), entretien);
    }
    
    /**
     * Returns a display string for the vehicle
     */
    @Override
    public String toString() {
        return marque + " " + modele + " (" + immatriculation + ")";
    }
}

