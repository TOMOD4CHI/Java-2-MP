package org.cpi2.service;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.repository.TypeExamenRepository;

import java.util.List;
import java.util.stream.Collectors;

public class TypeExamenService {
    private final TypeExamenRepository typeExamenRepository;

    public TypeExamenService() {
        this.typeExamenRepository = new TypeExamenRepository();
    }

    public List<String> getAllTypeExamens() {
        return typeExamenRepository.findAll();
    }


    public double getExamenCostByLibelle(String libelle) throws DataNotFound {
        return typeExamenRepository.findCoutByLibelle(libelle);
    }


}