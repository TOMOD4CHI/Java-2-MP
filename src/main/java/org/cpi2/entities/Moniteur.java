package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Moniteur extends Personne {
    private LocalDate dateEmbauche;
    private LocalDate dateNaissance;
    private String email;
    private Double salaire;
    private String statut;
    private Set<TypePermis> specialites;
    private Map<LocalDateTime, RendezVous> emploiDuTemps;

    public Moniteur() {
        super();
        this.specialites = new HashSet<>();
        this.emploiDuTemps = new TreeMap<>();
        this.statut = "Actif";
    }

    public Moniteur(String nom, String prenom, String cin, String adresse, String telephone, 
                   LocalDate dateNaissance, String email, LocalDate dateEmbauche, Double salaire) {
        super(nom, prenom, cin, adresse, telephone);
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.dateEmbauche = dateEmbauche;
        this.salaire = salaire;
        this.statut = "Actif";
        this.specialites = new HashSet<>();
        this.emploiDuTemps = new TreeMap<>();
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getSalaire() {
        return salaire;
    }

    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public String getStatut() {
        return statut;
    }

    public void setStatut(String statut) {
        this.statut = statut;
    }

    public Map<LocalDateTime, RendezVous> getEmploiDuTemps() {
        return emploiDuTemps;
    }

    public void setEmploiDuTemps(Map<LocalDateTime, RendezVous> emploiDuTemps) {
        this.emploiDuTemps = emploiDuTemps;
    }

    public Set<TypePermis> getSpecialites() {
        return specialites;
    }

    public void setSpecialites(Set<TypePermis> specialites) {
        this.specialites = specialites;
    }
    
    public void addSpecialite(TypePermis typePermis) {
        this.specialites.add(typePermis);
    }
    
    public void removeSpecialite(TypePermis typePermis) {
        this.specialites.remove(typePermis);
    }
    
    @Override
    public String toString() {
        return getNom() + " " + getPrenom();
    }
}
