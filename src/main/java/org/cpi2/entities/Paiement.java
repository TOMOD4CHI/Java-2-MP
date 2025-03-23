package org.cpi2.entities;

import java.time.LocalDateTime;

public abstract class Paiement {
    protected Long id;
    protected Double montant;
    protected LocalDateTime datePaiement;
    protected StatutPaiement statut;
    protected ModePaiement modePaiement;


    protected Paiement(Long id, Double montant, LocalDateTime datePaiement, ModePaiement modePaiement) {
        this.id = id;
        this.datePaiement = datePaiement;
        this.montant = montant;
        this.modePaiement = modePaiement;
        this.statut = StatutPaiement.EN_ATTENTE;
    }

    public ModePaiement getModePaiement() {
        return modePaiement;
    }

    public void setModePaiement(ModePaiement modePaiement) {
        this.modePaiement = modePaiement;
    }

    public LocalDateTime getDatePaiement() {
        return datePaiement;
    }

    public void setDatePaiement(LocalDateTime datePaiement) {
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
}
