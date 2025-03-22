package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Moniteur extends Personne {
    private LocalDate dateEmbauche;
    private List<TypePermis> specialites;
    
    public Moniteur() {
        super();
        this.specialites = new ArrayList<>();
    }
    
    public Moniteur(String nom, String prenom, String cin, String adresse, String telephone,
                   String email, LocalDate dateNaissance, LocalDate dateEmbauche) {
        super(nom, prenom, cin, adresse, telephone);
        this.email = email;
        this.dateNaissance = dateNaissance;
        this.dateEmbauche = dateEmbauche;
        this.specialites = new ArrayList<>();
    }
    
    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }
    
    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }
    
    public List<TypePermis> getSpecialites() {
        return specialites;
    }
    
    public void setSpecialites(List<TypePermis> specialites) {
        this.specialites = specialites;
    }
    
    public void addSpecialite(TypePermis typePermis) {
        this.specialites.add(typePermis);
    }
    
    public boolean removeSpecialite(TypePermis typePermis) {
        return this.specialites.remove(typePermis);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Moniteur moniteur = (Moniteur) o;
        return Objects.equals(id, moniteur.id) &&
               Objects.equals(cin, moniteur.cin);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, cin);
    }
    
    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
