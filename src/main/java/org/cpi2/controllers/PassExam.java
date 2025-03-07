package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

public class PassExam {

    @FXML
    private TextField idCandidatField;

    @FXML
    private TextField idExamenField;

    @FXML
    private ComboBox<String> resultatComboBox;

    @FXML
    private Button saveButton;

    @FXML
    private HBox buttonBox;

    private Button cancelbutton;

    @FXML
    public void initialize() {

        if (resultatComboBox.getItems().isEmpty()) {
            resultatComboBox.getItems().addAll("Réussi", "Échoué");
        }
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String candidatId = idCandidatField.getText();
        String examenId = idExamenField.getText();
        String resultat = resultatComboBox.getValue();

        if (candidatId == null || candidatId.trim().isEmpty() ||
                examenId == null || examenId.trim().isEmpty() ||
                resultat == null) {


            System.out.println("Veuillez remplir tous les champs");
            return;
        }

        // Process the form data - save to database or pass to another component
        System.out.println("Enregistrement: Candidat=" + candidatId +
                ", Examen=" + examenId +
                ", Résultat=" + resultat);

        // Clear fields after successful save if needed
        clearFields();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        idCandidatField.clear();
        idExamenField.clear();
        resultatComboBox.setValue(null);
    }
}