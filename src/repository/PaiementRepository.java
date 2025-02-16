package repository;

import entitties.Paiement;

import entitties.StatutPaiement;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Payment Repository
public class PaiementRepository extends BaseRepository<Paiement> {

    public Optional<Paiement> findById(Long id) {
        String sql = "SELECT * FROM paiement WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Paiement paiement = mapResultSetToPaiement(rs);
                loadTranches(conn, paiement);
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
                loadTranches(conn, paiement);
                paiements.add(paiement);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding paiements by candidat", e);
        }
        return paiements;
    }

    private Paiement mapResultSetToPaiement(ResultSet rs) throws SQLException {
        return new Paiement(
                rs.getLong("id"),
                new CandidatRepository().findById(rs.getLong("id_candidat")).orElseThrow(),
                rs.getDouble("montant"),
                rs.getTimestamp("date_paiement").toLocalDateTime(),
                TypePaiement.valueOf(rs.getString("type_paiement")),
                StatutPaiement.valueOf(rs.getString("statut"))
        );
    }

    private void loadTranches(Connection conn, Paiement paiement) throws SQLException {
        String sql = "SELECT * FROM tranche_paiement WHERE id_paiement = ? ORDER BY numero_tranche";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, paiement.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                TranchePaiement tranche = new TranchePaiement(
                        rs.getInt("numero_tranche"),
                        rs.getDouble("montant"),
                        rs.getDate("date_echeance").toLocalDate(),
                        StatutPaiement.valueOf(rs.getString("statut"))
                );
                tranche.setId(rs.getLong("id"));
                paiement.getTranches().add(tranche);
            }
        }
    }

    public boolean save(Paiement paiement) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            String paiementSql = """
                INSERT INTO paiement (id_candidat, montant, date_paiement, type_paiement, statut)
                VALUES (?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(paiementSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, paiement.getCandidat().getId());
                stmt.setDouble(2, paiement.getMontant());
                stmt.setTimestamp(3, Timestamp.valueOf(paiement.getDatePaiement()));
                stmt.setString(4, paiement.getTypePaiement().name());
                stmt.setString(5, paiement.getStatut().name());

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long paiementId = generatedKeys.getLong(1);
                            paiement.setId(paiementId);

                            // Save tranches if any
                            if (!paiement.getTranches().isEmpty()) {
                                String trancheSql = """
                                    INSERT INTO tranche_paiement 
                                    (id_paiement, numero_tranche, montant, date_echeance, statut)
                                    VALUES (?, ?, ?, ?, ?)
                                """;

                                try (PreparedStatement trancheStmt = conn.prepareStatement(trancheSql)) {
                                    for (TranchePaiement tranche : paiement.getTranches()) {
                                        trancheStmt.setLong(1, paiementId);
                                        trancheStmt.setInt(2, tranche.getNumeroTranche());
                                        trancheStmt.setDouble(3, tranche.getMontant());
                                        trancheStmt.setDate(4, Date.valueOf(tranche.getDateEcheance()));
                                        trancheStmt.setString(5, tranche.getStatut().name());
                                        trancheStmt.addBatch();
                                    }
                                    trancheStmt.executeBatch();
                                }
                            }

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

    public boolean updateTrancheStatus(Long trancheId, StatutPaiement nouveauStatut) {
        String sql = "UPDATE tranche_paiement SET statut = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nouveauStatut.name());
            stmt.setLong(2, trancheId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating tranche status", e);
            return false;
        }
    }
}
