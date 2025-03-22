package org.cpi2.entitties;

import java.time.LocalDateTime;

public class PaiementInscription extends Paiement {
    private Inscription inscription;
    private String typePaiement; // Daily, Weekly, Monthly...


    public PaiementInscription(Long id, Double montant, LocalDateTime datePaiement, ModePaiement modePaiement, Inscription inscription, String typePaiement) {
        super(id,montant,datePaiement,modePaiement);
        this.inscription = inscription;
        this.typePaiement = typePaiement;
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
