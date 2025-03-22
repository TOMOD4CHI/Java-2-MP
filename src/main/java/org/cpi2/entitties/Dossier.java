package org.cpi2.entitties;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class Dossier {
    private Long id;
    private Candidat candidat;
    private Map<String, TreeSet<Document>> documents;

    public Dossier() {
        this.documents = new HashMap<>();
    }

    public Map<String, TreeSet<Document>> getDocuments() {
        return documents;
    }

    public void setDocuments(Map<String, TreeSet<Document>> documents) {
        this.documents = documents;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public Candidat getCandidat() {
        return candidat;
    }
    
    public void setCandidat(Candidat candidat) {
        this.candidat = candidat;
    }
}
