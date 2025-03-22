package org.cpi2.repository;

import org.cpi2.entitties.Entretien;
import org.cpi2.entitties.TypePermis;
import org.cpi2.entitties.Vehicule;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.logging.Level;

// Vehicule Repository
public class VehiculeRepository extends BaseRepository<Vehicule> {

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
        String sql = "SELECT * FROM vehicule WHERE id_type_permis = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, typePermis.name());
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
        String typePermisStr = rs.getString("id_type_permis");
        TypePermis typePermis = Arrays.stream(TypePermis.values())
                .filter(tp -> tp.name().equals(typePermisStr))
                .findFirst()
                .orElseThrow(() -> new SQLException("Invalid type_permis value: " + typePermisStr));

        Vehicule vehicule = new Vehicule(
                rs.getString("immatriculation"),
                rs.getString("marque"),
                rs.getString("modele"),
                typePermis,
                rs.getDate("date_mise_en_service").toLocalDate(),
                rs.getInt("kilometrage_avant_entretien")
        );
        vehicule.setId(rs.getLong("id"));
        vehicule.setKilometrageTotal(rs.getInt("kilometrage_total"));
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
                entretien.setId(rs.getLong("id"));
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
                    entretien.setId(generatedKeys.getLong(1));
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
        String sql = """
            INSERT INTO vehicule 
            (immatriculation, marque, modele, id_type_permis, date_mise_en_service, 
             kilometrage_total, kilometrage_avant_entretien)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.setString(4, vehicule.getTypePermis().name());
            stmt.setDate(5, Date.valueOf(vehicule.getDateMiseEnService()));
            stmt.setInt(6, vehicule.getKilometrageTotal());
            stmt.setInt(7, vehicule.getKilometrageAvantEntretien());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    vehicule.setId(generatedKeys.getLong(1));
                    return true;
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving vehicule", e);
            return false;
        }
    }

    public boolean update(Vehicule vehicule) {
        String sql = """
            UPDATE vehicule 
            SET immatriculation = ?, marque = ?, modele = ?, id_type_permis = ?,
                date_mise_en_service = ?, kilometrage_total = ?, kilometrage_avant_entretien = ?
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, vehicule.getImmatriculation());
            stmt.setString(2, vehicule.getMarque());
            stmt.setString(3, vehicule.getModele());
            stmt.setString(4, vehicule.getTypePermis().name());
            stmt.setDate(5, Date.valueOf(vehicule.getDateMiseEnService()));
            stmt.setInt(6, vehicule.getKilometrageTotal());
            stmt.setInt(7, vehicule.getKilometrageAvantEntretien());
            stmt.setLong(8, vehicule.getId());

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
}
