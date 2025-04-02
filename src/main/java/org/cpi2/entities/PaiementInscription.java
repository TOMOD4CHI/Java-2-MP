package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaiementInscription extends Paiement {
    private Inscription inscription;
    private String typePaiement;


    public PaiementInscription(Long id, Candidat candidat, Double montant, LocalDate datePaiement, ModePaiement modePaiement, Inscription inscription, String typePaiement, String description) {
        super(id,candidat,montant,datePaiement,modePaiement,(description == null || description.isEmpty())?"Paiement d'inscription pour "+inscription.getPlan().getDescription():description,typePaiement);
        this.inscription = inscription;
    }

    public Inscription getInscription() {
        return inscription;
    }

    public void setInscription(Inscription inscription) {
        this.inscription = inscription;
    }

    public String getTypePaiement() {
        return typePaiement;
    }

    public void setTypePaiement(String typePaiement) {
        this.typePaiement = typePaiement;
    }
}
