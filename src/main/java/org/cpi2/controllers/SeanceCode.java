package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.TypePermis;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeanceCode {
    @FXML private TextField capfield;
    @FXML private DatePicker datefield;
    @FXML private TextField tempsfield;
    @FXML private TextField moniteurfield;

    @FXML private TableView<Moniteur> moniteurTableView;
    @FXML private TableColumn<Moniteur, Long> idMoniteurColumn;
    @FXML private TableColumn<Moniteur, String> nomMoniteurColumn;
    @FXML private TableColumn<Moniteur, String> prenomMoniteurColumn;
    @FXML private TableColumn<Moniteur, String> specialiteColumn;

    @FXML private Button cancelButton;
    @FXML private Button planifierButton;

    @FXML
    public void initialize() {
        // Initialize the table columns
        idMoniteurColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomMoniteurColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomMoniteurColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        specialiteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSpecialites().stream()
                        .map(TypePermis::toString)
                        .collect(Collectors.joining(", "))));

        // Populate table with mock data
        loadMockMoniteurs();
    }

    private void loadMockMoniteurs() {
        List<Moniteur> mockMoniteurs = createMockMoniteurs();
        moniteurTableView.getItems().clear();
        moniteurTableView.getItems().addAll(mockMoniteurs);
    }

    private List<Moniteur> createMockMoniteurs() {
        List<Moniteur> moniteurs = new ArrayList<>();

        Moniteur moniteur1 = new Moniteur("Dupont", "Jean", "AB1234",
                "123 Rue de Paris", "0601020304",
                LocalDate.of(1985, 5, 15), "jean.dupont@example.com",
                LocalDate.of(2015, 3, 1), 2500.0);
        moniteur1.addSpecialite(TypePermis.B);

        Moniteur moniteur2 = new Moniteur("Martin", "Sophie", "CD5678",
                "456 Avenue Lyon", "0607080910",
                LocalDate.of(1990, 8, 20), "sophie.martin@example.com",
                LocalDate.of(2018, 6, 15), 2700.0);
        moniteur2.addSpecialite(TypePermis.A);

        moniteurs.add(moniteur1);
        moniteurs.add(moniteur2);

        return moniteurs;
    }

    @FXML
    private void handlePlanifier() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        // Placeholder for seance planning logic
        showAlert("Planification", "Séance de code planifiée avec succès", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleCancel() {
        // Close the current window
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }

    private boolean validateInputs() {
        // Basic input validation
        if (capfield.getText().isEmpty()) {
            showAlert("Validation", "Veuillez saisir la capacité de la séance", Alert.AlertType.WARNING);
            return false;
        }

        if (datefield.getValue() == null) {
            showAlert("Validation", "Veuillez sélectionner une date", Alert.AlertType.WARNING);
            return false;
        }

        if (tempsfield.getText().isEmpty()) {
            showAlert("Validation", "Veuillez saisir le temps de la séance", Alert.AlertType.WARNING);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


}