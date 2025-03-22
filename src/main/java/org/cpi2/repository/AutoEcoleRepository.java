package org.cpi2.repository;

import org.cpi2.entitties.AutoEcole;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

// Auto École Repository
public class AutoEcoleRepository extends BaseRepository<AutoEcole> {
    
    private static final Logger LOGGER = Logger.getLogger(AutoEcoleRepository.class.getName());

    public Optional<AutoEcole> findById(long id) {
        String sql = "SELECT * FROM auto_ecole WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AutoEcole autoEcole = new AutoEcole();
                autoEcole.setId(rs.getInt("id"));
                autoEcole.setNom(rs.getString("nom"));
                autoEcole.setAdresse(rs.getString("adresse"));
                autoEcole.setTelephone(rs.getString("telephone"));
                autoEcole.setEmail(rs.getString("email"));
                autoEcole.setDirecteur(rs.getString("directeur"));
                autoEcole.setLogo(rs.getString("logo"));
                return Optional.of(autoEcole);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding auto école by ID", e);
        }
        return Optional.empty();
    }
    
    /**
     * Find the first auto-école in the database
     * @return The first auto-école found or null if none exists
     * @throws SQLException if a database error occurs
     */
    public AutoEcole findFirst() throws SQLException {
        String sql = "SELECT * FROM auto_ecole LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                AutoEcole autoEcole = new AutoEcole();
                autoEcole.setId(rs.getInt("id"));
                autoEcole.setNom(rs.getString("nom"));
                autoEcole.setAdresse(rs.getString("adresse"));
                autoEcole.setTelephone(rs.getString("telephone"));
                autoEcole.setEmail(rs.getString("email"));
                autoEcole.setDirecteur(rs.getString("directeur"));
                autoEcole.setLogo(rs.getString("logo"));
                return autoEcole;
            }
        }
        return null;
    }
    
    /**
     * Find all auto-écoles in the database
     * @return A list of all auto-écoles
     */
    public List<AutoEcole> findAll() {
        String sql = "SELECT * FROM auto_ecole";
        List<AutoEcole> ecoles = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                AutoEcole autoEcole = new AutoEcole();
                autoEcole.setId(rs.getInt("id"));
                autoEcole.setNom(rs.getString("nom"));
                autoEcole.setAdresse(rs.getString("adresse"));
                autoEcole.setTelephone(rs.getString("telephone"));
                autoEcole.setEmail(rs.getString("email"));
                autoEcole.setDirecteur(rs.getString("directeur"));
                autoEcole.setLogo(rs.getString("logo"));
                ecoles.add(autoEcole);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all auto écoles", e);
        }
        return ecoles;
    }

    public boolean save(AutoEcole autoEcole) {
        String sql = "INSERT INTO auto_ecole (nom, adresse, telephone, email, directeur, logo) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, autoEcole.getNom());
            stmt.setString(2, autoEcole.getAdresse());
            stmt.setString(3, autoEcole.getTelephone());
            stmt.setString(4, autoEcole.getEmail());
            stmt.setString(5, autoEcole.getDirecteur());
            stmt.setString(6, autoEcole.getLogo());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        autoEcole.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving auto école", e);
        }
        return false;
    }

    public boolean update(AutoEcole autoEcole) {
        String sql = "UPDATE auto_ecole SET nom = ?, adresse = ?, telephone = ?, email = ?, directeur = ?, logo = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, autoEcole.getNom());
            stmt.setString(2, autoEcole.getAdresse());
            stmt.setString(3, autoEcole.getTelephone());
            stmt.setString(4, autoEcole.getEmail());
            stmt.setString(5, autoEcole.getDirecteur());
            stmt.setString(6, autoEcole.getLogo());
            stmt.setInt(7, autoEcole.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating auto école", e);
            return false;
        }
    }
    
    /**
     * Delete an auto-école by its ID
     * @param id The ID of the auto-école to delete
     * @return true if the deletion was successful, false otherwise
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM auto_ecole WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting auto école with ID " + id, e);
            return false;
        }
    }

    public boolean delete() {
        String sql = "DELETE FROM auto_ecole WHERE id != 0";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting auto école", e);
            return false;
        }
    }
}
