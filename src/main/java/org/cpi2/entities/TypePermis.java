package org.cpi2.entities;

/**
 * Enum representing different types of driving licenses
 */
public enum TypePermis {
    A("Permis_Moto"),
    B("Permis_Voiture"),
    C("Permis_Camion");

    
    private final String description;
    
    TypePermis(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }

    public String getLibelle() {
        return this.name();
    }
}