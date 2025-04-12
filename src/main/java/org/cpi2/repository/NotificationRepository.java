package org.cpi2.repository;

import org.cpi2.entities.Notification;
import org.cpi2.repository.DatabaseConfig;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private Connection connection;

    public NotificationRepository() {
        try {
            this.connection = DatabaseConfig.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> getAllNotifications() {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT v.id, v.immatriculation, v.marque, v.modele, " +
                       "v.date_expiration_assurance, v.date_prochaine_visite_technique, v.date_prochain_entretien, " +
                       "v.kilometrage_total, v.kilometrage_prochain_entretien, v.statut " +
                       "FROM vehicule v";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int vehiculeId = rs.getInt("id");
                String immatriculation = rs.getString("immatriculation");
                String marque = rs.getString("marque");
                String modele = rs.getString("modele");
                String vehiculeInfo = marque + " " + modele + " (" + immatriculation + ")";
                int kilometrageTotal = rs.getInt("kilometrage_total");
                int kilometrageProchainEntretien = rs.getInt("kilometrage_prochain_entretien");

                Date assuranceDate = rs.getDate("date_expiration_assurance");
                if (assuranceDate != null) {
                    LocalDate dateExpiration = assuranceDate.toLocalDate();
                    LocalDate today = LocalDate.now();
                    LocalDate prochainMois = today.plusMonths(1);
                    
                    String statut = "ACTIVE";
                    String priorite = "MOYENNE";
                    
                    if (dateExpiration.isBefore(today)) {
                        priorite = "HAUTE";
                    } else if (dateExpiration.isBefore(prochainMois)) {
                        priorite = "MOYENNE";
                    } else {
                        statut = "A_VENIR";
                        priorite = "BASSE";
                    }
                    
                    Notification notification = new Notification(
                            null, "ASSURANCE", 
                            "Assurance à renouveler", 
                            "L'assurance du véhicule " + vehiculeInfo + " expire le " + dateExpiration, 
                            vehiculeId, vehiculeInfo, dateExpiration, LocalDateTime.now(), statut, priorite);
                    notifications.add(notification);
                }

                Date visiteDate = rs.getDate("date_prochaine_visite_technique");
                if (visiteDate != null) {
                    LocalDate dateVisite = visiteDate.toLocalDate();
                    LocalDate today = LocalDate.now();
                    LocalDate prochainMois = today.plusMonths(1);
                    
                    String statut = "ACTIVE";
                    String priorite = "MOYENNE";
                    
                    if (dateVisite.isBefore(today)) {
                        priorite = "HAUTE";
                    } else if (dateVisite.isBefore(prochainMois)) {
                        priorite = "MOYENNE";
                    } else {
                        statut = "A_VENIR";
                        priorite = "BASSE";
                    }
                    
                    Notification notification = new Notification(
                            null, "VIGNETTE", 
                            "Visite technique à planifier", 
                            "La visite technique du véhicule " + vehiculeInfo + " est prévue pour le " + dateVisite, 
                            vehiculeId, vehiculeInfo, dateVisite, LocalDateTime.now(), statut, priorite);
                    notifications.add(notification);
                }

                Date entretienDate = rs.getDate("date_prochain_entretien");
                if (entretienDate != null) {
                    LocalDate dateEntretien = entretienDate.toLocalDate();
                    LocalDate today = LocalDate.now();
                    LocalDate prochainMois = today.plusMonths(1);
                    
                    String statut = "ACTIVE";
                    String priorite = "MOYENNE";
                    
                    if (dateEntretien.isBefore(today)) {
                        priorite = "HAUTE";
                    } else if (dateEntretien.isBefore(prochainMois)) {
                        priorite = "MOYENNE";
                    } else {
                        statut = "A_VENIR";
                        priorite = "BASSE";
                    }
                    
                    Notification notification = new Notification(
                            null, "ENTRETIEN", 
                            "Entretien programmé", 
                            "L'entretien du véhicule " + vehiculeInfo + " est programmé pour le " + dateEntretien, 
                            vehiculeId, vehiculeInfo, dateEntretien, LocalDateTime.now(), statut, priorite);
                    notifications.add(notification);
                }

                if (kilometrageTotal >= kilometrageProchainEntretien && kilometrageProchainEntretien > 0) {
                    LocalDate today = LocalDate.now();
                    
                    Notification notification = new Notification(
                            null, "ENTRETIEN", 
                            "Entretien par kilométrage", 
                            "Le véhicule " + vehiculeInfo + " a atteint " + kilometrageTotal + 
                            " km et nécessite un entretien (prévu à " + kilometrageProchainEntretien + " km)", 
                            vehiculeId, vehiculeInfo, today, LocalDateTime.now(), "ACTIVE", "HAUTE");
                    notifications.add(notification);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return notifications;
    }
    
    public List<Notification> getNotificationsByType(String type) {
        List<Notification> notifications = getAllNotifications();
        List<Notification> filteredNotifications = new ArrayList<>();
        
        for (Notification notification : notifications) {
            if (notification.getType().equals(type)) {
                filteredNotifications.add(notification);
            }
        }
        
        return filteredNotifications;
    }
    
    public List<Notification> getNotificationsByStatut(String statut) {
        List<Notification> notifications = getAllNotifications();
        List<Notification> filteredNotifications = new ArrayList<>();
        
        for (Notification notification : notifications) {
            if (notification.getStatut().equals(statut)) {
                filteredNotifications.add(notification);
            }
        }
        
        return filteredNotifications;
    }
    
    public void marquerCommeTraite(int vehiculeId, String type) {
        String query = "";
        
        switch (type) {
            case "ASSURANCE":
                query = "UPDATE vehicule SET date_expiration_assurance = ? WHERE id = ?";
                break;
            case "VIGNETTE":
                query = "UPDATE vehicule SET date_prochaine_visite_technique = ? WHERE id = ?";
                break;
            case "ENTRETIEN":
                query = "UPDATE vehicule SET date_prochain_entretien = ?, kilometrage_prochain_entretien = ? WHERE id = ?";
                break;
        }
        
        if (!query.isEmpty()) {
            try (PreparedStatement pstmt = connection.prepareStatement(query)) {
                LocalDate nouvelleDate = LocalDate.now().plusMonths(12);

                if (type.equals("ENTRETIEN")) {
                    String getKmQuery = "SELECT kilometrage_total FROM vehicule WHERE id = ?";
                    try (PreparedStatement kmStmt = connection.prepareStatement(getKmQuery)) {
                        kmStmt.setInt(1, vehiculeId);
                        ResultSet rs = kmStmt.executeQuery();
                        if (rs.next()) {
                            int kmActuel = rs.getInt("kilometrage_total");
                            pstmt.setDate(1, Date.valueOf(nouvelleDate));
                            pstmt.setInt(2, kmActuel + 10000);
                            pstmt.setInt(3, vehiculeId);
                        }
                    }
                } else {
                    pstmt.setDate(1, Date.valueOf(nouvelleDate));
                    pstmt.setInt(2, vehiculeId);
                }
                
                pstmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
