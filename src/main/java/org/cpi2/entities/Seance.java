package org.cpi2.entities;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Seance {
    private Long id;
    private String type;
    private Long candidatId;
    private String candidatName;
    private Long moniteurId;
    private String moniteurName;
    private Long vehiculeId;
    private String vehiculeName;
    private String date;
    private String temps;
    private Double kilometrage;
    private String status;
    private String commentaire;
    private Double latitude;
    private Double longitude;
    private String salle;
    private String quartier;

    public Seance() {
    }

    public Seance(Long id, String type, Long candidatId, String candidatName, Long moniteurId, String moniteurName,
                 Long vehiculeId, String vehiculeName, String date, String temps, Double kilometrage,
                 String status, String commentaire, Double latitude, Double longitude) {
        this.id = id;
        this.type = type;
        this.candidatId = candidatId;
        this.candidatName = candidatName;
        this.moniteurId = moniteurId;
        this.moniteurName = moniteurName;
        this.vehiculeId = vehiculeId;
        this.vehiculeName = vehiculeName;
        this.date = date;
        this.temps = temps;
        this.kilometrage = kilometrage;
        this.status = status;
        this.commentaire = commentaire;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCandidatId() {
        return candidatId;
    }

    public void setCandidatId(Long candidatId) {
        this.candidatId = candidatId;
    }

    public String getCandidatName() {
        return candidatName;
    }

    public void setCandidatName(String candidatName) {
        this.candidatName = candidatName;
    }

    public Long getMoniteurId() {
        return moniteurId;
    }

    public void setMoniteurId(Long moniteurId) {
        this.moniteurId = moniteurId;
    }

    public String getMoniteurName() {
        return moniteurName;
    }

    public void setMoniteurName(String moniteurName) {
        this.moniteurName = moniteurName;
    }

    public Long getVehiculeId() {
        return vehiculeId;
    }

    public void setVehiculeId(Long vehiculeId) {
        this.vehiculeId = vehiculeId;
    }

    public String getVehiculeName() {
        return vehiculeName;
    }

    public void setVehiculeName(String vehiculeName) {
        this.vehiculeName = vehiculeName;
    }

    public String getDate() {
        return date;
    }

    /**
     * Get the date as a LocalDate object
     * @return LocalDate object
     */
    public LocalDate getLocalDate() {
        if (date == null || date.isEmpty()) {
            System.out.println("Warning: Empty date for session ID " + id + ", using current date");
            return LocalDate.now();
        }
        
        System.out.println("DEBUG - Trying to parse date: '" + date + "' for seance ID: " + id);
        
        try {
            // First try yyyy-MM-dd (ISO format)
            if (date.matches("\\d{4}-\\d{2}-\\d{2}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                return LocalDate.parse(date, formatter);
            }
            // Then try dd/MM/yyyy (European format)
            else if (date.matches("\\d{2}/\\d{2}/\\d{4}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(date, formatter);
            }
            // Try yyyy/MM/dd
            else if (date.matches("\\d{4}/\\d{2}/\\d{2}")) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                return LocalDate.parse(date, formatter);
            }
            else {
                System.out.println("DEBUG - Date format not recognized: '" + date + "', trying flexible parsing");
                // Try multiple formats in sequence
                String[] patterns = {
                    "yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "MM/dd/yyyy", "dd-MM-yyyy"
                };
                
                for (String pattern : patterns) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
                        return LocalDate.parse(date, formatter);
                    } catch (Exception e) {
                        // Continue to next pattern
                    }
                }
                
                // If all formats fail, log error and return current date
                System.out.println("ERROR: Could not parse date '" + date + "' using any known format");
                return LocalDate.now();
            }
        } catch (Exception e) {
            System.out.println("ERROR parsing date '" + date + "': " + e.getMessage());
            return LocalDate.now();
        }
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getHeure() {
        return temps;
    }

    public void setTemps(String temps) {
        this.temps = temps;
    }

    public Double getKilometrage() {
        return kilometrage;
    }

    public void setKilometrage(Double kilometrage) {
        this.kilometrage = kilometrage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCommentaire() {
        return commentaire;
    }

    public void setCommentaire(String commentaire) {
        this.commentaire = commentaire;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getSalle() {
        return salle;
    }

    public void setSalle(String salle) {
        this.salle = salle;
    }

    public String getQuartier() {
        return quartier;
    }

    public void setQuartier(String quartier) {
        this.quartier = quartier;
    }

    /**
     * Get a list of candidates associated with this session
     * This is a simplified version for the welcome page that just creates a list with this session's candidate
     * @return List of Candidat objects
     */
    public List<Candidat> getCandidats() {
        if (candidatId == null) {
            return new ArrayList<>();
        }
        
        // Create a dummy candidate with the ID and name
        Candidat candidat = new Candidat();
        candidat.setId(candidatId);
        candidat.setNom(candidatName);
        
        return Collections.singletonList(candidat);
    }

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", candidatId=" + candidatId +
                ", candidatName='" + candidatName + '\'' +
                ", moniteurId=" + moniteurId +
                ", moniteurName='" + moniteurName + '\'' +
                ", vehiculeId=" + vehiculeId +
                ", vehiculeName='" + vehiculeName + '\'' +
                ", date='" + date + '\'' +
                ", temps='" + temps + '\'' +
                ", kilometrage=" + kilometrage +
                ", status='" + status + '\'' +
                ", commentaire='" + commentaire + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", salle='" + salle + '\'' +
                ", quartier='" + quartier + '\'' +
                '}';
    }
}

