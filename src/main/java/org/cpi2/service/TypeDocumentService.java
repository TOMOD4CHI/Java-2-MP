package org.cpi2.service;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.repository.TypeDocumentRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TypeDocumentService {
    private final TypeDocumentRepository typeDocumentRepository;

    public TypeDocumentService() {
        this.typeDocumentRepository = new TypeDocumentRepository();
    }

    public String getTypeDocumentById(Integer id) throws DataNotFound {
        return typeDocumentRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("Type document not found"));
    }

    public List<String> getAllTypeDocuments() {
        return typeDocumentRepository.findAll();
    }

    public Long getTypeDocumentIdByLibelle(String libelle) throws DataNotFound {
        return typeDocumentRepository.findByLibelle(libelle)
                .orElseThrow(() -> new DataNotFound("Type document not found"));
    }
}