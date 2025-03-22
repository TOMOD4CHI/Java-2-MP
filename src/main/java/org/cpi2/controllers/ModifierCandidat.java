package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.cpi2.service.CandidatService;
import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.TypePermis;

public class ModifierCandidat {
    private final CandidatService candidatService = new CandidatService();
    private Candidat candidatToModify;

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private ComboBox<String> typeComboBox;

    public void initialize() {
        // Initialize type permis options
        for (TypePermis type : TypePermis.values()) {
            typeComboBox.getItems().add(type.name());
        }
    }

    public void setCandidatToModify(Candidat candidat) {
        this.candidatToModify = candidat;
        if (candidat != null) {
            nomField.setText(candidat.getNom());
            prenomField.setText(candidat.getPrenom());
            cinField.setText(candidat.getCin());
            addressField.setText(candidat.getAdresse());
            phoneField.setText(candidat.getTelephone());
        }
    }

    @FXML
    private void confirmAction() {
        if (candidatToModify == null) {
            showAlert("Erreur", "Aucun candidat sélectionné pour la modification.");
            return;
        }

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

        try {
            // Update the candidate object with new values
            candidatToModify.setNom(nom);
            candidatToModify.setPrenom(prenom);
            candidatToModify.setCin(cin);
            candidatToModify.setAdresse(address);
            candidatToModify.setTelephone(phone);

            if (candidatService.updateCandidat(candidatToModify)) {
                showSuccessMessage();
                closeWindow();
            } else {
                showAlert("Erreur", "La mise à jour du candidat a échoué.");
            }
        } catch (Exception e) {
            showAlert("Erreur", "Une erreur est survenue lors de la mise à jour: " + e.getMessage());
        }
    }

    @FXML
    private void cancelAction() {
        closeWindow();
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
