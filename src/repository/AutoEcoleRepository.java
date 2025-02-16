package repository;

import entitties.AutoEcole;

import java.sql.*;
import java.util.Optional;
import java.util.logging.Level;

// Auto École Repository
public class AutoEcoleRepository extends BaseRepository<AutoEcole> {

    public Optional<AutoEcole> findById(long id) {
        String sql = "SELECT * FROM auto_ecole WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AutoEcole autoEcole = new AutoEcole(
                        rs.getString("nom"),
                        rs.getString("adresse"),
                        rs.getString("telephone"),
                        rs.getString("email")
                );
                autoEcole.setId(rs.getLong("id"));
                autoEcole.setLogoPath(rs.getString("logo_path"));
                return Optional.of(autoEcole);
            }

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding auto école by ID", e);
        }
        return Optional.empty();
    }

    public boolean save(AutoEcole autoEcole) {
        String sql = "INSERT INTO auto_ecole (nom, adresse, telephone, email, logo_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, autoEcole.getNom());
            stmt.setString(2, autoEcole.getAdresse());
            stmt.setString(3, autoEcole.getTelephone());
            stmt.setString(4, autoEcole.getEmail());
            stmt.setString(5, autoEcole.getLogoPath());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        autoEcole.setId(generatedKeys.getLong(1));
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
        String sql = "UPDATE auto_ecole SET nom = ?, adresse = ?, telephone = ?, email = ?, logo_path = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, autoEcole.getNom());
            stmt.setString(2, autoEcole.getAdresse());
            stmt.setString(3, autoEcole.getTelephone());
            stmt.setString(4, autoEcole.getEmail());
            stmt.setString(5, autoEcole.getLogoPath());
            stmt.setLong(6, autoEcole.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating auto école", e);
            return false;
        }
    }

    public boolean delete(long id) {
        String sql = "DELETE FROM auto_ecole WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting auto école", e);
            return false;
        }
    }
}
