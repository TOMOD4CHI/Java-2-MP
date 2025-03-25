package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

public class PassExam {

    @FXML
    private TextField idCandidatField;

    @FXML
    private TextField idExamenField;

    @FXML
    private ComboBox<String> resultatComboBox;

    @FXML
    private TextArea commentaireArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    @FXML
    public void initialize() {
        // Populate ComboBox if not already populated
        if (resultatComboBox.getItems().isEmpty()) {
            resultatComboBox.getItems().addAll("Réussi", "Échoué");
        }
        
        // TableView initialization removed as these components are not in the FXML file
    }

    @FXML
    public void handleSave(ActionEvent event) {
        String candidatId = idCandidatField.getText();
        String examenId = idExamenField.getText();
        String resultat = resultatComboBox.getValue();
        String commentaire = commentaireArea.getText();

        // Validation
        if (candidatId == null || candidatId.trim().isEmpty() ||
                examenId == null || examenId.trim().isEmpty() ||
                resultat == null) {
            showAlert("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }

        // Process the form data
        System.out.println("Enregistrement: " +
                "Candidat=" + candidatId +
                ", Examen=" + examenId +
                ", Résultat=" + resultat +
                ", Commentaire=" + commentaire);

        // Here you would typically:
        // 1. Validate input
        // 2. Save to database
        // 3. Refresh the TableView
        // 4. Clear fields

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
        commentaireArea.clear();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // Model class for Exam (you would typically have this in a separate file)
    public static class Exam {
        private String idExamen;
        private String typeExamen;
        private String dateExamen;
        private String candidat;
        private String resultat;

        // Constructor
        public Exam(String idExamen, String typeExamen, String dateExamen, String candidat, String resultat) {
            this.idExamen = idExamen;
            this.typeExamen = typeExamen;
            this.dateExamen = dateExamen;
            this.candidat = candidat;
            this.resultat = resultat;
        }

        // Getters and setters
        public String getIdExamen() {
            return idExamen;
        }

        public void setIdExamen(String idExamen) {
            this.idExamen = idExamen;
        }

        public String getTypeExamen() {
            return typeExamen;
        }

        public void setTypeExamen(String typeExamen) {
            this.typeExamen = typeExamen;
        }

        public String getDateExamen() {
            return dateExamen;
        }

        public void setDateExamen(String dateExamen) {
            this.dateExamen = dateExamen;
        }

        public String getCandidat() {
            return candidat;
        }

        public void setCandidat(String candidat) {
            this.candidat = candidat;
        }

        public String getResultat() {
            return resultat;
        }

        public void setResultat(String resultat) {
            this.resultat = resultat;
        }
    }
}