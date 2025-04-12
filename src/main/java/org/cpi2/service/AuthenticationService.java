package org.cpi2.service;

import org.cpi2.entities.AutoEcole;
import org.cpi2.repository.AutoEcoleRepository;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service for handling user authentication
 */
public class AuthenticationService {
    private static final Logger LOGGER = Logger.getLogger(AuthenticationService.class.getName());
    private final AutoEcoleRepository autoEcoleRepository;
    
    public AuthenticationService() {
        this.autoEcoleRepository = new AutoEcoleRepository();
    }
    
    /**
     * Authenticates a user by checking username and password against the database
     * 
     * @param username The username to check
     * @param password The password to check
     * @return true if authentication is successful, false otherwise
     */
    public boolean authenticate(String username, String password) {
        try {
            AutoEcole autoEcole = autoEcoleRepository.findFirst();
            
            if (autoEcole == null) {
                LOGGER.log(Level.WARNING, "No auto-école found in database for authentication");
                return false;
            }

            return username.equals(autoEcole.getUsername()) &&
                   password.equals(autoEcole.getPassword());
            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error authenticating user", e);
            return false;
        }
    }
}