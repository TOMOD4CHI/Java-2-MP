package org.cpi2.entities;

public class Salle {
    private Long id;
    private String nom;
    private String numero;
    private int capacite;
    private String notes;

    public Salle() {
    }

    public Salle(String nom, String numero, int capacite, String notes) {
        this.nom = nom;
        this.numero = numero;
        this.capacite = capacite;
        this.notes = notes;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public int getCapacite() {
        return capacite;
    }

    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Override
    public String toString() {
        return "Salle{" +
                "id=" + id +
                ", nom='" + nom + '\'' +
                ", numero='" + numero + '\'' +
                ", capacite=" + capacite +
                ", notes='" + notes + '\'' +
                '}';
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
}
