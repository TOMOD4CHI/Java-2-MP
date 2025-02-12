package entitties;

import java.time.LocalDate;

public class Entretien {
    private Long id;
    private LocalDate dateEntretien;
    private String typeEntretien;
    private String description;
    private Double cout;
    private Document facture;

    public Entretien() {
    }

    public Entretien(LocalDate dateEntretien, String typeEntretien, Double cout) {
        this.dateEntretien = dateEntretien;
        this.typeEntretien = typeEntretien;
        this.cout = cout;
    }

    public Double getCout() {
        return cout;
    }

    public void setCout(Double cout) {
        this.cout = cout;
    }

    public LocalDate getDateEntretien() {
        return dateEntretien;
    }

    public void setDateEntretien(LocalDate dateEntretien) {
        this.dateEntretien = dateEntretien;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Document getFacture() {
        return facture;
    }

    public void setFacture(Document facture) {
        this.facture = facture;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTypeEntretien() {
        return typeEntretien;
    }

    public void setTypeEntretien(String typeEntretien) {
        this.typeEntretien = typeEntretien;
    }
}
