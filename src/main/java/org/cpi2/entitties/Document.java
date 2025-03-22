package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.Objects;

public class Document {
    private int id;
    private String type;
    private String nom;
    private String path;
    private LocalDate dateUpload;
    private int candidatId;
    
    public Document() {
        this.dateUpload = LocalDate.now();
    }
    
    public Document(int id, String type, String nom, String path, int candidatId) {
        this();
        this.id = id;
        this.type = type;
        this.nom = nom;
        this.path = path;
        this.candidatId = candidatId;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public LocalDate getDateUpload() {
        return dateUpload;
    }
    
    public void setDateUpload(LocalDate dateUpload) {
        this.dateUpload = dateUpload;
    }
    
    public int getCandidatId() {
        return candidatId;
    }
    
    public void setCandidatId(int candidatId) {
        this.candidatId = candidatId;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return id == document.id;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return type + ": " + nom;
    }
}
