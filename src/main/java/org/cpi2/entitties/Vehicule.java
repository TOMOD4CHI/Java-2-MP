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
    private TypePermis typePermis;
    private LocalDate dateMiseEnService;
    private Integer kilometrageTotal;
    private Integer kilometrageAvantEntretien;
    private Map<LocalDate, Entretien> historique;
    private LocalDate dateProchainEntretien;
    private LocalDate dateDerniereVisiteTechnique;
    private LocalDate dateProchaineVisiteTechnique;
    private LocalDate dateExpirationAssurance;
    
    /**
     * No-argument constructor for Vehicule class
     */
    public Vehicule() {
        this.kilometrageTotal = 0;
        this.historique = new TreeMap<>();
    }

    public Vehicule(String immatriculation, String voiture, String renault, String clio, int i, LocalDate localDate) {
        this.kilometrageTotal = 0;
        this.historique = new TreeMap<>();
    }
    
    public StringProperty marqueModeleProperty() {
        return new SimpleStringProperty(marque + " " + modele); // Concatenates marque and modele
    }

    public Vehicule(String immatriculation, String marque, String modele, TypePermis typePermis,
                    LocalDate dateMiseEnService, Integer kilometrageAvantEntretien) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.typePermis = typePermis;
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageAvantEntretien = kilometrageAvantEntretien;
        this.kilometrageTotal = 0;
        this.historique = new TreeMap<>();
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

    public Integer getKilometrageAvantEntretien() {
        return kilometrageAvantEntretien;
    }

    public void setKilometrageAvantEntretien(Integer kilometrageAvantEntretien) {
        this.kilometrageAvantEntretien = kilometrageAvantEntretien;
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

    public void setProchainEntretien(LocalDate dateProchainEntretien) {
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


}

