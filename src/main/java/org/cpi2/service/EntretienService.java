package org.cpi2.service;

import org.cpi2.entities.Entretien;
import org.cpi2.entities.Vehicule;
import org.cpi2.repository.EntretienRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Service class for managing vehicle maintenance records
 */
public class EntretienService {

    private final EntretienRepository entretienRepository;
    private final VehiculeService vehiculeService;
    /**
     * Constructor to initialize the EntretienRepository
     */
    public EntretienService() {
        this.entretienRepository = new EntretienRepository();
        this.vehiculeService = new VehiculeService();
    }

    /**
     * Get a maintenance record by its ID
     * @param id The entretien ID
     * @return The entretien if found, null otherwise
     */
    public Entretien getEntretienById(int id) {
        return entretienRepository.findById(id);
    }

    public List<Entretien> getEntretienByVehiculeImm(String imm) {
        return entretienRepository.findByVehiculeImm(imm);
    }
    /**
     * Get all maintenance records
     * @return List of all entretiens
     */
    public List<Entretien> getAllEntretiens() {
        return entretienRepository.findAll();
    }

    /**
     * Get all maintenance records for a specific vehicle
     * @param vehiculeId The vehicle ID
     * @return List of entretiens for the vehicle
     */
    public List<Entretien> getEntretiensByVehiculeId(int vehiculeId) {
        return entretienRepository.findByVehiculeId(vehiculeId);
    }

    /**
     * Get maintenance records by date range
     * @param startDate Start date of the range
     * @param endDate End date of the range
     * @return List of entretiens within the date range
     */
    public List<Entretien> getEntretiensByDateRange(LocalDate startDate, LocalDate endDate) {
        return entretienRepository.findByDateRange(startDate, endDate);
    }

    /**
     * Get upcoming maintenance records
     * @return List of upcoming maintenance records
     */
    public List<Entretien> getUpcomingMaintenances() {
        return entretienRepository.findUpcomingMaintenance();
    }

    /**
     * Get maintenance records by type
     * @param typeEntretien The type of maintenance
     * @return List of entretiens of the specified type
     */
    public List<Entretien> getEntretiensByType(String typeEntretien) {
        return entretienRepository.findByType(typeEntretien);
    }
    public List<Entretien> hasPendingEntretiens(long vehiculeId) {
        return entretienRepository.findByVehiculeId(vehiculeId).stream()
                .filter(entretien -> entretien.getDateProchainEntretien() != null && !entretien.isDone())
                .toList();
    }

    /**
     * Create a new maintenance record
     * @param entretien The entretien to create
     * @return The created entretien with generated ID
     */
    public boolean createEntretien(Entretien entretien) {
        // Set creation date if not already set
        if (entretien.getCreatedAt() == null) {
            entretien.setCreatedAt(LocalDate.now());
        }
        if(entretien.isDone() && hasPendingEntretiens(entretien.getVehiculeId()).stream().anyMatch(entretien1 -> entretien1.getTypeEntretien().equals(entretien.getTypeEntretien()))) {
            for (Entretien pendingEntretien : hasPendingEntretiens(entretien.getVehiculeId())) {
                if (pendingEntretien.getTypeEntretien().equals(entretien.getTypeEntretien())) {
                    entretienRepository.deleteById(pendingEntretien.getId());
                }
            }
        }
        entretien.setMaintenance(isMaitenance(entretien.getTypeEntretien()));
        entretien.setKilometrageActuel(vehiculeService.getVehiculeById(entretien.getVehiculeId()).get().getKilometrageTotal());

        if(entretienRepository.save(entretien)&& entretien.isDone()){
                return scheduleNextMaintenance(entretien.getVehiculeId(), entretien.getTypeEntretien(),
                        entretien.getKilometrageActuel(), entretien.getDateEntretien(),entretien.getCout());
        } else {
            return false;
        }
    }

    /**
     * Update an existing maintenance record
     * @param entretien The entretien to update
     * @return true if updated successfully, false otherwise
     */
    public boolean updateEntretien(Entretien entretien) {
        // Check if the entretien exists
        Entretien existingEntretien = entretienRepository.findById(entretien.getId());
        if (existingEntretien == null) {
            return false;
        }

        return entretienRepository.update(entretien);
    }

    /**
     * Delete a maintenance record by ID
     * @param id The ID of the entretien to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteEntretien(int id) {
        return entretienRepository.deleteById(id);
    }

    /**
     * Get overdue maintenance records
     * @return List of overdue maintenance records
     */
    public List<Entretien> getOverdueMaintenances() {
        return entretienRepository.findOverdueMaintenance();
    }

    /**
     * Schedule next maintenance based on current maintenance
     * @param vehiculeId The vehicle ID
     * @param typeEntretien The type of maintenance
     * @param kilometrageActuel The current mileage
     * @param dateEntretien The date of maintenance
     * @return The newly created maintenance record for the next maintenance
     */
    public boolean scheduleNextMaintenance(long vehiculeId, String typeEntretien,
                                             int kilometrageActuel, LocalDate dateEntretien,double cout) {
        LocalDate nextMaintenanceDate;
        if(isMaitenance(typeEntretien)) {
            nextMaintenanceDate = dateEntretien.plusMonths(6);
        }
        else {
            nextMaintenanceDate = dateEntretien.plusMonths(12);
        }

        Vehicule vehicule = vehiculeService.getVehiculeById(vehiculeId).orElseThrow();

        int estimatedKilometrage = kilometrageActuel + vehiculeService.getNextKilometrageByTypeVehicule(vehicule.getTypeVehicule());



        Entretien nextEntretien = new Entretien();
        nextEntretien.setVehiculeId(vehiculeId);
        nextEntretien.setTypeEntretien(typeEntretien);
        nextEntretien.setDateEntretien(nextMaintenanceDate);
        nextEntretien.setKilometrageActuel(estimatedKilometrage);
        nextEntretien.setCout(cout);
        nextEntretien.setMaintenance(true);
        nextEntretien.setDescription("Planifié: " + typeEntretien);

        if(entretienRepository.save(nextEntretien)) {
            if(vehicule.getDateProchainEntretien()!=null && vehicule.getDateProchainEntretien().isAfter(nextMaintenanceDate)){
                vehicule.setDateProchainEntretien(nextMaintenanceDate);
                vehiculeService.modifierVehicule(vehicule,vehicule);
            }
            return vehiculeService.updateNextKilometrage(vehiculeId, estimatedKilometrage);
        } else {
            return false;
        }
    }

    /**
     * Get maintenance statistics for a vehicle
     * @param vehiculeId The vehicle ID
     * @return Array containing [count of maintenances, total cost]
     */
    public Object[] getMaintenanceStatistics(int vehiculeId) {
        int count = entretienRepository.countByVehiculeId(vehiculeId);
        double totalCost = entretienRepository.getTotalCostByVehiculeId(vehiculeId);

        return new Object[]{count, totalCost};
    }

    /**
     * Find maintenance records that exceed a cost threshold
     * @param costThreshold The cost threshold
     * @return List of entretiens with cost greater than or equal to the threshold
     */
    public List<Entretien> getExpensiveMaintenances(double costThreshold) {
        return entretienRepository.findByCostGreaterThan(costThreshold);
    }

    /**
     * Check if a vehicle needs maintenance soon
     * @param vehiculeId The vehicle ID
     * @return true if maintenance is needed soon, false otherwise
     */
    public boolean isMaintenanceNeededSoon(int vehiculeId) {
        List<Entretien> maintenances = entretienRepository.findByVehiculeId(vehiculeId);

        LocalDate now = LocalDate.now();
        LocalDate oneMonthLater = now.plusMonths(1);

        for (Entretien entretien : maintenances) {
            // If there's a planned maintenance within the next month
            if (entretien.isMaintenance() &&
                    entretien.getDateProchainEntretien() != null &&
                    !entretien.getDateProchainEntretien().isBefore(now) &&
                    !entretien.getDateProchainEntretien().isAfter(oneMonthLater)) {
                return true;
            }
        }

        return false;
    }
    public List<Entretien> getWaitingEntretiens() {
        return entretienRepository.findAll().stream()
                .filter(entretien -> entretien.getDateProchainEntretien() == null && !entretien.isDone())
                .toList();
    }
    public boolean isMaitenance(String entretien) {
        return switch (entretien) {
            case "Vignette", "Assurance", "Lavage" -> false;
            case "Visite Technique", "Vidange", "Autre Maitenance" -> true;
            default -> throw new IllegalArgumentException("Invalid entretien type: " + entretien);
        };
    }
}