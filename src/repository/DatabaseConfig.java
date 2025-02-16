package repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

// Database configuration class
class DatabaseConfig {
    private static final String URL = "jdbc:mysql://localhost:3306/auto_ecole";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private static final Logger LOGGER = Logger.getLogger(DatabaseConfig.class.getName());
    private static Connection connection = null;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (SQLException e) {
                LOGGER.severe("Failed to connect to database: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }
}
