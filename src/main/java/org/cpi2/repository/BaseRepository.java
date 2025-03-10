package org.cpi2.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

// Base repository with common operations
abstract class BaseRepository<T> {
    protected final Logger LOGGER = Logger.getLogger(this.getClass().getName());

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    protected void closeQuietly(AutoCloseable resource) {
        if (resource != null) {
            try {
                resource.close();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error closing resource", e);
            }
        }
    }
}
