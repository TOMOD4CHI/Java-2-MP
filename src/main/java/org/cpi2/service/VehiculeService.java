package org.cpi2.service;

import org.cpi2.entitties.Entretien;
import org.cpi2.entitties.TypePermis;
import org.cpi2.entitties.Vehicule;
import org.cpi2.repository.VehiculeRepository;
import org.cpi2.utils.DatabaseConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehiculeService {
    private static final Logger LOGGER = Logger.getLogger(VehiculeService.class.getName());

    private final VehiculeRepository vehiculeRepository;

    public VehiculeService() {
        this.vehiculeRepository = new VehiculeRepository();
    }

    public Vehicule getVehiculeById(Long id) {
        return vehiculeRepository.findById(id).orElse(null);
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
        return vehicule.getKilometrage() >= vehicule.getKilometrageAvantEntretien();
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

    /**
     * Adds a new vehicule to the database
     * @param vehicule The vehicule to add
     * @return true if successful, false otherwise
     */
    public boolean addVehicule(Vehicule vehicule) {
        String sql = "INSERT INTO vehicule (marque, modele, immatriculation, annee, type, kilometrage, disponible) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, vehicule.getMarque());
            pstmt.setString(2, vehicule.getModele());
            pstmt.setString(3, vehicule.getImmatriculation());
            pstmt.setInt(4, vehicule.getAnnee());
            pstmt.setString(5, vehicule.getType());
            pstmt.setDouble(6, vehicule.getKilometrage());
            pstmt.setBoolean(7, vehicule.isDisponible());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Creating vehicule failed, no rows affected.");
                return false;
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehicule.setId(generatedKeys.getLong(1));
                    return true;
                } else {
                    LOGGER.log(Level.WARNING, "Creating vehicule failed, no ID obtained.");
                    return false;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding vehicule", e);
            return false;
        }
    }

    /**
     * Updates an existing vehicule in the database
     * @param vehicule The vehicule to update
     * @return true if successful, false otherwise
     */
    public boolean updateVehicule(Vehicule vehicule) {
        String sql = "UPDATE vehicule SET marque = ?, modele = ?, immatriculation = ?, annee = ?, type = ?, kilometrage = ?, disponible = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, vehicule.getMarque());
            pstmt.setString(2, vehicule.getModele());
            pstmt.setString(3, vehicule.getImmatriculation());
            pstmt.setInt(4, vehicule.getAnnee());
            pstmt.setString(5, vehicule.getType());
            pstmt.setDouble(6, vehicule.getKilometrage());
            pstmt.setBoolean(7, vehicule.isDisponible());
            pstmt.setLong(8, vehicule.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicule", e);
            return false;
        }
    }

    /**
     * Updates the kilometrage of a vehicule
     * @param id The ID of the vehicule
     * @param kilometrage The new kilometrage
     * @return true if successful, false otherwise
     */
    public boolean updateKilometrage(Long id, double kilometrage) {
        String sql = "UPDATE vehicule SET kilometrage = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, kilometrage);
            pstmt.setLong(2, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating kilometrage", e);
            return false;
        }
    }

    /**
     * Removes a vehicule from the database
     * @param id The ID of the vehicule to remove
     * @return true if successful, false otherwise
     */
    public boolean removeVehicule(Long id) {
        String sql = "DELETE FROM vehicule WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, id);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error removing vehicule", e);
            return false;
        }
    }

    /**
     * Gets all vehicules from the database
     * @return List of all vehicules
     */
    public List<Vehicule> getAllVehicules() {
        String sql = "SELECT * FROM vehicule ORDER BY marque, modele";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(extractVehiculeFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all vehicules", e);
        }
        return vehicules;
    }

    /**
     * Gets all available vehicules
     * @return List of available vehicules
     */
    public List<Vehicule> getAvailableVehicules() {
        String sql = "SELECT * FROM vehicule WHERE disponible = TRUE ORDER BY marque, modele";
        List<Vehicule> vehicules = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(extractVehiculeFromResultSet(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting available vehicules", e);
        }
        return vehicules;
    }

    /**
     * Helper method to extract a vehicule from a result set
     * @param rs The result set
     * @return The extracted vehicule
     * @throws SQLException If an error occurs
     */
    private Vehicule extractVehiculeFromResultSet(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getLong("id"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        vehicule.setAnnee(rs.getInt("annee"));
        vehicule.setType(rs.getString("type"));
        vehicule.setKilometrage(rs.getDouble("kilometrage"));
        vehicule.setDisponible(rs.getBoolean("disponible"));
        vehicule.setKilometrageAvantEntretien(rs.getDouble("kilometrage_avant_entretien"));
        vehicule.setKilometrageTotal(rs.getDouble("kilometrage_total"));
        
        // Handle date fields
        if (rs.getDate("date_mise_en_service") != null) {
            vehicule.setDateMiseEnService(rs.getDate("date_mise_en_service").toLocalDate());
        }
        
        // Handle TypePermis if present in the resultset
        try {
            String typePermisStr = rs.getString("id_type_permis");
            if (typePermisStr != null && !typePermisStr.isEmpty()) {
                TypePermis typePermis = TypePermis.valueOf(typePermisStr);
                vehicule.setTypePermis(typePermis);
            }
        } catch (SQLException | IllegalArgumentException e) {
            // Column might not exist or value might be invalid - just log and continue
            LOGGER.log(Level.WARNING, "Could not set TypePermis: " + e.getMessage());
        }
        
        return vehicule;
    }
}