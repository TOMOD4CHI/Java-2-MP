package org.cpi2.entitties;

/**
 * Enum representing different types of driving licenses
 */
public enum TypePermis {
    AM("Cyclomoteur"),
    A1("Moto légère"),
    A2("Moto intermédiaire"),
    A("Moto"),
    B("Voiture"),
    BE("Voiture avec remorque"),
    C("Poids lourd"),
    CE("Poids lourd avec remorque"),
    D("Autobus"),
    DE("Autobus avec remorque");
    
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