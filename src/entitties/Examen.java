package entitties;

import java.time.LocalDate;

public class Examen {
    private Long id;
    private TypeExamen type;
    private LocalDate date;
    private Double frais;
    private Boolean resultat;
    private Candidat candidat;

    public Examen() {
        this.resultat = false;
    }

    public Examen(TypeExamen type, LocalDate date, Double frais, Candidat candidat) {
        this();
        this.type = type;
        this.date = date;
        this.frais = frais;
        this.candidat = candidat;
    }

    public Candidat getCandidat() {
        return candidat;
    }

    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getFrais() {
        return frais;
    }

    public void setFrais(Double frais) {
        this.frais = frais;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getResultat() {
        return resultat;
    }

    public void setResultat(Boolean resultat) {
        this.resultat = resultat;
    }

    public TypeExamen getType() {
        return type;
    }

    public void setType(TypeExamen type) {
        this.type = type;
    }
}
