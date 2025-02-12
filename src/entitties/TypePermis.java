package entitties;

public enum TypePermis {
    A("A", "Permis Moto", "Permis pour conduire une moto"),
    B("B", "Permis Voiture", "Permis pour conduire une voiture"),
    C("C", "Permis Camion", "Permis pour conduire un camion");

    private final String code;
    private final String libelle;
    private String description;
    private Long id;

    TypePermis(String code, String libelle, String description) {
        this.code = code;
        this.libelle = libelle;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getLibelle() {
        return libelle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}