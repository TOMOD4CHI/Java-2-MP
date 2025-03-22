package org.cpi2.service;

import org.cpi2.entitties.Entretien;
import org.cpi2.entitties.TypePermis;
import org.cpi2.entitties.Vehicule;
import org.cpi2.repository.VehiculeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Logger;

public class VehiculeService {
    private static final Logger LOGGER = Logger.getLogger(VehiculeService.class.getName());
    private final VehiculeRepository vehiculeRepository;

    public VehiculeService() {
        this.vehiculeRepository = new VehiculeRepository();
    }

    public List<Vehicule> getAllVehicules() {
        return vehiculeRepository.findAll();
    }

    public Optional<Vehicule> getVehiculeById(Long id) {
        return vehiculeRepository.findById(id);
    }

    public Optional<Vehicule> getVehiculeByImmatriculation(String immatriculation) {
        return vehiculeRepository.findByImmatriculation(immatriculation);
    }

    public List<Vehicule> getVehiculesByTypePermis(TypePermis typePermis) {
        return vehiculeRepository.findAllByTypePermis(typePermis);
    }

    public boolean enregistrerVehicule(Vehicule vehicule) {
        if (vehicule.getImmatriculation() == null || vehicule.getImmatriculation().isEmpty() ||
            vehicule.getMarque() == null || vehicule.getMarque().isEmpty() ||
            vehicule.getModele() == null || vehicule.getModele().isEmpty() ||
            vehicule.getTypePermis() == null ||
            vehicule.getDateMiseEnService() == null) {
            LOGGER.warning("Missing required fields for vehicule");
            return false;
        }

        if (vehiculeExisteParImmatriculation(vehicule.getImmatriculation())) {
            LOGGER.warning("Vehicle with immatriculation " + vehicule.getImmatriculation() + " already exists");
            return false;
        }

        return vehiculeRepository.save(vehicule);
    }

    public boolean mettreAJourVehicule(Vehicule vehicule) {
        if (vehicule.getId() == null ||
            vehicule.getImmatriculation() == null || vehicule.getImmatriculation().isEmpty() ||
            vehicule.getMarque() == null || vehicule.getMarque().isEmpty() ||
            vehicule.getModele() == null || vehicule.getModele().isEmpty() ||
            vehicule.getTypePermis() == null ||
            vehicule.getDateMiseEnService() == null) {
            LOGGER.warning("Missing required fields for vehicule update");
            return false;
        }

        Optional<Vehicule> existingVehicule = vehiculeRepository.findByImmatriculation(vehicule.getImmatriculation());
        if (existingVehicule.isPresent() && !existingVehicule.get().getId().equals(vehicule.getId())) {
            LOGGER.warning("Another vehicle with immatriculation " + vehicule.getImmatriculation() + " already exists");
            return false;
        }

        return vehiculeRepository.update(vehicule);
    }

    public boolean supprimerVehicule(Long id) {
        return vehiculeRepository.delete(id);
    }

    public boolean enregistrerEntretien(Long vehiculeId, Entretien entretien) {
        if (entretien.getDateEntretien() == null ||
            entretien.getTypeEntretien() == null || entretien.getTypeEntretien().isEmpty()) {
            LOGGER.warning("Missing required fields for entretien");
            return false;
        }
        
        return vehiculeRepository.saveEntretien(vehiculeId, entretien);
    }

    public List<Entretien> getEntretiensVehicule(Vehicule vehicule) {
        return vehiculeRepository.findEntretiens(vehicule);
    }

    public boolean verifierEntretienNecessaire(Vehicule vehicule) {
        return vehicule.getKilometrageTotal() >= vehicule.getKilometrageAvantEntretien();
    }

    public List<Vehicule> getVehiculesNecessitantEntretien() {
        return vehiculeRepository.findAll().stream()
                .filter(this::verifierEntretienNecessaire)
                .collect(Collectors.toList());
    }

    public List<Entretien> getEntretiensRecents(Vehicule vehicule, int nombreMois) {
        LocalDate dateLimit = LocalDate.now().minusMonths(nombreMois);

        return vehiculeRepository.findEntretiens(vehicule).stream()
                .filter(entretien -> entretien.getDateEntretien().isAfter(dateLimit))
                .collect(Collectors.toList());
    }

    public double calculerCoutEntretienTotal(Vehicule vehicule, int nombreMois) {
        return getEntretiensRecents(vehicule, nombreMois).stream()
                .mapToDouble(Entretien::getCout)
                .sum();
    }

    public double calculerCoutEntretienMoyen(Vehicule vehicule, int nombreMois) {
        List<Entretien> entretiens = getEntretiensRecents(vehicule, nombreMois);
        if (entretiens.isEmpty()) return 0.0;

        double totalCost = calculerCoutEntretienTotal(vehicule, nombreMois);
        return totalCost / entretiens.size();
    }

    public boolean vehiculeExisteParImmatriculation(String immatriculation) {
        return vehiculeRepository.findByImmatriculation(immatriculation).isPresent();
    }

    public boolean verifierDisponibiliteVehicule(Long vehiculeId, LocalDate date) {
        // TODO: Implement proper availability check based on sessions/bookings
        return true;
    }

    public List<Vehicule> getVehiculesDisponibles(TypePermis typePermis, LocalDate date) {
        List<Vehicule> vehicules = getVehiculesByTypePermis(typePermis);

        return vehicules.stream()
                .filter(v -> verifierDisponibiliteVehicule(v.getId(), date))
                .collect(Collectors.toList());
    }
}