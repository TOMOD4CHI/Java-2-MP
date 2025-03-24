package org.cpi2.entities;

public class Candidat extends Personne {
    private Dossier dossier;
    private TypePermis typePermis;

    public Candidat() {
        super();
        this.dossier = new Dossier();
    }

    public Candidat(String nom, String prenom, String cin, String adresse, String telephone) {
        super(nom, prenom, cin, adresse, telephone);
        this.dossier = new Dossier();
    }

    public TypePermis getTypePermis() {
        return typePermis;
    }

    public Dossier getDossier() {
        return dossier;
    }

    public void setDossier(Dossier dossier) {
        this.dossier = dossier;
    }

    public void setTypePermis(TypePermis typePermis) {
        this.typePermis = typePermis;
    }


}
