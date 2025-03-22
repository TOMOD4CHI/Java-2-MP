package org.cpi2.controllers;

import org.cpi2.entitties.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AfficherSeance {

    @FXML
    private TableView<Session> sessionTable;

    @FXML
    private TableColumn<Session, LocalDate> dateSessionColumn;

    @FXML
    private TableColumn<Session, LocalTime> heureSessionColumn;

    @FXML
    private TableColumn<Session, String> moniteurColumn;

    @FXML
    private TableColumn<Session, Double> prixColumn;

    @FXML
    private TableColumn<Session, StatutSession> statutColumn;

    @FXML
    private TableColumn<Session, TypeSession> typeSessionColumn;

    @FXML
    private TableColumn<Session, String> detailsColumn;

    @FXML
    private ComboBox<TypeSession> typeSessionFilter;

    @FXML
    private DatePicker dateDebutFilter;

    @FXML
    private DatePicker dateFinFilter;

    @FXML
    private TextField moniteurIdFilter;

    @FXML
    private Button applyFiltersButton;

    @FXML
    private Button resetFiltersButton;

    private ObservableList<Session> sessionList = FXCollections.observableArrayList();

    // Service to fetch session data
    private SessionService sessionService;




    public void initialize() {
        setupTableColumns();
        setupFilters();
        loadSessions();
    }

    private void setupTableColumns() {
        dateSessionColumn.setCellValueFactory(new PropertyValueFactory<>("dateSession"));
        heureSessionColumn.setCellValueFactory(new PropertyValueFactory<>("heureSession"));
        moniteurColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getMoniteur().getNom() + " " + cellData.getValue().getMoniteur().getPrenom()
                )
        );
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statutColumn.setCellValueFactory(new PropertyValueFactory<>("statut"));
        typeSessionColumn.setCellValueFactory(new PropertyValueFactory<>("typeSession"));

        // Custom column to show type-specific details
        detailsColumn.setCellValueFactory(cellData -> {
            Session session = cellData.getValue();
            String details = "";

            if (session instanceof SessionCode) {
                SessionCode sessionCode = (SessionCode) session;
                details = "Salle: " + sessionCode.getSalle() +
                        " | Participants: " + sessionCode.getParticipants().size() +
                        "/" + sessionCode.getCapaciteMax();
            } else if (session instanceof SessionConduite) {
                // Adding placeholder for SessionConduite, assuming it exists
                // and has a getVehicule() method
                details = "Véhicule: " + "Variable à remplacer"; // Remplacer par sessionConduite.getVehicule().getImmatriculation()
            }

            return new javafx.beans.property.SimpleStringProperty(details);
        });

        // Custom cell factory for date formatting
        dateSessionColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Custom cell factory for time formatting
        heureSessionColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");

            @Override
            protected void updateItem(LocalTime time, boolean empty) {
                super.updateItem(time, empty);
                if (empty || time == null) {
                    setText(null);
                } else {
                    setText(formatter.format(time));
                }
            }
        });

        // Custom cell factory for price formatting
        prixColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", price));
                }
            }
        });

        // Custom cell factory for session type formatting
        typeSessionColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(TypeSession type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                } else {
                    setText(type == TypeSession.CODE ? "Code" : "Conduite");
                }
            }
        });
    }

    private void setupFilters() {
        // Setup type session filter
        typeSessionFilter.setItems(FXCollections.observableArrayList(TypeSession.values()));
        typeSessionFilter.setConverter(new StringConverter<TypeSession>() {
            @Override
            public String toString(TypeSession type) {
                if (type == null) {
                    return "Tous les types";
                }
                return type == TypeSession.CODE ? "Code" : "Conduite";
            }

            @Override
            public TypeSession fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
        typeSessionFilter.setValue(null); // Default to all types

        // Setup date filters with current date as default for date fin
        dateFinFilter.setValue(LocalDate.now());

        // Setup action handlers
        applyFiltersButton.setOnAction(this::applyFilters);
        resetFiltersButton.setOnAction(this::resetFilters);
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        TypeSession typeSelected = typeSessionFilter.getValue();
        LocalDate dateDebut = dateDebutFilter.getValue();
        LocalDate dateFin = dateFinFilter.getValue();
        String moniteurIdText = moniteurIdFilter.getText();

        Long moniteurId = null;
        if (moniteurIdText != null && !moniteurIdText.trim().isEmpty()) {
            try {
                moniteurId = Long.parseLong(moniteurIdText.trim());
            } catch (NumberFormatException e) {
                // Show error alert for invalid ID format
                showAlert(Alert.AlertType.ERROR, "Erreur de format",
                        "ID de moniteur invalide",
                        "Veuillez entrer un ID de moniteur valide (nombre entier).");
                return;
            }
        }

        // Fetch filtered sessions
        List<Session> filteredSessions = sessionService.getFilteredSessions(typeSelected, dateDebut, dateFin, moniteurId);
        sessionList.clear();
        sessionList.addAll(filteredSessions);
    }

    @FXML
    private void resetFilters(ActionEvent event) {
        typeSessionFilter.setValue(null);
        dateDebutFilter.setValue(null);
        dateFinFilter.setValue(LocalDate.now());
        moniteurIdFilter.clear();

        loadSessions();
    }

    public void loadSessions() {
        // Clear previous data
        sessionList.clear();

        /* Fetch sessions from service
        List<Session> sessions = sessionService.getAllSessions();
        sessionList.addAll(sessions);

        // Set items to table
        sessionTable.setItems(sessionList);*/
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Example service class stub (in real application, you would have a separate service class)
    private class SessionService {
        public List<Session> getAllSessions() {
            // This would be implemented to fetch data from your data source
            // For demonstration, returning an empty list
            return List.of();
        }

        public List<Session> getFilteredSessions(TypeSession type, LocalDate dateDebut, LocalDate dateFin, Long moniteurId) {
            // This would be implemented to fetch filtered data from your data source
            // For demonstration, returning an empty list
            return List.of();
        }
    }
}