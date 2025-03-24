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

    public String getTypeExamenById(Integer id) throws DataNotFound {
        return typeExamenRepository.findById(id)
                .orElseThrow(() -> new DataNotFound("Type examen not found"));
    }

    public List<String> getAllTypeExamens() {
        return typeExamenRepository.findAll();
    }

    public Integer getTypeExamenIdByLibelle(String libelle) throws DataNotFound {
        return typeExamenRepository.findByLibelle(libelle)
                .orElseThrow(() -> new DataNotFound("Type examen not found"));
    }

    public double getExamenCostById(Integer id) throws DataNotFound {
        return typeExamenRepository.findCoutById(id);
    }

    public double getExamenCostByLibelle(String libelle) throws DataNotFound {
        return typeExamenRepository.findCoutByLibelle(libelle);
    }

    public List<String> getMostExpensiveExamTypes(int limit) {
        return getAllTypeExamens().stream()
                .sorted((exam1, exam2) ->
                        Double.compare(
                                getExamenCostByLibelle(exam2),
                                getExamenCostByLibelle(exam1)
                        )
                )
                .limit(limit)
                .collect(Collectors.toList());
    }

    public double calculateTotalExamCosts(List<String> examTypes) {
        return examTypes.stream()
                .mapToDouble(this::getExamenCostByLibelle)
                .sum();
    }
}