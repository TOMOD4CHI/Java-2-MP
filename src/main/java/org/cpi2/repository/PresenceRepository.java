package org.cpi2.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository class for managing presence records for candidates in sessions
 */
public class PresenceRepository {
    private static final Logger LOGGER = Logger.getLogger(PresenceRepository.class.getName());

    private Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    public List<Presence> getAllPresencesCode() {
        String sql = "SELECT * FROM presence_code";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Presence> presences = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                long sessionId = rs.getLong("session_code_id");
                long candidatId = rs.getLong("candidat_id");
                presences.add(new Presence(id, sessionId, candidatId));
            }
            return presences;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all presences", e);
            return null;
        }
    }
    public List<Presence> getAllPresencesConduite() {
        String sql = "SELECT * FROM presence_conduite";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            List<Presence> presences = new ArrayList<>();
            while (rs.next()) {
                long id = rs.getLong("id");
                long sessionId = rs.getLong("session_code_id");
                long candidatId = rs.getLong("candidat_id");
                presences.add(new Presence(id, sessionId, candidatId));
            }
            return presences;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all presences", e);
            return null;
        }
    }


    /**
     * Records a candidate's presence in a code session
     * @param sessionCodeId The ID of the code session
     * @param candidatId The ID of the candidate
     * @param present Whether the candidate was present (true) or absent (false)
     * @return true if the record was created successfully, false otherwise
     */
    public boolean recordCodePresence(long sessionCodeId, long candidatId, boolean present) {
        String sql = "INSERT INTO presence_code (session_code_id, candidat_id, present) VALUES (?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE present = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionCodeId);
            stmt.setLong(2, candidatId);
            stmt.setBoolean(3, present);
            stmt.setBoolean(4, present);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error recording code presence", e);
            return false;
        }
    }

    /**
     * Records a candidate's presence in a driving session
     * @param sessionConduiteId The ID of the driving session
     * @param candidatId The ID of the candidate
     * @param present Whether the candidate was present (true) or absent (false)
     * @return true if the record was created successfully, false otherwise
     */
    public boolean recordConduitePresence(long sessionConduiteId, long candidatId, boolean present) {
        String sql = "INSERT INTO presence_conduite (session_conduite_id, candidat_id, present) VALUES (?, ?, ?)"
                + " ON DUPLICATE KEY UPDATE present = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, sessionConduiteId);
            stmt.setLong(2, candidatId);
            stmt.setBoolean(3, present);
            stmt.setBoolean(4, present);

            int affectedRows = stmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error recording driving presence", e);
            return false;
        }
    }

    /**
     * Records a candidate's presence in a seance (generic session)
     * This method determines the type of session and calls the appropriate method
     * @param seanceId The ID of the seance
     * @param candidatId The ID of the candidate
     * @param seanceType The type of seance ("Code" or "Conduite")
     * @param present Whether the candidate was present (true) or absent (false)
     * @return true if the record was created successfully, false otherwise
     */
    public boolean recordPresenceForSeance(long seanceId, long candidatId, String seanceType, boolean present) {
        if ("Code".equalsIgnoreCase(seanceType)) {
            return recordCodePresence(seanceId, candidatId, present);
        } else if ("Conduite".equalsIgnoreCase(seanceType)) {
            return recordConduitePresence(seanceId, candidatId, present);
        } else {
            LOGGER.log(Level.WARNING, "Unknown seance type: " + seanceType);
            return false;
        }
    }
    public class Presence{
        long id;
        long sessionId;
        long candidatId;

        public Presence(long id, long sessionId, long candidatId) {
            this.id = id;
            this.sessionId = sessionId;
            this.candidatId = candidatId;
        }
        public long getId() {
            return id;
        }
        public long getSessionId() {
            return sessionId;
        }
        public long getCandidatId() {
            return candidatId;
        }
    }
}