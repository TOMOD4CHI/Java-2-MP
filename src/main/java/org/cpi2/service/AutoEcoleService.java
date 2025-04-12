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

    public AutoEcole getAutoEcole() {
        try {
            return autoEcoleRepository.findFirst();
        } catch (SQLException e) {
            System.err.println("Error retrieving auto-école: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public AutoEcole getAutoEcoleData() {
        try {
            return autoEcoleRepository.findFirst();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<AutoEcole> findAllAutoEcoles() {
        return autoEcoleRepository.findAll();
    }

    public boolean saveAutoEcole(AutoEcole autoEcole) {
        try {
            return autoEcoleRepository.save(autoEcole);
        } catch (Exception e) {
            System.err.println("Error saving auto-école: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAutoEcole(AutoEcole autoEcole) {
        try {
            return autoEcoleRepository.update(autoEcole);
        } catch (Exception e) {
            System.err.println("Error updating auto-école: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

}