package org.cpi2.repository;

import org.cpi2.Exceptions.DataNotFound;

import java.sql.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeExamenRepository extends BaseRepository<Object> {
    private static final Logger LOGGER = Logger.getLogger(TypeExamenRepository.class.getName());

    public Optional<String> findById(int id) throws DataNotFound {
        String sql = """
            SELECT * FROM type_examen 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("libelle"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding type examen by ID", e);
        }
        throw new DataNotFound("Type examen not found");
    }

    public List<String> findAll() {
        List<String> typeExamens = new ArrayList<>();
        String sql = "SELECT * FROM type_examen";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                typeExamens.add(rs.getString("libelle"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all type examens", e);
        }
        return typeExamens;
    }

    public Optional<Integer> findByLibelle(String libelle) throws DataNotFound {
        String sql = """
            SELECT * FROM type_examen 
            WHERE libelle = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libelle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding type examen by libelle", e);
        }
        throw new DataNotFound("Type examen not found");
    }

    public double findCoutById(int id) throws DataNotFound {
        String sql = """
            SELECT cout FROM type_examen 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("cout");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding cout for type examen", e);
        }
        throw new DataNotFound("Cost for type examen with id "+id+" not found");
    }

    public double findCoutByLibelle(String libelle) throws DataNotFound {
        String sql = """
            SELECT cout FROM type_examen 
            WHERE libelle = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, libelle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("cout");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding cout for type examen", e);
        }
        throw new DataNotFound("Cost for type examen "+libelle+" not found");
    }
}