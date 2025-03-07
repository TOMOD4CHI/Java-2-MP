package org.cpi2.entitties;

import java.time.LocalDateTime;

public class RendezVous {
    private Long id;
    private Session session;
    private Candidat candidat;
    private String pointRencontre;
    private Boolean confirme;
    private LocalDateTime dateHeure;

    public RendezVous() {
        this.confirme = false;
    }

    public RendezVous(Session session, Candidat candidat, String pointRencontre, LocalDateTime dateHeure) {
        this();
        this.session = session;
        this.candidat = candidat;
        this.pointRencontre = pointRencontre;
        this.dateHeure = dateHeure;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public Boolean getConfirme() {
        return confirme;
    }

    public void setConfirme(Boolean confirme) {
        this.confirme = confirme;
    }

    public LocalDateTime getDateHeure() {
        return dateHeure;
    }

    public void setDateHeure(LocalDateTime dateHeure) {
        this.dateHeure = dateHeure;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPointRencontre() {
        return pointRencontre;
    }

    public void setPointRencontre(String pointRencontre) {
        this.pointRencontre = pointRencontre;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }
}
