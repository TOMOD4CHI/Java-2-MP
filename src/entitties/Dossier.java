package entitties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dossier {
    private Long id;
    private Map<TypeDocument, Document> documents;
    private List<Paiement> paiements;

    public Dossier() {
        this.documents = new HashMap<>();
        this.paiements = new ArrayList<>();
    }

    public Map<TypeDocument, Document> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<TypeDocument, Document> documents) {
        this.documents = documents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<Paiement> getPaiements() {
        return paiements;
    }

    public void setPaiements(List<Paiement> paiements) {
        this.paiements = paiements;
    }
}
