package entitties;

import java.util.HashMap;
import java.util.Map;

public class Dossier {
    private Long id;
    private Map<TypeDocument, Document> documents;

    public Dossier() {
        this.documents = new HashMap<>();
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
}
