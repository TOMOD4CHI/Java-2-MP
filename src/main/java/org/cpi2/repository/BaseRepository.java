package org.cpi2.repository;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
