package org.cpi2.repository;

import org.cpi2.entitties.Moniteur;
import org.cpi2.entitties.TypePermis;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// Monitor Repository
public class MoniteurRepository {
    private static final Logger LOGGER = Logger.getLogger(MoniteurRepository.class.getName());

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public List<Moniteur> findAll() {
        List<Moniteur> moniteurs = new ArrayList<>();
        String sql = "SELECT * FROM moniteur";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadSpecialites(conn, moniteur);
                moniteurs.add(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all moniteurs", e);
        }

        return moniteurs;
    }

    public Optional<Moniteur> findById(Long id) {
        String sql = "SELECT * FROM moniteur WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadSpecialites(conn, moniteur);
                return Optional.of(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding moniteur by id", e);
        }

        return Optional.empty();
    }

    public Optional<Moniteur> findByCin(String cin) {
        String sql = "SELECT * FROM moniteur WHERE cin = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadSpecialites(conn, moniteur);
                return Optional.of(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding moniteur by CIN", e);
        }

        return Optional.empty();
    }

    public boolean save(Moniteur moniteur) {
        String sql = "INSERT INTO moniteur (nom, prenom, cin, adresse, telephone, email, date_embauche) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, moniteur.getNom());
            stmt.setString(2, moniteur.getPrenom());
            stmt.setString(3, moniteur.getCin());
            stmt.setString(4, moniteur.getAdresse());
            stmt.setString(5, moniteur.getTelephone());
            stmt.setString(6, moniteur.getEmail());
            stmt.setDate(7, Date.valueOf(moniteur.getDateEmbauche()));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    moniteur.setId(generatedKeys.getLong(1));
                    return saveSpecialites(conn, moniteur);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving moniteur", e);
        }

        return false;
    }

    public boolean update(Moniteur moniteur) {
        String sql = "UPDATE moniteur SET nom = ?, prenom = ?, cin = ?, adresse = ?, " +
                    "telephone = ?, email = ?, date_embauche = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moniteur.getNom());
            stmt.setString(2, moniteur.getPrenom());
            stmt.setString(3, moniteur.getCin());
            stmt.setString(4, moniteur.getAdresse());
            stmt.setString(5, moniteur.getTelephone());
            stmt.setString(6, moniteur.getEmail());
            stmt.setDate(7, Date.valueOf(moniteur.getDateEmbauche()));
            stmt.setLong(8, moniteur.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            // Delete existing specialites
            String deleteSql = "DELETE FROM moniteur_specialite WHERE moniteur_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setLong(1, moniteur.getId());
                deleteStmt.executeUpdate();
            }

            // Save new specialites
            return saveSpecialites(conn, moniteur);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating moniteur", e);
        }

        return false;
    }

    private Moniteur mapResultSetToMoniteur(ResultSet rs) throws SQLException {
        Moniteur moniteur = new Moniteur();
        moniteur.setId(rs.getLong("id"));
        moniteur.setNom(rs.getString("nom"));
        moniteur.setPrenom(rs.getString("prenom"));
        moniteur.setCin(rs.getString("cin"));
        moniteur.setAdresse(rs.getString("adresse"));
        moniteur.setTelephone(rs.getString("telephone"));
        moniteur.setEmail(rs.getString("email"));
        moniteur.setDateEmbauche(rs.getDate("date_embauche").toLocalDate());
        moniteur.setSpecialites(new HashSet<>());
        return moniteur;
    }

    private void loadSpecialites(Connection conn, Moniteur moniteur) throws SQLException {
        String sql = "SELECT tp.code FROM type_permis tp " +
                    "JOIN moniteur_specialite ms ON tp.id = ms.type_permis_id " +
                    "WHERE ms.moniteur_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, moniteur.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                moniteur.getSpecialites().add(TypePermis.valueOf(rs.getString("code")));
            }
        }
    }

    private boolean saveSpecialites(Connection conn, Moniteur moniteur) throws SQLException {
        String sql = "INSERT INTO moniteur_specialite (moniteur_id, type_permis_id) VALUES (?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (TypePermis specialite : moniteur.getSpecialites()) {
                stmt.setLong(1, moniteur.getId());
                stmt.setInt(2, specialite.ordinal() + 1);
                stmt.addBatch();
            }
            stmt.executeBatch();
            return true;
        }
    }
}