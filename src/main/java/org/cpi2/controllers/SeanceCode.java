package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.SessionCode;
import org.cpi2.entities.TypePermis;
import org.cpi2.entities.TypeSession;
import org.cpi2.service.MoniteurService;
import org.cpi2.service.SessionService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    @FXML private TableColumn<Moniteur, String> candidatsColumn;

    @FXML private Button cancelButton;
    @FXML private Button planifierButton;

    private final MoniteurService moniteurService = new MoniteurService();
    private final SessionService sessionService = new SessionService();
    private Moniteur selectedMoniteur;

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

        // Set default date to today
        datefield.setValue(LocalDate.now());

        // Add listener for table selection
        moniteurTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMoniteur = newSelection;
                moniteurfield.setText(selectedMoniteur.getId() + " - " + selectedMoniteur.getNom() + " " + selectedMoniteur.getPrenom());
            }
        });

        // Add click handler for the table
        moniteurTableView.setOnMouseClicked(this::handleTableClick);

        // Set action handlers for buttons
        planifierButton.setOnAction(this::handlePlanifier);
        cancelButton.setOnAction(e -> handleCancel());

        // Load moniteurs from service instead of mock data
        loadMoniteurs();
    }

    private void loadMoniteurs() {
        try {
            List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
            if (moniteurs == null || moniteurs.isEmpty()) {
                // Fallback to mock data if no moniteurs found
                moniteurs = createMockMoniteurs();
            }
            moniteurTableView.getItems().clear();
            moniteurTableView.getItems().addAll(moniteurs);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des moniteurs: " + e.getMessage());
            e.printStackTrace();
            // Fallback to mock data
            loadMockMoniteurs();
        }
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
        moniteur1.setId(1L);

        Moniteur moniteur2 = new Moniteur("Martin", "Sophie", "CD5678",
                "456 Avenue Lyon", "0607080910",
                LocalDate.of(1990, 8, 20), "sophie.martin@example.com",
                LocalDate.of(2018, 6, 15), 2700.0);
        moniteur2.addSpecialite(TypePermis.A);
        moniteur2.setId(2L);

        moniteurs.add(moniteur1);
        moniteurs.add(moniteur2);

        return moniteurs;
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            Moniteur moniteur = moniteurTableView.getSelectionModel().getSelectedItem();
            if (moniteur != null) {
                selectedMoniteur = moniteur;
                moniteurfield.setText(selectedMoniteur.getId() + " - " + selectedMoniteur.getNom() + " " + selectedMoniteur.getPrenom());
            }
        }
    }

    @FXML
    private void handlePlanifier(ActionEvent event) {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            // Parse capacity
            int capacite = Integer.parseInt(capfield.getText());
            
            // Get date
            LocalDate date = datefield.getValue();
            
            // Parse time
            LocalTime time = LocalTime.parse(tempsfield.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            
            // Create session code
            SessionCode sessionCode = new SessionCode();
            sessionCode.setDateSession(date);
            sessionCode.setHeureSession(time);
            sessionCode.setMoniteur(selectedMoniteur);
            sessionCode.setCapaciteMax(capacite);
            sessionCode.setSalle("Salle de code");
            sessionCode.setTypeSession(TypeSession.CODE);
            
            // Définir un plan valide (par exemple, le plan 1 pour le code)
            sessionCode.setPlanId(1);
            
            // Définir une durée par défaut si non spécifiée (60 minutes)
            if (sessionCode.getDuree() <= 0) {
                sessionCode.setDuree(60);
            }
            
            // Save session
            boolean saved = sessionService.saveSessionCode(sessionCode);
            
            if (saved) {
                showAlert("Planification", "Séance de code planifiée avec succès", Alert.AlertType.INFORMATION);
                clearFields();
            } else {
                showAlert("Erreur", "Erreur lors de la planification de la séance", Alert.AlertType.ERROR);
            }
        } catch (NumberFormatException e) {
            showAlert("Validation", "La capacité doit être un nombre entier", Alert.AlertType.WARNING);
        } catch (Exception e) {
            System.err.println("Erreur lors de la planification: " + e.getMessage());
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de la planification: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void clearFields() {
        capfield.clear();
        datefield.setValue(LocalDate.now());
        tempsfield.clear();
        moniteurfield.clear();
        selectedMoniteur = null;
        moniteurTableView.getSelectionModel().clearSelection();
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
            showAlert("Validation", "Veuillez saisir le temps de la séance (format HH:mm)", Alert.AlertType.WARNING);
            return false;
        }

        if (selectedMoniteur == null) {
            showAlert("Validation", "Veuillez sélectionner un moniteur", Alert.AlertType.WARNING);
            return false;
        }

        // Validate time format
        try {
            LocalTime.parse(tempsfield.getText(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            showAlert("Validation", "Format de temps invalide. Utilisez le format HH:mm (ex: 14:30)", Alert.AlertType.WARNING);
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