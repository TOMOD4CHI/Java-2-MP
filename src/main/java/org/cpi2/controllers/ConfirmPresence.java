package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cpi2.entitties.Seance;
import org.cpi2.service.SeanceService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ConfirmPresence {
    
    @FXML private TableView<Seance> sessionsTable;
    @FXML private TableColumn<Seance, Long> idColumn;
    @FXML private TableColumn<Seance, String> typeColumn;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> timeColumn;
    @FXML private TableColumn<Seance, String> candidateColumn;
    @FXML private TableColumn<Seance, String> monitorColumn;
    
    @FXML private ComboBox<String> sessionIdCombo;
    @FXML private ComboBox<String> presenceStatusCombo;
    @FXML private TextArea commentArea;
    
    private final SeanceService seanceService = new SeanceService();
    private final ObservableList<Seance> seanceList = FXCollections.observableArrayList();
    
    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        timeColumn.setCellValueFactory(new PropertyValueFactory<>("temps"));
        candidateColumn.setCellValueFactory(new PropertyValueFactory<>("candidatName"));
        monitorColumn.setCellValueFactory(new PropertyValueFactory<>("moniteurName"));
        
        // Initialize the presence status combo box
        presenceStatusCombo.getItems().addAll("Présent", "Absent", "Retard", "Excusé");
        
        // Load available sessions from database
        loadSessions();
        
        // Add listener to the table selection to populate the combo box
        sessionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                sessionIdCombo.setValue(String.valueOf(newSelection.getId()));
            }
        });
        
        // Load session IDs into combo box
        loadSessionIds();
    }
    
    private void loadSessions() {
        try {
            // Load sessions with today's date or future dates
            List<Seance> sessions = seanceService.findAllSeances();
            
            // Filter for sessions that haven't been marked for attendance yet
            List<Seance> pendingSessions = sessions.stream()
                    .filter(seance -> seance.getStatut() == null || seance.getStatut().isEmpty())
                    .toList();
            
            seanceList.clear();
            seanceList.addAll(pendingSessions);
            sessionsTable.setItems(seanceList);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les séances: " + e.getMessage());
        }
    }
    
    private void loadSessionIds() {
        ObservableList<String> sessionIds = FXCollections.observableArrayList();
        for (Seance seance : seanceList) {
            sessionIds.add(String.valueOf(seance.getId()));
        }
        sessionIdCombo.setItems(sessionIds);
    }
    
    @FXML
    public void handleConfirm(ActionEvent event) {
        String sessionId = sessionIdCombo.getValue();
        String status = presenceStatusCombo.getValue();
        String comment = commentArea.getText();
        
        if (sessionId == null || sessionId.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une séance.");
            return;
        }
        
        if (status == null || status.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner un statut de présence.");
            return;
        }
        
        try {
            Long id = Long.parseLong(sessionId);
            boolean success = seanceService.updateSessionStatus(id, status, comment);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "Statut de présence enregistré avec succès!");
                clearForm();
                loadSessions(); // Reload the list
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'enregistrement du statut de présence.");
            }
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "ID de séance invalide.");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur s'est produite: " + e.getMessage());
        }
    }
    
    @FXML
    public void handleCancel(ActionEvent event) {
        clearForm();
    }
    
    private void clearForm() {
        sessionIdCombo.setValue(null);
        presenceStatusCombo.setValue(null);
        commentArea.clear();
        sessionsTable.getSelectionModel().clearSelection();
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 