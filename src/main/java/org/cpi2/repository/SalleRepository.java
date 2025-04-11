package org.cpi2.repository;

import org.cpi2.entities.Salle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalleRepository {
    private static final Logger LOGGER = Logger.getLogger(SalleRepository.class.getName());

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public List<Salle> findAll() {
        List<Salle> salles = new ArrayList<>();
        String sql = "SELECT * FROM salle";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Salle salle = mapResultSetToSalle(rs);
                salles.add(salle);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération de toutes les salles", e);
        }

        return salles;
    }

    public Optional<Salle> findById(Long id) {
        String sql = "SELECT * FROM salle WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSalle(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de la salle par ID", e);
        }

        return Optional.empty();
    }

    public Optional<Salle> findByNumero(String numero) {
        String sql = "SELECT * FROM salle WHERE numero = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, numero);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSalle(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la recherche de la salle par numéro", e);
        }

        return Optional.empty();
    }

    public boolean save(Salle salle) {
        String sql = "INSERT INTO salle (nom, numero, capacite, notes) VALUES (?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, salle.getNom());
            stmt.setString(2, salle.getNumero());
            stmt.setInt(3, salle.getCapacite());
            stmt.setString(4, salle.getNotes());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    salle.setId(generatedKeys.getLong(1));
                    LOGGER.log(Level.INFO, "Salle enregistrée avec succès, ID: " + salle.getId());
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement de la salle: " + e.getMessage(), e);
        }

        return false;
    }

    public boolean update(Salle salle) {
        String sql = "UPDATE salle SET nom = ?, numero = ?, capacite = ?, notes = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, salle.getNom());
            stmt.setString(2, salle.getNumero());
            stmt.setInt(3, salle.getCapacite());
            stmt.setString(4, salle.getNotes());
            stmt.setLong(5, salle.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la salle", e);
        }

        return false;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM salle WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la salle", e);
        }

        return false;
    }

    private Salle mapResultSetToSalle(ResultSet rs) throws SQLException {
        Salle salle = new Salle();
        salle.setId(rs.getLong("id"));
        salle.setNom(rs.getString("nom"));
        salle.setNumero(rs.getString("numero"));
        salle.setCapacite(rs.getInt("capacite"));
        salle.setNotes(rs.getString("notes"));
        return salle;
    }
}