package org.cpi2.service;

import org.cpi2.entities.AutoEcole;
import org.cpi2.repository.AutoEcoleRepository;

import java.sql.SQLException;
import java.util.List;

public class AutoEcoleService {
    
    private final AutoEcoleRepository autoEcoleRepository;
    
    public AutoEcoleService() {
        this.autoEcoleRepository = new AutoEcoleRepository();
    }
    
    /**
     * Get the auto-école information
     * Since only one auto-école is supposed to exist, this returns the first one found
     * @return The auto-école or null if none exists
     */
    public AutoEcole getAutoEcole() {
        try {
            return autoEcoleRepository.findFirst();
        } catch (SQLException e) {
            System.err.println("Error retrieving auto-école: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Get all auto-écoles from the database
     * @return A list of all auto-écoles
     */
    public List<AutoEcole> findAllAutoEcoles() {
        return autoEcoleRepository.findAll();
    }
    
    /**
     * Save new auto-école to database
     * @param autoEcole The auto-école to save
     * @return true if successful, false otherwise
     */
    public boolean saveAutoEcole(AutoEcole autoEcole) {
        try {
            return autoEcoleRepository.save(autoEcole);
        } catch (Exception e) {
            System.err.println("Error saving auto-école: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update an existing auto-école in database
     * @param autoEcole The auto-école to update
     * @return true if successful, false otherwise
     */
    public boolean updateAutoEcole(AutoEcole autoEcole) {
        try {
            return autoEcoleRepository.update(autoEcole);
        } catch (Exception e) {
            System.err.println("Error updating auto-école: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Delete an auto-école from database by ID
     * @param id The ID of the auto-école to delete
     * @return true if successful, false otherwise
     */
    public boolean deleteAutoEcole(int id) {
        try {
            return autoEcoleRepository.deleteById(id);
        } catch (Exception e) {
            System.err.println("Error deleting auto-école: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}