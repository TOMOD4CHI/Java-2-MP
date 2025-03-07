package org.cpi2.entitties;

import java.time.LocalDateTime;

public abstract class Paiement {
    protected Long id;
    protected Double montant;
    protected LocalDateTime datePaiement;
    protected StatutPaiement statut;

    protected Paiement() {
        this.datePaiement = LocalDateTime.now();
        this.statut = StatutPaiement.EN_ATTENTE;
    }

    protected Paiement(Double montant) {
        this();
        this.montant = montant;
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
