package entitties;

import java.util.ArrayList;
import java.util.List;

public class PaiementHeures extends Paiement {
    private Integer nombreHeures;
    private Boolean paiementPartiel;
    private List<Tranche> tranches;

    public PaiementHeures() {
        super();
        this.tranches = new ArrayList<>();
    }

    public PaiementHeures(Double montant, Integer nombreHeures, Boolean paiementPartiel) {
        super(montant);
        this.nombreHeures = nombreHeures;
        this.paiementPartiel = paiementPartiel;
        this.tranches = new ArrayList<>();
    }

    public Integer getNombreHeures() {
        return nombreHeures;
    }

    public void setNombreHeures(Integer nombreHeures) {
        this.nombreHeures = nombreHeures;
    }

    public Boolean getPaiementPartiel() {
        return paiementPartiel;
    }

    public void setPaiementPartiel(Boolean paiementPartiel) {
        this.paiementPartiel = paiementPartiel;
    }

    public List<Tranche> getTranches() {
        return tranches;
    }

    public void setTranches(List<Tranche> tranches) {
        this.tranches = tranches;
    }
}
