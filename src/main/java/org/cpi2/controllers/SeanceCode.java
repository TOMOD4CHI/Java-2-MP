package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class SeanceCode {

    @FXML
    private TextField candidatfield;

    @FXML
    private TextField sessionfield;

    @FXML
    private Button cancelButton;

    @FXML
    private Button planifierButton;

    @FXML
    private void handleCancel() {
        // Effacer les champs
        candidatfield.clear();
        sessionfield.clear();
    }

    @FXML
    private void handlePlanifier() {
        String candidatId = candidatfield.getText();
        String sessionId = sessionfield.getText();

        if (candidatId.isEmpty() || sessionId.isEmpty()) {
            showAlert("Erreur", "Veuillez remplir tous les champs.");
            return;
        }
        //controle sur les ids

        // Simulation d'une planification réussie
        showAlert("Succès", "Le candidat " + candidatId + " a été ajouté à la séance " + sessionId + ".");


    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
