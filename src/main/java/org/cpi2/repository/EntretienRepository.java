package org.cpi2.repository;

import org.cpi2.entities.Entretien;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC Implementation of the EntretienRepository interface
 */
public class EntretienRepository extends BaseRepository<Entretien> {
    private static final Logger LOGGER = Logger.getLogger(EntretienRepository.class.getName());

    public EntretienRepository() {
    }

    public Entretien findById(int id) {
        Entretien entretien = null;
        String sql = "SELECT * FROM entretien WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                entretien = mapResultSetToEntretien(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretien by ID", e);
        }

        return entretien;
    }

    public List<Entretien> findAll() {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all entretiens", e);
        }

        return entretiens;
    }

    public List<Entretien> findByVehiculeId(long vehiculeId) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE vehicule_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens by vehicule ID", e);
        }

        return entretiens;
    }
    public List<Entretien> findByVehiculeImm(String immatriculation) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien e inner join vehicule v on e.vehicule_id = v.id WHERE v.immatriculation = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, immatriculation);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens by vehicule ID", e);
        }

        return entretiens;
    }

    public List<Entretien> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE date_entretien BETWEEN ? AND ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens by date range", e);
        }

        return entretiens;
    }

    public List<Entretien> findUpcomingMaintenance() {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE entretien_suivant >= CURRENT_DATE()";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding upcoming maintenance", e);
        }

        return entretiens;
    }

    public List<Entretien> findByType(String typeEntretien) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE type_entretien = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, typeEntretien);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens by type", e);
        }

        return entretiens;
    }

    public List<Entretien> findByCostGreaterThan(double cout) {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE cout >= ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, cout);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding entretiens by cost", e);
        }

        return entretiens;
    }

    public boolean save(Entretien entretien) {
        String sql = "INSERT INTO entretien (vehicule_id, date_entretien, type_entretien, " +
                "description, cout, entretien_suivant, maintenance, kilometrage,facture_path,statut,created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, entretien.getVehiculeId());
            stmt.setDate(2, Date.valueOf(entretien.getDateEntretien()));
            stmt.setString(3, entretien.getTypeEntretien());
            stmt.setString(4, entretien.getDescription());
            stmt.setDouble(5, entretien.getCout());

            if (entretien.getDateProchainEntretien() != null) {
                stmt.setDate(6, Date.valueOf(entretien.getDateProchainEntretien()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setInt(7, entretien.isMaintenance() ? 1 : 0);
            stmt.setInt(8, entretien.getKilometrageActuel());

            LocalDate createdAt = entretien.getCreatedAt() != null ?
                    entretien.getCreatedAt() : LocalDate.now();
            stmt.setString(9, entretien.getCheminFacture());
            stmt.setInt(10, entretien.isDone() ? 1 : 0);
            stmt.setDate(11, Date.valueOf(createdAt));

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating entretien failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    entretien.setId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Creating entretien failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving entretien", e);
        }

        return true;
    }

    public boolean update(Entretien entretien) {
        String sql = "UPDATE entretien SET vehicule_id = ?, date_entretien = ?, " +
                "type_entretien = ?, description = ?, cout = ?, entretien_suivant = ?, " +
                "maintenance = ?, kilometrage = ? , facture_path = ? , statut = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, entretien.getVehiculeId());
            stmt.setDate(2, Date.valueOf(entretien.getDateEntretien()));
            stmt.setString(3, entretien.getTypeEntretien());
            stmt.setString(4, entretien.getDescription());
            stmt.setDouble(5, entretien.getCout());


            if (entretien.getDateProchainEntretien() != null) {
                stmt.setDate(6, Date.valueOf(entretien.getDateProchainEntretien()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            stmt.setInt(7, entretien.isMaintenance() ? 1 : 0);
            stmt.setInt(8, entretien.getKilometrageActuel());
            stmt.setString(9, entretien.getCheminFacture());
            stmt.setInt(10, entretien.isDone() ? 1 : 0);
            stmt.setInt(11, entretien.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating entretien", e);
            return false;
        }
    }

    public boolean deleteById(int id) {
        String sql = "DELETE FROM entretien WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting entretien", e);
            return false;
        }
    }

    public List<Entretien> findOverdueMaintenance() {
        List<Entretien> entretiens = new ArrayList<>();
        String sql = "SELECT * FROM entretien WHERE entretien_suivant < CURRENT_DATE() AND maintenance = 1";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                entretiens.add(mapResultSetToEntretien(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding overdue maintenance", e);
        }

        return entretiens;
    }

    public int countByVehiculeId(int vehiculeId) {
        String sql = "SELECT COUNT(*) FROM entretien WHERE vehicule_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error counting entretiens by vehicule ID", e);
        }

        return 0;
    }

    public double getTotalCostByVehiculeId(long vehiculeId) {
        String sql = "SELECT SUM(cout) FROM entretien WHERE vehicule_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, vehiculeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error calculating total cost by vehicule ID", e);
        }

        return 0;
    }

    private Entretien mapResultSetToEntretien(ResultSet rs) throws SQLException {
        Entretien entretien = new Entretien();

        entretien.setId(rs.getInt("id"));
        entretien.setVehiculeId(rs.getLong("vehicule_id"));
        entretien.setDateEntretien(rs.getDate("date_entretien").toLocalDate());
        entretien.setTypeEntretien(rs.getString("type_entretien"));
        entretien.setStatut(rs.getInt("statut") == 1);

        String description = rs.getString("description");
        if (!rs.wasNull()) {
            entretien.setDescription(description);
        }
        entretien.setCheminFacture(rs.getString("facture_path"));
        entretien.setCout(rs.getDouble("cout"));
        entretien.setMaintenance(rs.getInt("maintenance") == 1);

        Date nextMaintenanceDate = rs.getDate("entretien_suivant");
        if (nextMaintenanceDate != null) {
            entretien.setDateProchainEntretien(nextMaintenanceDate.toLocalDate());
        }

        entretien.setKilometrageActuel(rs.getInt("kilometrage"));

        Date createdAt = rs.getDate("created_at");
        if (createdAt != null) {
            entretien.setCreatedAt(createdAt.toLocalDate());
        } else {
            entretien.setCreatedAt(LocalDate.now());
        }

        return entretien;
    }
}