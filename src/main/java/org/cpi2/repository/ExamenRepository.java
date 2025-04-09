package org.cpi2.repository;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.Examen;
import org.cpi2.entities.TypeExamen;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExamenRepository extends BaseRepository<Examen> {
    private static final Logger LOGGER = Logger.getLogger(ExamenRepository.class.getName());
    private final CandidatRepository candidatRepository = new CandidatRepository();

    public Optional<Examen> findById(Long id) {
        String sql = """
            SELECT * FROM examen 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToExamen(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examen by ID", e);
        }
        return Optional.empty();
    }

    public List<Examen> findAll() {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                examens.add(mapResultSetToExamen(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all examens", e);
        }
        return examens;
    }

    public Map<Integer,TypeExamen> getTypeExamen() throws SQLException {
        Map<Integer,TypeExamen> typeExamenMap = new HashMap<>();
        String sql = "SELECT * FROM type_examen";
        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)){
             while (rs.next()) {
                    String libelle = rs.getString("libelle");
                    switch (libelle.toUpperCase()) {
                        case "CODE":
                            typeExamenMap.put(rs.getInt("id"), TypeExamen.CODE);
                        case "CONDUITE ":
                            typeExamenMap.put(rs.getInt("id"), TypeExamen.CONDUITE);
                        default:
                            continue;
                    }
                }
        }
        catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding type", e);
            return null;
        }
        return typeExamenMap;
    }
    private Examen mapResultSetToExamen(ResultSet rs) throws SQLException {
        TypeExamen typeExamen = getTypeExamen().getOrDefault(rs.getInt("type_examen_id"), null);
        Candidat candidat = candidatRepository.findById(rs.getLong("candidat_id")).orElse(null);
        if (candidat == null) {
            LOGGER.warning("Candidat not found for examen ID: " + rs.getLong("id"));
            return null;
        }
        if (typeExamen == null) {
            LOGGER.warning("Type examen not found for examen ID: " + rs.getLong("id"));
            return null;
        }
        Examen examen = new Examen();
        examen.setId(rs.getLong("id"));
        examen.setType(typeExamen);
        examen.setCandidat(candidat);
        examen.setDate(rs.getDate("date_examen").toLocalDate());
        examen.setFrais(rs.getDouble("frais"));

        // Handle nullable resultat field
        Object resultatObj = rs.getObject("resultat");
        if (resultatObj != null) {
            examen.setResultat((Boolean) resultatObj);
        }

        return examen;
    }

    public boolean save(Examen examen) {
        String sql = """
            INSERT INTO examen (candidat_id, type_examen_id, date_examen, frais, resultat)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, examen.getCandidat().getId().intValue());
            stmt.setInt(2, getTypeExamenId(examen.getType()));
            stmt.setDate(3, Date.valueOf(examen.getDate()));
            stmt.setDouble(4, examen.getFrais());

            if (examen.getResultat() != null) {
                stmt.setBoolean(5, examen.getResultat());
            } else {
                stmt.setNull(5, Types.BOOLEAN);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        examen.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving examen", e);
        }
        return false;
    }
    public int getTypeExamenId(TypeExamen typeExamen) throws SQLException {
        return getTypeExamen().entrySet().stream()
                .filter(entry -> entry.getValue().equals(typeExamen))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElseThrow();
    }
    public boolean update(Examen examen) {
        String sql = """
            UPDATE examen 
            SET candidat_id = ?, type_examen_id = ?, date_examen = ?, 
                frais = ?, resultat = ?
            WHERE id = ?
        """;
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examen.getCandidat().getId().intValue());
            stmt.setInt(2, getTypeExamenId(examen.getType()));
            stmt.setDate(3, Date.valueOf(examen.getDate()));
            stmt.setDouble(4, examen.getFrais());

            if (examen.getResultat() != null) {
                stmt.setBoolean(5, examen.getResultat());
            } else {
                stmt.setNull(5, Types.BOOLEAN);
            }

            stmt.setLong(6, examen.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating examen", e);
            return false;
        }
    }

    public List<Examen> findByCandidatId(Long candidatId) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE candidat_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                examens.add(mapResultSetToExamen(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by candidat ID", e);
        }
        return examens;
    }

    public List<Examen> findByTypeExamen(Integer typeExamenId) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE type_examen_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, typeExamenId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                examens.add(mapResultSetToExamen(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by type", e);
        }
        return examens;
    }

    public List<Examen> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE date_examen BETWEEN ? AND ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                examens.add(mapResultSetToExamen(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by date range", e);
        }
        return examens;
    }

    public List<Examen> findByResultat(Boolean resultat) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE resultat = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, resultat);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                examens.add(mapResultSetToExamen(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by resultat", e);
        }
        return examens;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM examen WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting examen", e);
            return false;
        }
    }
}