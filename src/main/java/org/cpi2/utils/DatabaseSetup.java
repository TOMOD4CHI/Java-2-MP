package org.cpi2.utils;

import org.cpi2.repository.DatabaseConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utilitaire pour configurer la base de données
 */
public class DatabaseSetup {
    private static final Logger LOGGER = Logger.getLogger(DatabaseSetup.class.getName());

    /**
     * Crée la table salle si elle n'existe pas déjà
     * @return true si la création a réussi, false sinon
     */
    public static boolean createSalleTable() {
        String createTableSQL = ""
            + "CREATE TABLE IF NOT EXISTS `salle` (\n"
            + "  `id` int(11) NOT NULL AUTO_INCREMENT,\n"
            + "  `nom` varchar(100) NOT NULL,\n"
            + "  `numero` varchar(20) NOT NULL,\n"
            + "  `capacite` int(11) NOT NULL DEFAULT 20,\n"
            + "  `notes` text DEFAULT NULL,\n"
            + "  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),\n"
            + "  PRIMARY KEY (`id`),\n"
            + "  UNIQUE KEY `numero` (`numero`)\n"
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute(createTableSQL);
            LOGGER.log(Level.INFO, "Table salle créée avec succès ou existe déjà");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Erreur lors de la création de la table salle", e);
            return false;
        }
    }
}