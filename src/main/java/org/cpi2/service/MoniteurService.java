package org.cpi2.service;

import org.cpi2.entities.Moniteur;
import org.cpi2.entities.RendezVous;
import org.cpi2.entities.TypePermis;
import org.cpi2.repository.MoniteurRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class MoniteurService {
    private static final Logger LOGGER = Logger.getLogger(MoniteurService.class.getName());
    private final MoniteurRepository moniteurRepository;

    public MoniteurService() {
        this.moniteurRepository = new MoniteurRepository();
    }

    public List<Moniteur> getAllMoniteurs() {
        return moniteurRepository.findAll();
    }

    public Optional<Moniteur> getMoniteurById(Long id) {
        return moniteurRepository.findById(id);
    }

    public boolean addMoniteur(Moniteur moniteur) {
        try {
            // Validate required fields
            if (moniteur.getNom() == null || moniteur.getNom().isEmpty() ||
                moniteur.getPrenom() == null || moniteur.getPrenom().isEmpty() ||
                moniteur.getCin() == null || moniteur.getCin().isEmpty() ||
                moniteur.getAdresse() == null || moniteur.getAdresse().isEmpty() ||
                moniteur.getTelephone() == null || moniteur.getTelephone().isEmpty() ||
                moniteur.getEmail() == null || moniteur.getEmail().isEmpty() ||
                moniteur.getDateEmbauche() == null ||
                moniteur.getSpecialites() == null || moniteur.getSpecialites().isEmpty()) {
                LOGGER.warning("Required fields are missing for moniteur");
                return false;
            }

            // Check if CIN already exists
            if (moniteurRepository.findByCin(moniteur.getCin()).isPresent()) {
                LOGGER.warning("CIN already exists: " + moniteur.getCin());
                return false;
            }

            return moniteurRepository.save(moniteur);
        } catch (Exception e) {
            LOGGER.severe("Error adding moniteur: " + e.getMessage());
            return false;
        }
    }

    public boolean updateMoniteur(Moniteur moniteur) {
        try {
            // Validate required fields
            if (moniteur.getNom() == null || moniteur.getNom().isEmpty() ||
                moniteur.getPrenom() == null || moniteur.getPrenom().isEmpty() ||
                moniteur.getCin() == null || moniteur.getCin().isEmpty() ||
                moniteur.getAdresse() == null || moniteur.getAdresse().isEmpty() ||
                moniteur.getTelephone() == null || moniteur.getTelephone().isEmpty() ||
                moniteur.getEmail() == null || moniteur.getEmail().isEmpty() ||
                moniteur.getDateEmbauche() == null ||
                moniteur.getSpecialites() == null || moniteur.getSpecialites().isEmpty()) {
                LOGGER.warning("Required fields are missing for moniteur");
                return false;
            }

            // Check if CIN exists for another moniteur
            Optional<Moniteur> existingMoniteur = moniteurRepository.findByCin(moniteur.getCin());
            if (existingMoniteur.isPresent() && !existingMoniteur.get().getId().equals(moniteur.getId())) {
                LOGGER.warning("CIN already exists for another moniteur: " + moniteur.getCin());
                return false;
            }

            return moniteurRepository.update(moniteur);
        } catch (Exception e) {
            LOGGER.severe("Error updating moniteur: " + e.getMessage());
            return false;
        }
    }

    public List<Moniteur> findMoniteursBySpeciality(TypePermis typePermis) {
        return moniteurRepository.findAll().stream()
                .filter(moniteur -> moniteur.getSpecialites().contains(typePermis))
                .collect(Collectors.toList());
    }

    public List<Moniteur> findAvailableMoniteurs(LocalDateTime dateTime) {
        return moniteurRepository.findAll().stream()
                .filter(moniteur -> !moniteur.getEmploiDuTemps().containsKey(dateTime))
                .collect(Collectors.toList());
    }

    public List<Moniteur> findAvailableMoniteursBySpeciality(LocalDateTime dateTime, TypePermis typePermis) {
        return moniteurRepository.findAll().stream()
                .filter(moniteur -> !moniteur.getEmploiDuTemps().containsKey(dateTime))
                .filter(moniteur -> moniteur.getSpecialites().contains(typePermis))
                .collect(Collectors.toList());
    }

    public boolean addSpeciality(Moniteur moniteur, TypePermis typePermis) {
        try {
            Set<TypePermis> specialites = moniteur.getSpecialites();
            specialites.add(typePermis);
            moniteur.setSpecialites(specialites);
            return moniteurRepository.update(moniteur);
        } catch (Exception e) {
            LOGGER.severe("Error adding speciality: " + e.getMessage());
            return false;
        }
    }

    public boolean removeSpeciality(Moniteur moniteur, TypePermis typePermis) {
        try {
            Set<TypePermis> specialites = moniteur.getSpecialites();
            specialites.remove(typePermis);
            moniteur.setSpecialites(specialites);
            return moniteurRepository.update(moniteur);
        } catch (Exception e) {
            LOGGER.severe("Error removing speciality: " + e.getMessage());
            return false;
        }
    }

    public boolean scheduleRendezVous(Moniteur moniteur, LocalDateTime dateTime, RendezVous rendezVous) {
        try {
            if (moniteur.getEmploiDuTemps().containsKey(dateTime)) {
                LOGGER.warning("Time slot already occupied for moniteur: " + moniteur.getId());
                return false;
            }

            Map<LocalDateTime, RendezVous> emploiDuTemps = moniteur.getEmploiDuTemps();
            emploiDuTemps.put(dateTime, rendezVous);
            moniteur.setEmploiDuTemps(emploiDuTemps);
            return moniteurRepository.update(moniteur);
        } catch (Exception e) {
            LOGGER.severe("Error scheduling rendez-vous: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelRendezVous(Moniteur moniteur, LocalDateTime dateTime) {
        try {
            if (!moniteur.getEmploiDuTemps().containsKey(dateTime)) {
                LOGGER.warning("No rendez-vous found for time: " + dateTime);
                return false;
            }

            Map<LocalDateTime, RendezVous> emploiDuTemps = moniteur.getEmploiDuTemps();
            emploiDuTemps.remove(dateTime);
            moniteur.setEmploiDuTemps(emploiDuTemps);
            return moniteurRepository.update(moniteur);
        } catch (Exception e) {
            LOGGER.severe("Error canceling rendez-vous: " + e.getMessage());
            return false;
        }
    }

    public Map<LocalDateTime, RendezVous> getMoniteurSchedule(Moniteur moniteur) {
        return moniteur.getEmploiDuTemps();
    }

    public List<Moniteur> findMoniteursHiredAfter(LocalDate date) {
        return moniteurRepository.findAll().stream()
                .filter(moniteur -> moniteur.getDateEmbauche().isAfter(date))
                .collect(Collectors.toList());
    }

    public Moniteur findByCin(String cin) {
        return moniteurRepository.findByCin(cin).orElse(null);
    }
}