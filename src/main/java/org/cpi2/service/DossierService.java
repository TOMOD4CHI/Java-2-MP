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

    public boolean dossierExiste(Long dossierId) {
        return dossierRepository.findById(dossierId).isPresent();
    }

    public boolean candidatPossedeUnDossier(Long candidatId) {
        return dossierRepository.findByCandidatId(candidatId).isPresent();
    }
}