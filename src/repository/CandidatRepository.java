package repository;

import entitties.Candidat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Candidat Repository
public class CandidatRepository extends BaseRepository<Candidat> {

    public Optional<Candidat> findById(Long id) {
        String sql = """
            SELECT p.*, c.date_inscription, c.id_type_permis 
            FROM candidat c 
            JOIN personne p ON c.id_personne = p.id 
            WHERE p.id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCandidat(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding candidat by ID", e);
        }
        return Optional.empty();
    }

    public List<Candidat> findAll() {
        List<Candidat> candidats = new ArrayList<>();
        String sql = """
            SELECT p.*, c.date_inscription, c.id_type_permis 
            FROM candidat c 
            JOIN personne p ON c.id_personne = p.id
        """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                candidats.add(mapResultSetToCandidat(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all candidats", e);
        }
        return candidats;
    }

    private Candidat mapResultSetToCandidat(ResultSet rs) throws SQLException {
        Candidat candidat = new Candidat(
                rs.getString("nom"),
                rs.getString("prenom"),
                rs.getString("cin"),
                rs.getString("adresse"),
                rs.getString("telephone"),
                rs.getDate("date_inscription").toLocalDate(),
                TypePermis.valueOf(rs.getString("id_type_permis"))
        );
        candidat.setId(rs.getLong("id"));
        candidat.setEmail(rs.getString("email"));
        return candidat;
    }

    public boolean save(Candidat candidat) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert into personne first
            String personSql = """
                INSERT INTO personne (nom, prenom, cin, adresse, telephone, email)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, candidat.getNom());
                stmt.setString(2, candidat.getPrenom());
                stmt.setString(3, candidat.getCin());
                stmt.setString(4, candidat.getAdresse());
                stmt.setString(5, candidat.getTelephone());
                stmt.setString(6, candidat.getEmail());

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long personId = generatedKeys.getLong(1);

                            // Insert into candidat
                            String candidatSql = """
                                INSERT INTO candidat (id_personne, date_inscription, id_type_permis)
                                VALUES (?, ?, ?)
                            """;

                            try (PreparedStatement candidatStmt = conn.prepareStatement(candidatSql)) {
                                candidatStmt.setLong(1, personId);
                                candidatStmt.setDate(2, Date.valueOf(candidat.getDateInscription()));
                                candidatStmt.setInt(3, candidat.getTypePermis().ordinal() + 1);

                                if (candidatStmt.executeUpdate() > 0) {
                                    conn.commit();
                                    candidat.setId(personId);
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
            LOGGER.log(Level.SEVERE, "Error saving candidat", e);
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

    public boolean update(Candidat candidat) {
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
                stmt.setString(1, candidat.getNom());
                stmt.setString(2, candidat.getPrenom());
                stmt.setString(3, candidat.getCin());
                stmt.setString(4, candidat.getAdresse());
                stmt.setString(5, candidat.getTelephone());
                stmt.setString(6, candidat.getEmail());
                stmt.setLong(7, candidat.getId());

                if (stmt.executeUpdate() > 0) {
                    // Update candidat
                    String candidatSql = """
                        UPDATE candidat 
                        SET date_inscription = ?, id_type_permis = ?
                        WHERE id_personne = ?
                    """;

                    try (PreparedStatement candidatStmt = conn.prepareStatement(candidatSql)) {
                        candidatStmt.setDate(1, Date.valueOf(candidat.getDateInscription()));
                        candidatStmt.setInt(2, candidat.getTypePermis().ordinal() + 1);
                        candidatStmt.setLong(3, candidat.getId());

                        if (candidatStmt.executeUpdate() > 0) {
                            conn.commit();
                            return true;
                        }
                    }
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating candidat", e);
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
