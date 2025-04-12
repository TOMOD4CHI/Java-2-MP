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
    private final String uploadDirectory = "uploads/documents/";  
    private static final Logger LOGGER = Logger.getLogger(DocumentService.class.getName());

    public DocumentService() {
        this.documentRepository = new DocumentRepository();
        this.typeDocumentRepository = new TypeDocumentRepository();

        
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
            
            String uniqueFileName = generateUniqueFileName(document.getNomFichier());
            String subDirectory = String.valueOf(dossierId);

            
            String dossierDirectory = uploadDirectory + subDirectory + "/";
            createDirectoryIfNotExists(dossierDirectory);

            
            String filePath = dossierDirectory + uniqueFileName;

            
            Path destination = Paths.get(filePath);
            Files.copy(fichierSource.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

            
            document.setCheminFichier(filePath);
            document.setDateUpload(LocalDateTime.now());

            
            return documentRepository.save(document, dossierId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error uploading document", e);
            return false;
        }
    }

    

    public boolean updateDocument(Document document, Long dossierId, File nouveauFichier) throws DataNotFound {
        try {
            
            Optional<Document> existingDocumentOpt = documentRepository.findById(document.getId());
            if (existingDocumentOpt.isPresent()) {
                Document existingDocument = existingDocumentOpt.get();

                
                File oldFile = new File(existingDocument.getCheminFichier());
                if (oldFile.exists()) {
                    try {
                        oldFile.delete();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to delete file: " + oldFile.getPath(), e);
                    }
                }

                
                String uniqueFileName = generateUniqueFileName(document.getNomFichier());
                String subDirectory = String.valueOf(dossierId);
                String dossierDirectory = uploadDirectory + subDirectory + "/";

                
                String filePath = dossierDirectory + uniqueFileName;
                Path destination = Paths.get(filePath);
                Files.copy(nouveauFichier.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);

                
                document.setCheminFichier(filePath);
                document.setDateUpload(LocalDateTime.now());

                
                return documentRepository.update(document, dossierId);
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating document", e);
            return false;
        }
    }

    public boolean deleteDocument(Long documentId) {
        try {
            
            Optional<Document> documentOpt = documentRepository.findById(documentId);
            if (documentOpt.isPresent()) {
                Document document = documentOpt.get();

                
                File file = new File(document.getCheminFichier());
                if (file.exists()) {
                    try {
                        file.delete();
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Failed to delete file: " + file.getPath(), e);
                    }
                }

                
                return documentRepository.delete(documentId);
            }
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting document", e);
            return false;
        }
    }

    public List<String> getAvailableDocumentTypes() {
        return typeDocumentRepository.findAll();
    }

    private String generateUniqueFileName(String originalFileName) {
        
        String extension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            extension = originalFileName.substring(i);
        }

        
        String uuid = UUID.randomUUID().toString();

        return uuid + extension;
    }

    private void createDirectoryIfNotExists(String directoryPath) {
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            try {
                directory.mkdirs();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to create directory: " + directoryPath, e);
            }
        }
    }

    public List<Document> getExpiredDocuments() {
        return documentRepository.findExpiredDocuments();
    }

    public List<Document> getDocumentsExpiringInDays(int days) {
        return documentRepository.findDocumentsExpiringInDays(days);
    }

    public boolean validateDocument(Document document, String expectedType, long maxFileSizeMB) {
        
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

        
        File file = new File(document.getCheminFichier());
        double fileSizeInMB = (double) file.length() / (1024 * 1024);
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