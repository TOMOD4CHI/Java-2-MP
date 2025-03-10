package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.image.ImageView;

public class ModifierEcole {

    public TextField nomField;
    public TextField adresseField;
    public TextField telephoneField;
    public TextField emailField;
    public ImageView logoImageView;

    private Image logoImage;

    // Simulating existing data for modification
    public void initialize() {
        // You can set these to pre-filled data if available, for example:
        nomField.setText("École Exemple");
        adresseField.setText("123 Rue Exemple");
        telephoneField.setText("0123456789");
        emailField.setText("ecole@example.com");
        // logoImageView.setImage(existingLogo);  // Set an existing logo if available
    }

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

    // Handle saving the modified school data
    public void handleSave(ActionEvent event) {
        String nom = nomField.getText();
        String adresse = adresseField.getText();
        String telephone = telephoneField.getText();
        String email = emailField.getText();

        if (nom.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty()) {
            showAlert("Erreur", "Tous les champs doivent être remplis.");
        } else {
            // Save the modified school data (e.g., to a database or a file)
            showAlert("Succès", "Les informations de l'école ont été modifiées avec succès!");
        }
    }

    // Handle cancelling the operation
    public void handleCancel(ActionEvent event) {
        nomField.clear();
        adresseField.clear();
        telephoneField.clear();
        emailField.clear();

    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
