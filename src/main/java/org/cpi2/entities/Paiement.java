package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public abstract class Paiement {
    protected Long id;
    protected Candidat candidat;
    protected Double montant;
    protected LocalDate datePaiement;
    protected StatutPaiement statut;
    protected ModePaiement modePaiement;
    protected String description;
    protected String typePaiement;

    protected Paiement(Long id,Candidat candidat ,Double montant, LocalDate datePaiement, ModePaiement modePaiement,String description,String typePaiement) {
        this.id = id;
        this.candidat = candidat;
        this.datePaiement = datePaiement;
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.statut = StatutPaiement.EN_ATTENTE;
        this.description = description;
        this.typePaiement = typePaiement;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public LocalDate getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDate datePaiement) {
        this.datePaiement = datePaiement;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
