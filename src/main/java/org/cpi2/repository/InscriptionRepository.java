package org.cpi2.repository;

import org.cpi2.entities.CoursePlan;
import org.cpi2.entities.Inscription;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class InscriptionRepository extends BaseRepository<Inscription> {
    private static final Logger LOGGER = Logger.getLogger(InscriptionRepository.class.getName());
    private final PlanRepository planRepository;
    private HashMap<Integer, CoursePlan> plans = new HashMap<>();

    public InscriptionRepository() {
        planRepository = new PlanRepository();
        for(CoursePlan plan : planRepository.findAll()) {
            plans.put(plan.getId(), plan);
        }
    }

    public Optional<Inscription> findById(Integer id) {
        String sql = """
            SELECT * FROM inscription 
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToInscription(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding inscription by ID", e);
        }
        return Optional.empty();
    }

    public List<Inscription> findAll() {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT * FROM inscription";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inscriptions.add(mapResultSetToInscription(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all inscriptions", e);
        }
        return inscriptions;
    }

    private Inscription mapResultSetToInscription(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String cin = rs.getString("cin");
        int planId = rs.getInt("plan_id");
        boolean paymentStatus = rs.getString("statut_paiement").equalsIgnoreCase("paid");
        String statut = rs.getString("statut");
        Date nextPaymentDate = rs.getDate("date_paiement_suivant");
        String cyclePaiement = rs.getString("cycle_paiement");
        Date dateInscription = rs.getDate("date_inscription");

        CoursePlan plan = plans.get(planId);

        return new Inscription(id, cin, plan, paymentStatus, statut, nextPaymentDate, cyclePaiement, dateInscription);
    }


    public boolean save(Inscription inscription) {
        String sql = """
            INSERT INTO inscription (cin, plan_id, statut, statut_paiement, cycle_paiement,date_inscription, date_paiement_suivant)
            VALUES (?,?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, inscription.getCin());
            stmt.setInt(2, inscription.getPlan().getId());
            stmt.setString(3, inscription.getStatus());
            stmt.setString(4, inscription.isPaymentStatus() ? "paid" : "unpaid");
            stmt.setString(5, inscription.getPaymentCycle());
            stmt.setDate(6, new Date(inscription.getInscriptioDate().getTime()));
            stmt.setDate(7, inscription.getnextPaymentDate() != null
                    ? new Date(inscription.getnextPaymentDate().getTime())
                    : null);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        inscription.setId(generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving inscription", e);
        }
        return false;
    }

    public boolean update(Inscription inscription) {
        String sql = """
            UPDATE inscription 
            SET cin = ?, plan_id = ?, statut = ?, staut_paiement = ?, 
                cycle_paiement = ?, date_paiement_suivant = ?
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, inscription.getCin());
            stmt.setInt(2, inscription.getPlan().getId());
            stmt.setString(3, inscription.getStatus());
            stmt.setString(4, inscription.isPaymentStatus() ? "paid" : "unpaid");
            stmt.setString(5, inscription.getPaymentCycle());
            stmt.setDate(6, inscription.getnextPaymentDate() != null
                    ? new Date(inscription.getnextPaymentDate().getTime())
                    : null);
            stmt.setInt(7, inscription.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating inscription", e);
            return false;
        }
    }

    public List<Inscription> findByCin(String cin) {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT * FROM inscription WHERE cin = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                inscriptions.add(mapResultSetToInscription(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding inscriptions by CIN", e);
        }
        return inscriptions;
    }

    public List<Inscription> findByPaymentStatus(boolean isPaid) {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT * FROM inscription WHERE staut_paiement = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, isPaid ? "paid" : "unpaid");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                inscriptions.add(mapResultSetToInscription(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding inscriptions by payment status", e);
        }
        return inscriptions;
    }
    public List<Inscription> findByStatus(String statut) {
        List<Inscription> inscriptions = new ArrayList<>();
        String sql = "SELECT * FROM inscription WHERE statut = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, statut);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                inscriptions.add(mapResultSetToInscription(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding inscriptions by payment status", e);
        }
        return inscriptions;
    }
}