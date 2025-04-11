package org.cpi2.service;

import org.cpi2.entities.Document;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.repository.DossierRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

public class DossierService {
    private final DossierRepository dossierRepository;
    private final DocumentService documentService;
    private final CandidatService candidatService;

    public DossierService() {
        this.dossierRepository = new DossierRepository();
        this.documentService = new DocumentService();
        this.candidatService = new CandidatService(this);
    }

    public DossierService(CandidatService candidatService) {
        this.dossierRepository = new DossierRepository();
        this.documentService = new DocumentService();
        this.candidatService = candidatService;
    }

    public Optional<Dossier> getDossierById(Long id) {
        return dossierRepository.findById(id);
    }

    public List<Dossier> getAllDossiers() {
        return dossierRepository.findAll();
    }

    public List<Document> getDocumentsByDossierId(Long dossierId) {
        Optional<Dossier> dossierOpt = dossierRepository.findById(dossierId);
        List<Document> documents = new ArrayList<>();
        for(Map.Entry<TypeDocument, TreeSet<Document>> entry : dossierOpt.get().getDocuments().entrySet()) {
            documents.addAll(entry.getValue());
        }
        return documents;
    }

    public Optional<Dossier> getDossierByCandidat(String candidatCin) {
        long candidatId = candidatService.CinToId(candidatCin);
        if (candidatId!=-1 && candidatService.getCandidatById(candidatId).isPresent()) {
            Dossier dossier = dossierRepository.findByCandidatId(candidatId).orElse(null);
            if (dossier != null) {
                return Optional.of(dossier);
            }
            else {

                if(creerDossier(new Dossier(), candidatId))
                    return dossierRepository.findByCandidatId(candidatId);
            }
            }
        return Optional.empty();
    }

    public boolean creerDossier(Dossier dossier, Long candidatId) {
        return dossierRepository.save(dossier, candidatId);
    }

    //this method could be used when modifying the type of a document and
    //in case it exists in the dossier, it should can be overwritten
    public boolean containsType(Dossier dossier,TypeDocument type) {
        return dossier.getDocuments().containsKey(type);
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

    public Map<TypeDocument, TreeSet<Document>> getDocumentsByDossier(Long dossierId) {
        Optional<Dossier> dossierOpt = dossierRepository.findById(dossierId);
        return dossierOpt.map(Dossier::getDocuments).orElse(null);
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
    public boolean dossierContientDocument(Long dossierId, TypeDocument documentType) {
        return dossierRepository.findById(dossierId)
                .map(dossier -> dossier.getDocuments().containsKey(documentType))
                .orElse(false);
    }


    public boolean dossierExiste(Long dossierId) {
        return dossierRepository.findById(dossierId).isPresent();
    }

    public boolean candidatPossedeUnDossier(Long candidatId) {
        return dossierRepository.findByCandidatId(candidatId).isPresent();
    }
    //The commented methods below may concern the vehicule management part of the application
    /*
    public boolean verifierExpirationDocuments(Long dossierId) {
        List<Document> documents = documentService.getDocumentsByDossierId(dossierId);
        LocalDate today = LocalDate.now();

        for (Document doc : documents) {
            if (doc.getDateExpiration() != null &&
                doc.getDateExpiration().toLocalDate().isBefore(today)) {
                return false;
            }
        }
        return true;
    }

    public Map<String, List<Document>> getDocumentsExpirantBientot(Long dossierId, int joursAvantExpiration) {
        Map<String, List<Document>> documentsParType = new HashMap<>();
        List<Document> documents = documentService.getDocumentsByDossierId(dossierId);
        LocalDate dateLimit = LocalDate.now().plusDays(joursAvantExpiration);

        for (Document doc : documents) {
            if (doc.getDateExpiration() != null &&
                doc.getDateExpiration().toLocalDate().isBefore(dateLimit)) {
                documentsParType
                    .computeIfAbsent(doc.getTypeDocument(), k -> new ArrayList<>())
                    .add(doc);
            }
        }
        return documentsParType;
    }
    */
    public String genererRapportEtatDossier(Long dossierId) {
        Optional<Dossier> dossierOpt = getDossierById(dossierId);
        if (dossierOpt.isEmpty()) {
            return "Dossier non trouvé";
        }

        StringBuilder rapport = new StringBuilder();
        rapport.append("État du dossier #").append(dossierId).append("\n\n");

        Map<TypeDocument, TreeSet<Document>> documents = dossierOpt.get().getDocuments();
        List<String> typesObligatoires = List.of("CIN", "PERMIS", "CERTIFICAT_MEDICAL");

        // Vérifier documents obligatoires
        rapport.append("Documents obligatoires:\n");
        for (String type : typesObligatoires) {
            rapport.append("- ").append(type).append(": ");
            if (documents.containsKey(type) && !documents.get(type).isEmpty()) {
                rapport.append("✓ Présent\n");
            } else {
                rapport.append("✗ Manquant\n");
            }
        }
    /*
        // Vérifier documents expirés
        rapport.append("\nDocuments expirés:\n");
        for (Map.Entry<String, TreeSet<Document>> entry : documents.entrySet()) {
            for (Document doc : entry.getValue()) {
                if (doc.getDateExpiration() != null &&
                    doc.getDateExpiration().toLocalDate().isBefore(LocalDate.now())) {
                    rapport.append("- ").append(doc.getTypeDocument())
                          .append(" (Expiré le: ").append(doc.getDateExpiration().toLocalDate())
                          .append(")\n");
                }
            }
        }

   */

        return rapport.toString();
    }
}