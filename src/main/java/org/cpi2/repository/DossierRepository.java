package org.cpi2.repository;

import org.cpi2.entities.Document;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DossierRepository extends BaseRepository<Dossier> {
    private static final Logger LOGGER = Logger.getLogger(DossierRepository.class.getName());
    private final DocumentRepository documentRepository = new DocumentRepository();
    private final TypeDocumentRepository typeDocumentRepository = new TypeDocumentRepository();

    public Optional<Dossier> findById(Long id) {
        String sql = """
            SELECT * FROM dossier 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dossier dossier = mapResultSetToDossier(rs);
                loadDocumentsForDossier(dossier);
                return Optional.of(dossier);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dossier by ID", e);
        }
        return Optional.empty();
    }

    public List<Dossier> findAll() {
        List<Dossier> dossiers = new ArrayList<>();
        String sql = "SELECT * FROM dossier";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Dossier dossier = mapResultSetToDossier(rs);
                loadDocumentsForDossier(dossier);
                dossiers.add(dossier);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all dossiers", e);
        }
        return dossiers;
    }

    private Dossier mapResultSetToDossier(ResultSet rs) throws SQLException {
        Dossier dossier = new Dossier();
        dossier.setId(rs.getLong("id"));
        return dossier;
    }

    private void loadDocumentsForDossier(Dossier dossier) {
        List<Document> documents = documentRepository.findByDossierId(dossier.getId());
        Map<TypeDocument, TreeSet<Document>> documentMap = new HashMap<>();
        for (Document document : documents) {
            documentMap.putIfAbsent(document.getTypeDocument(), new TreeSet<>());
            documentMap.get(document.getTypeDocument()).add(document);
        }
        dossier.setDocuments(documentMap);
    }

    public boolean save(Dossier dossier, Long candidatId) {
        String sql = """
            INSERT INTO dossier (candidat_id)
            VALUES (?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, candidatId);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        Long id = generatedKeys.getLong(1);
                        dossier.setId(id);
                        saveDocuments(dossier);

                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving dossier", e);
        }
        return false;
    }

    private void saveDocuments(Dossier dossier) {
        for (Map.Entry<TypeDocument, TreeSet<Document>> entry : dossier.getDocuments().entrySet()) {
            for (Document document : entry.getValue()) {
                documentRepository.save(document, dossier.getId());
            }
        }
    }

    public boolean delete(Long id) {
        List<Document> documents = documentRepository.findByDossierId(id);
        for (Document doc : documents) {
            documentRepository.delete(doc.getId());
        }

        String sql = "DELETE FROM dossier WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting dossier", e);
            return false;
        }
    }

    public Optional<Dossier> findByCandidatId(Long candidatId) {
        String sql = """
            SELECT * FROM dossier 
            WHERE candidat_id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Dossier dossier = mapResultSetToDossier(rs);
                loadDocumentsForDossier(dossier);
                return Optional.of(dossier);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dossier by candidat ID", e);
        }
        return Optional.empty();
    }

    public boolean addDocument(Long dossierId, Document document) {
        return documentRepository.save(document, dossierId);
    }

    public boolean updateDocument(Long dossierId, Document document) {
        return documentRepository.update(document, dossierId);
    }

    public boolean removeDocument(Long documentId) {
        return documentRepository.delete(documentId);
    }

    public List<Dossier> findDossiersWithMissingDocuments(List<TypeDocument> requiredDocumentTypes) {
        List<Dossier> dossiers = new ArrayList<>();
        StringBuilder sql = new StringBuilder("""
            SELECT DISTINCT d.* FROM dossier d
            WHERE NOT EXISTS (
                SELECT 1 FROM document doc
                JOIN type_document td ON doc.type_document_id = td.id
                WHERE doc.dossier_id = d.id
                AND td.libelle = ?
            )
        """);

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (TypeDocument docType : requiredDocumentTypes) {
                stmt.setString(1, docType.name());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    Dossier dossier = mapResultSetToDossier(rs);
                    loadDocumentsForDossier(dossier);
                    dossiers.add(dossier);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding dossiers with missing documents", e);
        }
        return dossiers;
    }
}