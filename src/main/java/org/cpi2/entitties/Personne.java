package org.cpi2.entitties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

public abstract class Personne {
    protected Long id;
    protected String nom;
    protected String prenom;
    protected String cin;
    protected String adresse;
    protected String telephone;
    protected String email;
    protected LocalDateTime dateCreation;
    protected LocalDate dateNaissance;

    protected Personne() {
        this.dateCreation = LocalDateTime.now();
        this.dateNaissance = LocalDate.now();
    }

    protected Personne(String nom, String prenom, String cin, String adresse, String telephone) {
        this();
        this.nom = nom;
        this.prenom = prenom;
        this.cin = cin;
        this.adresse = adresse;
        this.telephone = telephone;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Personne)) return false;
        Personne personne = (Personne) o;
        return Objects.equals(cin, personne.cin);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cin);
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getCin() {
        return cin;
    }

    public void setCin(String cin) {
        this.cin = cin;
    }

    public LocalDateTime getDateCreation() {
        return dateCreation;
    }

    public void setDateCreation(LocalDateTime dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

        public String getTelephone() {
        return telephone;
    }

        public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }

    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
}
