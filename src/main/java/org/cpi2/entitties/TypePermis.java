package org.cpi2.entitties;

public enum TypePermis {
    A("Motocyclette"),
    B("Voiture"),
    C("Poids lourd"),
    D("Autobus"),
    E("Remorque");
    
    private final String description;
    
    TypePermis(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    @Override
    public String toString() {
        return name() + " - " + description;
    }
}