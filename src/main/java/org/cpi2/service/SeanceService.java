package org.cpi2.service;

import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.Moniteur;
import org.cpi2.entitties.Seance;
import org.cpi2.entitties.Vehicule;
import org.cpi2.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SeanceService {
    private static final Logger LOGGER = Logger.getLogger(SeanceService.class.getName());
    private final MoniteurService moniteurService = new MoniteurService();
    private final CandidatService candidatService = new CandidatService();
    private final VehiculeService vehiculeService = new VehiculeService();

    /**
     * Adds a new seance to the database
     * @param seance The seance to add
     * @return true if successful, false otherwise
     */
    public boolean addSeance(Seance seance) {
        String sql = "INSERT INTO seance (date, duree, type, moniteur_id, candidat_id, vehicule_id, statut) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(seance.getDate()));
            pstmt.setInt(2, seance.getDuree());
            pstmt.setString(3, seance.getType());
            pstmt.setLong(4, seance.getMoniteur().getId());
            pstmt.setLong(5, seance.getCandidat().getId());
            
            // Vehicle can be null for non-driving sessions
            if (seance.getVehicule() != null) {
                pstmt.setLong(6, seance.getVehicule().getId());
            } else {
                pstmt.setNull(6, Types.BIGINT);
            }
            
            pstmt.setString(7, seance.getStatut());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Creating seance failed, no rows affected.");
                return false;
            }

            // Get the generated ID
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    seance.setId(generatedKeys.getLong(1));
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "Creating seance failed, no ID obtained.");
                    return false;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding seance", e);
            return false;
        }
    }

    /**
     * Updates an existing seance in the database
     * @param seance The seance to update
     * @return true if successful, false otherwise
     */
    public boolean updateSeance(Seance seance) {
        String sql = "UPDATE seance SET date = ?, duree = ?, type = ?, moniteur_id = ?, candidat_id = ?, vehicule_id = ?, statut = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(seance.getDate()));
            pstmt.setInt(2, seance.getDuree());
            pstmt.setString(3, seance.getType());
            pstmt.setLong(4, seance.getMoniteur().getId());
            pstmt.setLong(5, seance.getCandidat().getId());
            
            // Vehicle can be null for non-driving sessions
            if (seance.getVehicule() != null) {
                pstmt.setLong(6, seance.getVehicule().getId());
            } else {
                pstmt.setNull(6, Types.BIGINT);
            }
            
            pstmt.setString(7, seance.getStatut());
            pstmt.setLong(8, seance.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating seance", e);
            return false;
        }
    }

    /**
     * Removes a seance from the database
     * @param id The ID of the seance to remove
     * @return true if successful, false otherwise
     */
    public boolean removeSeance(Long id) {
        String sql = "DELETE FROM seance WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing seance", e);
            return false;
        }
    }

    /**
     * Gets a seance by its ID
     * @param id The ID of the seance to get
     * @return The seance if found, null otherwise
     */
    public Seance getSeanceById(Long id) {
        String sql = "SELECT * FROM seance WHERE id = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return extractSeanceFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting seance by ID", e);
        }
        
        return null;
    }

    /**
     * Gets all seances from the database
     * @return List of all seances
     */
    public List<Seance> getAllSeances() {
        String sql = "SELECT * FROM seance ORDER BY date";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                seances.add(extractSeanceFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all seances", e);
        }
        return seances;
    }

    /**
     * Gets all seances for a specific candidat
     * @param candidatId The ID of the candidat
     * @return List of seances for the candidat
     */
    public List<Seance> getSeancesByCandidat(Long candidatId) {
        String sql = "SELECT * FROM seance WHERE candidat_id = ? ORDER BY date";
        List<Seance> seances = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, candidatId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(extractSeanceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting seances by candidat", e);
        }
        
        return seances;
    }

    /**
     * Gets all seances for a specific moniteur
     * @param moniteurId The ID of the moniteur
     * @return List of seances for the moniteur
     */
    public List<Seance> getSeancesByMoniteur(Long moniteurId) {
        String sql = "SELECT * FROM seance WHERE moniteur_id = ? ORDER BY date";
        List<Seance> seances = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, moniteurId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(extractSeanceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting seances by moniteur", e);
        }
        
        return seances;
    }

    /**
     * Gets all seances for a specific vehicule
     * @param vehiculeId The ID of the vehicule
     * @return List of seances for the vehicule
     */
    public List<Seance> getSeancesByVehicule(Long vehiculeId) {
        String sql = "SELECT * FROM seance WHERE vehicule_id = ? ORDER BY date";
        List<Seance> seances = new ArrayList<>();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, vehiculeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(extractSeanceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting seances by vehicule", e);
        }
        
        return seances;
    }

    /**
     * Gets all seances for a specific date
     * @param date The date to search for
     * @return List of seances for the date
     */
    public List<Seance> getSeancesByDate(LocalDateTime date) {
        String sql = "SELECT * FROM seance WHERE DATE(date) = DATE(?) ORDER BY date";
        List<Seance> seances = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setTimestamp(1, Timestamp.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    seances.add(extractSeanceFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting seances by date", e);
        }
        return seances;
    }

    /**
     * Helper method to extract a seance from a result set
     * @param rs The result set
     * @return The extracted seance
     * @throws SQLException If an error occurs
     */
    private Seance extractSeanceFromResultSet(ResultSet rs) throws SQLException {
        Seance seance = new Seance();
        seance.setId(rs.getLong("id"));
        seance.setDate(rs.getTimestamp("date").toLocalDateTime());
        seance.setDuree(rs.getInt("duree"));
        seance.setType(rs.getString("type"));
        seance.setStatut(rs.getString("statut"));

        // Get related entities
        Long moniteurId = rs.getLong("moniteur_id");
        Moniteur moniteur = moniteurService.getMoniteurById(moniteurId);
        seance.setMoniteur(moniteur);

        Long candidatId = rs.getLong("candidat_id");
        Candidat candidat = candidatService.getCandidatById(candidatId);
        seance.setCandidat(candidat);

        Long vehiculeId = rs.getLong("vehicule_id");
        if (!rs.wasNull()) {
            Vehicule vehicule = vehiculeService.getVehiculeById(vehiculeId);
            seance.setVehicule(vehicule);
        }

        return seance;
    }
}