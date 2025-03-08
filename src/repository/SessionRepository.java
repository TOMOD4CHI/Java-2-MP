package repository;

import entitties.*;

import java.sql.*;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

// Session Repository
public class SessionRepository extends BaseRepository<Session> {

    public List<SessionCode> findAllCodeSessions() {
        List<SessionCode> sessions = new ArrayList<>();
        String sql = """
        SELECT sc.*, m.id as moniteur_id, m.nom as moniteur_nom, m.prenom as moniteur_prenom,
               m.cin as moniteur_cin, m.telephone as moniteur_telephone, m.email as moniteur_email,
               p.id as plan_id, p.libelle as plan_libelle, p.prix as plan_prix
        FROM session_code sc
        JOIN moniteur m ON sc.moniteur_id = m.id
        JOIN plan p ON sc.plan_id = p.id
        ORDER BY sc.date_session, sc.heure_debut
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sessions.add(mapResultSetToSessionCode(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all code sessions", e);
        }
        return sessions;
    }

    public List<SessionConduite> findAllConduiteSessions() {
        List<SessionConduite> sessions = new ArrayList<>();
        String sql = """
        SELECT sco.*, m.id as moniteur_id, m.nom as moniteur_nom, m.prenom as moniteur_prenom,
               m.cin as moniteur_cin, m.telephone as moniteur_telephone, m.email as moniteur_email,
               v.id as vehicule_id, v.immatriculation, v.marque, v.modele,
               p.id as plan_id, p.libelle as plan_libelle, p.prix as plan_prix
        FROM session_conduite sco
        JOIN moniteur m ON sco.moniteur_id = m.id
        JOIN vehicule v ON sco.vehicule_id = v.id
        JOIN plan p ON sco.plan_id = p.id
        ORDER BY sco.date_session, sco.heure_debut
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sessions.add(mapResultSetToSessionConduite(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error fetching all conduite sessions", e);
        }
        return sessions;
    }

    public Optional<SessionCode> findCodeSessionById(Long id) {
        String sql = """
            SELECT sc.*, m.id as moniteur_id, m.nom as moniteur_nom, m.prenom as moniteur_prenom,
                   m.cin as moniteur_cin, m.telephone as moniteur_telephone, m.email as moniteur_email,
                   p.id as plan_id, p.libelle as plan_libelle, p.prix as plan_prix
            FROM session_code sc
            JOIN moniteur m ON sc.moniteur_id = m.id
            JOIN plan p ON sc.plan_id = p.id
            WHERE sc.id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSessionCode(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding code session by ID", e);
        }
        return Optional.empty();
    }

    public Optional<SessionConduite> findConduiteSessionById(Long id) {
        String sql = """
            SELECT sco.*, m.id as moniteur_id, m.nom as moniteur_nom, m.prenom as moniteur_prenom,
                  m.cin as moniteur_cin, m.telephone as moniteur_telephone, m.email as moniteur_email,
                  v.id as vehicule_id, v.immatriculation, v.marque, v.modele,
                  p.id as plan_id, p.libelle as plan_libelle, p.prix as plan_prix
            FROM session_conduite sco
            JOIN moniteur m ON sco.moniteur_id = m.id
            JOIN vehicule v ON sco.vehicule_id = v.id
            JOIN plan p ON sco.plan_id = p.id
            WHERE sco.id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToSessionConduite(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding conduite session by ID", e);
        }
        return Optional.empty();
    }

    public List<SessionCode> findAllCodeSessionsByMoniteur(Long moniteurId) {
        List<SessionCode> sessions = new ArrayList<>();
        String sql = """
            SELECT sc.*, p.id as plan_id, p.libelle as plan_libelle, p.prix as plan_prix
            FROM session_code sc
            JOIN plan p ON sc.plan_id = p.id
            WHERE sc.moniteur_id = ?
            ORDER BY sc.date_session, sc.heure_debut
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, moniteurId);
            ResultSet rs = stmt.executeQuery();

            MoniteurRepository moniteurRepo = new MoniteurRepository();
            Optional<Moniteur> moniteur = moniteurRepo.findById(moniteurId);

            if (moniteur.isPresent()) {
                while (rs.next()) {
                    SessionCode session = setupSCode(rs,moniteur.get());

                    session.setId(rs.getLong("id"));
                    session.setStatut(StatutSession.valueOf(rs.getString("statut_id")));
                    session.setCapaciteMax(rs.getInt("capacite_max"));
                    sessions.add(session);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding code sessions by moniteur", e);
        }
        return sessions;
    }

    public List<SessionConduite> findAllConduiteSessionsByMoniteur(Long moniteurId) {
        List<SessionConduite> sessions = new ArrayList<>();
        String sql = """
            SELECT sco.*, p.id as plan_id, p.libelle as plan_libelle, p.prix as plan_prix
            FROM session_conduite sco
            JOIN plan p ON sco.plan_id = p.id
            WHERE sco.moniteur_id = ?
            ORDER BY sco.date_session, sco.heure_debut
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, moniteurId);
            ResultSet rs = stmt.executeQuery();

            MoniteurRepository moniteurRepo = new MoniteurRepository();
            VehiculeRepository vehiculeRepo = new VehiculeRepository();
            Optional<Moniteur> moniteur = moniteurRepo.findById(moniteurId);

            if (moniteur.isPresent()) {
                while (rs.next()) {
                    Optional<Vehicule> vehicule = vehiculeRepo.findById(rs.getLong("vehicule_id"));
                    if (vehicule.isPresent()) {
                        SessionConduite session = setupSCon(rs,moniteur.get(),vehicule.get());

                        session.setId(rs.getLong("id"));
                        session.setStatut(StatutSession.valueOf(rs.getString("statut_id")));
                        session.setKilometresParcourus(rs.getInt("kilometres_parcourus"));
                        sessions.add(session);
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding conduite sessions by moniteur", e);
        }
        return sessions;
    }

    private SessionCode setupSCode(ResultSet rs,Moniteur moniteur)throws SQLException{
        LocalTime heureDebut = rs.getTime("heure_debut").toLocalTime();
        LocalTime heureFin = rs.getTime("heure_fin").toLocalTime();
        return new SessionCode(
                rs.getDate("date_session").toLocalDate(),
                heureDebut,
                Duration.between(heureDebut,heureFin).toMinutes(),
                moniteur,
                rs.getInt("plan_id"),
                10
        );
    }
    private SessionCode mapResultSetToSessionCode(ResultSet rs) throws SQLException {
        Moniteur moniteur = new Moniteur();
        moniteur.setId(rs.getLong("moniteur_id"));
        moniteur.setNom(rs.getString("moniteur_nom"));
        moniteur.setPrenom(rs.getString("moniteur_prenom"));
        moniteur.setCin(rs.getString("moniteur_cin"));
        moniteur.setTelephone(rs.getString("moniteur_telephone"));
        moniteur.setEmail(rs.getString("moniteur_email"));

        SessionCode session= setupSCode(rs,moniteur);
        session.setId(rs.getLong("id"));
        session.setStatut(StatutSession.valueOf(rs.getString("statut_id")));
        return session;
    }
    private SessionConduite setupSCon(ResultSet rs,Moniteur moniteur,Vehicule vehicule)throws SQLException{
        LocalTime heureDebut = rs.getTime("heure_debut").toLocalTime();
        LocalTime heureFin = rs.getTime("heure_fin").toLocalTime();
        return new SessionConduite(
                rs.getLong("plan_id"),
                rs.getDate("date_session").toLocalDate(),
                heureDebut,
                Duration.between(heureDebut,heureFin).toMinutes(),
                moniteur,
                vehicule,
                getPointRencontre(rs)
        );
    }
    private SessionConduite mapResultSetToSessionConduite(ResultSet rs) throws SQLException {
        Moniteur moniteur = new Moniteur();
        moniteur.setId(rs.getLong("moniteur_id"));
        moniteur.setNom(rs.getString("moniteur_nom"));
        moniteur.setPrenom(rs.getString("moniteur_prenom"));
        moniteur.setCin(rs.getString("moniteur_cin"));
        moniteur.setTelephone(rs.getString("moniteur_telephone"));
        moniteur.setEmail(rs.getString("moniteur_email"));

        Vehicule vehicule = new Vehicule();
        vehicule.setId(rs.getLong("vehicule_id"));
        vehicule.setImmatriculation(rs.getString("immatriculation"));
        vehicule.setMarque(rs.getString("marque"));
        vehicule.setModele(rs.getString("modele"));

        SessionConduite session = setupSCon(rs,moniteur,vehicule);

        session.setId(rs.getLong("id"));
        session.setStatut(StatutSession.valueOf(rs.getString("statut_id")));
        session.setKilometresParcourus(rs.getInt("kilometres_parcourus"));

        return session;
    }

    private Location getPointRencontre(ResultSet rs) throws SQLException {
        Location pointRencontre = new Location(rs.getDouble("point_rencontre_lat"), rs.getDouble("point_rencontre_lon"));
        return pointRencontre;
    }

    public boolean saveCodeSession(SessionCode session) {
        String sql = """
            INSERT INTO session_code 
            (plan_id, date_session, heure_debut, heure_fin, moniteur_id, 
             statut_id, capacite_max)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, session.getPlanId());
            stmt.setDate(2, Date.valueOf(session.getDateSession()));
            stmt.setTime(3, Time.valueOf(session.getHeureSession()));
            stmt.setTime(4, Time.valueOf(session.getHeureSession().plus(Duration.ofMinutes(session.getDuree()))));
            stmt.setLong(5, session.getMoniteur().getId());
            stmt.setString(6, session.getStatut().name());
            stmt.setInt(7, session.getCapaciteMax());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving code session", e);
            return false;
        }
    }

    public boolean saveConduiteSession(SessionConduite session) {
        String sql = """
            INSERT INTO session_conduite 
            (plan_id, date_session, heure_debut, heure_fin, moniteur_id, vehicule_id,
             point_rencontre_lat, point_rencontre_lon, kilometres_parcourus, statut_id)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, session.getPlanId());
            stmt.setDate(2, Date.valueOf(session.getDateSession()));
            stmt.setTime(3, Time.valueOf(session.getHeureSession()));
            stmt.setTime(4, Time.valueOf(session.getHeureSession().plus(Duration.ofMinutes(session.getDuree()))));
            stmt.setLong(5, session.getMoniteur().getId());
            stmt.setLong(6, session.getVehicule().getId());

            // Handle point_rencontre which might be in "lat,lon" format
            Location pointRencontre = session.getPointRencontre();
            if (pointRencontre != null) {
                stmt.setDouble(7, pointRencontre.getLatitude());
                stmt.setDouble(8, pointRencontre.getLongitude());
            } else {
                stmt.setNull(7, Types.DECIMAL);
                stmt.setNull(8, Types.DECIMAL);
            }

            stmt.setInt(9, session.getKilometresParcourus());
            stmt.setString(10, session.getStatut().name());

            if (stmt.executeUpdate() > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        session.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving conduite session", e);
            return false;
        }
    }

    public boolean updateCodeSession(SessionCode session) {
        String sql = """
            UPDATE session_code 
            SET plan_id = ?, date_session = ?, heure_debut = ?, heure_fin = ?, 
                moniteur_id = ?, statut_id = ?, capacite_max = ?
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, session.getPlanId());
            stmt.setDate(2, Date.valueOf(session.getDateSession()));
            stmt.setTime(3, Time.valueOf(session.getHeureSession()));
            stmt.setTime(4, Time.valueOf(session.getHeureSession().plus(Duration.ofMinutes(session.getDuree()))));
            stmt.setLong(5, session.getMoniteur().getId());
            stmt.setString(6, session.getStatut().name());
            stmt.setInt(7, session.getCapaciteMax());
            stmt.setLong(8, session.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating code session", e);
            return false;
        }
    }

    public boolean updateConduiteSession(SessionConduite session) {
        String sql = """
            UPDATE session_conduite 
            SET plan_id = ?, date_session = ?, heure_debut = ?, heure_fin = ?, 
                moniteur_id = ?, vehicule_id = ?, point_rencontre_lat = ?, 
                point_rencontre_lon = ?, kilometres_parcourus = ?, statut_id = ?
            WHERE id = ?
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, session.getPlanId());
            stmt.setDate(2, Date.valueOf(session.getDateSession()));
            stmt.setTime(3, Time.valueOf(session.getHeureSession()));
            stmt.setTime(4, Time.valueOf(session.getHeureSession().plus(Duration.ofMinutes(session.getDuree()))));
            stmt.setLong(5, session.getMoniteur().getId());
            stmt.setLong(6, session.getVehicule().getId());
            stmt.setDouble(7, session.getPointRencontre().getLatitude());
            stmt.setDouble(8, session.getPointRencontre().getLongitude());

            stmt.setInt(9, session.getKilometresParcourus());
            stmt.setString(10, session.getStatut().name());
            stmt.setLong(11, session.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating conduite session", e);
            return false;
        }
    }

    public boolean deleteCodeSession(Long id) {
        String sql = "DELETE FROM session_code WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting code session", e);
            return false;
        }
    }

    public boolean deleteConduiteSession(Long id) {
        String sql = "DELETE FROM session_conduite WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting conduite session", e);
            return false;
        }
    }
}