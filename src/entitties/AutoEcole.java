package entitties;

import java.util.*;

public class AutoEcole {
    private Long id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String logoPath;
    private List<Vehicule> flotte;
    private Map<Long, Moniteur> moniteurs;
    private Map<Long, Candidat> candidats;

    public AutoEcole() {
        this.flotte = new ArrayList<>();
        this.moniteurs = new HashMap<>();
        this.candidats = new TreeMap<>();
    }

    public AutoEcole(String nom, String adresse, String telephone, String email) {
        this();
        this.nom = nom;
        this.adresse = adresse;
        this.telephone = telephone;
        this.email = email;
    }

    public String getAdresse() {
        return adresse;
    }

    public void setAdresse(String adresse) {
        this.adresse = adresse;
    }

    public Map<Long, Candidat> getCandidats() {
        return candidats;
    }

    public void setCandidats(Map<Long, Candidat> candidats) {
        this.candidats = candidats;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<Vehicule> getFlotte() {
        return flotte;
    }

    public void setFlotte(List<Vehicule> flotte) {
        this.flotte = flotte;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public Map<Long, Moniteur> getMoniteurs() {
        return moniteurs;
    }

    public void setMoniteurs(Map<Long, Moniteur> moniteurs) {
        this.moniteurs = moniteurs;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
