package org.cpi2.service;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.repository.TypePermisRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TypePermisService {
    private final TypePermisRepository typePermisRepository;

    public TypePermisService() {
        this.typePermisRepository = new TypePermisRepository();
    }

    public String getTypePermisById(Integer id) throws DataNotFound {
        return typePermisRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("Type permis not found"));
    }

    public List<String> getAllTypePermis() {
        return typePermisRepository.findAll();
    }

    public Integer getTypePermisIdByLibelle(String libelle) throws DataNotFound {
        return typePermisRepository.findByLibelle(libelle)
                .orElseThrow(() -> new DataNotFound("Type permis not found"));
    }

    public Integer getTypePermisIdByCode(String code) throws DataNotFound {
        return typePermisRepository.findByCode(code)
                .orElseThrow(() -> new DataNotFound("Type permis not found"));
    }
}