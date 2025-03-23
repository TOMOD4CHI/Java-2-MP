package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SessionCode extends Session {
    private String salle;
    private Integer capaciteMax;
    private List<Candidat> participants;

    public SessionCode() {
        super();
        this.participants = new ArrayList<>();
    }

    public SessionCode(LocalDate dateSession, LocalTime heureSession, Moniteur moniteur,
                       Double prix, String salle, Integer capaciteMax) {
        super(dateSession, heureSession, moniteur, prix, TypeSession.CODE);
        this.salle = salle;
        this.capaciteMax = capaciteMax;
        this.participants = new ArrayList<>();
    }

    public SessionCode(LocalDate dateSession, LocalTime heureDebut, long minutes, Moniteur moniteur, int planId, int capaciteMax) {
    }

    public Integer getCapaciteMax() {
        return capaciteMax;
    }

    public void setCapaciteMax(Integer capaciteMax) {
        this.capaciteMax = capaciteMax;
    }

    public List<Candidat> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Candidat> participants) {
        this.participants = participants;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }
}
