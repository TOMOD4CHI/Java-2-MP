package org.cpi2.repository;

import org.cpi2.entitties.Seance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class SeanceRepository extends BaseRepository<Seance> {

    public Optional<Seance> findById(Long id) {
        String sql = "SELECT s.*, " +
                "c.nom AS candidat_nom, c.prenom AS candidat_prenom, " +
                "m.nom AS moniteur_nom, m.prenom AS moniteur_prenom, " +
                "v.marque AS vehicule_marque, v.modele AS vehicule_modele " +
                "FROM seance s " +
                "LEFT JOIN candidat c ON s.candidat_id = c.id " +
                "LEFT JOIN moniteur m ON s.moniteur_id = m.id " +
                "LEFT JOIN vehicule v ON s.vehicule_id = v.id " +
                "WHERE s.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setType(rs.getString("type"));
                seance.setCandidatId(rs.getLong("candidat_id"));
                seance.setCandidatName(rs.getString("candidat_nom") + " " + rs.getString("candidat_prenom"));
                seance.setMoniteurId(rs.getLong("moniteur_id"));
                seance.setMoniteurName(rs.getString("moniteur_nom") + " " + rs.getString("moniteur_prenom"));
                seance.setVehiculeId(rs.getLong("vehicule_id"));
                seance.setVehiculeName(rs.getString("vehicule_marque") + " " + rs.getString("vehicule_modele"));
                seance.setDate(rs.getString("date"));
                seance.setTemps(rs.getString("temps"));
                seance.setKilometrage(rs.getDouble("kilometrage"));
                seance.setStatus(rs.getString("status"));
                seance.setCommentaire(rs.getString("commentaire"));
                seance.setLatitude(rs.getDouble("latitude"));
                seance.setLongitude(rs.getDouble("longitude"));
                
                return Optional.of(seance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding seance by ID", e);
        }
        return Optional.empty();
    }

    public List<Seance> findAll() {
        String sql = "SELECT s.*, " +
                "c.nom AS candidat_nom, c.prenom AS candidat_prenom, " +
                "m.nom AS moniteur_nom, m.prenom AS moniteur_prenom, " +
                "v.marque AS vehicule_marque, v.modele AS vehicule_modele " +
                "FROM seance s " +
                "LEFT JOIN candidat c ON s.candidat_id = c.id " +
                "LEFT JOIN moniteur m ON s.moniteur_id = m.id " +
                "LEFT JOIN vehicule v ON s.vehicule_id = v.id";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setType(rs.getString("type"));
                seance.setCandidatId(rs.getLong("candidat_id"));
                seance.setCandidatName(rs.getString("candidat_nom") + " " + rs.getString("candidat_prenom"));
                seance.setMoniteurId(rs.getLong("moniteur_id"));
                seance.setMoniteurName(rs.getString("moniteur_nom") + " " + rs.getString("moniteur_prenom"));
                seance.setVehiculeId(rs.getLong("vehicule_id"));
                seance.setVehiculeName(rs.getString("vehicule_marque") + " " + rs.getString("vehicule_modele"));
                seance.setDate(rs.getString("date"));
                seance.setTemps(rs.getString("temps"));
                seance.setKilometrage(rs.getDouble("kilometrage"));
                seance.setStatus(rs.getString("status"));
                seance.setCommentaire(rs.getString("commentaire"));
                seance.setLatitude(rs.getDouble("latitude"));
                seance.setLongitude(rs.getDouble("longitude"));
                
                seances.add(seance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all seances", e);
        }
        return seances;
    }

    public boolean save(Seance seance) {
        String sql = "INSERT INTO seance (type, candidat_id, moniteur_id, vehicule_id, date, temps, kilometrage, latitude, longitude) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, seance.getType());
            stmt.setLong(2, seance.getCandidatId());
            stmt.setLong(3, seance.getMoniteurId());
            stmt.setLong(4, seance.getVehiculeId());
            stmt.setString(5, seance.getDate());
            stmt.setString(6, seance.getTemps());
            
            if (seance.getKilometrage() != null) {
                stmt.setDouble(7, seance.getKilometrage());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }
            
            if (seance.getLatitude() != null) {
                stmt.setDouble(8, seance.getLatitude());
            } else {
                stmt.setNull(8, Types.DOUBLE);
            }
            
            if (seance.getLongitude() != null) {
                stmt.setDouble(9, seance.getLongitude());
            } else {
                stmt.setNull(9, Types.DOUBLE);
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        seance.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving seance", e);
        }
        return false;
    }

    public boolean update(Seance seance) {
        String sql = "UPDATE seance SET type = ?, candidat_id = ?, moniteur_id = ?, vehicule_id = ?, " +
                "date = ?, temps = ?, kilometrage = ?, status = ?, commentaire = ?, latitude = ?, longitude = ? " +
                "WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, seance.getType());
            stmt.setLong(2, seance.getCandidatId());
            stmt.setLong(3, seance.getMoniteurId());
            stmt.setLong(4, seance.getVehiculeId());
            stmt.setString(5, seance.getDate());
            stmt.setString(6, seance.getTemps());
            
            if (seance.getKilometrage() != null) {
                stmt.setDouble(7, seance.getKilometrage());
            } else {
                stmt.setNull(7, Types.DOUBLE);
            }
            
            stmt.setString(8, seance.getStatus());
            stmt.setString(9, seance.getCommentaire());
            
            if (seance.getLatitude() != null) {
                stmt.setDouble(10, seance.getLatitude());
            } else {
                stmt.setNull(10, Types.DOUBLE);
            }
            
            if (seance.getLongitude() != null) {
                stmt.setDouble(11, seance.getLongitude());
            } else {
                stmt.setNull(11, Types.DOUBLE);
            }
            
            stmt.setLong(12, seance.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating seance", e);
            return false;
        }
    }
    
    public boolean updateStatus(Long id, String status, String commentaire) {
        String sql = "UPDATE seance SET status = ?, commentaire = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, commentaire);
            stmt.setLong(3, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating seance status", e);
            return false;
        }
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM seance WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting seance with ID " + id, e);
            return false;
        }
    }
} 