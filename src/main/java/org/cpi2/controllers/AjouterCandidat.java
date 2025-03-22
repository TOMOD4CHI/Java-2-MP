package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.cpi2.entitties.Inscription;
import org.cpi2.service.CandidatService;
import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.TypePermis;
import org.cpi2.service.InscriptionService;

import java.sql.Date;
import java.time.LocalDate;

public class AjouterCandidat {
    private final CandidatService candidatService = new CandidatService();
    private final InscriptionService inscriptionService = new InscriptionService();

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    @FXML
    private void initialize() {
        typeComboBox.setPrefWidth(200);
        // Add type permis options
        for (TypePermis type : TypePermis.values()) {
            typeComboBox.getItems().add(type.name());
        }
    }

    @FXML
    private void cancelAction() {
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        addressField.clear();
        phoneField.clear();
    }

    @FXML
    private void confirmAction() {
        if (nomField.getText().isEmpty() || cinField.getText().isEmpty() ||
                typeComboBox.getSelectionModel().isEmpty() || addressField.getText().isEmpty() ||
                phoneField.getText().isEmpty()) {

            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Merci de remplir tout les champs!");
            alert.showAndWait();
            return;
        }

        try {
            Inscription inscription = new Inscription();
            Candidat candidat = new Candidat();
            candidat.setNom(nomField.getText());
            candidat.setPrenom(prenomField.getText());
            candidat.setCin(cinField.getText());
            candidat.setAdresse(addressField.getText());
            candidat.setTelephone(phoneField.getText());

            inscription.setCin(candidat.getCin());
            inscription.setPaymentStatus(false);
            inscription.setStatus("En Cours");
            inscription.setPaymentCycle("Non défini");
            inscription.setInscriptioDate(Date.valueOf(LocalDate.now()));
            //inscription.setPlan(typeComboBox.getSelectionModel().getSelectedIndex());


            if (candidatService.addCandidat(candidat)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Candidat ajouté avec succès!");
                alert.showAndWait();
                cancelAction();
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Erreur lors de l'ajout du candidat!");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
