package entitties;

import java.time.LocalDateTime;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomFichier() {
        return nomFichier;
    }

    public void setNomFichier(String nomFichier) {
        this.nomFichier = nomFichier;
    }

    public String getTypeDocument() {
        return typeDocument;
    }

    public void setTypeDocument(String typeDocument) {
        this.typeDocument = typeDocument;
    }
    @Override
    public int compareTo(Document o) {
        return this.dateUpload.compareTo(o.dateUpload);
    }
}
