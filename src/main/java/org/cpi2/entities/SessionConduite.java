package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SessionConduite extends Session {
    private Vehicule vehicule;
    private String pointRencontre;
    private Integer kilometresParcourus;
    private List<Candidat> candidats;

    public SessionConduite() {
        super();
        this.setTypeSession(TypeSession.CONDUITE);
        this.candidats = new ArrayList<>();
    }
    
    public List<Candidat> getCandidats() {
        return candidats;
    }
    
    public void setCandidats(List<Candidat> candidats) {
        this.candidats = candidats;
    }
    
    public void ajouterCandidat(Candidat candidat) {
        if (this.candidats == null) {
            this.candidats = new ArrayList<>();
        }
        this.candidats.add(candidat);
    }

    public SessionConduite(LocalDate dateSession, LocalTime heureSession, Moniteur moniteur,
                           Double prix, Vehicule vehicule, String pointRencontre) {
        super(dateSession, heureSession, moniteur, prix, TypeSession.CONDUITE);
        this.vehicule = vehicule;
        this.pointRencontre = pointRencontre;
        this.kilometresParcourus = 0;
    }

    public SessionConduite(long planId, LocalDate dateSession, LocalTime heureDebut, long minutes, Moniteur moniteur, Vehicule vehicule, Location pointRencontre) {
        super(dateSession, heureDebut, moniteur, null, TypeSession.CONDUITE);
        this.vehicule = vehicule;
        this.pointRencontre = pointRencontre != null ? pointRencontre.getLatitude() + "," + pointRencontre.getLongitude() : "Non défini";
        this.kilometresParcourus = 0;
        // Stocker le planId dans la classe parent
        this.setPlanId(planId);
        // Définir la durée dans la classe parent
        this.setDuree(minutes);
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
