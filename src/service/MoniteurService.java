package service;

import entitties.Moniteur;
import entitties.RendezVous;
import entitties.TypePermis;
import repository.MoniteurRepository;

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
        return moniteurRepository.save(moniteur);
    }

    public boolean updateMoniteur(Moniteur moniteur) {
        return moniteurRepository.update(moniteur);
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
        Set<TypePermis> specialites = moniteur.getSpecialites();
        specialites.add(typePermis);
        moniteur.setSpecialites(specialites);
        return moniteurRepository.update(moniteur);
    }

    public boolean removeSpeciality(Moniteur moniteur, TypePermis typePermis) {
        Set<TypePermis> specialites = moniteur.getSpecialites();
        specialites.remove(typePermis);
        moniteur.setSpecialites(specialites);
        return moniteurRepository.update(moniteur);
    }

    public boolean scheduleRendezVous(Moniteur moniteur, LocalDateTime dateTime, RendezVous rendezVous) {
        if (moniteur.getEmploiDuTemps().containsKey(dateTime)) {
            return false;
        }

        Map<LocalDateTime, RendezVous> emploiDuTemps = moniteur.getEmploiDuTemps();
        emploiDuTemps.put(dateTime, rendezVous);
        moniteur.setEmploiDuTemps(emploiDuTemps);
        return true;
    }

    public boolean cancelRendezVous(Moniteur moniteur, LocalDateTime dateTime) {
        if (!moniteur.getEmploiDuTemps().containsKey(dateTime)) {
            return false;
        }

        Map<LocalDateTime, RendezVous> emploiDuTemps = moniteur.getEmploiDuTemps();
        emploiDuTemps.remove(dateTime);
        moniteur.setEmploiDuTemps(emploiDuTemps);
        return true;
    }

    public Map<LocalDateTime, RendezVous> getMoniteurSchedule(Moniteur moniteur) {
        return moniteur.getEmploiDuTemps();
    }

    public List<Moniteur> findMoniteursHiredAfter(LocalDate date) {
        return moniteurRepository.findAll().stream()
                .filter(moniteur -> moniteur.getDateEmbauche().isAfter(date))
                .collect(Collectors.toList());
    }

    public long countMoniteursBySpeciality(TypePermis typePermis) {
        return moniteurRepository.findAll().stream()
                .filter(moniteur -> moniteur.getSpecialites().contains(typePermis))
                .count();
    }
}