package org.cpi2.entitties;

import java.time.LocalDate;

public class Seance {
    private Long id;
    private String type; // "Conduite" or "Code"
    private Long candidatId;
    private String candidatName;
    private Long moniteurId;
    private String moniteurName;
    private Long vehiculeId;
    private String vehiculeName;
    private String date;
    private String temps;
    private Double kilometrage;
    private String status; // "Présent", "Absent", "Retard", "Excusé"
    private String commentaire;
    private Double latitude;
    private Double longitude;

    public Seance() {
    }

    public Seance(Long id, String type, Long candidatId, Long moniteurId, Long vehiculeId, 
                  String date, String temps, Double kilometrage) {
        this.id = id;
        this.type = type;
        this.candidatId = candidatId;
        this.moniteurId = moniteurId;
        this.vehiculeId = vehiculeId;
        this.date = date;
        this.temps = temps;
        this.kilometrage = kilometrage;
    }

    // Full constructor with all fields
    public Seance(Long id, String type, Long candidatId, String candidatName, Long moniteurId, 
                  String moniteurName, Long vehiculeId, String vehiculeName, String date, 
                  String temps, Double kilometrage, String status, String commentaire, 
                  Double latitude, Double longitude) {
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

    // Getters and setters
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

    public void setDate(String date) {
        this.date = date;
    }

    public String getTemps() {
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

    @Override
    public String toString() {
        return "Seance{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", candidatId=" + candidatId +
                ", moniteurId=" + moniteurId +
                ", date='" + date + '\'' +
                ", temps='" + temps + '\'' +
                '}';
    }

    public CharSequence getStatut() {
        return null;
    }
}