package org.cpi2.repository;

import org.cpi2.entities.*;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

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
        SELECT s.*, m.id as moniteur_id, m.nom as moniteur_nom, m.prenom as moniteur_prenom,
               m.cin as moniteur_cin, m.telephone as moniteur_telephone, m.email as moniteur_email,
               v.id as vehicule_id, v.immatriculation, v.marque, v.modele
        FROM seance s
        JOIN moniteur m ON s.moniteur_id = m.id
        JOIN vehicule v ON s.vehicule_id = v.id
        WHERE s.type = 'Conduite'
        ORDER BY s.date, s.heure
    """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                try {
                    SessionConduite session = mapSeanceToSessionConduite(rs);
                    sessions.add(session);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Erreur lors du mapping d'une session conduite", e);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la récupération des sessions de conduite", e);
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
            SELECT s.*, m.id as moniteur_id, m.nom as moniteur_nom, m.prenom as moniteur_prenom,
                  m.cin as moniteur_cin, m.telephone as moniteur_telephone, m.email as moniteur_email,
                  v.id as vehicule_id, v.immatriculation, v.marque, v.modele
            FROM seance s
            JOIN moniteur m ON s.moniteur_id = m.id
            JOIN vehicule v ON s.vehicule_id = v.id
            WHERE s.id = ? AND s.type = 'Conduite'
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapSeanceToSessionConduite(rs));
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
            SELECT s.*, v.id as vehicule_id, v.immatriculation, v.marque, v.modele
            FROM seance s
            JOIN vehicule v ON s.vehicule_id = v.id
            WHERE s.moniteur_id = ? AND s.type = 'Conduite'
            ORDER BY s.date, s.heure
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
                    try {
                        SessionConduite session = new SessionConduite();
                        session.setId(rs.getLong("id"));
                        session.setDateSession(rs.getDate("date").toLocalDate());
                        session.setHeureSession(rs.getTime("heure").toLocalTime());
                        session.setDuree(rs.getInt("duree"));
                        session.setMoniteur(moniteur.get());
                        session.setTypeSession(TypeSession.CONDUITE);

                        Vehicule vehicule = new Vehicule();
                        vehicule.setId((int) rs.getLong("vehicule_id"));
                        vehicule.setImmatriculation(rs.getString("immatriculation"));
                        vehicule.setMarque(rs.getString("marque"));
                        vehicule.setModele(rs.getString("modele"));
                        session.setVehicule(vehicule);

                        String statutStr = rs.getString("statut");
                        try {
                            session.setStatut(StatutSession.valueOf(statutStr.toUpperCase()));
                        } catch (Exception e) {
                            session.setStatut(StatutSession.PLANIFIEE);
                        }

                        String lieu = rs.getString("lieu");
                        if (lieu != null && !lieu.isEmpty()) {
                            session.setPointRencontre(lieu);
                        } else {
                            Double lat = rs.getDouble("latitude");
                            Double lon = rs.getDouble("longitude");
                            if (!rs.wasNull() && lat != null && lon != null) {
                                session.setPointRencontre(lat + "," + lon);
                            } else {
                                session.setPointRencontre("Non défini");
                            }
                        }

                        Integer kmDebut = rs.getInt("kilometrage_debut");
                        if (!rs.wasNull()) {
                            session.setKilometresParcourus(kmDebut);
                        } else {
                            session.setKilometresParcourus(0);
                        }
                        
                        sessions.add(session);
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Erreur lors du mapping d'une séance de conduite par moniteur: " + e.getMessage(), e);
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
        long dureeMinutes = Duration.between(heureDebut,heureFin).toMinutes();
        int planId = rs.getInt("plan_id");
        int capaciteMax = rs.getInt("capacite_max");
        
        SessionCode session = new SessionCode(
                rs.getDate("date_session").toLocalDate(),
                heureDebut,
                dureeMinutes,
                moniteur,
                planId,
                capaciteMax
        );

        if (rs.getObject("plan_prix") != null) {
            session.setPrix(rs.getDouble("plan_prix"));
        }

        LOGGER.info("Session code créée avec succès: ID=" + rs.getLong("id") + ", Date=" + session.getDateSession());
        
        return session;
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
        session.setStatut(StatutSession.valueOf(rs.getString("statut")));
        return session;
    }
    private SessionConduite setupSCon(ResultSet rs, Moniteur moniteur, Vehicule vehicule) throws SQLException {
        try {
            LocalTime heureDebut = rs.getTime("heure_debut").toLocalTime();
            LocalTime heureFin = rs.getTime("heure_fin").toLocalTime();

            long dureeMinutes = java.time.Duration.between(heureDebut, heureFin).toMinutes();

            long planId = rs.getLong("plan_id");

            Location location = getPointRencontre(rs);

            SessionConduite session = new SessionConduite(
                    planId,
                    rs.getDate("date_session").toLocalDate(),
                    heureDebut,
                    dureeMinutes,
                    moniteur,
                    vehicule,
                    location
            );

            if (session.getPointRencontre() == null || session.getPointRencontre().equals("Non défini")) {
                String pointRencontre = rs.getString("point_rencontre");
                if (pointRencontre != null && !pointRencontre.isEmpty()) {
                    session.setPointRencontre(pointRencontre);
                }
            }

            if (session.getPrix() == null && rs.getObject("prix") != null) {
                session.setPrix(rs.getDouble("prix"));
            }

            LOGGER.info("Session conduite créée avec succès: ID=" + rs.getLong("id") + ", Date=" + session.getDateSession());
            
            return session;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création d'une session conduite", e);
            throw new SQLException("Erreur lors de la création d'une session conduite: " + e.getMessage(), e);
        }
    }
    private SessionConduite mapResultSetToSessionConduite(ResultSet rs) throws SQLException {
        try {
            Moniteur moniteur = new Moniteur();
            moniteur.setId(rs.getLong("moniteur_id"));
            moniteur.setNom(rs.getString("moniteur_nom"));
            moniteur.setPrenom(rs.getString("moniteur_prenom"));
            moniteur.setCin(rs.getString("moniteur_cin"));
            moniteur.setTelephone(rs.getString("moniteur_telephone"));
            moniteur.setEmail(rs.getString("moniteur_email"));

            Vehicule vehicule = new Vehicule();
            vehicule.setId((int) rs.getLong("vehicule_id"));
            vehicule.setImmatriculation(rs.getString("immatriculation"));
            vehicule.setMarque(rs.getString("marque"));
            vehicule.setModele(rs.getString("modele"));

            SessionConduite session = setupSCon(rs,moniteur,vehicule);

            session.setId(rs.getLong("id"));
            session.setStatut(StatutSession.valueOf(rs.getString("statut")));
            session.setKilometresParcourus(rs.getInt("kilometres_parcourus"));

            LOGGER.info("Session conduite créée: ID=" + session.getId() + ", Date=" + session.getDateSession() + ", Moniteur=" + session.getMoniteur().getNom());
            
            return session;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du mapping d'une session conduite", e);
            throw new SQLException("Erreur lors du mapping d'une session conduite: " + e.getMessage(), e);
        }
    }
    
    private SessionConduite mapSeanceToSessionConduite(ResultSet rs) throws SQLException {
        try {
            Moniteur moniteur = new Moniteur();
            moniteur.setId(rs.getLong("moniteur_id"));
            moniteur.setNom(rs.getString("moniteur_nom"));
            moniteur.setPrenom(rs.getString("moniteur_prenom"));
            moniteur.setCin(rs.getString("moniteur_cin"));
            moniteur.setTelephone(rs.getString("moniteur_telephone"));
            moniteur.setEmail(rs.getString("moniteur_email"));

            Vehicule vehicule = new Vehicule();
            vehicule.setId((int) rs.getLong("vehicule_id"));
            vehicule.setImmatriculation(rs.getString("immatriculation"));
            vehicule.setMarque(rs.getString("marque"));
            vehicule.setModele(rs.getString("modele"));

            LocalDate dateSession = rs.getDate("date").toLocalDate();
            LocalTime heureSession = rs.getTime("heure").toLocalTime();
            long dureeMinutes = rs.getInt("duree");

            SessionConduite session = new SessionConduite();
            session.setId(rs.getLong("id"));
            session.setDateSession(dateSession);
            session.setHeureSession(heureSession);
            session.setDuree(dureeMinutes);
            session.setMoniteur(moniteur);
            session.setVehicule(vehicule);
            session.setTypeSession(TypeSession.CONDUITE);

            String statutStr = rs.getString("statut");
            try {
                session.setStatut(StatutSession.valueOf(statutStr.toUpperCase()));
            } catch (Exception e) {
                session.setStatut(StatutSession.PLANIFIEE);
            }

            String lieu = rs.getString("lieu");
            if (lieu != null && !lieu.isEmpty()) {
                session.setPointRencontre(lieu);
            } else {
                Double lat = rs.getDouble("latitude");
                Double lon = rs.getDouble("longitude");
                if (!rs.wasNull() && lat != null && lon != null) {
                    session.setPointRencontre(lat + "," + lon);
                } else {
                    session.setPointRencontre("Non défini");
                }
            }

            Integer kmDebut = rs.getInt("kilometrage_debut");
            Integer kmFin = rs.getInt("kilometrage_fin");
            if (!rs.wasNull() && kmDebut != null && kmFin != null) {
                session.setKilometresParcourus(kmFin - kmDebut);
            } else if (!rs.wasNull() && kmDebut != null) {
                session.setKilometresParcourus(kmDebut);
            } else {
                session.setKilometresParcourus(0);
            }
            
            return session;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Erreur lors du mapping d'une seance en session conduite: " + e.getMessage(), e);
            throw new SQLException("Erreur lors du mapping d'une seance en session conduite: " + e.getMessage(), e);
        }
    }

    private Location getPointRencontre(ResultSet rs) throws SQLException {
        try {
            double lat = rs.getDouble("point_rencontre_lat");
            double lon = rs.getDouble("point_rencontre_lon");

            if (rs.wasNull() || (lat == 0.0 && lon == 0.0)) {
                return null;
            }
            
            return new Location(lat, lon);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Erreur lors de la récupération du point de rencontre", e);
            return null;
        }
    }
    
    /**
     * Affecte un candidat à une séance de code
     * @param sessionId L'ID de la session code
     * @param candidatId L'ID du candidat
     * @return true si l'affectation a réussi, false sinon
     */
    public boolean affecterCandidatASessionCode(long sessionId, long candidatId) {
        String checkSql = "SELECT capacite_max, nombre_inscrits FROM session_code WHERE id = ?";
        
        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
            
            checkStmt.setLong(1, sessionId);
            ResultSet rs = checkStmt.executeQuery();
            
            if (rs.next()) {
                int capaciteMax = rs.getInt("capacite_max");
                int nombreInscrits = rs.getInt("nombre_inscrits");
                
                if (nombreInscrits >= capaciteMax) {
                    LOGGER.warning("La session code est complète, capacité maximale atteinte");
                    return false;
                }

                String insertSql = "INSERT INTO presence_code (session_code_id, candidat_id, present) VALUES (?, ?, false)";
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setLong(1, sessionId);
                    insertStmt.setLong(2, candidatId);
                    insertStmt.executeUpdate();

                    String updateSql = "UPDATE session_code SET nombre_inscrits = nombre_inscrits + 1 WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setLong(1, sessionId);
                        updateStmt.executeUpdate();
                        return true;
                    }
                }
            } else {
                LOGGER.warning("Session code avec ID " + sessionId + " non trouvée");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'affectation du candidat à la session code", e);
        }
        
        return false;
    }
    
    /**
     * Affecte un candidat à une séance de conduite
     * @param seanceId L'ID de la séance de conduite
     * @param candidatId L'ID du candidat
     * @return true si l'affectation a réussi, false sinon
     */
    public boolean affecterCandidatASeanceConduite(long seanceId, long candidatId) {
        try (Connection conn = getConnection()) {
            String checkSql = "SELECT id FROM seance WHERE id = ? AND type = 'Conduite'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setLong(1, seanceId);
                ResultSet rs = checkStmt.executeQuery();
                
                if (!rs.next()) {
                    LOGGER.warning("Séance de conduite avec ID " + seanceId + " non trouvée");
                    return false;
                }
            }

            String updateSql = "UPDATE seance SET candidat_id = ? WHERE id = ?";
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setLong(1, candidatId);
                updateStmt.setLong(2, seanceId);
                int rowsAffected = updateStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    String insertSql = "INSERT INTO presence_conduite (session_conduite_id, candidat_id, present) VALUES (?, ?, false)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, seanceId);
                        insertStmt.setLong(2, candidatId);
                        insertStmt.executeUpdate();
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'affectation du candidat à la séance de conduite", e);
        }
        
        return false;
    }

    public boolean saveCodeSession(SessionCode session) {
        String sql = """
            INSERT INTO session_code 
            (plan_id, date_session, heure_debut, heure_fin, moniteur_id, 
             statut, capacite_max)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection()) {
            if (session.getMoniteur() == null || session.getMoniteur().getId() == null) {
                LOGGER.severe("Impossible d'enregistrer la session code: moniteur non défini ou sans ID");
                return false;
            }
            
            if (session.getDateSession() == null) {
                LOGGER.severe("Impossible d'enregistrer la session code: date non définie");
                return false;
            }
            
            if (session.getHeureSession() == null) {
                LOGGER.severe("Impossible d'enregistrer la session code: heure non définie");
                return false;
            }

            long planId = session.getPlanId();
            if (planId <= 0) {
                planId = 1;
                session.setPlanId(planId);
            }

            String checkPlanSql = "SELECT id FROM plan WHERE id = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPlanSql)) {
                checkStmt.setLong(1, planId);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) {
                    LOGGER.severe("Impossible d'enregistrer la session code: le plan avec ID " + planId + " n'existe pas");
                    return false;
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setLong(1, planId);
                stmt.setDate(2, Date.valueOf(session.getDateSession()));
                stmt.setTime(3, Time.valueOf(session.getHeureSession()));

                LocalTime heureFin = session.getHeureSession().plus(Duration.ofMinutes(session.getDuree()));
                stmt.setTime(4, Time.valueOf(heureFin));
                
                stmt.setLong(5, session.getMoniteur().getId());
                stmt.setString(6, session.getStatut().name());
                stmt.setInt(7, session.getCapaciteMax());
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long id = generatedKeys.getLong(1);
                            session.setId(id);
                            return true;
                        }
                    }
                }
                return false;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement de la session code: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception inattendue lors de l'enregistrement de la session code: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean saveConduiteSession(SessionConduite session) {
        String sql = """
            INSERT INTO seance 
            (type, date, heure, duree, moniteur_id, candidat_id, vehicule_id,
             lieu, statut, commentaire, kilometrage_debut, latitude, longitude)
            VALUES ('Conduite', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (session.getMoniteur() == null || session.getMoniteur().getId() == null) {
                LOGGER.severe("Impossible d'enregistrer la session conduite: moniteur non défini ou sans ID");
                return false;
            }
            
            if (session.getVehicule() == null || session.getVehicule().getId() == 0) {
                LOGGER.severe("Impossible d'enregistrer la session conduite: véhicule non défini ou sans ID");
                return false;
            }
            
            if (session.getDateSession() == null) {
                LOGGER.severe("Impossible d'enregistrer la session conduite: date non définie");
                return false;
            }
            
            if (session.getHeureSession() == null) {
                LOGGER.severe("Impossible d'enregistrer la session conduite: heure non définie");
                return false;
            }

            LOGGER.info("Tentative d'enregistrement d'une session conduite: " + 
                       "date=" + session.getDateSession() + 
                       ", heure=" + session.getHeureSession() + 
                       ", durée=" + session.getDuree() + " minutes" + 
                       ", moniteur=" + session.getMoniteur().getId() + 
                       ", véhicule=" + session.getVehicule().getId());

            stmt.setDate(1, Date.valueOf(session.getDateSession()));
            stmt.setTime(2, Time.valueOf(session.getHeureSession()));
            stmt.setInt(3, (int) session.getDuree());
            stmt.setLong(4, session.getMoniteur().getId());

            long candidatId = 0;
            try {
                if (session.getCandidats() != null && !session.getCandidats().isEmpty()) {
                    candidatId = session.getCandidats().get(0).getId();
                }

                if (candidatId <= 0) {
                    String checkSql = "SELECT id FROM candidat LIMIT 1";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                         ResultSet rs = checkStmt.executeQuery()) {
                        if (rs.next()) {
                            candidatId = rs.getLong("id");
                        } else {
                            candidatId = 1L;
                        }
                    }
                }
                
                stmt.setLong(5, candidatId);
                LOGGER.info("Utilisation du candidat avec ID=" + candidatId + " pour la séance de conduite");
            } catch (Exception e) {
                LOGGER.warning("Impossible de définir un candidat: " + e.getMessage());
                stmt.setLong(5, 1L);
            }
            
            stmt.setLong(6, session.getVehicule().getId());

            String pointRencontre = session.getPointRencontre();
            if (pointRencontre != null && !pointRencontre.equals("Non défini")) {
                stmt.setString(7, pointRencontre);

                try {
                    String[] coords = pointRencontre.split(",");
                    if (coords.length == 2) {
                        double lat = Double.parseDouble(coords[0]);
                        double lon = Double.parseDouble(coords[1]);
                        stmt.setDouble(11, lat);
                        stmt.setDouble(12, lon);
                        LOGGER.info("Coordonnées du point de rencontre enregistrées: lat=" + lat + ", lon=" + lon);
                    } else {
                        stmt.setNull(11, Types.DOUBLE);
                        stmt.setNull(12, Types.DOUBLE);
                    }
                } catch (NumberFormatException e) {
                    stmt.setNull(11, Types.DOUBLE);
                    stmt.setNull(12, Types.DOUBLE);
                }
            } else {
                stmt.setString(7, "Non défini");
                stmt.setNull(11, Types.DOUBLE);
                stmt.setNull(12, Types.DOUBLE);
            }

            stmt.setString(8, session.getStatut().name());

            stmt.setString(9, "");

            if (session.getKilometresParcourus() != null && session.getKilometresParcourus() > 0) {
                stmt.setInt(10, session.getKilometresParcourus());
            } else {
                stmt.setInt(10, 0);
            }

            LOGGER.info("Exécution de la requête SQL d'insertion pour la session conduite");
            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Nombre de lignes affectées: " + rowsAffected);
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long id = generatedKeys.getLong(1);
                        session.setId(id);
                        LOGGER.info("Session conduite enregistrée avec succès, ID: " + id);
                        return true;
                    } else {
                        LOGGER.warning("Aucune clé générée après insertion de la session conduite");
                    }
                }
            }
            LOGGER.warning("Échec de l'enregistrement de la session conduite");
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de l'enregistrement de la session conduite: " + e.getMessage(), e);
            return false;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Exception inattendue lors de l'enregistrement de la session conduite: " + e.getMessage(), e);
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
            UPDATE seance 
            SET date = ?, heure = ?, duree = ?, moniteur_id = ?, vehicule_id = ?, 
                lieu = ?, statut = ?, commentaire = ?, kilometrage_debut = ?, 
                latitude = ?, longitude = ?
            WHERE id = ? AND type = 'Conduite'
        """;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(session.getDateSession()));
            stmt.setTime(2, Time.valueOf(session.getHeureSession()));
            stmt.setInt(3, (int) session.getDuree());
            stmt.setLong(4, session.getMoniteur().getId());
            stmt.setLong(5, session.getVehicule().getId());

            String pointRencontre = session.getPointRencontre();
            if (pointRencontre != null && !pointRencontre.equals("Non défini")) {
                stmt.setString(6, pointRencontre);
                
                try {
                    String[] coords = pointRencontre.split(",");
                    if (coords.length == 2) {
                        double lat = Double.parseDouble(coords[0]);
                        double lon = Double.parseDouble(coords[1]);
                        stmt.setDouble(10, lat);
                        stmt.setDouble(11, lon);
                        LOGGER.info("Coordonnées du point de rencontre mises à jour: lat=" + lat + ", lon=" + lon);
                    } else {
                        stmt.setNull(10, Types.DOUBLE);
                        stmt.setNull(11, Types.DOUBLE);
                        LOGGER.warning("Format de coordonnées incorrect: " + pointRencontre);
                    }
                } catch (NumberFormatException e) {
                    stmt.setNull(10, Types.DOUBLE);
                    stmt.setNull(11, Types.DOUBLE);
                    LOGGER.warning("Impossible de parser les coordonnées: " + pointRencontre + ", erreur: " + e.getMessage());
                }
            } else {
                stmt.setString(6, "Non défini");
                stmt.setNull(10, Types.DOUBLE);
                stmt.setNull(11, Types.DOUBLE);
                LOGGER.info("Point de rencontre non défini, enregistré comme NULL");
            }

            stmt.setString(7, session.getStatut().name());
            stmt.setString(8, "");
            stmt.setInt(9, session.getKilometresParcourus());
            stmt.setLong(12, session.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating conduite session: " + e.getMessage(), e);
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
        String sql = "DELETE FROM seance WHERE id = ? AND type = 'Conduite'";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting conduite session with ID " + id, e);
            return false;
        }
    }
}