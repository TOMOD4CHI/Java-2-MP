package org.cpi2.repository;

import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.TypePermis;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Candidat Repository
public class CandidatRepository {
    private static final Logger LOGGER = Logger.getLogger(CandidatRepository.class.getName());
    private static final String URL = "jdbc:mysql://localhost:3306/auto_ecole";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<Candidat> findAll() {
        List<Candidat> candidats = new ArrayList<>();
        String sql = "SELECT * FROM candidat";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Candidat candidat = mapResultSetToCandidat(rs);
                candidats.add(candidat);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all candidats", e);
        }

        return candidats;
    }

    public Optional<Candidat> findById(Long id) {
        String sql = "SELECT * FROM candidat WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCandidat(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding candidat by id", e);
        }

        return Optional.empty();
    }

    public Optional<Candidat> findByCin(String cin) {
        String sql = "SELECT * FROM candidat WHERE cin = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCandidat(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding candidat by CIN", e);
        }

        return Optional.empty();
    }

    public boolean save(Candidat candidat) {
        String sql = "INSERT INTO candidat (nom, prenom, cin, adresse, telephone, email, date_naissance, type_permis) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, candidat.getNom());
            stmt.setString(2, candidat.getPrenom());
            stmt.setString(3, candidat.getCin());
            stmt.setString(4, candidat.getAdresse());
            stmt.setString(5, candidat.getTelephone());
            stmt.setString(6, candidat.getEmail());
            stmt.setDate(7, Date.valueOf(candidat.getDateNaissance()));
            stmt.setInt(8, candidat.getTypePermis().ordinal() + 1);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    candidat.setId(generatedKeys.getLong(1));
                    return true;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving candidat", e);
        }

        return false;
    }

    public boolean update(Candidat candidat) {
        String sql = "UPDATE candidat SET nom = ?, prenom = ?, cin = ?, adresse = ?, " +
                    "telephone = ?, email = ?, date_naissance = ?, type_permis = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, candidat.getNom());
            stmt.setString(2, candidat.getPrenom());
            stmt.setString(3, candidat.getCin());
            stmt.setString(4, candidat.getAdresse());
            stmt.setString(5, candidat.getTelephone());
            stmt.setString(6, candidat.getEmail());
            stmt.setDate(7, Date.valueOf(candidat.getDateNaissance()));
            stmt.setInt(8, candidat.getTypePermis().ordinal() + 1);
            stmt.setLong(9, candidat.getId());

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating candidat", e);
        }

        return false;
    }

    private Candidat mapResultSetToCandidat(ResultSet rs) throws SQLException {
        Candidat candidat = new Candidat();
        candidat.setId(rs.getLong("id"));
        candidat.setNom(rs.getString("nom"));
        candidat.setPrenom(rs.getString("prenom"));
        candidat.setCin(rs.getString("cin"));
        candidat.setAdresse(rs.getString("adresse"));
        candidat.setTelephone(rs.getString("telephone"));
        candidat.setEmail(rs.getString("email"));
        candidat.setDateNaissance(rs.getDate("date_naissance").toLocalDate());
        candidat.setTypePermis(TypePermis.values()[rs.getInt("type_permis") - 1]);
        return candidat;
    }
}
