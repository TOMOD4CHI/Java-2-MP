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
            SELECT p.*, m.date_embauche 
            FROM moniteur m 
            JOIN personne p ON m.id_personne = p.id 
            WHERE p.id = ?
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
        String sql = """
            SELECT p.*, m.date_embauche 
            FROM moniteur m 
            JOIN personne p ON m.id_personne = p.id
        """;

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

            // Insert into personne
            String personSql = """
                INSERT INTO personne (nom, prenom, cin, adresse, telephone, email)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, moniteur.getNom());
                stmt.setString(2, moniteur.getPrenom());
                stmt.setString(3, moniteur.getCin());
                stmt.setString(4, moniteur.getAdresse());
                stmt.setString(5, moniteur.getTelephone());
                stmt.setString(6, moniteur.getEmail());

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long personId = generatedKeys.getLong(1);

                            // Insert into moniteur
                            String moniteurSql = "INSERT INTO moniteur (id_personne, date_embauche) VALUES (?, ?)";

                            try (PreparedStatement moniteurStmt = conn.prepareStatement(moniteurSql)) {
                                moniteurStmt.setLong(1, personId);
                                moniteurStmt.setDate(2, Date.valueOf(moniteur.getDateEmbauche()));

                                if (moniteurStmt.executeUpdate() > 0) {
                                    // Insert specialities
                                    String specialitySql = "INSERT INTO moniteur_specialite (id_moniteur, id_type_permis) VALUES (?, ?)";

                                    try (PreparedStatement specStmt = conn.prepareStatement(specialitySql)) {
                                        for (TypePermis specialite : moniteur.getSpecialites()) {
                                            specStmt.setLong(1, personId);
                                            specStmt.setInt(2, specialite.ordinal() + 1);
                                            specStmt.addBatch();
                                        }
                                        specStmt.executeBatch();
                                    }

                                    conn.commit();
                                    moniteur.setId(personId);
                                    return true;
                                }
                            }
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

            // Update personne
            String personSql = """
                UPDATE personne 
                SET nom = ?, prenom = ?, cin = ?, adresse = ?, telephone = ?, email = ?
                WHERE id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(personSql)) {
                stmt.setString(1, moniteur.getNom());
                stmt.setString(2, moniteur.getPrenom());
                stmt.setString(3, moniteur.getCin());
                stmt.setString(4, moniteur.getAdresse());
                stmt.setString(5, moniteur.getTelephone());
                stmt.setString(6, moniteur.getEmail());
                stmt.setLong(7, moniteur.getId());

                if (stmt.executeUpdate() > 0) {
                    // Update moniteur
                    String moniteurSql = "UPDATE moniteur SET date_embauche = ? WHERE id_personne = ?";

                    try (PreparedStatement moniteurStmt = conn.prepareStatement(moniteurSql)) {
                        moniteurStmt.setDate(1, Date.valueOf(moniteur.getDateEmbauche()));
                        moniteurStmt.setLong(2, moniteur.getId());

                        if (moniteurStmt.executeUpdate() > 0) {
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
