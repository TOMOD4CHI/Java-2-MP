package org.cpi2.entities;

public enum TypePermis {
    VOITURE("Voiture"),
    MOTO("Moto"),
    CAMION("Camion");
    
    private final String value;
    
    TypePermis(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
}
