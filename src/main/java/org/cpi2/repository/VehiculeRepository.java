package org.cpi2.repository;

import org.cpi2.db.DBConnect;
import org.cpi2.entitties.Entretien;
import org.cpi2.entitties.TypePermis;
import org.cpi2.entitties.Vehicule;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository for vehicle data access
 */
public class VehiculeRepository {
    private static final Logger LOGGER = Logger.getLogger(VehiculeRepository.class.getName());

    /**
     * Retrieves all vehicles from the database
     * @return List of all vehicles
     */
    public List<Vehicule> findAll() {
        List<Vehicule> vehicules = new ArrayList<>();
        String query = "SELECT v.*, tp.id as type_id FROM vehicule v JOIN type_permis tp ON v.type_permis_id = tp.id";
        
        try (Connection conn = DBConnect.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving vehicles", e);
        }
        
        return vehicules;
    }

    /**
     * Finds a vehicle by its ID
     * @param id The vehicle ID
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicule> findById(int id) {
        String query = "SELECT v.*, tp.id as type_id FROM vehicule v JOIN type_permis tp ON v.type_permis_id = tp.id WHERE v.id = ?";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicule(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding vehicle with ID: " + id, e);
        }
        
        return Optional.empty();
    }

    /**
     * Finds a vehicle by its registration plate
     * @param immatriculation The registration plate
     * @return Optional containing the vehicle if found
     */
    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        String query = "SELECT v.*, tp.id as type_id FROM vehicule v JOIN type_permis tp ON v.type_permis_id = tp.id WHERE v.immatriculation = ?";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, immatriculation);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToVehicule(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding vehicle with immatriculation: " + immatriculation, e);
        }
        
        return Optional.empty();
    }

    /**
     * Finds vehicles by license type
     * @param typePermis The license type
     * @return List of vehicles for the specified license type
     */
    public List<Vehicule> findByTypePermis(TypePermis typePermis) {
        List<Vehicule> vehicules = new ArrayList<>();
        String query = "SELECT v.*, tp.id as type_id FROM vehicule v " +
                       "JOIN type_permis tp ON v.type_permis_id = tp.id " +
                       "WHERE tp.code = ?";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, typePermis.name());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vehicules.add(mapResultSetToVehicule(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding vehicles for type permis: " + typePermis, e);
        }
        
        return vehicules;
    }

    /**
     * Saves a new vehicle to the database
     * @param vehicule The vehicle to save
     * @return The saved vehicle with ID populated, or null if operation failed
     */
    public Vehicule save(Vehicule vehicule) {
        String query = "INSERT INTO vehicule (immatriculation, type_permis_id, marque, modele, annee, " +
                       "date_mise_service, kilometrage_total, kilometrage_prochain_entretien, " +
                       "date_prochain_entretien, date_derniere_visite_technique, date_prochaine_visite_technique, " +
                       "date_expiration_assurance, statut) " +
                       "VALUES (?, (SELECT id FROM type_permis WHERE code = ?), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            prepareVehiculeStatement(pstmt, vehicule);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Creating vehicle failed, no rows affected.");
                return null;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehicule.setId(generatedKeys.getInt(1));
                    return vehicule;
                } else {
                    LOGGER.log(Level.WARNING, "Creating vehicle failed, no ID obtained.");
                    return null;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving vehicle: " + vehicule.getImmatriculation(), e);
            return null;
        }
    }

    /**
     * Updates an existing vehicle in the database
     * @param vehicule The vehicle to update
     * @return true if successful, false otherwise
     */
    public boolean update(Vehicule vehicule) {
        String query = "UPDATE vehicule SET immatriculation = ?, type_permis_id = (SELECT id FROM type_permis WHERE code = ?), " +
                       "marque = ?, modele = ?, annee = ?, date_mise_service = ?, kilometrage_total = ?, " +
                       "kilometrage_prochain_entretien = ?, date_prochain_entretien = ?, date_derniere_visite_technique = ?, " +
                       "date_prochaine_visite_technique = ?, date_expiration_assurance = ?, statut = ? " +
                       "WHERE id = ?";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            prepareVehiculeStatement(pstmt, vehicule);
            pstmt.setInt(14, vehicule.getId());
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicle: " + vehicule.getId(), e);
            return false;
        }
    }

    /**
     * Deletes a vehicle by its ID
     * @param id The vehicle ID to delete
     * @return true if successful, false otherwise
     */
    public boolean delete(int id) {
        String query = "DELETE FROM vehicule WHERE id = ?";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, id);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting vehicle with ID: " + id, e);
            return false;
        }
    }

    /**
     * Saves a new maintenance record
     * @param entretien The maintenance record to save
     * @return The saved maintenance record with ID populated, or null if operation failed
     */
    public Entretien saveEntretien(Entretien entretien) {
        String query = "INSERT INTO entretien (vehicule_id, date_entretien, type_entretien, description, " +
                       "kilometrage_actuel, cout, prestataire) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, entretien.getVehiculeId());
            pstmt.setDate(2, Date.valueOf(entretien.getDateEntretien()));
            pstmt.setString(3, entretien.getTypeEntretien());
            pstmt.setString(4, entretien.getDescription());
            pstmt.setInt(5, entretien.getKilometrageActuel());
            pstmt.setDouble(6, entretien.getCout());
            pstmt.setString(7, entretien.getPrestataire());
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                LOGGER.log(Level.WARNING, "Creating maintenance record failed, no rows affected.");
                return null;
            }
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entretien.setId(generatedKeys.getInt(1));
                    return entretien;
                } else {
                    LOGGER.log(Level.WARNING, "Creating maintenance record failed, no ID obtained.");
                    return null;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving maintenance record for vehicle ID: " + entretien.getVehiculeId(), e);
            return null;
        }
    }

    /**
     * Gets all maintenance records for a vehicle
     * @param vehiculeId The vehicle ID
     * @return List of maintenance records
     */
    public List<Entretien> getEntretiensByVehiculeId(int vehiculeId) {
        List<Entretien> entretiens = new ArrayList<>();
        String query = "SELECT * FROM entretien WHERE vehicule_id = ? ORDER BY date_entretien DESC";
        
        try (Connection conn = DBConnect.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, vehiculeId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    entretiens.add(mapResultSetToEntretien(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error retrieving maintenance records for vehicle ID: " + vehiculeId, e);
        }
        
        return entretiens;
    }

    /**
     * Populates a PreparedStatement with vehicle data
     * @param pstmt The PreparedStatement to populate
     * @param vehicule The vehicle data source
     * @throws SQLException If a database error occurs
     */
    private void prepareVehiculeStatement(PreparedStatement pstmt, Vehicule vehicule) throws SQLException {
        pstmt.setString(1, vehicule.getImmatriculation());
        pstmt.setString(2, vehicule.getTypePermis().name());
        pstmt.setString(3, vehicule.getMarque());
        pstmt.setString(4, vehicule.getModele());
        
        if (vehicule.getAnnee() != null) {
            pstmt.setInt(5, vehicule.getAnnee());
        } else {
            pstmt.setNull(5, Types.INTEGER);
        }
        
        pstmt.setDate(6, vehicule.getDateMiseEnService() != null ? 
                         Date.valueOf(vehicule.getDateMiseEnService()) : null);
        pstmt.setInt(7, vehicule.getKilometrageTotal());
        pstmt.setInt(8, vehicule.getKilometrageProchainEntretien());
        
        pstmt.setDate(9, vehicule.getDateProchainEntretien() != null ? 
                         Date.valueOf(vehicule.getDateProchainEntretien()) : null);
        pstmt.setDate(10, vehicule.getDateDerniereVisiteTechnique() != null ? 
                          Date.valueOf(vehicule.getDateDerniereVisiteTechnique()) : null);
        pstmt.setDate(11, vehicule.getDateProchaineVisiteTechnique() != null ? 
                          Date.valueOf(vehicule.getDateProchaineVisiteTechnique()) : null);
        pstmt.setDate(12, vehicule.getDateExpirationAssurance() != null ? 
                          Date.valueOf(vehicule.getDateExpirationAssurance()) : null);
        pstmt.setString(13, vehicule.getStatut());
    }

    /**
     * Maps a database result set to a Vehicule object
     * @param rs The result set to map
     * @return The mapped Vehicule object
     * @throws SQLException If a database error occurs
     */
    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getInt("id"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        
        try {
            String typePermisCode = rs.getString("code");
            vehicule.setTypePermis(TypePermis.valueOf(typePermisCode));
        } catch (IllegalArgumentException e) {
            // Default to B if there's an issue
            vehicule.setTypePermis(TypePermis.B);
        }
        
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));
        
        vehicule.setAnnee(rs.getObject("annee", Integer.class));
        
        Date dateMiseService = rs.getDate("date_mise_service");
        if (dateMiseService != null) {
            vehicule.setDateMiseEnService(dateMiseService.toLocalDate());
        }
        
        vehicule.setKilometrageTotal(rs.getInt("kilometrage_total"));
        vehicule.setKilometrageProchainEntretien(rs.getInt("kilometrage_prochain_entretien"));
        
        Date dateProchainEntretien = rs.getDate("date_prochain_entretien");
        if (dateProchainEntretien != null) {
            vehicule.setDateProchainEntretien(dateProchainEntretien.toLocalDate());
        }
        
        Date dateDerniereVisiteTechnique = rs.getDate("date_derniere_visite_technique");
        if (dateDerniereVisiteTechnique != null) {
            vehicule.setDateDerniereVisiteTechnique(dateDerniereVisiteTechnique.toLocalDate());
        }
        
        Date dateProchaineVisiteTechnique = rs.getDate("date_prochaine_visite_technique");
        if (dateProchaineVisiteTechnique != null) {
            vehicule.setDateProchaineVisiteTechnique(dateProchaineVisiteTechnique.toLocalDate());
        }
        
        Date dateExpirationAssurance = rs.getDate("date_expiration_assurance");
        if (dateExpirationAssurance != null) {
            vehicule.setDateExpirationAssurance(dateExpirationAssurance.toLocalDate());
        }
        
        vehicule.setStatut(rs.getString("statut"));
        
        Date createdAt = rs.getDate("created_at");
        if (createdAt != null) {
            vehicule.setCreatedAt(createdAt.toLocalDate());
        }
        
        return vehicule;
    }

    /**
     * Maps a database result set to an Entretien object
     * @param rs The result set to map
     * @return The mapped Entretien object
     * @throws SQLException If a database error occurs
     */
    private Entretien mapResultSetToEntretien(ResultSet rs) throws SQLException {
        Entretien entretien = new Entretien();
        entretien.setId(rs.getInt("id"));
        entretien.setVehiculeId(rs.getInt("vehicule_id"));
        
        Date dateEntretien = rs.getDate("date_entretien");
        if (dateEntretien != null) {
            entretien.setDateEntretien(dateEntretien.toLocalDate());
        }
        
        entretien.setTypeEntretien(rs.getString("type_entretien"));
        entretien.setDescription(rs.getString("description"));
        entretien.setKilometrageActuel(rs.getInt("kilometrage_actuel"));
        entretien.setCout(rs.getDouble("cout"));
        entretien.setPrestataire(rs.getString("prestataire"));
        
        Date createdAt = rs.getDate("created_at");
        if (createdAt != null) {
            entretien.setCreatedAt(createdAt.toLocalDate());
        }
        
        return entretien;
    }
}
