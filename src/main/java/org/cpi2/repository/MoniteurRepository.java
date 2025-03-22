package org.cpi2.repository;

import org.cpi2.entitties.Moniteur;
import org.cpi2.entitties.RendezVous;
import org.cpi2.entitties.TypePermis;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;

// Monitor Repository
public class MoniteurRepository {
    private static final Logger LOGGER = Logger.getLogger(MoniteurRepository.class.getName());
    private static final String URL = "jdbc:mysql://localhost:3306/autoecole";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public List<Moniteur> findAll() {
        List<Moniteur> moniteurs = new ArrayList<>();
        String sql = "SELECT * FROM moniteur";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadSpecialites(conn, moniteur);
                loadRendezVous(conn, moniteur);
                moniteurs.add(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all moniteurs", e);
        }

        return moniteurs;
    }

    public Optional<Moniteur> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        
        String sql = "SELECT * FROM moniteur WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadSpecialites(conn, moniteur);
                loadRendezVous(conn, moniteur);
                return Optional.of(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding moniteur by id", e);
        }

        return Optional.empty();
    }

    public Optional<Moniteur> findByCin(String cin) {
        if (cin == null || cin.isEmpty()) {
            return Optional.empty();
        }
        
        String sql = "SELECT * FROM moniteur WHERE cin = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cin);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Moniteur moniteur = mapResultSetToMoniteur(rs);
                loadSpecialites(conn, moniteur);
                loadRendezVous(conn, moniteur);
                return Optional.of(moniteur);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding moniteur by CIN", e);
        }

        return Optional.empty();
    }

    public boolean save(Moniteur moniteur) {
        if (moniteur == null) {
            LOGGER.log(Level.WARNING, "Cannot save null moniteur");
            return false;
        }
        
        String sql = "INSERT INTO moniteur (nom, prenom, cin, adresse, telephone, email, date_naissance, date_embauche) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, moniteur.getNom());
            stmt.setString(2, moniteur.getPrenom());
            stmt.setString(3, moniteur.getCin());
            stmt.setString(4, moniteur.getAdresse());
            stmt.setString(5, moniteur.getTelephone());
            stmt.setString(6, moniteur.getEmail());
            stmt.setDate(7, moniteur.getDateNaissance() != null ? 
                            Date.valueOf(moniteur.getDateNaissance()) : null);
            stmt.setDate(8, moniteur.getDateEmbauche() != null ? 
                            Date.valueOf(moniteur.getDateEmbauche()) : null);

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    moniteur.setId(generatedKeys.getLong(1));
                    boolean specialitesResult = saveSpecialites(conn, moniteur);
                    boolean rendezVousResult = saveRendezVous(conn, moniteur);
                    return specialitesResult && rendezVousResult;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving moniteur", e);
        }


        return false;
    }

    public boolean update(Moniteur moniteur) {
        if (moniteur == null || moniteur.getId() == null) {
            LOGGER.log(Level.WARNING, "Cannot update null moniteur or moniteur without ID");
            return false;
        }
        
        String sql = "UPDATE moniteur SET nom = ?, prenom = ?, cin = ?, adresse = ?, " +
                    "telephone = ?, email = ?, date_naissance = ?, date_embauche = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, moniteur.getNom());
            stmt.setString(2, moniteur.getPrenom());
            stmt.setString(3, moniteur.getCin());
            stmt.setString(4, moniteur.getAdresse());
            stmt.setString(5, moniteur.getTelephone());
            stmt.setString(6, moniteur.getEmail());
            stmt.setDate(7, moniteur.getDateNaissance() != null ? 
                            Date.valueOf(moniteur.getDateNaissance()) : null);
            stmt.setDate(8, moniteur.getDateEmbauche() != null ? 
                            Date.valueOf(moniteur.getDateEmbauche()) : null);
            stmt.setLong(9, moniteur.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                return false;
            }

            // Update specialites
            String deleteSpecialitesSql = "DELETE FROM moniteur_specialite WHERE id_moniteur = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSpecialitesSql)) {
                deleteStmt.setLong(1, moniteur.getId());
                deleteStmt.executeUpdate();
            }
            boolean specialitesResult = saveSpecialites(conn, moniteur);
            
            // Update rendez-vous
            String deleteRendezVousSql = "DELETE FROM moniteur_rendez_vous WHERE id_moniteur = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteRendezVousSql)) {
                deleteStmt.setLong(1, moniteur.getId());
                deleteStmt.executeUpdate();
            }
            boolean rendezVousResult = saveRendezVous(conn, moniteur);
            
            return specialitesResult && rendezVousResult;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating moniteur", e);
        }

        return false;
    }

    private Moniteur mapResultSetToMoniteur(ResultSet rs) throws SQLException {
        Moniteur moniteur = new Moniteur();
        moniteur.setId(rs.getLong("id"));
        moniteur.setNom(rs.getString("nom"));
        moniteur.setPrenom(rs.getString("prenom"));
        moniteur.setCin(rs.getString("cin"));
        moniteur.setAdresse(rs.getString("adresse"));
        moniteur.setTelephone(rs.getString("telephone"));
        moniteur.setEmail(rs.getString("email"));
        
        Date dateNaissance = rs.getDate("date_naissance");
        if (dateNaissance != null) {
            moniteur.setDateNaissance(dateNaissance.toLocalDate());
        }
        
        Date dateEmbauche = rs.getDate("date_embauche");
        if (dateEmbauche != null) {
            moniteur.setDateEmbauche(dateEmbauche.toLocalDate());
        }
        
        moniteur.setSpecialites(new ArrayList<>());
        moniteur.setEmploiDuTemps(new HashMap<>());
        return moniteur;
    }

    private void loadSpecialites(Connection conn, Moniteur moniteur) throws SQLException {
        if (moniteur == null || moniteur.getId() == null) {
            return;
        }
        
        String sql = "SELECT tp.id, tp.code FROM type_permis tp " +
                    "JOIN moniteur_specialite ms ON tp.id = ms.id_type_permis " +
                    "WHERE ms.id_moniteur = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, moniteur.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String typePermisCode = rs.getString("code");
                try {
                    TypePermis typePermis = TypePermis.valueOf(typePermisCode);
                    moniteur.addSpecialite(typePermis);
                } catch (IllegalArgumentException e) {
                    LOGGER.log(Level.WARNING, "Invalid TypePermis code in database: " + typePermisCode, e);
                }
            }
        }
    }

    private boolean saveSpecialites(Connection conn, Moniteur moniteur) throws SQLException {
        if (moniteur == null || moniteur.getId() == null || moniteur.getSpecialites() == null) {
            return false;
        }
        
        // First get the IDs of all TypePermis from the database
        Map<String, Integer> typePermisIds = new HashMap<>();
        String getTypePermisIdsSql = "SELECT id, code FROM type_permis";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(getTypePermisIdsSql)) {
            
            while (rs.next()) {
                typePermisIds.put(rs.getString("code"), rs.getInt("id"));
            }
        }
        
        // Now insert the specialities
        String sql = "INSERT INTO moniteur_specialite (id_moniteur, id_type_permis) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (TypePermis specialite : moniteur.getSpecialites()) {
                Integer typePermisId = typePermisIds.get(specialite.name());
                if (typePermisId == null) {
                    LOGGER.log(Level.WARNING, "TypePermis not found in database: " + specialite.name());
                    continue;
                }
                
                stmt.setLong(1, moniteur.getId());
                stmt.setInt(2, typePermisId);
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            return true;
        }
    }
    
    private void loadRendezVous(Connection conn, Moniteur moniteur) throws SQLException {
        if (moniteur == null || moniteur.getId() == null) {
            return;
        }
        
        String sql = "SELECT mrv.date_time, rv.* FROM moniteur_rendez_vous mrv " +
                    "JOIN rendez_vous rv ON mrv.id_rendez_vous = rv.id " +
                    "WHERE mrv.id_moniteur = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, moniteur.getId());
            ResultSet rs = stmt.executeQuery();

            Map<LocalDateTime, RendezVous> emploiDuTemps = new HashMap<>();
            while (rs.next()) {
                try {
                    // Get the date_time from the moniteur_rendez_vous table
                    Timestamp timestamp = rs.getTimestamp("date_time");
                    LocalDateTime dateTime = timestamp.toLocalDateTime();
                    
                    // Create and populate the RendezVous object
                    RendezVous rendezVous = new RendezVous();
                    rendezVous.setId(rs.getLong("id"));
                    // Set other RendezVous properties based on your schema
                    
                    // Add to emploiDuTemps
                    emploiDuTemps.put(dateTime, rendezVous);
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Error loading rendez-vous", e);
                }
            }
            
            moniteur.setEmploiDuTemps(emploiDuTemps);
        }
    }
    
    private boolean saveRendezVous(Connection conn, Moniteur moniteur) throws SQLException {
        if (moniteur == null || moniteur.getId() == null || moniteur.getEmploiDuTemps() == null) {
            return false;
        }
        
        // If no rendez-vous to save, return true (success)
        if (moniteur.getEmploiDuTemps().isEmpty()) {
            return true;
        }
        
        // Insert the rendez-vous associations
        String sql = "INSERT INTO moniteur_rendez_vous (id_moniteur, id_rendez_vous, date_time) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Map.Entry<LocalDateTime, RendezVous> entry : moniteur.getEmploiDuTemps().entrySet()) {
                LocalDateTime dateTime = entry.getKey();
                RendezVous rendezVous = entry.getValue();
                
                if (rendezVous == null || rendezVous.getId() == null) {
                    LOGGER.log(Level.WARNING, "RendezVous or its ID is null");
                    continue;
                }
                
                stmt.setLong(1, moniteur.getId());
                stmt.setLong(2, rendezVous.getId());
                stmt.setTimestamp(3, Timestamp.valueOf(dateTime));
                stmt.addBatch();
            }
            
            stmt.executeBatch();
            return true;
        }
    }
}