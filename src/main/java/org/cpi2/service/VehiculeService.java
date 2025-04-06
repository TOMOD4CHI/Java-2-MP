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

/**
 * Service class for vehicle management
 */
public class VehiculeService {
    private static final Logger LOGGER = Logger.getLogger(VehiculeService.class.getName());
    private final VehiculeRepository vehiculeRepository;

    public VehiculeService() {
        this.vehiculeRepository = new VehiculeRepository();
    }

    public boolean ajouterVehicule(Vehicule vehicule) {
        if (!validerVehicule(vehicule)) {
            return false;
        }

        // Check if vehicle with same registration already exists
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

    /**
     * Gets all vehicles
     * @return List of all vehicles
     */
    public List<Vehicule> getAllVehicules() {
        return vehiculeRepository.findAll();
    }

    /**
     * Gets a vehicle by its ID
     * @param id The vehicle ID
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicule> getVehiculeById(long id) {
        return vehiculeRepository.findById(id);
    }

    /**
     * Gets a vehicle by its registration plate
     * @param immatriculation The vehicle registration plate
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicule> getVehiculeByImmatriculation(String immatriculation) {
        return vehiculeRepository.findByImmatriculation(immatriculation);
    }

    /**
     * Gets vehicles by license type
     * @param typePermis The license type
     * @return List of vehicles for the specified license type
     */
    public List<Vehicule> findByTypePermis(TypePermis typePermis) {
        List<Vehicule> vehicules = new ArrayList<>();
        for (Vehicule vehicule : getAllVehicules()) {
            if (vehicule.getTypePermis() == typePermis) {
                vehicules.add(vehicule);
            }
        }
        return vehicules;
    }



    /**
     * Updates an existing vehicle
     * @param vehicule The vehicle to update
     * @return true if successful, false otherwise
     */
    public boolean modifierVehicule(Vehicule old,Vehicule vehicule) {
        if (!validerVehicule(vehicule)) {
            return false;
        }

        try {
            Optional<Vehicule> existingVehicule = vehiculeRepository.findByImmatriculation(old.getImmatriculation());

            // Check if the immatriculation is unique (if changed)
            if (!existingVehicule.get().getImmatriculation().equals(vehicule.getImmatriculation())) {
                Optional<Vehicule> byImmatriculation = vehiculeRepository.findByImmatriculation(vehicule.getImmatriculation());
                if (byImmatriculation.isPresent() && byImmatriculation.get().getId() != vehicule.getId()) {
                    LOGGER.log(Level.WARNING, "Vehicle with registration " + vehicule.getImmatriculation() + " already exists");
                    return false;
                }
            }

            return vehiculeRepository.update(vehicule);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle", e);
            return false;
        }
    }

    /**
     * Deletes a vehicle by its ID
     * @param id The vehicle ID to delete
     * @return true if successful, false otherwise
     */
    public boolean supprimerVehicule(int id) {
        try {
            // Check if vehicle exists
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

    /**
     * Adds a maintenance record for a vehicle
     * @param vehiculeId The vehicle ID
     * @param entretien The maintenance record to add
     * @return true if successful, false otherwise
     */
    public boolean ajouterEntretien(int vehiculeId, Entretien entretien) {
        if (!validerEntretien(entretien)) {
            return false;
        }

        try {
            // Check if vehicle exists
            Optional<Vehicule> vehicule = vehiculeRepository.findById((long) vehiculeId);
            if (vehicule.isEmpty()) {
                LOGGER.log(Level.WARNING, "Vehicle with ID " + vehiculeId + " not found");
                return false;
            }

            entretien.setVehiculeId(vehiculeId);
            boolean savedEntretien = vehiculeRepository.saveEntretien((long)vehiculeId,entretien);
            
            if (savedEntretien != false) {
                // Update vehicle maintenance information
                Vehicule updatedVehicule = vehicule.get();
                updatedVehicule.setKilometrageTotal(entretien.getKilometrageActuel());
                updatedVehicule.setKilometrageProchainEntretien(entretien.getKilometrageActuel() + 10000);
                updatedVehicule.setDateProchainEntretien(LocalDate.now().plusMonths(6));
                
                return vehiculeRepository.update(updatedVehicule);
            }
            
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error adding maintenance record", e);
            return false;
        }
    }

    /**
     * Gets maintenance records for a vehicle
     * @param vehiculeId The vehicle ID
     * @return List of maintenance records
     */
    public List<Entretien> getEntretiens(int vehiculeId) {
        return vehiculeRepository.getEntretiensByVehiculeId((long) vehiculeId);
    }

    /**
     * Validates a vehicle before saving or updating
     * @param vehicule The vehicle to validate
     * @return true if valid, false otherwise
     */
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

    /**
     * Validates a maintenance record before saving
     * @param entretien The maintenance record to validate
     * @return true if valid, false otherwise
     */
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
                return vehiculeRepository.update(updatedVehicule);
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
                return vehiculeRepository.update(updatedVehicule);
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
                return vehiculeRepository.update(updatedVehicule);
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
            return 10000; // Default value for unknown vehicle types
        }
    }
}