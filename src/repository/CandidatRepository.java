package org.cpi2.repository;

import entitties.Candidat;
import entitties.TypePermis;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Candidat Repository
public class CandidatRepository extends BaseRepository<Candidat> {

    public Optional<Candidat> findById(Long id) {
        String sql = """
            SELECT * 
            FROM candidat
            WHERE id = ?
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
    public Optional<Candidat> findByCin(String cin) {
        String sql = """
            SELECT * 
            FROM candidat
            WHERE cin = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);
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
            SELECT *
            FROM candidat 
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
                rs.getString("telephone"));
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
                INSERT INTO candidat (nom, prenom, cin, adresse, telephone, email)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(personSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, candidat.getNom());
                stmt.setString(2, candidat.getPrenom());
                stmt.setString(3, candidat.getCin());
                stmt.setString(4, candidat.getAdresse());
                stmt.setString(5, candidat.getTelephone());
                stmt.setString(6, candidat.getEmail());
            }
            conn.rollback();
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
        return true;
    }

    public boolean update(Candidat candidat) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Update personne
            String personSql = """
                UPDATE candidat 
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
                    conn.commit();
                    return true;
                }
            }
            conn.rollback();
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
        return true;
    }

}
