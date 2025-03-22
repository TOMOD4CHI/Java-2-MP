package org.cpi2.entitties;

import java.time.LocalDateTime;
import java.util.Objects;

public class Document implements Comparable<Document> {
    private Long id;
    private String typeDocument;
    private String nomFichier;
    private String cheminFichier;
    private LocalDateTime dateUpload;
    
    public Document() {
        this.dateUpload = LocalDateTime.now();
    }
    
    public Document(String typeDocument, String nomFichier, String cheminFichier) {
        this();
        this.typeDocument = typeDocument;
        this.nomFichier = nomFichier;
        this.cheminFichier = cheminFichier;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTypeDocument() {
        return typeDocument;
    }
    
    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }
    
    public String getNomFichier() {
        return nomFichier;
    }
    
    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }
    
    public String getCheminFichier() {
        return cheminFichier;
    }
    
    public void setCheminFichier(String cheminFichier) {
        this.cheminFichier = cheminFichier;
    }
    
    public LocalDateTime getDateUpload() {
        return dateUpload;
    }
    
    public void setDateUpload(LocalDateTime dateUpload) {
        this.dateUpload = dateUpload;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return typeDocument + ": " + nomFichier;
    }
    
    @Override
    public int compareTo(Document other) {
        if (this.dateUpload == null && other.dateUpload == null) {
            return 0;
        }
        if (this.dateUpload == null) {
            return -1;
        }
        if (other.dateUpload == null) {
            return 1;
        }
        return this.dateUpload.compareTo(other.dateUpload);
    }
}
