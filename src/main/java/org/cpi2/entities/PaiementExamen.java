package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class PaiementExamen extends Paiement {
    private Examen examen;


    public PaiementExamen(StatutPaiement statutPaiement,Long id, Candidat candidat, Double montant, LocalDate datePaiement, ModePaiement modePaiement, Examen examen, String description) {
        super(statutPaiement,id,candidat,montant, datePaiement,modePaiement,(description == null || description.isEmpty())?"Paiement d'inscription pour "+examen.getTypeExamen():description,null);
        this.examen = examen;
    }

    public Examen getTypeExamen() {
        return examen;
    }

    public void setTypeExamen(Examen examen) {
        this.examen = examen;
    }
}
