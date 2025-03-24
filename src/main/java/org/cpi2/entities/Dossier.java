package org.cpi2.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Dossier {
    private Long id;
    private Map<TypeDocument, TreeSet<Document>> documents;

    public Dossier() {
        this.documents = new HashMap<>();
    }

    public Map<TypeDocument, TreeSet<Document>> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<TypeDocument, TreeSet<Document>> documents) {
        this.documents = documents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
