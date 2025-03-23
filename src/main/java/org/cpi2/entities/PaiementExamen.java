package org.cpi2.entities;

import java.time.LocalDateTime;

public class PaiementExamen extends Paiement {
    private Examen examen;


    public PaiementExamen(Long id,Double montant, LocalDateTime datePaiement, ModePaiement modePaiement,Examen examen) {
        super(id,montant, datePaiement,modePaiement);
        this.examen = examen;
    }

    public Examen getTypeExamen() {
        return examen;
    }

    public void setTypeExamen(Examen examen) {
        this.examen = examen;
    }
}
