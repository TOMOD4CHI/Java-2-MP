package entitties;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Moniteur extends Personne {
    private LocalDate dateEmbauche;
    private Set<TypePermis> specialites;
    private Map<LocalDateTime, RendezVous> emploiDuTemps;

    public Moniteur() {
        super();
        this.specialites = new HashSet<>();
        this.emploiDuTemps = new TreeMap<>();
    }

    public Moniteur(String nom, String prenom, String cin, String adresse, String telephone, LocalDate dateEmbauche) {
        super(nom, prenom, cin, adresse, telephone);
        this.dateEmbauche = dateEmbauche;
        this.specialites = new HashSet<>();
        this.emploiDuTemps = new TreeMap<>();
    }

    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    public Map<LocalDateTime, RendezVous> getEmploiDuTemps() {
        return emploiDuTemps;
    }

    public void setEmploiDuTemps(Map<LocalDateTime, RendezVous> emploiDuTemps) {
        this.emploiDuTemps = emploiDuTemps;
    }

    public Set<TypePermis> getSpecialites() {
        return specialites;
    }

    public void setSpecialites(Set<TypePermis> specialites) {
        this.specialites = specialites;
    }
}
