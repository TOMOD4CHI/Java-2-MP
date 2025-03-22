package org.cpi2.entitties;

public class TypePermis {
    private int id;
    private String code;
    private String libelle;
    private String description;
    private int ageMinimum;

    public TypePermis() {
        this.ageMinimum = 18;
    }

    public TypePermis(int id, String code, String libelle, String description, int ageMinimum) {
        this.id = id;
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.ageMinimum = ageMinimum;
    }

    public TypePermis(String code, String libelle, String description) {
        this.code = code;
        this.libelle = libelle;
        this.description = description;
        this.ageMinimum = 18;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLibelle() {
        return libelle;
    }

    public void setLibelle(String libelle) {
        this.libelle = libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAgeMinimum() {
        return ageMinimum;
    }

    public void setAgeMinimum(int ageMinimum) {
        this.ageMinimum = ageMinimum;
    }

    @Override
    public String toString() {
        return code + " - " + libelle;
    }
}