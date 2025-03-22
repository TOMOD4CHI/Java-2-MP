package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.Objects;

public class Vehicule {
    private Long id;
    private String marque;
    private String modele;
    private String immatriculation;
    private int annee;
    private String type;
    private double kilometrage;
    private boolean disponible;
    private double kilometrageAvantEntretien;
    private double kilometrageTotal;
    private LocalDate dateMiseEnService;
    private TypePermis typePermis;
    
    public Vehicule() {
        this.disponible = true; // Default value
        this.kilometrageAvantEntretien = 10000.0; // Default value
    }
    
    public Vehicule(Long id, String marque, String modele, String immatriculation, 
                 int annee, String type, double kilometrage, boolean disponible) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.annee = annee;
        this.type = type;
        this.kilometrage = kilometrage;
        this.disponible = disponible;
        this.kilometrageAvantEntretien = 10000.0; // Default value
    }
    
    public Vehicule(Long id, String marque, String modele, String immatriculation, 
                 int annee, String type, double kilometrage, boolean disponible, double kilometrageAvantEntretien) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.annee = annee;
        this.type = type;
        this.kilometrage = kilometrage;
        this.disponible = disponible;
        this.kilometrageAvantEntretien = kilometrageAvantEntretien;
    }
    
    public Vehicule(String immatriculation, String marque, String modele, 
                 String type, int annee, double kilometrage, boolean disponible, double kilometrageAvantEntretien) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.type = type;
        this.annee = annee;
        this.kilometrage = kilometrage;
        this.disponible = disponible;
        this.kilometrageAvantEntretien = kilometrageAvantEntretien;
    }
    
    public Vehicule(String immatriculation, String marque, String modele, 
                 TypePermis typePermis, LocalDate dateMiseEnService, double kilometrageAvantEntretien) {
        this.immatriculation = immatriculation;
        this.marque = marque;
        this.modele = modele;
        this.typePermis = typePermis;
        this.type = typePermis.name();  
        this.annee = dateMiseEnService.getYear();
        this.dateMiseEnService = dateMiseEnService;
        this.kilometrageAvantEntretien = kilometrageAvantEntretien;
        this.disponible = true;
        this.kilometrage = 0.0;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
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
    
    public String getImmatriculation() {
        return immatriculation;
    }
    
    public void setImmatriculation(String immatriculation) {
        this.immatriculation = immatriculation;
    }
    
    public int getAnnee() {
        return annee;
    }
    
    public void setAnnee(int annee) {
        this.annee = annee;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public double getKilometrage() {
        return kilometrage;
    }
    
    public void setKilometrage(double kilometrage) {
        this.kilometrage = kilometrage;
    }
    
    public boolean isDisponible() {
        return disponible;
    }
    
    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }
    
    public double getKilometrageAvantEntretien() {
        return kilometrageAvantEntretien;
    }
    
    public void setKilometrageAvantEntretien(double kilometrageAvantEntretien) {
        this.kilometrageAvantEntretien = kilometrageAvantEntretien;
    }
    
    public double getKilometrageTotal() {
        return kilometrageTotal;
    }
    
    public void setKilometrageTotal(double kilometrageTotal) {
        this.kilometrageTotal = kilometrageTotal;
    }
    
    public LocalDate getDateMiseEnService() {
        return dateMiseEnService;
    }
    
    public void setDateMiseEnService(LocalDate dateMiseEnService) {
        this.dateMiseEnService = dateMiseEnService;
    }
    
    public TypePermis getTypePermis() {
        return typePermis;
    }
    
    public void setTypePermis(TypePermis typePermis) {
        this.typePermis = typePermis;
        this.type = typePermis.name();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Vehicule vehicule = (Vehicule) o;
        return Objects.equals(id, vehicule.id) &&
               Objects.equals(immatriculation, vehicule.immatriculation);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, immatriculation);
    }
    
    @Override
    public String toString() {
        return marque + " " + modele + " (" + immatriculation + ")";
    }
}
