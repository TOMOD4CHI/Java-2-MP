package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Candidat extends Personne {
    private LocalDate dateInscription;
    private TypePermis typePermis;
    private Dossier dossier;
    private List<RendezVous> rendezVous;
    private Set<Examen> examens;

    public Candidat() {
        super();
        this.dossier = new Dossier();
        this.rendezVous = new ArrayList<>();
        this.examens = new HashSet<>();
    }

    public Candidat(String nom, String prenom, String cin, String adresse, String telephone,
                    LocalDate dateInscription, TypePermis typePermis) {
        super(nom, prenom, cin, adresse, telephone);
        this.dateInscription = dateInscription;
        this.typePermis = typePermis;
        this.dossier = new Dossier();
        this.rendezVous = new ArrayList<>();
        this.examens = new HashSet<>();
    }

    public LocalDate getDateInscription() {
        return dateInscription;
    }

    public void setDateInscription(LocalDate dateInscription) {
        this.dateInscription = dateInscription;
    }

    public Dossier getDossier() {
        return dossier;
    }

    public void setDossier(Dossier dossier) {
        this.dossier = dossier;
    }

    public Set<Examen> getExamens() {
        return examens;
    }

    public void setExamens(Set<Examen> examens) {
        this.examens = examens;
    }

    public List<RendezVous> getRendezVous() {
        return rendezVous;
    }

    public void setRendezVous(List<RendezVous> rendezVous) {
        this.rendezVous = rendezVous;
    }

    public TypePermis getTypePermis() {
        return typePermis;
    }

    public void setTypePermis(TypePermis typePermis) {
        this.typePermis = typePermis;
    }
}
