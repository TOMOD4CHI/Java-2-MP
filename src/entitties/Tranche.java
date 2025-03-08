package entitties;

import java.time.LocalDate;

public class Tranche {
    private Long id;
    private Integer numero;
    private LocalDate dateEcheance;
    private Double montant;
    private StatutPaiement statut;

    public Tranche() {
        this.statut = StatutPaiement.EN_ATTENTE;
    }

    public Tranche(Integer numero, LocalDate dateEcheance, Double montant) {
        this();
        this.numero = numero;
        this.dateEcheance = dateEcheance;
        this.montant = montant;
    }

    public LocalDate getDateEcheance() {
        return dateEcheance;
    }

    public void setDateEcheance(LocalDate dateEcheance) {
        this.dateEcheance = dateEcheance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getMontant() {
        return montant;
    }

    public void setMontant(Double montant) {
        this.montant = montant;
    }

    public Integer getNumero() {
        return numero;
    }

    public void setNumero(Integer numero) {
        this.numero = numero;
    }

    public StatutPaiement getStatut() {
        return statut;
    }

    public void setStatut(StatutPaiement statut) {
        this.statut = statut;
    }
}
