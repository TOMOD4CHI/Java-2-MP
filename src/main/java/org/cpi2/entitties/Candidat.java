package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Candidat extends Personne {
    private TypePermis typePermis;
    private List<Document> documents;
    private Dossier dossier;
    
    public Candidat() {
        super();
        this.documents = new ArrayList<>();
    }
    
    public Candidat(String nom, String prenom, String cin, String adresse, String telephone, 
                   String email, LocalDate dateNaissance, TypePermis typePermis) {
        super(nom, prenom, cin, adresse, telephone);
        this.email = email;
        this.dateNaissance = dateNaissance;
        this.typePermis = typePermis;
        this.documents = new ArrayList<>();
    }
    
    public TypePermis getTypePermis() {
        return typePermis;
    }
    
    public void setTypePermis(TypePermis typePermis) {
        this.typePermis = typePermis;
    }
    
    public List<Document> getDocuments() {
        return documents;
    }
    
    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }
    
    public void addDocument(Document document) {
        this.documents.add(document);
    }
    
    public boolean removeDocument(Document document) {
        return this.documents.remove(document);
    }
    
    public Dossier getDossier() {
        return dossier;
    }
    
    public void setDossier(Dossier dossier) {
        this.dossier = dossier;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidat candidat = (Candidat) o;
        return Objects.equals(id, candidat.id) &&
               Objects.equals(cin, candidat.cin);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, cin);
    }
    
    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
