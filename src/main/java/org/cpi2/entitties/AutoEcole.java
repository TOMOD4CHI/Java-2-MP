package org.cpi2.entitties;

import java.util.*;
import java.util.Objects;

public class AutoEcole {
    private int id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String directeur;
    private String logo;
    private List<Vehicule> flotte;
    private Map<Long, Moniteur> moniteurs;
    private Map<Long, Candidat> candidats;

    public AutoEcole() {
        this.flotte = new ArrayList<>();
        this.moniteurs = new HashMap<>();
        this.candidats = new TreeMap<>();
    }

    public AutoEcole(int id, String nom, String adresse, String telephone, String email, String directeur, String logo) {
        this();
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.directeur = directeur;
        this.logo = logo;
    }

    public AutoEcole(int id, String nom, String adresse, String telephone, String email, String directeur) {
        this.id = id;
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
        this.directeur = directeur;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDirecteur() {
        return directeur;
    }

    public void setDirecteur(String directeur) {
        this.directeur = directeur;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<Vehicule> getFlotte() {
        return flotte;
    }

    public void setFlotte(List<Vehicule> flotte) {
        this.flotte = flotte;
    }

    public Map<Long, Moniteur> getMoniteurs() {
        return moniteurs;
    }

    public void setMoniteurs(Map<Long, Moniteur> moniteurs) {
        this.moniteurs = moniteurs;
    }

    public Map<Long, Candidat> getCandidats() {
        return candidats;
    }

    public void setCandidats(Map<Long, Candidat> candidats) {
        this.candidats = candidats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AutoEcole autoEcole = (AutoEcole) o;
        return id == autoEcole.id &&
               Objects.equals(nom, autoEcole.nom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom);
    }

    @Override
    public String toString() {
        return "AutoEcole{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", adresse='" + adresse + '\'' +
                ", telephone='" + telephone + '\'' +
                ", email='" + email + '\'' +
                ", directeur='" + directeur + '\'' +
                ", logo='" + logo + '\'' +
                '}';
    }
}
