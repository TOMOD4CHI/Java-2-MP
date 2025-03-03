package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

public class AjouterEcole {

    public TextField nomField;
    public TextField adresseField;
    public TextField telephoneField;
    public TextField emailField;
    public ImageView logoImageView;

    private Image logoImage;

    // Handle file upload for the logo
    public void handleLogoUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Choisir une image");
        Stage stage = (Stage) logoImageView.getScene().getWindow();
        var file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            logoImage = new Image(file.toURI().toString());
            logoImageView.setImage(logoImage);
        }
    }

    // Handle saving the school data
    public void handleSave(ActionEvent event) {
        String nom = nomField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        String email = emailField.getText();

        if (nom.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty() || logoImage == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
        } else {
            // Save the school data (e.g., to a database or a file)
            showAlert("Succès", "École ajoutée avec succès!");
        }
    }

    // Handle cancelling the operation
    public void handleCancel(ActionEvent event) {
        nomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();
        logoImageView.setImage(null);
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void handleAjouterEcole(ActionEvent actionEvent) {
        String nom = nomField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        String email = emailField.getText();

        if (nom.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty() || logoImage == null) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
        } else {
            // Save the school data (e.g., to a database or a file)
            showAlert("Succès", "École ajoutée avec succès!");
        }
    }
}
