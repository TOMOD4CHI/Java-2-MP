package org.cpi2.entitties;

public class PaiementExamen extends Paiement {
    private TypeExamen typeExamen;
    private Boolean reussite;

    public PaiementExamen() {
        super();
    }

    public PaiementExamen(Double montant, TypeExamen typeExamen) {
        super(montant);
        this.typeExamen = typeExamen;
        this.reussite = false;
    }

    public Boolean getReussite() {
        return reussite;
    }

    public void setReussite(Boolean reussite) {
        this.reussite = reussite;
    }

    public TypeExamen getTypeExamen() {
        return typeExamen;
    }

    public void setTypeExamen(TypeExamen typeExamen) {
        this.typeExamen = typeExamen;
    }
}
