package org.cpi2.service;

import org.cpi2.entities.Salle;
import org.cpi2.repository.SalleRepository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SalleService {
    private static final Logger LOGGER = Logger.getLogger(SalleService.class.getName());
    private final SalleRepository salleRepository;

    public SalleService() {
        this.salleRepository = new SalleRepository();
    }

    public List<Salle> getAllSalles() {
        return salleRepository.findAll();
    }

    public Optional<Salle> getSalleById(Long id) {
        return salleRepository.findById(id);
    }

    public Optional<Salle> getSalleByNumero(String numero) {
        return salleRepository.findByNumero(numero);
    }

    public boolean addSalle(Salle salle) {
        try {
            Optional<Salle> existingSalle = salleRepository.findByNumero(salle.getNumero());
            if (existingSalle.isPresent()) {
                LOGGER.log(Level.INFO, "Une salle avec le numéro " + salle.getNumero() + " existe déjà");
                return false;
            }
            
            if (!validateSalle(salle)) {
                LOGGER.log(Level.WARNING, "Données de salle invalides");
                return false;
            }
            
            return salleRepository.save(salle);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'ajout de la salle", e);
            return false;
        }
    }

    public boolean updateSalle(Salle salle) {
        try {
            if (salleRepository.findById(salle.getId()).isEmpty()) {
                LOGGER.log(Level.WARNING, "Impossible de mettre à jour la salle: Salle non trouvée");
                return false;
            }

            Optional<Salle> existingSalle = salleRepository.findByNumero(salle.getNumero());
            if (existingSalle.isPresent() && !existingSalle.get().getId().equals(salle.getId())) {
                LOGGER.log(Level.INFO, "Une autre salle avec le numéro " + salle.getNumero() + " existe déjà");
                return false;
            }

            if (!validateSalle(salle)) {
                LOGGER.log(Level.WARNING, "Données de salle invalides");
                return false;
            }
            
            return salleRepository.update(salle);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la mise à jour de la salle", e);
            return false;
        }
    }

    public boolean deleteSalle(Long id) {
        try {
            if (salleRepository.findById(id).isEmpty()) {
                LOGGER.log(Level.WARNING, "Impossible de supprimer la salle: Salle non trouvée");
                return false;
            }
            
            return salleRepository.delete(id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la suppression de la salle", e);
            return false;
        }
    }

    private boolean validateSalle(Salle salle) {
        if (salle.getNom() == null || salle.getNom().trim().isEmpty()) {
            return false;
        }

        if (salle.getNumero() == null || salle.getNumero().trim().isEmpty()) {
            return false;
        }

        return salle.getCapacite() > 0;
    }
}