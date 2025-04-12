package org.cpi2.service;

import org.cpi2.entities.Entretien;
import org.cpi2.entities.Vehicule;
import org.cpi2.repository.EntretienRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class EntretienService {

    private final EntretienRepository entretienRepository;
    private final VehiculeService vehiculeService;
    public EntretienService() {
        this.entretienRepository = new EntretienRepository();
        this.vehiculeService = new VehiculeService();
    }

    public Entretien getEntretienById(int id) {
        return entretienRepository.findById(id);
    }

    public List<Entretien> getEntretienByVehiculeImm(String imm) {
        return entretienRepository.findByVehiculeImm(imm);
    }
    public List<Entretien> getAllEntretiens() {
        return entretienRepository.findAll();
    }

    public List<Entretien> getEntretiensByVehiculeId(int vehiculeId) {
        return entretienRepository.findByVehiculeId(vehiculeId);
    }

    public List<Entretien> getEntretiensByDateRange(LocalDate startDate, LocalDate endDate) {
        return entretienRepository.findByDateRange(startDate, endDate);
    }

    public List<Entretien> getUpcomingMaintenances() {
        return entretienRepository.findUpcomingMaintenance();
    }

    public List<Entretien> getEntretiensByType(String typeEntretien) {
        return entretienRepository.findByType(typeEntretien);
    }
    public List<Entretien> hasPendingEntretiens(long vehiculeId) {
        return entretienRepository.findByVehiculeId(vehiculeId).stream()
                .filter(entretien -> entretien.getDateProchainEntretien() != null && !entretien.isDone())
                .toList();
    }

    public boolean createEntretien(Entretien entretien) {
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

        if(entretienRepository.save(entretien)){
        if(entretien.isDone()){
                return scheduleNextMaintenance(entretien.getVehiculeId(), entretien.getTypeEntretien(),
                        entretien.getKilometrageActuel(), entretien.getDateEntretien(),entretien.getCout());

        } else{
            Vehicule vehicule = vehiculeService.getVehiculeById(entretien.getVehiculeId()).orElseThrow();
            if(Objects.equals(entretien.getTypeEntretien(), "Visite Technique")){
                if (vehicule.getDateProchaineVisiteTechnique() == null || vehicule.getDateProchaineVisiteTechnique().isAfter(entretien.getDateEntretien())) {
                    vehicule.setDateProchainEntretien(entretien.getDateEntretien());
                }
            }
            else if(Objects.equals(entretien.getTypeEntretien(), "Assurance")){
                if (vehicule.getDateExpirationAssurance() == null || vehicule.getDateExpirationAssurance().isAfter(entretien.getDateEntretien())) {
                    vehicule.setDateExpirationAssurance(entretien.getDateEntretien());
                }

            }
            else {
                if (vehicule.getDateProchainEntretien() == null || vehicule.getDateProchainEntretien().isAfter(entretien.getDateEntretien())) {
                    vehicule.setDateProchainEntretien(entretien.getDateEntretien());
                }
            }
            vehiculeService.modifierVehicule(vehicule, vehicule);
            return true;

        } }else {

            return false;
        }
    }
    public boolean markEntretienAsDone(int id) {
        Entretien entretien = entretienRepository.findById(id);
        if (entretien != null) {
            entretien.setStatut(true);
            entretien.setDateEntretien(LocalDate.now());
            entretien.setKilometrageActuel(vehiculeService.getVehiculeById(entretien.getVehiculeId()).get().getKilometrageTotal());
            vehiculeService.updateDernierEntretien(entretien.getVehiculeId(), entretien.getDateEntretien());
            return updateEntretien(entretien);
        }
        return false;
    }


    public boolean updateEntretien(Entretien entretien) {
        
        Entretien existingEntretien = entretienRepository.findById(entretien.getId());
        if (existingEntretien == null) {
            return false;
        }
        if(entretienRepository.update(entretien) && entretien.isDone()){
            System.out.println("i'm here");
            return scheduleNextMaintenance(entretien.getVehiculeId(), entretien.getTypeEntretien(),
                    entretien.getKilometrageActuel(), entretien.getDateEntretien(),entretien.getCout());
        }
        return false;
    }

    public boolean deleteEntretien(int id) {
        return entretienRepository.deleteById(id);
    }

    public List<Entretien> getOverdueMaintenances() {
        return entretienRepository.findOverdueMaintenance();
    }

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
        nextEntretien.setMaintenance(isMaitenance(typeEntretien));
        nextEntretien.setDescription("Planifié: " + typeEntretien);
        nextEntretien.setStatut(false);

        if(vehicule.getKilometrageProchainEntretien() == 0 || vehicule.getKilometrageProchainEntretien() > estimatedKilometrage) {
            vehicule.setKilometrageProchainEntretien(estimatedKilometrage);
        }

        if(entretienRepository.save(nextEntretien)) {
            if(typeEntretien.equals("Visite Technique")){
                if(vehicule.getDateProchaineVisiteTechnique()==null || vehicule.getDateProchaineVisiteTechnique().isAfter(nextMaintenanceDate)){
                    vehicule.setDateProchaineVisiteTechnique(nextMaintenanceDate);
                }
            }
            else if(typeEntretien.equals("Assurance")){
                if(vehicule.getDateExpirationAssurance()==null || vehicule.getDateExpirationAssurance().isAfter(nextMaintenanceDate)){
                    vehicule.setDateExpirationAssurance(nextMaintenanceDate);
                }
            }
            else
            {
                if(vehicule.getDateProchainEntretien()==null || vehicule.getDateProchainEntretien().isAfter(nextMaintenanceDate)){
                    vehicule.setDateProchainEntretien(nextMaintenanceDate);
                }
            }
            return vehiculeService.modifierVehicule(vehicule,vehicule);
        }else {
            return false;
        }
    }
    public boolean isMaitenance(String entretien) {
        return switch (entretien) {
            case "Vignette", "Assurance", "Lavage" -> false;
            case "Visite Technique", "Vidange", "Autre Maitenance" -> true;
            default -> throw new IllegalArgumentException("Invalid entretien type: " + entretien);
        };
    }
    public double getTotalCost(LocalDate startDate, LocalDate endDate) {
        return entretienRepository.findAll().stream()
                .filter(entretien -> entretien.isDone() &&
                        entretien.getDateEntretien() != null &&
                        entretien.getDateEntretien().isAfter(startDate) &&
                        entretien.getDateEntretien().isBefore(endDate))
                .mapToDouble(Entretien::getCout)
                .sum();
    }

}