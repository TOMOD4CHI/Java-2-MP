package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ModifierMoniteur {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private DatePicker dateEmbaucheField;
    @FXML private ComboBox<String> typePermisComboBox;

    @FXML
    private void modifierAction() {
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String cin = cinField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        String email = emailField.getText();
        String dateEmbauche = dateEmbaucheField.getValue() != null ? dateEmbaucheField.getValue().toString() : "";


        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty() || dateEmbauche.isEmpty()) {
            showAlert("Erreur", "Tous les champs sont obligatoires.");
            return;
        }

        showAlert("Succès", "Moniteur modifie avec succès !");
    }

    @FXML
    private void cancelAction() {
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        dateEmbaucheField.setValue(null);

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
