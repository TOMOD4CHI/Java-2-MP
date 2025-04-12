package org.cpi2.repository;

import org.cpi2.Exceptions.DataNotFound;
import org.cpi2.entities.CoursePlan;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PlanRepository extends BaseRepository<CoursePlan> {
    private static final Logger LOGGER = Logger.getLogger(PlanRepository.class.getName());

    public Optional<CoursePlan> findById(Integer id) {
        String sql = "SELECT * FROM plan WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToPlan(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding plan by ID", e);
        }
        return Optional.empty();
    }

    public List<CoursePlan> findAll() {
        List<CoursePlan> plans = new ArrayList<>();
        String sql = "SELECT * FROM plan";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                plans.add(mapResultSetToPlan(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding all plans", e);
        }
        return plans;
    }

    private CoursePlan mapResultSetToPlan(ResultSet rs) throws SQLException {
        try {
            int id = rs.getInt("id");
            return CoursePlan.getById(id);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "No matching CoursePlan found for ID", e);
            throw new SQLException("Invalid plan ID", e);
        }
    }

    public List<CoursePlan> findByTypePermis(String category) {
        List<CoursePlan> matchingPlans = new ArrayList<>();

        for (CoursePlan plan : CoursePlan.values()) {
            if (plan.getCategory().getLibelle().equalsIgnoreCase(category)) {
                matchingPlans.add(plan);
            }
        }

        return matchingPlans;
    }
    public Optional<CoursePlan> findByName(String name) {
        for (CoursePlan plan : CoursePlan.values()) {
            if (plan.getName().equalsIgnoreCase(name)) {
                return Optional.of(plan);
            }
        }
        return Optional.empty();
    }

}