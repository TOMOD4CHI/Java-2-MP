package org.cpi2.controllers;

import org.cpi2.entities.*;
import org.cpi2.entities.Moniteur;
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
import java.util.ArrayList;

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
    private ComboBox<String> moniteurIdFilter;

    @FXML
    private Button applyFiltersButton;

    @FXML
    private Button resetFiltersButton;

    private ObservableList<Session> sessionList = FXCollections.observableArrayList();

    // Service to fetch session data
    private org.cpi2.service.SessionService sessionService = new org.cpi2.service.SessionService();
    
    // Service to fetch moniteur data
    private org.cpi2.service.MoniteurService moniteurService = new org.cpi2.service.MoniteurService();




    public void initialize() {
        // Initialiser d'abord les colonnes du tableau
        setupTableColumns();
        // Puis configurer les filtres
        setupFilters();
        // Enfin charger les données
        loadSessions();
        
        // Ajouter un écouteur pour détecter les changements dans la liste des sessions
        sessionList.addListener((javafx.collections.ListChangeListener.Change<? extends Session> c) -> {
            System.out.println("Liste des sessions modifiée, nouvelle taille: " + sessionList.size());
        });
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
                SessionConduite sessionConduite = (SessionConduite) session;
                Vehicule vehicule = sessionConduite.getVehicule();
                if (vehicule != null) {
                    details = "Véhicule: " + vehicule.getImmatriculation() + 
                             " | Point de rencontre: " + sessionConduite.getPointRencontre();
                } else {
                    details = "Véhicule: Non assigné";
                }
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
        try {
            // Setup type session filter
            ObservableList<TypeSession> typesList = FXCollections.observableArrayList(TypeSession.values());
            typesList.add(0, null); // Add null for "All types"
            typeSessionFilter.setItems(typesList);
            
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
            
            // Load moniteurs into the ComboBox
            loadMoniteurs();

            // Setup action handlers
            applyFiltersButton.setOnAction(this::applyFilters);
            resetFiltersButton.setOnAction(this::resetFilters);
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des filtres: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadMoniteurs() {
        try {
            // Charger les moniteurs depuis la base de données
            List<Moniteur> moniteursList = moniteurService.getAllMoniteurs();
            
            ObservableList<String> moniteurs = FXCollections.observableArrayList();
            moniteurs.add("Tous les moniteurs"); // Option pour afficher tous les moniteurs
            
            if (moniteursList != null && !moniteursList.isEmpty()) {
                // Ajouter les moniteurs à la liste déroulante
                for (Moniteur moniteur : moniteursList) {
                    moniteurs.add(moniteur.getId() + " - " + moniteur.getNom() + " " + moniteur.getPrenom());
                }
            }
            
            moniteurIdFilter.setItems(moniteurs);
            moniteurIdFilter.setValue("Tous les moniteurs"); // Valeur par défaut
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des moniteurs: " + e.getMessage());
            e.printStackTrace();
            
            // Créer une liste par défaut en cas d'erreur
            ObservableList<String> defaultList = FXCollections.observableArrayList();
            defaultList.add("Tous les moniteurs");
            moniteurIdFilter.setItems(defaultList);
            moniteurIdFilter.setValue("Tous les moniteurs");
        }
    }

    @FXML
    private void applyFilters(ActionEvent event) {
        TypeSession typeSelected = typeSessionFilter.getValue();
        LocalDate dateDebut = dateDebutFilter.getValue();
        LocalDate dateFin = dateFinFilter.getValue();
        String moniteurSelection = moniteurIdFilter.getValue();

        Long moniteurId = null;
        if (moniteurSelection != null && !moniteurSelection.equals("Tous les moniteurs")) {
            try {
                // Extraire l'ID du format "ID - Nom Prénom"
                moniteurId = Long.parseLong(moniteurSelection.split(" - ")[0]);
            } catch (Exception e) {
                // En cas d'erreur, ignorer le filtre de moniteur
                showAlert(Alert.AlertType.WARNING, "Avertissement",
                        "Problème avec la sélection du moniteur",
                        "Le filtre de moniteur sera ignoré.");
            }
        }

        // Clear previous data
        sessionList.clear();
        
        // Apply filters based on selected criteria
        List<Session> filteredSessions = new java.util.ArrayList<>();
        
        // Get sessions based on type
        if (typeSelected == TypeSession.CODE) {
            filteredSessions.addAll(sessionService.viewAllSessionCode());
        } else if (typeSelected == TypeSession.CONDUITE) {
            filteredSessions.addAll(sessionService.viewAllSessionConduite());
        } else {
            // If no type selected, get all sessions
            filteredSessions.addAll(sessionService.viewAllSessionCode());
            filteredSessions.addAll(sessionService.viewAllSessionConduite());
        }
        
        // Filter by date range
        if (dateDebut != null || dateFin != null) {
            filteredSessions = filteredSessions.stream()
                .filter(session -> {
                    LocalDate sessionDate = session.getDateSession();
                    boolean afterStartDate = dateDebut == null || !sessionDate.isBefore(dateDebut);
                    boolean beforeEndDate = dateFin == null || !sessionDate.isAfter(dateFin);
                    return afterStartDate && beforeEndDate;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Filter by moniteur
        if (moniteurId != null) {
            Long finalMoniteurId = moniteurId;
            filteredSessions = filteredSessions.stream()
                .filter(session -> session.getMoniteur() != null && 
                                  session.getMoniteur().getId().equals(finalMoniteurId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // Update the table
        sessionList.addAll(filteredSessions);
        
        // Si aucune session n'est trouvée, afficher un message
        if (filteredSessions.isEmpty()) {
            showAlert(Alert.AlertType.INFORMATION, "Information", 
                    "Aucune séance trouvée", 
                    "Aucune séance ne correspond aux critères de recherche.");
        }
    }

    @FXML
    private void resetFilters(ActionEvent event) {
        typeSessionFilter.setValue(null);
        dateDebutFilter.setValue(null);
        dateFinFilter.setValue(LocalDate.now());
        moniteurIdFilter.setValue("Tous les moniteurs");

        loadSessions();
    }

    @FXML
    public void loadSessions() {
        // Clear previous data
        sessionList.clear();
        
        try {
            // Fetch all sessions (both code and conduite)
            List<SessionCode> codeSessions = sessionService.viewAllSessionCode();
            System.out.println("Sessions code récupérées: " + (codeSessions != null ? codeSessions.size() : "null"));
            
            List<SessionConduite> conduiteSessions = sessionService.viewAllSessionConduite();
            System.out.println("Sessions conduite récupérées: " + (conduiteSessions != null ? conduiteSessions.size() : "null"));
            
            // Vérifier si les listes sont nulles
            if (codeSessions == null) {
                System.err.println("La liste des sessions code est nulle");
                codeSessions = new ArrayList<>();
            }
            
            if (conduiteSessions == null) {
                System.err.println("La liste des sessions conduite est nulle");
                conduiteSessions = new ArrayList<>();
            }
            
            // Add all sessions to the list
            sessionList.addAll(codeSessions);
            sessionList.addAll(conduiteSessions);
            
            // Set items to table
            sessionTable.setItems(sessionList);
            
            // Log the number of sessions loaded for debugging
            System.out.println("Sessions chargées: " + sessionList.size() + 
                              " (Code: " + codeSessions.size() + ", Conduite: " + conduiteSessions.size() + ")");
            
            // If no sessions are loaded, show a message
            if (sessionList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", 
                        "Aucune séance disponible", 
                        "Aucune séance n'est disponible dans la base de données.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Erreur lors du chargement des séances", 
                    "Une erreur s'est produite lors du chargement des séances: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // No internal service class needed as we use the external service directly
}