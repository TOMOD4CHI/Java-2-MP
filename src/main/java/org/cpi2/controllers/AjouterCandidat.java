package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class AjouterCandidat {

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    // Action for the Cancel button
    @FXML
    private void cancelAction() {
        // Clear all fields (reset form)
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        addressField.clear();
        phoneField.clear();
    }

    // Action for the Confirm button
    @FXML
    private void confirmAction() {
        // Example validation (you can add more logic as needed)
        if (nomField.getText().isEmpty() || cinField.getText().isEmpty() ||
                typeComboBox.getSelectionModel().isEmpty() || addressField.getText().isEmpty() ||
                phoneField.getText().isEmpty()) {

            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Merci de remplir tout les champs!");
            alert.showAndWait();
        } else {
           //l ajout dans la base de donnee
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("succees");
            alert.setHeaderText(null);
            alert.setContentText("Candidat ajouter avec success!");
            alert.showAndWait();


            cancelAction();
        }
    }
}
