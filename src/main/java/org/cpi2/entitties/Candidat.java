package org.cpi2.entitties;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Candidat {
    private Long id;
    private String nom;
    private String prenom;
    private LocalDate dateNaissance;
    private String email;
    private String telephone;
    private String adresse;
    private TypePermis typePermis;
    private List<Document> documents;
    
    public Candidat() {
        this.documents = new ArrayList<>();
    }
    
    public Candidat(Long id, String nom, String prenom, LocalDate dateNaissance, String email, 
                  String telephone, String adresse, TypePermis typePermis) {
        this();
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.dateNaissance = dateNaissance;
        this.email = email;
        this.telephone = telephone;
        this.adresse = adresse;
        this.typePermis = typePermis;
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNom() {
        return nom;
    }
    
    public void setNom(String nom) {
        this.nom = nom;
    }
    
    public String getPrenom() {
        return prenom;
    }
    
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }
    
    public LocalDate getDateNaissance() {
        return dateNaissance;
    }
    
    public void setDateNaissance(LocalDate dateNaissance) {
        this.dateNaissance = dateNaissance;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
    
    public String getAdresse() {
        return adresse;
    }
    
    public void setAdresse(String adresse) {
        this.adresse = adresse;
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
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Candidat candidat = (Candidat) o;
        return Objects.equals(id, candidat.id) &&
               Objects.equals(nom, candidat.nom) &&
               Objects.equals(prenom, candidat.prenom);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom);
    }
    
    @Override
    public String toString() {
        return nom + " " + prenom;
    }
}
