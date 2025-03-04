package repository;

import entitties.Moniteur;
import entitties.TypePermis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Monitor Repository
public class MoniteurRepository extends BaseRepository<Moniteur> {

    public Optional<Moniteur> findById(Long id) {
        String sql = """
            SELECT * FROM moniteur 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadMoniteurSpecialities(conn, moniteur);
                return Optional.of(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding moniteur by ID", e);
        }
        return Optional.empty();
    }

    public List<Moniteur> findAll() {
        List<Moniteur> moniteurs = new ArrayList<>();
        String sql = "SELECT * FROM moniteur";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadMoniteurSpecialities(conn, moniteur);
                moniteurs.add(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all moniteurs", e);
        }
        return moniteurs;
    }

    private Moniteur mapResultSetToMoniteur(ResultSet rs) throws SQLException {
        Moniteur moniteur = new Moniteur(
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("cin"),
                rs.getString("adresse"),
                rs.getString("telephone"),
                rs.getDate("date_embauche").toLocalDate()
        );
        moniteur.setId(rs.getLong("id"));
        moniteur.setEmail(rs.getString("email"));
        return moniteur;
    }

    private void loadMoniteurSpecialities(Connection conn, Moniteur moniteur) throws SQLException {
        String sql = """
            SELECT tp.* 
            FROM moniteur_specialite ms 
            JOIN type_permis tp ON ms.id_type_permis = tp.id 
            WHERE ms.id_moniteur = ?
        """;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, moniteur.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                moniteur.getSpecialites().add(TypePermis.valueOf(rs.getString("code")));
            }
        }
    }

    public boolean save(Moniteur moniteur) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert moniteur directly
            String moniteurSql = """
                INSERT INTO moniteur (nom, prenom, cin, adresse, telephone, email, date_embauche)
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(moniteurSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, moniteur.getNom());
                stmt.setString(2, moniteur.getPrenom());
                stmt.setString(3, moniteur.getCin());
                stmt.setString(4, moniteur.getAdresse());
                stmt.setString(5, moniteur.getTelephone());
                stmt.setString(6, moniteur.getEmail());
                stmt.setDate(7, Date.valueOf(moniteur.getDateEmbauche()));

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long moniteurId = generatedKeys.getLong(1);

                            // Insert specialities
                            String specialitySql = "INSERT INTO moniteur_specialite (id_moniteur, id_type_permis) VALUES (?, ?)";

                            try (PreparedStatement specStmt = conn.prepareStatement(specialitySql)) {
                                for (TypePermis specialite : moniteur.getSpecialites()) {
                                    specStmt.setLong(1, moniteurId);
                                    specStmt.setInt(2, specialite.ordinal() + 1);
                                    specStmt.addBatch();
                                }
                                specStmt.executeBatch();
                            }

                            conn.commit();
                            moniteur.setId(moniteurId);
                            return true;
                        }
                    }
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving moniteur", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

    public boolean update(Moniteur moniteur) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Update moniteur directly
            String moniteurSql = """
                UPDATE moniteur 
                SET nom = ?, prenom = ?, cin = ?, adresse = ?, telephone = ?, email = ?, date_embauche = ?
                WHERE id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(moniteurSql)) {
                stmt.setString(1, moniteur.getNom());
                stmt.setString(2, moniteur.getPrenom());
                stmt.setString(3, moniteur.getCin());
                stmt.setString(4, moniteur.getAdresse());
                stmt.setString(5, moniteur.getTelephone());
                stmt.setString(6, moniteur.getEmail());
                stmt.setDate(7, Date.valueOf(moniteur.getDateEmbauche()));
                stmt.setLong(8, moniteur.getId());

                if (stmt.executeUpdate() > 0) {
                    // Update specialities
                    // First delete existing
                    String deleteSpecSql = "DELETE FROM moniteur_specialite WHERE id_moniteur = ?";
                    try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSpecSql)) {
                        deleteStmt.setLong(1, moniteur.getId());
                        deleteStmt.executeUpdate();
                    }

                    // Then insert new ones
                    String specialitySql = "INSERT INTO moniteur_specialite (id_moniteur, id_type_permis) VALUES (?, ?)";
                    try (PreparedStatement specStmt = conn.prepareStatement(specialitySql)) {
                        for (TypePermis specialite : moniteur.getSpecialites()) {
                            specStmt.setLong(1, moniteur.getId());
                            specStmt.setInt(2, specialite.ordinal() + 1);
                            specStmt.addBatch();
                        }
                        specStmt.executeBatch();
                    }

                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating moniteur", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            return false;
        } finally {
            closeQuietly(conn);
        }
    }
}