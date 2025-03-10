package service;

import entitties.Document;
import entitties.Dossier;
import repository.DossierRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

public class DossierService {
    private final DossierRepository dossierRepository;
    private final DocumentService documentService;

    public DossierService() {
        this.dossierRepository = new DossierRepository();
        this.documentService = new DocumentService();
    }

    public Optional<Dossier> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }

    public Optional<Dossier> getDossierByCandidat(Long candidatId) {
        return dossierRepository.findByCandidatId(candidatId);
    }

    public boolean creerDossier(Dossier dossier, Long candidatId) {
        return dossierRepository.save(dossier, candidatId);
    }

    public boolean supprimerDossier(Long dossierId) {
        return dossierRepository.delete(dossierId);
    }

    public boolean ajouterDocument(Long dossierId, Document document, File fichierSource) {
        try {
            return documentService.uploadDocument(document, dossierId, fichierSource);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean mettreAJourDocument(Long dossierId, Document document, File nouveauFichier) {
        try {
            return documentService.updateDocument(document, dossierId, nouveauFichier);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimerDocument(Long documentId) {
        return documentService.deleteDocument(documentId);
    }

    public Map<String, TreeSet<Document>> getDocumentsByDossier(Long dossierId) {
        Optional<Dossier> dossierOpt = dossierRepository.findById(dossierId);
        return dossierOpt.map(Dossier::getDocuments).orElse(Map.of());
    }

    public boolean verifierDocumentsObligatoires(Long dossierId, List<String> typesObligatoires) {
        try {
            for (String type : typesObligatoires) {
                List<Document> documents = documentService.getDocumentsByDossierAndType(dossierId, type);
                if (documents.isEmpty()) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String verifierCompletudeEtObtenirMessage(Long dossierId, List<String> typesObligatoires) {
        try {
            List<String> documentsManquants = new ArrayList<>();

            for (String type : typesObligatoires) {
                List<Document> documents = documentService.getDocumentsByDossierAndType(dossierId, type);
                if (documents.isEmpty()) {
                    documentsManquants.add(type);
                }
            }

            if (documentsManquants.isEmpty()) {
                return "Le dossier est complet.";
            } else {
                return "Documents manquants : " + String.join(", ", documentsManquants);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Erreur lors de la vérification du dossier.";
        }
    }

    public List<Document> getDocumentsByType(Long dossierId, String typeDocument) {
        try {
            return documentService.getDocumentsByDossierAndType(dossierId, typeDocument);
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public boolean dossierExiste(Long dossierId) {
        return dossierRepository.findById(dossierId).isPresent();
    }

    public boolean candidatPossedeUnDossier(Long candidatId) {
        return dossierRepository.findByCandidatId(candidatId).isPresent();
    }
}