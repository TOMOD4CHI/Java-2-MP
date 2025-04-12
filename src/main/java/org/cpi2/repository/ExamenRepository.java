package org.cpi2.repository;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.Examen;
import org.cpi2.entities.TypeExamen;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExamenRepository extends BaseRepository<Examen> {
    private static final Logger LOGGER = Logger.getLogger(ExamenRepository.class.getName());
    private final CandidatRepository candidatRepository = new CandidatRepository();
    private final Map<Integer, TypeExamen> typeExamenMap;

    public ExamenRepository() {
        this.typeExamenMap = new HashMap<>();
        try {
            typeExamenMap.putAll(getTypeExamen());
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing ExamenRepository", e);
        }
    }

    public Optional<Examen> findById(Long id) {
        String sql = "SELECT * FROM examen WHERE id = ?";
        Map<String, Object> examenData = null;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    examenData = extractExamenData(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examen by ID", e);
        }

        if (examenData != null) {
            Examen examen = createExamenFromData(examenData);
            return Optional.ofNullable(examen);
        }

        return Optional.empty();
    }

    public List<Examen> findAll() {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            LOGGER.info("Database connection established: " + !conn.isClosed());

            List<Map<String, Object>> examenDataList = new ArrayList<>();

            try (ResultSet rs = stmt.executeQuery(sql)) {
                boolean hasRows = false;

                while (rs.next()) {
                    hasRows = true;
                    examenDataList.add(extractExamenData(rs));
                }

                LOGGER.info("Query returned data: " + hasRows + ", row count: " + examenDataList.size());
            }

            // Now process the data after ResultSet is closed
            for (Map<String, Object> examenData : examenDataList) {
                Examen examen = createExamenFromData(examenData);
                if (examen != null) {
                    examens.add(examen);
                } else {
                    LOGGER.warning("Failed to create Examen object from data: " + examenData);
                }
            }

            LOGGER.info("Final examen list size: " + examens.size());

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all examens", e);
        }

        return examens;
    }

    private Map<String, Object> extractExamenData(ResultSet rs) throws SQLException {
        Map<String, Object> examenData = new HashMap<>();
        examenData.put("id", rs.getLong("id"));
        examenData.put("type_examen_id", rs.getInt("type_examen_id"));
        examenData.put("date_examen", rs.getDate("date_examen"));
        examenData.put("frais", rs.getDouble("frais"));
        examenData.put("resultat", rs.getObject("resultat"));
        examenData.put("candidat_id", rs.getLong("candidat_id"));

        // Handle commentaire column which might not exist in older schema versions
        try {
            examenData.put("commentaire", rs.getString("commentaire"));
        } catch (SQLException e) {
            // Column doesn't exist, just continue
            examenData.put("commentaire", null);
        }

        return examenData;
    }

    private Examen createExamenFromData(Map<String, Object> data) {
        try {
            TypeExamen typeExamen = typeExamenMap.get((Integer) data.get("type_examen_id"));
            long id = (Long) data.get("id");
            LocalDate dateExamen = ((Date) data.get("date_examen")).toLocalDate();
            double frais = (Double) data.get("frais");
            Object resultat = data.get("resultat");
            long candidatId = (Long) data.get("candidat_id");
            String commentaire = (String) data.get("commentaire");

            Candidat candidat = candidatRepository.findById(candidatId).orElse(null);

            if (candidat == null) {
                LOGGER.warning("Candidat not found for examen ID: " + id);
                return null;
            }
            if (typeExamen == null) {
                LOGGER.warning("Type examen not found for examen ID: " + id);
                return null;
            }

            Examen examen = new Examen();
            examen.setId(id);
            examen.setType(typeExamen);
            examen.setCandidat(candidat);
            examen.setDate(dateExamen);
            examen.setFrais(frais);
            examen.setCommentaire(commentaire);

            // Handle nullable resultat field
            if (resultat != null) {
                examen.setResultat((Boolean) resultat);
            }
            else {
                examen.setResultat(null);
            }
            return examen;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error creating examen from data: " + e.getMessage(), e);
            return null;
        }
    }

    public Map<Integer, TypeExamen> getTypeExamen() throws SQLException {
        Map<Integer, TypeExamen> typeExamenMap = new HashMap<>();
        String sql = "SELECT * FROM type_examen";

        try (Connection connection = getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String libelle = rs.getString("libelle");
                int id = rs.getInt("id");
                if (libelle.equalsIgnoreCase("code")) {
                    typeExamenMap.put(id, TypeExamen.CODE);
                }
                else if (libelle.equalsIgnoreCase("conduite")) {
                    typeExamenMap.put(id, TypeExamen.CONDUITE);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding type", e);
            return null;
        }
        return typeExamenMap;
    }

    public boolean save(Examen examen) {
        String sql = "INSERT INTO examen (candidat_id, type_examen_id, date_examen, frais, resultat) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, examen.getCandidat().getId().intValue());
            stmt.setInt(2, getTypeExamenId(examen.getType()));
            stmt.setDate(3, Date.valueOf(examen.getDate()));
            stmt.setDouble(4, examen.getFrais());
            stmt.setNull(5, Types.BOOLEAN);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        examen.setId(generatedKeys.getLong(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving examen", e);
        }
        return false;
    }

    public int getTypeExamenId(TypeExamen typeExamen) throws SQLException {
        for (Map.Entry<Integer, TypeExamen> entry : typeExamenMap.entrySet()) {
            if (entry.getValue() == typeExamen) {
                System.out.println("TypeExamen ID: " + entry.getKey());
                return entry.getKey();
            }
        }
        throw new SQLException("TypeExamen not found: " + typeExamen);
    }

    public boolean update(Examen examen) {
        String sql = "UPDATE examen SET candidat_id = ?, type_examen_id = ?, date_examen = ?, frais = ?, resultat = ?, commentaire = ? WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, examen.getCandidat().getId().intValue());
            stmt.setInt(2, getTypeExamenId(examen.getType()));
            stmt.setDate(3, Date.valueOf(examen.getDate()));
            stmt.setDouble(4, examen.getFrais());

            if (examen.getResultat() != null) {
                stmt.setBoolean(5, examen.getResultat());
            } else {
                stmt.setNull(5, Types.BOOLEAN);
            }

            if (examen.getCommentaire() == null) {
                stmt.setNull(6, Types.VARCHAR);
            } else {
                stmt.setString(6, examen.getCommentaire());
            }

            stmt.setLong(7, examen.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating examen", e);
            return false;
        }
    }

    public List<Examen> findByCandidatId(Long candidatId) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE candidat_id = ?";
        List<Map<String, Object>> examenDataList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, candidatId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examenDataList.add(extractExamenData(rs));
                }
            }

            // Process data after ResultSet is closed
            for (Map<String, Object> examenData : examenDataList) {
                Examen examen = createExamenFromData(examenData);
                if (examen != null) {
                    examens.add(examen);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by candidat ID", e);
        }
        return examens;
    }

    public List<Examen> findByTypeExamen(Integer typeExamenId) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE type_examen_id = ?";
        List<Map<String, Object>> examenDataList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, typeExamenId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examenDataList.add(extractExamenData(rs));
                }
            }

            // Process data after ResultSet is closed
            for (Map<String, Object> examenData : examenDataList) {
                Examen examen = createExamenFromData(examenData);
                if (examen != null) {
                    examens.add(examen);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by type", e);
        }
        return examens;
    }

    public List<Examen> findByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE date_examen BETWEEN ? AND ?";
        List<Map<String, Object>> examenDataList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(startDate));
            stmt.setDate(2, Date.valueOf(endDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examenDataList.add(extractExamenData(rs));
                }
            }

            // Process data after ResultSet is closed
            for (Map<String, Object> examenData : examenDataList) {
                Examen examen = createExamenFromData(examenData);
                if (examen != null) {
                    examens.add(examen);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by date range", e);
        }
        return examens;
    }

    public List<Examen> findByResultat(Boolean resultat) {
        List<Examen> examens = new ArrayList<>();
        String sql = "SELECT * FROM examen WHERE resultat = ?";
        List<Map<String, Object>> examenDataList = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, resultat);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    examenDataList.add(extractExamenData(rs));
                }
            }

            // Process data after ResultSet is closed
            for (Map<String, Object> examenData : examenDataList) {
                Examen examen = createExamenFromData(examenData);
                if (examen != null) {
                    examens.add(examen);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding examens by resultat", e);
        }
        return examens;
    }

    public boolean delete(Long id) {
        String sql = "DELETE FROM examen WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deleting examen", e);
            return false;
        }
    }
}