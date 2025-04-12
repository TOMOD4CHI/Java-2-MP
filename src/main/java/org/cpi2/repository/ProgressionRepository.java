package org.cpi2.repository;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.Inscription;
import org.cpi2.entities.TypePermis;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ProgressionRepository {
    private static final Logger LOGGER = Logger.getLogger(ProgressionRepository.class.getName());

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public Optional<Map<String, Object>> getCandidatInscription(long candidatId) {
        String sql = "SELECT i.date_inscription, i.statut, p.heures_code, p.heures_conduite, p.type_permis_id " +
                    "FROM inscription i " +
                    "JOIN candidat c ON i.cin = c.cin " +
                    "JOIN plan p ON i.plan_id = p.id " +
                    "WHERE c.id = ? " +
                    "ORDER BY i.date_inscription DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Map<String, Object> inscriptionData = new HashMap<>();
                inscriptionData.put("dateInscription", rs.getDate("date_inscription"));
                inscriptionData.put("statut", rs.getString("statut"));
                inscriptionData.put("heuresCode", rs.getInt("heures_code"));
                inscriptionData.put("heuresConduite", rs.getInt("heures_conduite"));
                inscriptionData.put("typePermisId", rs.getInt("type_permis_id"));
                return Optional.of(inscriptionData);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting candidat inscription", e);
        }

        return Optional.empty();
    }

    public int getCompletedCodeSessions(long candidatId) {
        String sql = "SELECT COUNT(*) FROM presence_code pc " +
                    "JOIN session_code sc ON pc.session_code_id = sc.id " +
                    "WHERE pc.candidat_id = ? AND pc.present = 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting completed code sessions", e);
        }

        return 0;
    }

    public int getCompletedDrivingSessions(long candidatId) {
        String sql = "SELECT COUNT(*) FROM presence_conduite pc " +
                    "JOIN session_conduite sc ON pc.session_conduite_id = sc.id " +
                    "WHERE pc.candidat_id = ? AND pc.present = 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting completed driving sessions", e);
        }

        return 0;
    }

    public Map<String, Integer> getMonthlyCodeSessions(long candidatId, int monthsCount) {
        Map<String, Integer> monthlyData = new HashMap<>();
        
        String sql = "SELECT MONTH(sc.date) as month, COUNT(*) as count " +
                    "FROM presence_code pc " +
                    "JOIN session_code sc ON pc.session_code_id = sc.id " +
                    "WHERE pc.candidat_id = ? AND pc.present = 1 " +
                    "AND sc.date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                    "GROUP BY MONTH(sc.date) " +
                    "ORDER BY MONTH(sc.date)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            stmt.setInt(2, monthsCount);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int month = rs.getInt("month");
                int count = rs.getInt("count");
                String monthName = getMonthName(month);
                monthlyData.put(monthName, count);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting monthly code sessions", e);
        }

        return monthlyData;
    }

    public Map<String, Integer> getMonthlyDrivingSessions(long candidatId, int monthsCount) {
        Map<String, Integer> monthlyData = new HashMap<>();
        
        String sql = "SELECT MONTH(sc.date) as month, COUNT(*) as count " +
                    "FROM presence_conduite pc " +
                    "JOIN session_conduite sc ON pc.session_conduite_id = sc.id " +
                    "WHERE pc.candidat_id = ? AND pc.present = 1 " +
                    "AND sc.date >= DATE_SUB(CURDATE(), INTERVAL ? MONTH) " +
                    "GROUP BY MONTH(sc.date) " +
                    "ORDER BY MONTH(sc.date)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            stmt.setInt(2, monthsCount);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int month = rs.getInt("month");
                int count = rs.getInt("count");
                String monthName = getMonthName(month);
                monthlyData.put(monthName, count);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting monthly driving sessions", e);
        }

        return monthlyData;
    }

    private String getMonthName(int month) {
        String[] monthNames = {"Janvier", "Février", "Mars", "Avril", "Mai", "Juin", 
                               "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"};
        return monthNames[month - 1];
    }

    public Map<String, Object> getExamStatistics(long candidatId) {
        Map<String, Object> examStats = new HashMap<>();
        examStats.put("codeExamPassed", false);
        examStats.put("driveExamPassed", false);
        examStats.put("codeAttempts", 0);
        examStats.put("driveAttempts", 0);
        
        String sql = "SELECT type_examen_id, resultat, COUNT(*) as attempts " +
                    "FROM examen " +
                    "WHERE candidat_id = ? " +
                    "GROUP BY type_examen_id, resultat";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            int codeAttempts = 0;
            int driveAttempts = 0;
            boolean codeExamPassed = false;
            boolean driveExamPassed = false;

            while (rs.next()) {
                int examType = rs.getInt("type_examen_id");
                String result = rs.getString("resultat");
                int attempts = rs.getInt("attempts");
                
                if (examType == 1) {
                    codeAttempts += attempts;
                    if ("REUSSI".equals(result)) {
                        codeExamPassed = true;
                    }
                } else if (examType == 2) {
                    driveAttempts += attempts;
                    if ("REUSSI".equals(result)) {
                        driveExamPassed = true;
                    }
                }
            }
            
            examStats.put("codeExamPassed", codeExamPassed);
            examStats.put("driveExamPassed", driveExamPassed);
            examStats.put("codeAttempts", codeAttempts);
            examStats.put("driveAttempts", driveAttempts);
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting exam statistics", e);
        }

        return examStats;
    }

    public Map<String, Integer> getSessionsByPeriod(long candidatId, String period) {
        Map<String, Integer> sessionsData = new HashMap<>();
        
        String timeInterval;
        String groupBy;
        
        switch (period.toLowerCase()) {
            case "semaine":
                timeInterval = "WEEK";
                groupBy = "DAYOFWEEK(sc.date)";
                break;
            case "mois":
                timeInterval = "MONTH";
                groupBy = "DAY(sc.date)";
                break;
            case "année":
                timeInterval = "YEAR";
                groupBy = "MONTH(sc.date)";
                break;
            default:
                timeInterval = "MONTH";
                groupBy = "DAY(sc.date)";
        }

        String sqlCode = "SELECT " + groupBy + " as time_unit, COUNT(*) as count " +
                        "FROM presence_code pc " +
                        "JOIN session_code sc ON pc.session_code_id = sc.id " +
                        "WHERE pc.candidat_id = ? AND pc.present = 1 " +
                        "AND sc.date >= DATE_SUB(CURDATE(), INTERVAL 1 " + timeInterval + ") " +
                        "GROUP BY " + groupBy;

        String sqlDriving = "SELECT " + groupBy + " as time_unit, COUNT(*) as count " +
                           "FROM presence_conduite pc " +
                           "JOIN session_conduite sc ON pc.session_conduite_id = sc.id " +
                           "WHERE pc.candidat_id = ? AND pc.present = 1 " +
                           "AND sc.date >= DATE_SUB(CURDATE(), INTERVAL 1 " + timeInterval + ") " +
                           "GROUP BY " + groupBy;
        
        try (Connection conn = getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(sqlCode)) {
                stmt.setLong(1, candidatId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int timeUnit = rs.getInt("time_unit");
                    int count = rs.getInt("count");
                    String timeLabel = getTimeLabel(timeUnit, period);
                    sessionsData.put("Code-" + timeLabel, count);
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sqlDriving)) {
                stmt.setLong(1, candidatId);
                ResultSet rs = stmt.executeQuery();
                
                while (rs.next()) {
                    int timeUnit = rs.getInt("time_unit");
                    int count = rs.getInt("count");
                    String timeLabel = getTimeLabel(timeUnit, period);
                    sessionsData.put("Conduite-" + timeLabel, count);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting sessions by period", e);
        }
        
        return sessionsData;
    }
    
    private String getTimeLabel(int timeUnit, String period) {
        switch (period.toLowerCase()) {
            case "semaine":
                String[] days = {"Dim", "Lun", "Mar", "Mer", "Jeu", "Ven", "Sam"};
                return days[timeUnit % 7];
            case "mois":
                return String.valueOf(timeUnit);
            case "année":
                String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin", 
                                  "Juil", "Août", "Sep", "Oct", "Nov", "Déc"};
                return months[timeUnit - 1];
            default:
                return String.valueOf(timeUnit);
        }
    }
}
