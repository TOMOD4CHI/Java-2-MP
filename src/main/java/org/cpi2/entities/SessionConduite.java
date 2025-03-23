package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionConduite extends Session {
    private Vehicule vehicule;
    private String pointRencontre;
    private Integer kilometresParcourus;

    public SessionConduite() {
        super();
    }

    public SessionConduite(LocalDate dateSession, LocalTime heureSession, Moniteur moniteur,
                           Double prix, Vehicule vehicule, String pointRencontre) {
        super(dateSession, heureSession, moniteur, prix, TypeSession.CONDUITE);
        this.vehicule = vehicule;
        this.pointRencontre = pointRencontre;
        this.kilometresParcourus = 0;
    }

    public SessionConduite(long planId, LocalDate dateSession, LocalTime heureDebut, long minutes, Moniteur moniteur, Vehicule vehicule, Location pointRencontre) {
    }

    public Integer getKilometresParcourus() {
        return kilometresParcourus;
    }

    public void setKilometresParcourus(Integer kilometresParcourus) {
        this.kilometresParcourus = kilometresParcourus;
    }

    public String getPointRencontre() {
        return pointRencontre;
    }

    public void setPointRencontre(String pointRencontre) {
        this.pointRencontre = pointRencontre;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
}
