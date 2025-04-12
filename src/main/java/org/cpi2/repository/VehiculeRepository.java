package org.cpi2.repository;

import org.cpi2.entities.Entretien;
import org.cpi2.entities.TypePermis;
import org.cpi2.entities.Vehicule;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VehiculeRepository extends BaseRepository<Vehicule> {
    private final TypePermisRepository typePermisRepository;
    private static final Logger LOGGER = Logger.getLogger(VehiculeRepository.class.getName());
    private HashMap<Integer, TypePermis> permis_mapper = new HashMap<>();

    public VehiculeRepository() {
        this.typePermisRepository = new TypePermisRepository();
        List<String> permisList = typePermisRepository.findAll();
        for (String permis : permisList) {
            permis_mapper.put(typePermisRepository.findByCode(permis).get(),TypePermis.valueOf(permis));
        }

    }


    public List<Vehicule> findAll() {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all vehicules", e);
        }
        return vehicules;
    }

    public Optional<Vehicule> findById(Long id) {
        String sql = "SELECT * FROM vehicule WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToVehicule(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding vehicule by ID", e);
        }
        return Optional.empty();
    }

    public Optional<Vehicule> findByImmatriculation(String immatriculation) {
        String sql = "SELECT * FROM vehicule WHERE immatriculation = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, immatriculation);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToVehicule(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding vehicule by immatriculation", e);
        }
        return Optional.empty();
    }

    public List<Vehicule> findAllByTypePermis(TypePermis typePermis) {
        List<Vehicule> vehicules = new ArrayList<>();
        String sql = "SELECT * FROM vehicule WHERE type_permis_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            int id = typePermisRepository.findByCode(typePermis.name()).get();
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                vehicules.add(mapResultSetToVehicule(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding vehicules by type permis", e);
        }
        return vehicules;
    }

    private Vehicule mapResultSetToVehicule(ResultSet rs) throws SQLException {
        int permis_id = rs.getInt("type_permis_id");

        String immatriculation = rs.getString("immatriculation");
        String marque = rs.getString("marque");
        String modele = rs.getString("modele");
        int annee = rs.getInt("annee");
        LocalDate dateMiseEnService = rs.getDate("date_mise_service").toLocalDate();
        int kilometrageTotal = rs.getInt("kilometrage_total");
        int kilometrageProchainEntretien = rs.getInt("kilometrage_prochain_entretien");
        String statut = rs.getString("statut");
        LocalDate dateProchainEntretien = rs.getDate("date_prochain_entretien") != null ? rs.getDate("date_prochain_entretien").toLocalDate() : null;
        LocalDate dateDerniereVisiteTechnique = rs.getDate("date_derniere_visite_technique") != null ? rs.getDate("date_derniere_visite_technique").toLocalDate() : null;
        LocalDate dateProchaineVisiteTechnique = rs.getDate("date_prochaine_visite_technique") != null ? rs.getDate("date_prochaine_visite_technique").toLocalDate() : null;
        LocalDate dateExpirationAssurance = rs.getDate("date_expiration_assurance") != null ? rs.getDate("date_expiration_assurance").toLocalDate() : null;
        long id =  rs.getLong("id");
        TypePermis typePermis = permis_mapper.get(permis_id);


        Vehicule vehicule = new Vehicule(
                immatriculation,
                marque,
                modele,
                annee,
                typePermis,
                dateMiseEnService,
                kilometrageTotal,
                kilometrageProchainEntretien
        );
        vehicule.setId((int) id);
        vehicule.setDateProchainEntretien(dateProchainEntretien);
        vehicule.setDateDerniereVisiteTechnique(dateDerniereVisiteTechnique);
        vehicule.setDateProchaineVisiteTechnique(dateProchaineVisiteTechnique);
        vehicule.setDateExpirationAssurance(dateExpirationAssurance);
        vehicule.setStatut(statut);
        return vehicule;
    }

    public List<Entretien> findEntretiens(Vehicule vehicule) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE id_vehicule = ? ORDER BY date_entretien DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehicule.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Entretien entretien = new Entretien(
                        rs.getDate("date_entretien").toLocalDate(),
                        rs.getString("type_entretien"),
                        rs.getDouble("cout")
                );
                entretien.setId((int) rs.getLong("id"));
                entretien.setDescription(rs.getString("description"));
                entretiens.add(entretien);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens for vehicule", e);
        }
        return entretiens;
    }

    public boolean saveEntretien(Long vehiculeId, Entretien entretien) {
        String sql = "INSERT INTO entretien (id_vehicule, date_entretien, type_entretien, cout, description) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, vehiculeId);
            stmt.setDate(2, Date.valueOf(entretien.getDateEntretien()));
            stmt.setString(3, entretien.getTypeEntretien());
            stmt.setDouble(4, entretien.getCout());
            stmt.setString(5, entretien.getDescription());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entretien.setId((int) generatedKeys.getLong(1));
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving entretien", e);
            return false;
        }
    }

    public boolean save(Vehicule vehicule) {
        int typePermisId;
        try {
            typePermisId = typePermisRepository.findByCode(vehicule.getTypePermis().name()).get();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting type permis ID", e);
            return false;
        }

        String sql = """
        INSERT INTO vehicule 
        (immatriculation, marque, modele, type_permis_id, date_mise_service, 
         kilometrage_total, kilometrage_prochain_entretien, statut)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.setInt(4, typePermisId);
            stmt.setDate(5, Date.valueOf(vehicule.getDateMiseEnService()));
            stmt.setInt(6, vehicule.getKilometrageTotal());
            stmt.setInt(7, vehicule.getKilometrageProchainEntretien());
            stmt.setString(8, vehicule.getStatut());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehicule.setId((int) generatedKeys.getLong(1));
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving vehicule", e);
            return false;
        }
    }

    public boolean update(long id , Vehicule vehicule) {
        int typePermisId;
        try {
            typePermisId = typePermisRepository.findByCode(vehicule.getTypePermis().name()).get();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error getting type permis ID", e);
            return false;
        }
        String sql = """
            UPDATE vehicule 
            SET immatriculation = ?, marque = ?, modele = ?, type_permis_id = ?,
                date_mise_service = ?, kilometrage_total = ?, kilometrage_prochain_entretien = ?,statut = ?,
        date_prochaine_visite_technique=?,date_derniere_visite_technique=?,date_prochain_entretien=?,date_expiration_assurance=?
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.setInt(4, typePermisId);
            stmt.setDate(5, Date.valueOf(vehicule.getDateMiseEnService()));
            stmt.setInt(6, vehicule.getKilometrageTotal());
            stmt.setInt(7, vehicule.getKilometrageProchainEntretien());
            stmt.setString(8, vehicule.getStatut());
            if(vehicule.getDateDerniereVisiteTechnique() != null) {
                stmt.setDate(10, Date.valueOf(vehicule.getDateDerniereVisiteTechnique()));
            } else {
                stmt.setNull(10, Types.DATE);
            }
            if(vehicule.getDateProchaineVisiteTechnique() != null) {
                stmt.setDate(9, Date.valueOf(vehicule.getDateProchaineVisiteTechnique()));
            } else {
                stmt.setNull(9, Types.DATE);
            }
            if(vehicule.getDateProchainEntretien() != null) {
                stmt.setDate(11, Date.valueOf(vehicule.getDateProchainEntretien()));
            } else {
                stmt.setNull(11, Types.DATE);
            }
            if(vehicule.getDateExpirationAssurance() != null) {
                stmt.setDate(12, Date.valueOf(vehicule.getDateExpirationAssurance()));
            } else {
                stmt.setNull(12, Types.DATE);
            }
            stmt.setLong(13, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating vehicule", e);
            return false;
        }
    }
    
    public boolean delete(Long id) {
        String sql = "DELETE FROM vehicule WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
             
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting vehicule", e);
            return false;
        }
    }


    public List<Entretien> getEntretiensByVehiculeId(Long vehiculeId) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE id_vehicule = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Entretien entretien = new Entretien(
                        rs.getDate("date_entretien").toLocalDate(),
                        rs.getString("type_entretien"),
                        rs.getDouble("cout")
                );
                entretien.setId(rs.getInt("id"));
                entretien.setDescription(rs.getString("description"));
                entretiens.add(entretien);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens by vehicule ID", e);
        }
        return entretiens;
    }
}
