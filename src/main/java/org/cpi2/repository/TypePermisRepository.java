package org.cpi2.repository;

import org.cpi2.Exceptions.DataNotFound;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypePermisRepository extends BaseRepository<Object> {
    private static final Logger LOGGER = Logger.getLogger(TypePermisRepository.class.getName());

    public Optional<String> findById(Integer id) throws DataNotFound {
        String sql = """
            SELECT * FROM type_permis 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("code"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding type permis by ID", e);
        }
        throw new DataNotFound("Type permis not found");
    }

    public List<String> findAll() {
        List<String> typePermis = new ArrayList<>();
        String sql = "SELECT * FROM type_permis";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                typePermis.add(rs.getString("code"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all type permis", e);
        }
        return typePermis;
    }

    public Optional<Integer> findByLibelle(String libelle) throws DataNotFound {
        String sql = """
            SELECT * FROM type_permis 
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
            LOGGER.log(Level.SEVERE, "Error finding type permis by libelle", e);
        }
        throw new DataNotFound("Type permis not found");
    }

    public Optional<Integer> findByCode(String code) throws DataNotFound {
        String sql = """
            SELECT * FROM type_permis 
            WHERE code = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getInt("id"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding type permis by code", e);
        }
        throw new DataNotFound("Type permis not found");
    }
}