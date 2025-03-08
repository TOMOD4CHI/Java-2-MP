package entitties;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class SessionCode extends Session {
    private Integer capaciteMax;
    private List<Candidat> participants;

    public SessionCode() {
        super();
        this.participants = new ArrayList<>();
    }

    public SessionCode(LocalDate dateSession, LocalTime heureSession, long duree,Moniteur moniteur,
                       long  planId, Integer capaciteMax) {
        super(planId,dateSession, heureSession,duree, moniteur);
        this.capaciteMax = capaciteMax;
        this.participants = new ArrayList<>();
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

}
