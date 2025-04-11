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
import org.cpi2.utils.AlertUtil;

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

    
    private org.cpi2.service.SessionService sessionService = new org.cpi2.service.SessionService();
    
    
    private org.cpi2.service.MoniteurService moniteurService = new org.cpi2.service.MoniteurService();




    public void initialize() {
        
        setupTableColumns();
        
        setupFilters();
        
        loadSessions();
        
        
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
            
            ObservableList<TypeSession> typesList = FXCollections.observableArrayList(TypeSession.values());
            typesList.add(0, null); 
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
                    return null; 
                }
            });
            typeSessionFilter.setValue(null); 

            
            dateFinFilter.setValue(LocalDate.now());
            
            
            loadMoniteurs();

            
            applyFiltersButton.setOnAction(this::applyFilters);
            resetFiltersButton.setOnAction(this::resetFilters);
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des filtres: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadMoniteurs() {
        try {
            
            List<Moniteur> moniteursList = moniteurService.getAllMoniteurs();
            
            ObservableList<String> moniteurs = FXCollections.observableArrayList();
            moniteurs.add("Tous les moniteurs"); 
            
            if (moniteursList != null && !moniteursList.isEmpty()) {
                
                for (Moniteur moniteur : moniteursList) {
                    moniteurs.add(moniteur.getId() + " - " + moniteur.getNom() + " " + moniteur.getPrenom());
                }
            }
            
            moniteurIdFilter.setItems(moniteurs);
            moniteurIdFilter.setValue("Tous les moniteurs"); 
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des moniteurs: " + e.getMessage());
            e.printStackTrace();
            
            
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
                
                moniteurId = Long.parseLong(moniteurSelection.split(" - ")[0]);
            } catch (Exception e) {
                AlertUtil.showWarning( "Problème avec la sélection du moniteur",
                        "Le filtre de moniteur sera ignoré." );

            }
        }

        
        sessionList.clear();
        
        
        List<Session> filteredSessions = new java.util.ArrayList<>();
        
        
        if (typeSelected == TypeSession.CODE) {
            filteredSessions.addAll(sessionService.viewAllSessionCode());
        } else if (typeSelected == TypeSession.CONDUITE) {
            filteredSessions.addAll(sessionService.viewAllSessionConduite());
        } else {
            
            filteredSessions.addAll(sessionService.viewAllSessionCode());
            filteredSessions.addAll(sessionService.viewAllSessionConduite());
        }
        
        
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
        
        
        if (moniteurId != null) {
            Long finalMoniteurId = moniteurId;
            filteredSessions = filteredSessions.stream()
                .filter(session -> session.getMoniteur() != null && 
                                  session.getMoniteur().getId().equals(finalMoniteurId))
                .collect(java.util.stream.Collectors.toList());
        }
        
        
        sessionList.addAll(filteredSessions);
        
        
        if (filteredSessions.isEmpty()) {
            AlertUtil.showInfo("Aucune séance trouvée",
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
        
        sessionList.clear();
        
        try {
            
            List<SessionCode> codeSessions = sessionService.viewAllSessionCode();
            System.out.println("Sessions code récupérées: " + (codeSessions != null ? codeSessions.size() : "null"));
            
            List<SessionConduite> conduiteSessions = sessionService.viewAllSessionConduite();
            System.out.println("Sessions conduite récupérées: " + (conduiteSessions != null ? conduiteSessions.size() : "null"));
            
            
            if (codeSessions == null) {
                System.err.println("La liste des sessions code est nulle");
                codeSessions = new ArrayList<>();
            }
            
            if (conduiteSessions == null) {
                System.err.println("La liste des sessions conduite est nulle");
                conduiteSessions = new ArrayList<>();
            }
            
            
            sessionList.addAll(codeSessions);
            sessionList.addAll(conduiteSessions);
            
            
            sessionTable.setItems(sessionList);
            
            
            System.out.println("Sessions chargées: " + sessionList.size() + 
                              " (Code: " + codeSessions.size() + ", Conduite: " + conduiteSessions.size() + ")");
            
            
            if (sessionList.isEmpty()) {
                AlertUtil.showInfo(
                        "Aucune séance disponible", 
                        "Aucune séance n'est disponible dans la base de données.");
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError(
                    "Erreur lors du chargement des séances", 
                    "Une erreur s'est produite lors du chargement des séances: " + e.getMessage());
        }
    }
}