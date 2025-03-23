package org.cpi2.service;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.entities.Document;
import org.cpi2.repository.DocumentRepository;
import org.cpi2.repository.TypeDocumentRepository;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DocumentService {
    private final DocumentRepository documentRepository;
    private final TypeDocumentRepository typeDocumentRepository;
    private final String uploadDirectory = "uploads/documents/";  // Base directory for document storage
    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());

    public DocumentService() {
        this.documentRepository = new DocumentRepository();
        this.typeDocumentRepository = new TypeDocumentRepository();
        
        // Ensure upload directory exists
        createDirectoryIfNotExists(uploadDirectory);
    }

    public Optional<Document> getDocumentById(Long id) {
        return documentRepository.findById(id);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public List<Document> getDocumentsByDossierId(Long dossierId) {
        return documentRepository.findByDossierId(dossierId);
    }

    public List<Document> getDocumentsByType(String typeDocument) throws DataNotFound {
        return documentRepository.findByTypeDocument(typeDocument);
    }

    public List<Document> getDocumentsByDossierAndType(Long dossierId, String typeDocument) throws DataNotFound {
        return documentRepository.findDocumentInDossier(dossierId, typeDocument);
    }

    public boolean uploadDocument(Document document, Long dossierId, File fichierSource) throws DataNotFound {
        try {
            // Generate unique filename
            String uniqueFileName = generateUniqueFileName(document.getNomFichier());
            String subDirectory = String.valueOf(dossierId);
            
            // Create specific directory for this dossier if it doesn't exist
            String dossierDirectory = uploadDirectory + subDirectory + "/";
            createDirectoryIfNotExists(dossierDirectory);
            
            // Set full file path
            String filePath = dossierDirectory + uniqueFileName;
            
            // Copy file to destination
            Path destination = Paths.get(filePath);
            Files.copy(fichierSource.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
            
            // Update document with file path and upload date
            document.setCheminFichier(filePath);
            document.setDateUpload(LocalDateTime.now());
            
            // Save document in database
            return documentRepository.save(document, dossierId);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateDocument(Document document, Long dossierId, File nouveauFichier) throws DataNotFound {
        try {
            // Get existing document to find its file path
            Optional<Document> existingDocumentOpt = documentRepository.findById(document.getId());
            if (existingDocumentOpt.isPresent()) {
                Document existingDocument = existingDocumentOpt.get();
                
                // Delete old file if it exists
                File oldFile = new File(existingDocument.getCheminFichier());
                if (oldFile.exists()) {
                    oldFile.delete();
                }
                
                // Generate unique filename for new file
                String uniqueFileName = generateUniqueFileName(document.getNomFichier());
                String subDirectory = String.valueOf(dossierId);
                String dossierDirectory = uploadDirectory + subDirectory + "/";
                
                // Copy new file to destination
                String filePath = dossierDirectory + uniqueFileName;
                Path destination = Paths.get(filePath);
                Files.copy(nouveauFichier.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
                
                // Update document with new file path and upload date
                document.setCheminFichier(filePath);
                document.setDateUpload(LocalDateTime.now());
                
                // Update document in database
                return documentRepository.update(document, dossierId);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteDocument(Long documentId) {
        try {
            // Get document to find its file path
            Optional<Document> documentOpt = documentRepository.findById(documentId);
            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();
                
                // Delete file if it exists
                File file = new File(document.getCheminFichier());
                if (file.exists()) {
                    file.delete();
                }
                
                // Delete document from database
                return documentRepository.delete(documentId);
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getAvailableDocumentTypes() {
        return typeDocumentRepository.findAll();
    }

    private String generateUniqueFileName(String originalFileName) {
        // Extract file extension
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }
        
        // Generate UUID for uniqueness
        String uuid = UUID.randomUUID().toString();
        
        return uuid + extension;
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public List<Document> getExpiredDocuments() {
        return documentRepository.findExpiredDocuments();
    }

    public List<Document> getDocumentsExpiringInDays(int days) {
        return documentRepository.findDocumentsExpiringInDays(days);
    }

    public boolean validateDocument(Document document, String expectedType, long maxFileSizeMB) {
        // Validate file type
        String fileExtension = getFileExtension(document.getNomFichier()).toLowerCase();
        boolean isValidType = switch (expectedType) {
            case "IMAGE" -> fileExtension.matches("jpg|jpeg|png|gif");
            case "PDF" -> fileExtension.equals("pdf");
            case "DOCUMENT" -> fileExtension.matches("pdf|doc|docx");
            default -> false;
        };

        if (!isValidType) {
            return false;
        }

        // Validate file size
        File file = new File(document.getCheminFichier());
        long fileSizeInMB = file.length() / (1024 * 1024);
        return fileSizeInMB <= maxFileSizeMB;
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }

    public boolean backupDocument(Document document, String backupDirectory) {
        try {
            File sourceFile = new File(document.getCheminFichier());
            if (!sourceFile.exists()) {
                return false;
            }

            String backupPath = backupDirectory + "/" + 
                               document.getId() + "_" + 
                               LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" +
                               document.getNomFichier();

            Files.copy(sourceFile.toPath(), 
                      Paths.get(backupPath), 
                      StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error backing up document", e);
            return false;
        }
    }
}
