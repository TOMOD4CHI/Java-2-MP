package org.cpi2.repository;

import org.cpi2.entitties.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Payment Repository
public class PaiementRepository extends BaseRepository<Paiement> {
    public List<Paiement> findAll() {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement ORDER BY date_paiement DESC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Paiement paiement = mapResultSetToPaiement(rs);
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all paiements", e);
        }
        return paiements;
    }
    public Optional<Paiement> findById(Long id) {
        String sql = "SELECT * FROM paiement WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Paiement paiement = mapResultSetToPaiement(rs);
                return Optional.of(paiement);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding paiement by ID", e);
        }
        return Optional.empty();
    }

    public List<Paiement> findAllByCandidat(Long candidatId) {
        List<Paiement> paiements = new ArrayList<>();
        String sql = "SELECT * FROM paiement p inner join inscription i on p.inscription_id = i.id WHERE id_candidat = ? ORDER BY date_paiement DESC";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Paiement paiement = mapResultSetToPaiement(rs);
                //loadTranches(conn, paiement);
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding paiements by candidat", e);
        }
        return paiements;
    }

    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        if (rs.getObject("id_examen",Integer.class) == null){
            ModePaiement modePaiement = ModePaiement.valueOf(rs.getString("mode_paiement"));
            return new PaiementInscription(
                rs.getLong("id"),
                rs.getDouble("montant"),
                rs.getTimestamp("date_paiement").toLocalDateTime(),
                modePaiement,
                new InscriptionRepository().findById(rs.getInt("id_inscription")).orElseThrow(),
                rs.getString("type_paiement")
        );
        }
        else{
            return new PaiementExamen(
                rs.getLong("id"),
                rs.getDouble("montant"),
                rs.getTimestamp("date_paiement").toLocalDateTime(),
                null,
                new ExamenRepository().findById(rs.getInt("id_examen")).orElseThrow()
        );
        }
    }
    //Take in mind that its possible to pay with tranches only for inscription (so this method is safe)
    public List<PaiementInscription> getTranches(int inscriptionId) throws SQLException {
        String sql = "SELECT * FROM tranche_paiement WHERE inscription_id = ? ORDER BY date_paiement ASC";
        List<PaiementInscription> tranchePaiements = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, inscriptionId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Paiement paiement = mapResultSetToPaiement(rs);
                tranchePaiements.add((PaiementInscription) paiement);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all paiements", e);
            return null;
        }
        return tranchePaiements;
    }

    public boolean save(Paiement paiement) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String paiementSql = """
                INSERT INTO paiement (inscription_id,id_examen,type_paiement, montant, date_paiement,mode_paiement)
                VALUES (?, ?, ?, ?,?,?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(paiementSql, Statement.RETURN_GENERATED_KEYS)) {
                if (paiement instanceof PaiementInscription) {
                    PaiementInscription paiementInscription = (PaiementInscription) paiement;
                    stmt.setLong(1, paiementInscription.getInscription().getId());
                    stmt.setNull(2, Types.INTEGER);
                    stmt.setString(3, paiementInscription.getTypePaiement());
                } else {
                    PaiementExamen paiementExamen = (PaiementExamen) paiement;
                    stmt.setNull(1, Types.INTEGER);
                    stmt.setLong(2, paiementExamen.getTypeExamen().getId());
                    stmt.setNull(3, Types.VARCHAR);
                }
                stmt.setDouble(4, paiement.getMontant());
                stmt.setTimestamp(5, Timestamp.valueOf(paiement.getDatePaiement()));
                stmt.setString(6, paiement.getModePaiement().name());

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long paiementId = generatedKeys.getLong(1);
                            paiement.setId(paiementId);
                            conn.commit();
                            return true;
                        }
                    }
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving paiement", e);
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    LOGGER.log(Level.SEVERE, "Error rolling back transaction", ex);
                }
            }
            return false;
        } finally {
            closeQuietly(conn);
        }
    }

}