package org.cpi2.repository;

import org.cpi2.entities.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;

// Payment Repository
public class PaiementRepository extends BaseRepository<Paiement> {
    private final CandidatRepository candidatRepository = new CandidatRepository();
    private final  ExamenRepository examenRepository = new ExamenRepository();
    private final InscriptionRepository inscriptionRepository = new InscriptionRepository();
    private  List<Candidat> candidats = candidatRepository.findAll();
    private List<Inscription> inscriptions = inscriptionRepository.findAll();
    private List<Examen> examens = examenRepository.findAll();

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
        }
        return paiements;
    }

    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {

        long id = rs.getLong("id");
        LocalDate datePaiement = rs.getDate("date_paiement").toLocalDate();
        double montant = rs.getDouble("montant");
        String typePaiement = rs.getString("type_paiement");
        String notes = rs.getString("notes");
        long candidatId = rs.getLong("id_candidat");
        ModePaiement modePaiement = ModePaiement.valueOf(rs.getString("mode_paiement"));

        Long idExamen = rs.getObject("id_examen", Long.class);
        Integer idInscription = rs.getObject("inscription_id", Integer.class);

        Candidat candidat = candidats.stream()
                .filter(c -> c.getId() == candidatId)
                .findFirst()
                .orElseThrow();

        if (idExamen == null) {
            return new PaiementInscription(
                    StatutPaiement.COMPLETE,
                    id,
                    candidat,
                    montant,
                    datePaiement,
                    modePaiement,
                    inscriptions.stream()
                            .filter(inscription -> inscription.getId() == idInscription)
                            .findFirst()
                            .orElseThrow(),
                    typePaiement,
                    notes
            );
        } else {
            return new PaiementExamen(
                    StatutPaiement.COMPLETE,
                    id,
                    candidat,
                    montant,
                    datePaiement,
                    modePaiement,
                    examens.stream()
                            .filter(examen -> Objects.equals(examen.getId(), idExamen))
                            .findFirst()
                            .orElseThrow(),
                    notes
            );
        }
    }
    //Take in mind that its possible to pay with tranches only for inscription (so this method is safe)
    public List<PaiementInscription> getTranches(int inscriptionId) throws SQLException {
        String sql = "SELECT * FROM paiement WHERE inscription_id = ? ORDER BY date_paiement ASC";
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
                INSERT INTO paiement (id_candidat,inscription_id,id_examen,type_paiement, montant, date_paiement,mode_paiement,notes,statut)
                VALUES (?,?,?, ?, ?,?,?,?,?)
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
                stmt.setString(9,paiement.getStatut().name());
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
    public boolean update(Paiement paiement) {
        String sql = "UPDATE paiement SET montant = ?, date_paiement = ?, mode_paiement = ?, notes = ? statut=? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDouble(1, paiement.getMontant());
            stmt.setDate(2, Date.valueOf(paiement.getDatePaiement()));
            stmt.setString(3, paiement.getModePaiement().name());
            stmt.setString(4, paiement.getDescription());
            stmt.setString(5,paiement.getStatut().name());
            stmt.setLong(6, paiement.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating paiement", e);
            return false;
        }
    }
    public boolean delete(long  id) {
        String sql = "DELETE FROM paiement WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting paiement", e);
            return false;
        }
    }

}