package org.cpi2.service;

import org.cpi2.entities.Entretien;
import org.cpi2.entities.TypePermis;
import org.cpi2.entities.Vehicule;
import org.cpi2.repository.VehiculeRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehiculeService {
    private static final Logger LOGGER = Logger.getLogger(VehiculeService.class.getName());
    private final VehiculeRepository vehiculeRepository;

    public VehiculeService() {
        this.vehiculeRepository = new VehiculeRepository();
    }

    public boolean ajouterVehicule(Vehicule vehicule) {
        vehicule.setAnnee(vehicule.getDateMiseEnService().getYear());
        if (!validerVehicule(vehicule)) {
            return false;
        }

        if (vehiculeRepository.findByImmatriculation(vehicule.getImmatriculation()).isPresent()) {
            LOGGER.log(Level.WARNING, "Vehicle with registration " + vehicule.getImmatriculation() + " already exists");
            return false;
        }

        try {
            vehicule.setKilometrageProchainEntretien(getNextKilometrageByTypeVehicule(vehicule.getTypeVehicule()));
            return vehiculeRepository.save(vehicule);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding vehicle", e);
            return false;
        }
    }

    public List<Vehicule> getAllVehicules() {
        return vehiculeRepository.findAll();
    }

    public Optional<Vehicule> getVehiculeById(long id) {
        return vehiculeRepository.findById(id);
    }

    public Optional<Vehicule> getVehiculeByImmatriculation(String immatriculation) {
        return vehiculeRepository.findByImmatriculation(immatriculation);
    }

    public List<Vehicule> findByTypePermis(TypePermis typePermis) {
        List<Vehicule> vehicules = new ArrayList<>();
        for (Vehicule vehicule : getAllVehicules()) {
            if (vehicule.getTypePermis() == typePermis) {
                vehicules.add(vehicule);
            }
        }
        return vehicules;
    }



    public boolean modifierVehicule(Vehicule old,Vehicule vehicule) {
        vehicule.setAnnee(vehicule.getDateMiseEnService().getYear());
        if (!validerVehicule(vehicule)) {
            return false;
        }

        try {
            Optional<Vehicule> existingVehicule = vehiculeRepository.findByImmatriculation(old.getImmatriculation());

            if (!existingVehicule.get().getImmatriculation().equals(vehicule.getImmatriculation())) {
                Optional<Vehicule> byImmatriculation = vehiculeRepository.findByImmatriculation(vehicule.getImmatriculation());
                if (byImmatriculation.isPresent() && byImmatriculation.get().getId() != vehicule.getId()) {
                    LOGGER.log(Level.WARNING, "Vehicle with registration " + vehicule.getImmatriculation() + " already exists");
                    return false;
                }
            }
            return vehiculeRepository.update(old.getId(), vehicule);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle", e);
            return false;
        }
    }

    public boolean supprimerVehicule(int id) {
        try {
            if (vehiculeRepository.findById((long) id).isEmpty()) {
                LOGGER.log(Level.WARNING, "Vehicle with ID " + id + " not found");
                return false;
            }

            return vehiculeRepository.delete((long) id);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error deleting vehicle", e);
            return false;
        }
    }

    public boolean ajouterEntretien(int vehiculeId, Entretien entretien) {
        if (!validerEntretien(entretien)) {
            return false;
        }

        try {
            Optional<Vehicule> vehicule = vehiculeRepository.findById((long) vehiculeId);
            if (vehicule.isEmpty()) {
                LOGGER.log(Level.WARNING, "Vehicle with ID " + vehiculeId + " not found");
                return false;
            }

            entretien.setVehiculeId(vehiculeId);
            boolean savedEntretien = vehiculeRepository.saveEntretien((long)vehiculeId,entretien);
            
            if (savedEntretien != false) {
                Vehicule updatedVehicule = vehicule.get();
                updatedVehicule.setKilometrageTotal(entretien.getKilometrageActuel());
                updatedVehicule.setKilometrageProchainEntretien(entretien.getKilometrageActuel() + 10000);
                updatedVehicule.setDateProchainEntretien(LocalDate.now().plusMonths(6));
                
                return vehiculeRepository.update(updatedVehicule.getId(),updatedVehicule);
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding maintenance record", e);
            return false;
        }
    }

    public List<Entretien> getEntretiens(int vehiculeId) {
        return vehiculeRepository.getEntretiensByVehiculeId((long) vehiculeId);
    }

    private boolean validerVehicule(Vehicule vehicule) {
        if (vehicule == null) {
            LOGGER.log(Level.WARNING, "Vehicle cannot be null");
            return false;
        }

        if (vehicule.getImmatriculation() == null || vehicule.getImmatriculation().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Vehicle registration cannot be null or empty");
            return false;
        }

        if (vehicule.getMarque() == null || vehicule.getMarque().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Vehicle make cannot be null or empty");
            return false;
        }
        if (vehicule.getAnnee() == null || vehicule.getAnnee() < 1886 || vehicule.getAnnee() > LocalDate.now().getYear()) {
            LOGGER.log(Level.WARNING, "Vehicle year cannot be null or invalid");
            return false;
        }
        if (vehicule.getKilometrageTotal() < 0) {
            LOGGER.log(Level.WARNING, "Vehicle total mileage cannot be negative");
            return false;
        }
        if (vehicule.getKilometrageProchainEntretien() < 0) {
            LOGGER.log(Level.WARNING, "Vehicle next maintenance mileage cannot be negative");
            return false;
        }

        if (vehicule.getModele() == null || vehicule.getModele().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Vehicle model cannot be null or empty");
            return false;
        }

        if (vehicule.getTypePermis() == null) {
            LOGGER.log(Level.WARNING, "Vehicle license type cannot be null");
            return false;
        }

        if (vehicule.getDateMiseEnService() == null) {
            LOGGER.log(Level.WARNING, "Vehicle service date cannot be null");
            return false;
        }

        if (vehicule.getDateMiseEnService().isAfter(LocalDate.now())) {
            LOGGER.log(Level.WARNING, "Vehicle service date cannot be in the future");
            return false;
        }

        return true;
    }

    private boolean validerEntretien(Entretien entretien) {
        if (entretien == null) {
            LOGGER.log(Level.WARNING, "Maintenance record cannot be null");
            return false;
        }

        if (entretien.getDateEntretien() == null) {
            LOGGER.log(Level.WARNING, "Maintenance date cannot be null");
            return false;
        }

        if (entretien.getDateEntretien().isAfter(LocalDate.now())) {
            LOGGER.log(Level.WARNING, "Maintenance date cannot be in the future");
            return false;
        }

        if (entretien.getTypeEntretien() == null || entretien.getTypeEntretien().trim().isEmpty()) {
            LOGGER.log(Level.WARNING, "Maintenance type cannot be null or empty");
            return false;
        }

        if (entretien.getKilometrageActuel() < 0) {
            LOGGER.log(Level.WARNING, "Maintenance mileage cannot be negative");
            return false;
        }

        if (entretien.getCout() < 0) {
            LOGGER.log(Level.WARNING, "Maintenance cost cannot be negative");
            return false;
        }

        return true;
    }
    public boolean updateDernierEntretien(long vehiculeId, LocalDate dateEntretien) {
        try {
            Optional<Vehicule> vehicule = vehiculeRepository.findById((long) vehiculeId);
            if (vehicule.isPresent()) {
                Vehicule updatedVehicule = vehicule.get();
                updatedVehicule.setDateDerniereVisiteTechnique(dateEntretien);
                return vehiculeRepository.update(updatedVehicule.getId(),updatedVehicule);
            } else {
                LOGGER.log(Level.WARNING, "Vehicle with ID " + vehiculeId + " not found");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle maintenance date", e);
            return false;
        }
    }
    public boolean updateNextKilometrage(long vehiculeId, int kilometrage) {
        try {
            Optional<Vehicule> vehicule = vehiculeRepository.findById((long) vehiculeId);
            if (vehicule.isPresent()) {
                Vehicule updatedVehicule = vehicule.get();
                updatedVehicule.setKilometrageProchainEntretien(kilometrage);
                return vehiculeRepository.update(updatedVehicule.getId(),updatedVehicule);
            } else {
                LOGGER.log(Level.WARNING, "Vehicle with ID " + vehiculeId + " not found");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle mileage", e);
            return false;
        }
    }

    public boolean updateKilometrage(long vehiculeId, int kilometrage) {
        try {
            Optional<Vehicule> vehicule = vehiculeRepository.findById((long) vehiculeId);
            if (vehicule.isPresent()) {
                Vehicule updatedVehicule = vehicule.get();
                updatedVehicule.setKilometrageTotal(kilometrage);
                return vehiculeRepository.update(updatedVehicule.getId(),updatedVehicule);
            } else {
                LOGGER.log(Level.WARNING, "Vehicle with ID " + vehiculeId + " not found");
                return false;
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle mileage", e);
            return false;
        }
    }

    public int getNextKilometrageByTypeVehicule(String typeVehicule) {
        if (typeVehicule.equalsIgnoreCase("Voiture")) {
            return 10000;
        } else if (typeVehicule.equalsIgnoreCase("Camion")) {
            return 20000;
        } else if (typeVehicule.equalsIgnoreCase("Moto")) {
            return 5000;
        } else {
            return 10000;
        }
    }
}