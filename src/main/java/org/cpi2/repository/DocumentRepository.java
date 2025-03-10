package org.cpi2.repository;

import Exceptions.DataNotFound;
import entitties.Document;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentRepository extends BaseRepository<Document> {
    private static final Logger LOGGER = Logger.getLogger(DocumentRepository.class.getName());
    private final TypeDocumentRepository typeDocumentRepository = new TypeDocumentRepository();

    public Optional<Document> findById(Long id) {
        String sql = """
            SELECT * FROM document 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding document by ID", e);
        }
        return Optional.empty();
    }

    public List<Document> findAll() {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM document";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all documents", e);
        }
        return documents;
    }

    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        String typeDocument = typeDocumentRepository
                .findById(rs.getInt("type_document_id"))
                .orElse(null);

        Document document = new Document();
        document.setId(rs.getLong("id"));
        document.setTypeDocument(typeDocument);
        document.setNomFichier(rs.getString("nom_fichier"));
        document.setCheminFichier(rs.getString("chemin_fichier"));

        Timestamp timestamp = rs.getTimestamp("date_upload");
        document.setDateUpload(timestamp != null ? timestamp.toLocalDateTime() : null);

        return document;
    }

    public boolean save(Document document, Long dossierId) throws DataNotFound {
        String sql = """
            INSERT INTO document (dossier_id, type_document_id, nom_fichier, chemin_fichier)
            VALUES (?, ?, ?, ?)
        """;
        long typeDocumentId = (long)typeDocumentRepository.findByLibelle(document.getTypeDocument()).get();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, dossierId);
            stmt.setLong(2, typeDocumentId);
            stmt.setString(3, document.getNomFichier());
            stmt.setString(4, document.getCheminFichier());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        document.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving document", e);
        }
        return false;
    }

    public boolean update(Document document, Long dossierId) throws DataNotFound {
        String sql = """
            UPDATE document 
            SET dossier_id = ?, type_document_id = ?, nom_fichier = ?, chemin_fichier = ?
            WHERE id = ?
        """;
        long typeDocumentId = (long)typeDocumentRepository.findByLibelle(document.getTypeDocument()).get();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, dossierId);
            stmt.setLong(2, typeDocumentId);
            stmt.setString(3, document.getNomFichier());
            stmt.setString(4, document.getCheminFichier());
            stmt.setLong(5, document.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating document", e);
            return false;
        }
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM document WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting document", e);
            return false;
        }
    }

    public List<Document> findByDossierId(Long dossierId) {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM document WHERE dossier_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, dossierId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by dossier ID", e);
        }
        return documents;
    }

    public List<Document> findByTypeDocument(String typeDocument) throws DataNotFound {
        List<Document> documents = new ArrayList<>();
        String sql = "SELECT * FROM document WHERE type_document_id = ?";
        long typeDocumentId = (long)typeDocumentRepository.findByLibelle(typeDocument).get();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, typeDocumentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding documents by type", e);
        }
        return documents;
    }

    public List<Document> findDocumentInDossier(Long dossierId, String  typeDocument) throws DataNotFound {
        String sql = "SELECT * FROM document WHERE dossier_id = ? AND type_document_id = ?";
        long typeDocumentId = (long) typeDocumentRepository.findByLibelle(typeDocument).get();
        List<Document> documents = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, dossierId);
            stmt.setLong(2, typeDocumentId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                documents.add(mapResultSetToDocument(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding document by dossier and type", e);
        }
        return documents;
    }
}