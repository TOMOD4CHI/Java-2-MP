package org.cpi2.repository;

import org.cpi2.entitties.Examen;

import java.util.Optional;

public class ExamenRepository extends  BaseRepository<Examen>{
    public Optional<Examen> findById(int idExamen) {
        return Optional.empty();
    }
}
