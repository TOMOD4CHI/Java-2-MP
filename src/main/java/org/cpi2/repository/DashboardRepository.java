package org.cpi2.repository;

import org.cpi2.entities.AutoEcole;
import org.cpi2.entities.Seance;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardRepository {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/autoecole";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public int getCandidatesCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM candidat")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des candidats: " + e.getMessage());
        }
        return 0;
    }

    public int getSessionsCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM seance")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des séances: " + e.getMessage());
        }
        return 0;
    }

    public int getMoniteursCount() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM moniteur")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des moniteurs: " + e.getMessage());
        }
        return 0;
    }

    public List<Seance> getUpcomingSessions(int limit) {
        List<Seance> sessions = new ArrayList<>();
        String query = "SELECT s.*, " +
                "c.nom AS candidat_nom, c.prenom AS candidat_prenom, " +
                "m.nom AS moniteur_nom, m.prenom AS moniteur_prenom, " +
                "v.marque AS vehicule_marque, v.modele AS vehicule_modele " +
                "FROM seance s " +
                "LEFT JOIN candidat c ON s.candidat_id = c.id " +
                "LEFT JOIN moniteur m ON s.moniteur_id = m.id " +
                "LEFT JOIN vehicule v ON s.vehicule_id = v.id " +
                "WHERE s.date >= CURRENT_DATE() " +
                "ORDER BY s.date, s.heure " +
                "LIMIT " + limit;

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setType(rs.getString("type"));
                seance.setCandidatId(rs.getLong("candidat_id"));
                seance.setCandidatName(rs.getString("candidat_nom") + " " + rs.getString("candidat_prenom"));
                seance.setMoniteurId(rs.getLong("moniteur_id"));
                seance.setMoniteurName(rs.getString("moniteur_nom") + " " + rs.getString("moniteur_prenom"));

                Object vehiculeIdObj = rs.getObject("vehicule_id");
                if (vehiculeIdObj != null) {
                    seance.setVehiculeId(rs.getLong("vehicule_id"));
                    String marque = rs.getString("vehicule_marque");
                    String modele = rs.getString("vehicule_modele");
                    if (marque != null && modele != null) {
                        seance.setVehiculeName(marque + " " + modele);
                    }
                }

                seance.setDate(rs.getString("date"));
                seance.setTemps(rs.getString("heure"));
                seance.setStatus(rs.getString("statut"));
                sessions.add(seance);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des séances à venir: " + e.getMessage());
        }
        return sessions;
    }

    public AutoEcole getAutoEcoleInfo() {
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM auto_ecole LIMIT 1")) {
            if (rs.next()) {
                AutoEcole autoEcole = new AutoEcole();
                autoEcole.setNom(rs.getString("nom"));
                autoEcole.setAdresse(rs.getString("adresse"));
                autoEcole.setTelephone(rs.getString("telephone"));
                autoEcole.setEmail(rs.getString("email"));
                return autoEcole;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des informations de l'auto-école: " + e.getMessage());
        }
        return null;
    }

    public List<Seance> getTodaySessions() {
        List<Seance> sessions = new ArrayList<>();
        String query = "SELECT * FROM seance WHERE DATE(date) = CURRENT_DATE()";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setDate(rs.getString("date"));
                sessions.add(seance);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des séances du jour: " + e.getMessage());
        }
        return sessions;
    }

    public List<Seance> getSessionsWithoutMoniteur() {
        List<Seance> sessions = new ArrayList<>();
        String query = "SELECT * FROM seance WHERE moniteur_id IS NULL";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                sessions.add(seance);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des séances sans moniteur: " + e.getMessage());
        }
        return sessions;
    }

    public boolean needsVehicleMaintenance() {
        return false;
    }

    public int getIncompleteCandidatesCount() {
        String query = "SELECT COUNT(*) FROM candidat WHERE email IS NULL OR email = '' OR type_permis IS NULL";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des candidats incomplets: " + e.getMessage());
        }
        return 0;
    }

    public double getMonthlyIncome() {
        String query = "SELECT SUM(montant) FROM paiement WHERE MONTH(date_paiement) = MONTH(CURRENT_DATE()) AND YEAR(date_paiement) = YEAR(CURRENT_DATE())";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des revenus mensuels: " + e.getMessage());
        }
        return 0;
    }

    public double getPreviousMonthIncome() {
        String query = "SELECT SUM(montant) FROM paiement WHERE MONTH(date_paiement) = MONTH(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH)) AND YEAR(date_paiement) = YEAR(DATE_SUB(CURRENT_DATE(), INTERVAL 1 MONTH))";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des revenus du mois précédent: " + e.getMessage());
        }
        return 0;
    }

    public double getExamSuccessRate() {
        String query = "SELECT (COUNT(CASE WHEN resultat = 1 THEN 1 END) * 100.0 / COUNT(*)) " +
                "FROM examen " +
                "WHERE date_examen BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 6 MONTH) AND CURRENT_DATE() " +
                "AND resultat IS NOT NULL";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération du taux de réussite: " + e.getMessage());
        }
        return Double.NaN;
    }

    public int getSuccessfulExamsCount() {
        String query = "SELECT COUNT(*) FROM examen WHERE resultat = 1";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des examens réussis: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalVehicles() {
        String query = "SELECT COUNT(*) FROM vehicule";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des véhicules: " + e.getMessage());
        }
        return 0;
    }

    public int getAvailableVehicles() {
        String query = "SELECT COUNT(*) FROM vehicule WHERE statut = 'Disponible'";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des véhicules disponibles: " + e.getMessage());
        }
        return 0;
    }

    public Map<Integer, Integer> getSessionsPerMonth() {
        Map<Integer, Integer> sessionsByMonth = new HashMap<>();
        String query = "SELECT MONTH(date) AS month, COUNT(*) AS count " +
                "FROM seance " +
                "WHERE date BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 6 MONTH) AND CURRENT_DATE() " +
                "GROUP BY MONTH(date) " +
                "ORDER BY MONTH(date)";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                sessionsByMonth.put(rs.getInt("month"), rs.getInt("count"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des séances par mois: " + e.getMessage());
        }
        return sessionsByMonth;
    }

    public Map<Integer, Integer> getRegistrationsPerMonth() {
        Map<Integer, Integer> registrationsByMonth = new HashMap<>();
        String query = "SELECT MONTH(DATE(created_at)) AS month, COUNT(*) AS count " +
                "FROM candidat " +
                "WHERE DATE(created_at) BETWEEN DATE_SUB(CURRENT_DATE(), INTERVAL 6 MONTH) AND CURRENT_DATE() " +
                "GROUP BY MONTH(DATE(created_at)) " +
                "ORDER BY MONTH(DATE(created_at))";
        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                registrationsByMonth.put(rs.getInt("month"), rs.getInt("count"));
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des inscriptions par mois: " + e.getMessage());
        }
        return registrationsByMonth;
    }
}