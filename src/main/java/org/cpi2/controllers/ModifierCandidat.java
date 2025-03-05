package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ModifierCandidat {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> typeComboBox;

    public void initialize() {
        // Load candidate details (e.g., from database)
        nomField.setText("Exemple Nom");
        prenomField.setText("Exemple Prénom");
        cinField.setText("12345678");
        addressField.setText("Exemple Adresse");
        phoneField.setText("123456789");
        typeComboBox.setValue("Voiture");
    }

    @FXML
    private void confirmAction() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String cin = cinField.getText();
        String address = addressField.getText();
        String phone = phoneField.getText();
        String typePermis = typeComboBox.getValue();

        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || address.isEmpty() || phone.isEmpty() || typePermis == null) {
            showAlert("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        // Logic to update candidate (e.g., saving the data)
        showSuccessMessage();

        // Optionally, you can leave the stage open if desired.
        // Stage stage = (Stage) nomField.getScene().getWindow();
        // stage.close(); // Do not close the window.
    }

    @FXML
    private void cancelAction() {
       nomField.clear();
       prenomField.clear();
       cinField.clear();
       addressField.clear();
       phoneField.clear();
       typeComboBox.setValue(null);

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage() {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Succès");
        successAlert.setHeaderText(null);
        successAlert.setContentText("Les informations du candidat ont été mises à jour avec succès.");
        successAlert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}
