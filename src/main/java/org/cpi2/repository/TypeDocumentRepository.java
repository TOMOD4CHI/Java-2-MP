package org.cpi2.repository;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.entitties.TypeDocument;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TypeDocumentRepository extends BaseRepository<TypeDocument> {
    private static final Logger LOGGER = Logger.getLogger(TypeDocumentRepository.class.getName());

    public Optional<String > findById(Integer id) throws DataNotFound {
        String sql = """
            SELECT * FROM type_document 
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
            LOGGER.log(Level.SEVERE, "Error finding type document by ID", e);
        }
        throw new DataNotFound("Type document not found");
    }
    public List<String> findAll() {
        List<String> typeDocuments = new ArrayList<>();
        String sql = "SELECT * FROM type_document";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                typeDocuments.add(rs.getString("libelle"));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all type documents", e);
        }
        return typeDocuments;
    }

    public Optional<Integer> findByLibelle(String libelle) throws DataNotFound {
        String sql = """
            SELECT * FROM type_document 
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
            LOGGER.log(Level.SEVERE, "Error finding type document by libelle", e);
        }
        throw new DataNotFound("Type document not found");
    }
}