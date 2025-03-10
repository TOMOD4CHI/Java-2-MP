package org.cpi2.repository;

import entitties.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Session Repository
public class SessionRepository extends BaseRepository<Session> {

    public Optional<Session> findById(Long id) {
        String sql = """
            SELECT s.*, sc.salle, sc.capacite_max, 
                   sco.id_vehicule, sco.point_rencontre, sco.kilometres_parcourus
            FROM session s
            LEFT JOIN session_code sc ON s.id = sc.id_session
            LEFT JOIN session_conduite sco ON s.id = sco.id_session
            WHERE s.id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding session by ID", e);
        }
        return Optional.empty();
    }

    public List<Session> findAllByMoniteur(Long moniteurId) {
        List<Session> sessions = new ArrayList<>();
        String sql = """
            SELECT s.*, sc.salle, sc.capacite_max, 
                   sco.id_vehicule, sco.point_rencontre, sco.kilometres_parcourus
            FROM session s
            LEFT JOIN session_code sc ON s.id = sc.id_session
            LEFT JOIN session_conduite sco ON s.id = sco.id_session
            WHERE s.id_moniteur = ?
            ORDER BY s.date_session, s.heure_session
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, moniteurId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                sessions.add(mapResultSetToSession(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding sessions by moniteur", e);
        }
        return sessions;
    }

    private Session mapResultSetToSession(ResultSet rs) throws SQLException {
        TypeSession typeSession = TypeSession.valueOf(rs.getString("type_session"));
        MoniteurRepository moniteurRepo = new MoniteurRepository();

        if (typeSession == TypeSession.CODE) {
            SessionCode session = new SessionCode(
                    rs.getDate("date_session").toLocalDate(),
                    rs.getTime("heure_session").toLocalTime(),
                    moniteurRepo.findById(rs.getLong("id_moniteur")).orElseThrow(),
                    rs.getDouble("prix"),
                    rs.getString("salle"),
                    rs.getInt("capacite_max")
            );
            session.setId(rs.getLong("id"));
            session.setStatut(StatutSession.valueOf(rs.getString("statut")));
            return session;
        } else {
            VehiculeRepository vehiculeRepo = new VehiculeRepository();
            SessionConduite session = new SessionConduite(
                    rs.getDate("date_session").toLocalDate(),
                    rs.getTime("heure_session").toLocalTime(),
                    moniteurRepo.findById(rs.getLong("id_moniteur")).orElseThrow(),
                    rs.getDouble("prix"),
                    vehiculeRepo.findById(rs.getLong("id_vehicule")).orElseThrow(),
                    rs.getString("point_rencontre")
            );
            session.setId(rs.getLong("id"));
            session.setStatut(StatutSession.valueOf(rs.getString("statut")));
            session.setKilometresParcourus(rs.getInt("kilometres_parcourus"));
            return session;
        }
    }

    public boolean save(Session session) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Insert into session
            String sessionSql = """
                INSERT INTO session (date_session, heure_session, id_moniteur, prix, statut, type_session)
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setDate(1, Date.valueOf(session.getDateSession()));
                stmt.setTime(2, Time.valueOf(session.getHeureSession()));
                stmt.setLong(3, session.getMoniteur().getId());
                stmt.setDouble(4, session.getPrix());
                stmt.setString(5, session.getStatut().name());
                stmt.setString(6, session.getTypeSession().name());

                if (stmt.executeUpdate() > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            Long sessionId = generatedKeys.getLong(1);
                            session.setId(sessionId);

                            // Insert specific session type data
                            if (session instanceof SessionCode) {
                                String codeSql = "INSERT INTO session_code (id_session, salle, capacite_max) VALUES (?, ?, ?)";
                                try (PreparedStatement codeStmt = conn.prepareStatement(codeSql)) {
                                    SessionCode sessionCode = (SessionCode) session;
                                    codeStmt.setLong(1, sessionId);
                                    codeStmt.setString(2, sessionCode.getSalle());
                                    codeStmt.setInt(3, sessionCode.getCapaciteMax());
                                    codeStmt.executeUpdate();
                                }
                            } else if (session instanceof SessionConduite) {
                                String conduiteSql = """
                                    INSERT INTO session_conduite 
                                    (id_session, id_vehicule, point_rencontre, kilometres_parcourus)
                                    VALUES (?, ?, ?, ?)
                                """;
                                try (PreparedStatement conduiteStmt = conn.prepareStatement(conduiteSql)) {
                                    SessionConduite sessionConduite = (SessionConduite) session;
                                    conduiteStmt.setLong(1, sessionId);
                                    conduiteStmt.setLong(2, sessionConduite.getVehicule().getId());
                                    conduiteStmt.setString(3, sessionConduite.getPointRencontre());
                                    conduiteStmt.setInt(4, sessionConduite.getKilometresParcourus());
                                    conduiteStmt.executeUpdate();
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
            LOGGER.log(Level.SEVERE, "Error saving session", e);
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

    public boolean update(Session session) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Update session
            String sessionSql = """
                UPDATE session 
                SET date_session = ?, heure_session = ?, id_moniteur = ?, 
                    prix = ?, statut = ?, type_session = ?
                WHERE id = ?
            """;

            try (PreparedStatement stmt = conn.prepareStatement(sessionSql)) {
                stmt.setDate(1, Date.valueOf(session.getDateSession()));
                stmt.setTime(2, Time.valueOf(session.getHeureSession()));
                stmt.setLong(3, session.getMoniteur().getId());
                stmt.setDouble(4, session.getPrix());
                stmt.setString(5, session.getStatut().name());
                stmt.setString(6, session.getTypeSession().name());
                stmt.setLong(7, session.getId());

                if (stmt.executeUpdate() > 0) {
                    // Update specific session type data
                    if (session instanceof SessionCode) {
                        String codeSql = """
                            UPDATE session_code 
                            SET salle = ?, capacite_max = ?
                            WHERE id_session = ?
                        """;
                        try (PreparedStatement codeStmt = conn.prepareStatement(codeSql)) {
                            SessionCode sessionCode = (SessionCode) session;
                            codeStmt.setString(1, sessionCode.getSalle());
                            codeStmt.setInt(2, sessionCode.getCapaciteMax());
                            codeStmt.setLong(3, session.getId());
                            codeStmt.executeUpdate();
                        }
                    } else if (session instanceof SessionConduite) {
                        String conduiteSql = """
                            UPDATE session_conduite 
                            SET id_vehicule = ?, point_rencontre = ?, kilometres_parcourus = ?
                            WHERE id_session = ?
                        """;
                        try (PreparedStatement conduiteStmt = conn.prepareStatement(conduiteSql)) {
                            SessionConduite sessionConduite = (SessionConduite) session;
                            conduiteStmt.setLong(1, sessionConduite.getVehicule().getId());
                            conduiteStmt.setString(2, sessionConduite.getPointRencontre());
                            conduiteStmt.setInt(3, sessionConduite.getKilometresParcourus());
                            conduiteStmt.setLong(4, session.getId());
                            conduiteStmt.executeUpdate();
                        }
                    }

                    conn.commit();
                    return true;
                }
            }

            conn.rollback();
            return false;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating session", e);
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
