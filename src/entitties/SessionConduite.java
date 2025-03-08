package entitties;

import java.time.LocalDate;
import java.time.LocalTime;

public class SessionConduite extends Session {
    private Vehicule vehicule;
    private Location pointRencontre;
    private Integer kilometresParcourus;

    public SessionConduite() {
        super();
    }

    public SessionConduite(long planId,LocalDate dateSession, LocalTime heureSession,long duree, Moniteur moniteur
                           , Vehicule vehicule, Location pointRencontre) {
        super(planId,dateSession,heureSession,duree, moniteur);
        this.vehicule = vehicule;
        this.pointRencontre = pointRencontre;
        this.kilometresParcourus = 0;
    }

    public Integer getKilometresParcourus() {
        return kilometresParcourus;
    }

    public void setKilometresParcourus(Integer kilometresParcourus) {
        this.kilometresParcourus = kilometresParcourus;
    }

    public Location getPointRencontre() {
        return pointRencontre;
    }

    public void setPointRencontre(Location pointRencontre) {
        this.pointRencontre = pointRencontre;
    }

    public Vehicule getVehicule() {
        return vehicule;
    }

    public void setVehicule(Vehicule vehicule) {
        this.vehicule = vehicule;
    }
}
