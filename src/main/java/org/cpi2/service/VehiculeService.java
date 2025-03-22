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

public class VehiculeService {
    private final VehiculeRepository vehiculeRepository;

    public VehiculeService() {
        this.vehiculeRepository = new VehiculeRepository();
    }

    public Optional<Vehicule> getVehiculeById(Long id) {
        return vehiculeRepository.findById(id);
    }

    public List<Vehicule> getVehiculesByTypePermis(TypePermis typePermis) {
        return vehiculeRepository.findAllByTypePermis(typePermis);
    }

    public boolean enregistrerVehicule(Vehicule vehicule) {
        return vehiculeRepository.save(vehicule);
    }

    public boolean mettreAJourVehicule(Vehicule vehicule) {
        return vehiculeRepository.update(vehicule);
    }

    public boolean enregistrerEntretien(Long vehiculeId, Entretien entretien) {
        return vehiculeRepository.saveEntretien(vehiculeId, entretien);
    }

    public List<Entretien> getEntretiensVehicule(Vehicule vehicule) {
        return vehiculeRepository.findEntretiens(vehicule);
    }

    public boolean verifierEntretienNecessaire(Vehicule vehicule) {
        return vehicule.getKilometrageTotal() >= vehicule.getKilometrageAvantEntretien();
    }

    public List<Vehicule> getVehiculesNecessitantEntretien() {
        List<Vehicule> allVehicules = new ArrayList<>();

        for (TypePermis type : TypePermis.values()) {
            allVehicules.addAll(vehiculeRepository.findAllByTypePermis(type));
        }

        return allVehicules.stream()
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
        List<Vehicule> allVehicules = new ArrayList<>();
        for (TypePermis type : TypePermis.values()) {
            allVehicules.addAll(vehiculeRepository.findAllByTypePermis(type));
        }

        return allVehicules.stream()
                .anyMatch(v -> v.getImmatriculation().equals(immatriculation));
    }

    public boolean verifierDisponibiliteVehicule(Long vehiculeId, LocalDate date) {

        // be5el bich n5amem fiha 2am :))
        return true;
    }

    public List<Vehicule> getVehiculesDisponibles(TypePermis typePermis, LocalDate date) {
        List<Vehicule> vehicules = getVehiculesByTypePermis(typePermis);

        return vehicules.stream()
                .filter(v -> verifierDisponibiliteVehicule(v.getId(), date))
                .collect(Collectors.toList());
    }
}