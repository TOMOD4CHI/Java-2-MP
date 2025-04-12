package org.cpi2.repository;

import org.cpi2.entities.Seance;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

public class SeanceRepository extends BaseRepository<Seance> {

    public Optional<Seance> findById(Long id) {
        String sql = "SELECT s.*, " +
                "c.nom AS candidat_nom, c.prenom AS candidat_prenom, " +
                "m.nom AS moniteur_nom, m.prenom AS moniteur_prenom, " +
                "v.marque AS vehicule_marque, v.modele AS vehicule_modele " +
                "FROM seance s " +
                "LEFT JOIN candidat c ON s.candidat_id = c.id " +
                "LEFT JOIN moniteur m ON s.moniteur_id = m.id " +
                "LEFT JOIN vehicule v ON s.vehicule_id = v.id " +
                "WHERE s.id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setType(rs.getString("type"));
                seance.setCandidatId(rs.getLong("candidat_id"));
                seance.setCandidatName(rs.getString("candidat_nom") + " " + rs.getString("candidat_prenom"));
                seance.setMoniteurId(rs.getLong("moniteur_id"));
                seance.setMoniteurName(rs.getString("moniteur_nom") + " " + rs.getString("moniteur_prenom"));
                seance.setVehiculeId(rs.getLong("vehicule_id"));
                seance.setVehiculeName(rs.getString("vehicule_marque") + " " + rs.getString("vehicule_modele"));
                seance.setDate(rs.getString("date"));
                seance.setTemps(rs.getString("heure"));
                seance.setKilometrage(getDoubleFromInt(rs, "kilometrage_debut"));
                seance.setStatus(rs.getString("statut"));
                seance.setCommentaire(rs.getString("commentaire"));

                if (rs.getObject("latitude") != null) {
                    seance.setLatitude(rs.getDouble("latitude"));
                }
                if (rs.getObject("longitude") != null) {
                    seance.setLongitude(rs.getDouble("longitude"));
                }

                String lieu = rs.getString("lieu");
                if (lieu != null) {
                    if ("Code".equals(seance.getType())) {
                        seance.setSalle(lieu);
                    } else if ("Conduite".equals(seance.getType())) {
                        seance.setQuartier(lieu);
                    }
                }

                return Optional.of(seance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding seance by ID", e);
        }
        return Optional.empty();
    }

    public List<Seance> findAll() {
        String sql = "SELECT s.*, " +
                "c.nom AS candidat_nom, c.prenom AS candidat_prenom, " +
                "m.nom AS moniteur_nom, m.prenom AS moniteur_prenom, " +
                "v.marque AS vehicule_marque, v.modele AS vehicule_modele " +
                "FROM seance s " +
                "LEFT JOIN candidat c ON s.candidat_id = c.id " +
                "LEFT JOIN moniteur m ON s.moniteur_id = m.id " +
                "LEFT JOIN vehicule v ON s.vehicule_id = v.id";

        List<Seance> seances = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Seance seance = new Seance();
                seance.setId(rs.getLong("id"));
                seance.setType(rs.getString("type"));
                seance.setCandidatId(rs.getLong("candidat_id"));
                seance.setCandidatName(rs.getString("candidat_nom") + " " + rs.getString("candidat_prenom"));
                seance.setMoniteurId(rs.getLong("moniteur_id"));
                seance.setMoniteurName(rs.getString("moniteur_nom") + " " + rs.getString("moniteur_prenom"));
                seance.setVehiculeId(rs.getLong("vehicule_id"));
                seance.setVehiculeName(rs.getString("vehicule_marque") + " " + rs.getString("vehicule_modele"));
                seance.setDate(rs.getString("date"));
                seance.setTemps(rs.getString("heure"));
                seance.setKilometrage(getDoubleFromInt(rs, "kilometrage_debut"));
                seance.setStatus(rs.getString("statut"));
                seance.setCommentaire(rs.getString("commentaire"));

                if (rs.getObject("latitude") != null) {
                    seance.setLatitude(rs.getDouble("latitude"));
                }
                if (rs.getObject("longitude") != null) {
                    seance.setLongitude(rs.getDouble("longitude"));
                }

                String lieu = rs.getString("lieu");
                if (lieu != null) {
                    if ("Code".equals(seance.getType())) {
                        seance.setSalle(lieu);
                    } else if ("Conduite".equals(seance.getType())) {
                        seance.setQuartier(lieu);
                    }
                }

                seances.add(seance);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all seances", e);
        }
        return seances;
    }

    public boolean save(Seance seance) {
        String sql = "INSERT INTO seance (type, candidat_id, moniteur_id, vehicule_id, date, heure, duree, " +
                "kilometrage_debut, statut, commentaire, latitude, longitude, lieu) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, seance.getType());
            stmt.setLong(2, seance.getCandidatId());
            stmt.setLong(3, seance.getMoniteurId());

            if (seance.getVehiculeId() != null) {
                stmt.setLong(4, seance.getVehiculeId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, seance.getDate());
            stmt.setString(6, seance.getHeure());
            stmt.setInt(7, 60);

            if (seance.getKilometrage() != null) {
                stmt.setInt(8, seance.getKilometrage().intValue());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            stmt.setString(9, seance.getStatus() != null ? seance.getStatus() : "Planifiée");
            stmt.setString(10, seance.getCommentaire());

            if (seance.getLatitude() != null) {
                stmt.setDouble(11, seance.getLatitude());
            } else {
                stmt.setNull(11, Types.DOUBLE);
            }

            if (seance.getLongitude() != null) {
                stmt.setDouble(12, seance.getLongitude());
            } else {
                stmt.setNull(12, Types.DOUBLE);
            }
            
            if ("Code".equals(seance.getType())) {
                stmt.setString(13, seance.getSalle());
            } else if ("Conduite".equals(seance.getType())) {
                String quartier = seance.getQuartier();
                if (quartier == null || quartier.isEmpty()) {
                    if (seance.getLatitude() != null && seance.getLongitude() != null) {
                        quartier = "Position: " + seance.getLatitude() + ", " + seance.getLongitude();
                    } else {
                        quartier = "Lieu non spécifié";
                    }
                }
                stmt.setString(13, quartier);
            } else {
                stmt.setString(13, seance.getSalle());
            }

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        seance.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving seance: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean update(Seance seance) {
        String sql = "UPDATE seance SET type = ?, candidat_id = ?, moniteur_id = ?, vehicule_id = ?, " +
                "date = ?, heure = ?, kilometrage_debut = ?, statut = ?, commentaire = ?, " +
                "latitude = ?, longitude = ?, lieu = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, seance.getType());
            stmt.setLong(2, seance.getCandidatId());
            stmt.setLong(3, seance.getMoniteurId());

            if (seance.getVehiculeId() != null) {
                stmt.setLong(4, seance.getVehiculeId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            stmt.setString(5, seance.getDate());
            stmt.setString(6, seance.getHeure());

            if (seance.getKilometrage() != null) {
                stmt.setInt(7, seance.getKilometrage().intValue());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            stmt.setString(8, seance.getStatus());
            stmt.setString(9, seance.getCommentaire());

            if (seance.getLatitude() != null) {
                stmt.setDouble(10, seance.getLatitude());
            } else {
                stmt.setNull(10, Types.DOUBLE);
            }

            if (seance.getLongitude() != null) {
                stmt.setDouble(11, seance.getLongitude());
            } else {
                stmt.setNull(11, Types.DOUBLE);
            }

            if ("Code".equals(seance.getType())) {
                stmt.setString(12, seance.getSalle());
            } else if ("Conduite".equals(seance.getType())) {
                String quartier = seance.getQuartier();
                if (quartier == null || quartier.isEmpty()) {
                    if (seance.getLatitude() != null && seance.getLongitude() != null) {
                        quartier = "Position: " + seance.getLatitude() + ", " + seance.getLongitude();
                    } else {
                        quartier = "Lieu non spécifié";
                    }
                }
                stmt.setString(12, quartier);
            } else {
                stmt.setString(12, seance.getSalle());
            }

            stmt.setLong(13, seance.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating seance: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateStatus(Long id, String status, String commentaire) {
        String sql = "UPDATE seance SET statut = ?, commentaire = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setString(2, commentaire);
            stmt.setLong(3, id);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating seance status: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean deleteById(Long id) {
        String sql = "DELETE FROM seance WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting seance with ID " + id, e);
            return false;
        }
    }

    /**
     * Helper method to convert Integer to Double from ResultSet
     * Used for kilometrage_debut column which is INTEGER in DB but Double in Java model
     */
    private Double getDoubleFromInt(ResultSet rs, String columnName) throws SQLException {
        int value = rs.getInt(columnName);
        if (rs.wasNull()) {
            return null;
        }
        return (double) value;
    }
}