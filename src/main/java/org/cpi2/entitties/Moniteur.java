package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Moniteur {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private String adresse;
    private LocalDate dateEmbauche;
    private String cin;
    private List<TypePermis> specialites;
    
    public Moniteur() {
        this.specialites = new ArrayList<>();
    }
    
    public Moniteur(Long id, String nom, String prenom, String email, String telephone, 
                   String adresse, LocalDate dateEmbauche, String cin) {
        this();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.dateEmbauche = dateEmbauche;
        this.cin = cin;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }
    
    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }
    
    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }
    
    public String getCin() {
        return cin;
    }
    
    public void setCin(String cin) {
        this.cin = cin;
    }
    
    public List<TypePermis> getSpecialites() {
        return specialites;
    }
    
    public void setSpecialites(List<TypePermis> specialites) {
        this.specialites = specialites;
    }
    
    public void addSpecialite(TypePermis specialite) {
        if (!this.specialites.contains(specialite)) {
            this.specialites.add(specialite);
        }
    }
    
    public boolean removeSpecialite(TypePermis specialite) {
        return this.specialites.remove(specialite);
    }
    
    public boolean hasSpecialite(TypePermis specialite) {
        return this.specialites.contains(specialite);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Moniteur moniteur = (Moniteur) o;
        return id.equals(moniteur.id) &&
               Objects.equals(nom, moniteur.nom) &&
               Objects.equals(prenom, moniteur.prenom);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom);
    }
    
    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
