package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.CheckBox;

public class SeanceCode {

    @FXML
    private TextField candidatfield;

    @FXML
    private TextField sessionfield;

    @FXML
    private CheckBox autoAssignCheckBox;

    @FXML
    private Button cancelButton;

    @FXML
    private Button planifierButton;

    @FXML
    private void handleCancel() {
        // Effacer les champs
        candidatfield.clear();
        sessionfield.clear();
        autoAssignCheckBox.setSelected(false);
    }

    @FXML
    private void handlePlanifier() {
        if (autoAssignCheckBox.isSelected()) {
            // Auto-assign logic
            String sessionId = sessionfield.getText();

            if (sessionId.isEmpty()) {
                showAlert("Erreur", "Veuillez spécifier un ID de séance pour l'assignation automatique.");
                return;
            }

            try {
                // Here we would interact with the existing service layer
                // Since we're not importing services, we'll assume these methods exist elsewhere
                // and just mock the functionality for now

                // Simulate checking if session exists and is a code session
                boolean sessionExists = true; // This would be validated by a service
                boolean isCodeSession = true; // This would be validated by a service

                if (!sessionExists) {
                    showAlert("Erreur", "Séance introuvable.");
                    return;
                }

                if (!isCodeSession) {
                    showAlert("Erreur", "La séance spécifiée n'est pas une séance de code.");
                    return;
                }

                // Simulate auto-assignment based on inscription date and capacity
                int candidatsAssigned = 3; // This would be the actual number returned by the service

                showAlert("Succès", candidatsAssigned + " candidat(s) ont été assignés automatiquement à la séance " + sessionId + ".");
            } catch (Exception e) {
                showAlert("Erreur", "Une erreur est survenue: " + e.getMessage());
            }
        } else {
            // Original manual assignment logic
            String candidatId = candidatfield.getText();
            String sessionId = sessionfield.getText();

            if (candidatId.isEmpty() || sessionId.isEmpty()) {
                showAlert("Erreur", "Veuillez remplir tous les champs.");
                return;
            }

            // Simulation d'une planification réussie
            showAlert("Succès", "Le candidat " + candidatId + " a été ajouté à la séance " + sessionId + ".");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}