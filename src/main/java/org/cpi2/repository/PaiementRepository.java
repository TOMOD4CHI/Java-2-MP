package org.cpi2.repository;

import org.cpi2.entities.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Payment Repository
public class PaiementRepository extends BaseRepository<Paiement> {
    private final CandidatRepository candidatRepository = new CandidatRepository();
    private final  ExamenRepository examenRepository = new ExamenRepository();
    private final InscriptionRepository inscriptionRepository = new InscriptionRepository();

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
        String sql = "SELECT * FROM paiement WHERE id_candidat = ? ORDER BY date_paiement DESC";

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
        ModePaiement modePaiement = ModePaiement.valueOf(rs.getString("mode_paiement"));
        if (rs.getObject("id_examen",Integer.class) == null){
            return new PaiementInscription(
                rs.getLong("id"),
                candidatRepository.findById(rs.getLong("id_candidat")).orElseThrow(),
                rs.getDouble("montant"),
                rs.getDate("date_paiement").toLocalDate(),
                modePaiement,
                inscriptionRepository.findById(rs.getInt("id_inscription")).orElseThrow(),
                rs.getString("type_paiement"),
                rs.getString("notes")
        );
        }
        else{
            return new PaiementExamen(
                rs.getLong("id"),
                candidatRepository.findById(rs.getLong("id_candidat")).orElseThrow(),
                rs.getDouble("montant"),
                    rs.getDate("date_paiement").toLocalDate(),
                modePaiement,
                examenRepository.findById((long) rs.getInt("id_examen")).orElseThrow(),
                rs.getString("notes")
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
                INSERT INTO paiement (id_candidat,inscription_id,id_examen,type_paiement, montant, date_paiement,mode_paiement,notes)
                VALUES (?,?,?, ?, ?,?,?,?)
             """;

            try (PreparedStatement stmt = conn.prepareStatement(paiementSql, Statement.RETURN_GENERATED_KEYS)) {
                if (paiement instanceof PaiementInscription) {
                    PaiementInscription paiementInscription = (PaiementInscription) paiement;
                    stmt.setLong(2, paiementInscription.getInscription().getId());
                    stmt.setNull(3, Types.INTEGER);
                    if(paiementInscription.getTypePaiement() == null){
                        stmt.setNull(4, Types.VARCHAR);
                    }
                    else {
                        stmt.setString(4, paiementInscription.getTypePaiement());
                    }
                } else {
                    PaiementExamen paiementExamen = (PaiementExamen) paiement;
                    stmt.setNull(2, Types.INTEGER);
                    stmt.setLong(3, paiementExamen.getTypeExamen().getId());
                    stmt.setNull(4, Types.VARCHAR);
                }
                stmt.setLong(1, paiement.getCandidat().getId());
                stmt.setDouble(5, paiement.getMontant());
                stmt.setDate(6, Date.valueOf(paiement.getDatePaiement()));
                stmt.setString(7, paiement.getModePaiement().name());
                stmt.setString(8, paiement.getDescription());


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