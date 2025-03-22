package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.cpi2.entitties.Candidat;
import org.cpi2.entitties.Moniteur;
import org.cpi2.entitties.Seance;
import org.cpi2.entitties.Vehicule;
import org.cpi2.service.CandidatService;
import org.cpi2.service.MoniteurService;
import org.cpi2.service.SeanceService;
import org.cpi2.service.VehiculeService;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class GestionSeances implements Initializable {

    // FXML Components for planning a session
    @FXML private ComboBox<String> typeSeanceComboBox;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<LocalTime> heureComboBox;
    @FXML private ComboBox<Integer> dureeComboBox;
    @FXML private ComboBox<Moniteur> moniteurComboBox;
    @FXML private ComboBox<Candidat> candidatComboBox;
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private Label vehiculeLabel;

    // FXML Components for filtering sessions
    @FXML private ComboBox<String> filtreTypeComboBox;
    @FXML private ComboBox<String> filtreStatutComboBox;
    @FXML private DatePicker filtreDate;

    // FXML Components for displaying sessions
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> heureColumn;
    @FXML private TableColumn<Seance, String> dureeColumn;
    @FXML private TableColumn<Seance, String> typeColumn;
    @FXML private TableColumn<Seance, String> moniteurColumn;
    @FXML private TableColumn<Seance, String> candidatColumn;
    @FXML private TableColumn<Seance, String> vehiculeColumn;
    @FXML private TableColumn<Seance, String> statutColumn;

    // Services
    private final SeanceService seanceService = new SeanceService();
    private final MoniteurService moniteurService = new MoniteurService();
    private final CandidatService candidatService = new CandidatService();
    private final VehiculeService vehiculeService = new VehiculeService();

    // Observable lists for the table view
    private ObservableList<Seance> seancesList = FXCollections.observableArrayList();
    private ObservableList<Seance> filteredSeancesList = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize comboboxes with data
        setupComboBoxes();
        setupTableView();
        
        // Initialize with today's date
        datePicker.setValue(LocalDate.now());
        filtreDate.setValue(null); // Initially no date filter
        
        // Load initial data
        loadSeances();
        
        // Add listeners
        setupListeners();
    }
    
    private void setupComboBoxes() {
        // Session types
        typeSeanceComboBox.getItems().addAll("Conduite", "Code", "Examen blanc");
        typeSeanceComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isConduiteSelected = "Conduite".equals(newVal);
            vehiculeComboBox.setDisable(!isConduiteSelected);
            vehiculeLabel.setDisable(!isConduiteSelected);
        });
        
        // Hours (8:00 to 18:00, every hour)
        ObservableList<LocalTime> hours = FXCollections.observableArrayList();
        for (int hour = 8; hour <= 18; hour++) {
            hours.add(LocalTime.of(hour, 0));
        }
        heureComboBox.setItems(hours);
        heureComboBox.setConverter(new StringConverter<LocalTime>() {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            @Override
            public String toString(LocalTime time) {
                return time != null ? formatter.format(time) : "";
            }
            
            @Override
            public LocalTime fromString(String string) {
                return string != null && !string.isEmpty() 
                    ? LocalTime.parse(string, formatter) : null;
            }
        });
        
        // Duration (30, 45, 60, 90, 120 minutes)
        dureeComboBox.setItems(FXCollections.observableArrayList(30, 45, 60, 90, 120));
        
        // Moniteurs, Candidats and Vehicules from services
        refreshComboBoxesData();
        
        // Setup filter comboboxes
        filtreTypeComboBox.getItems().addAll("Tous", "Conduite", "Code", "Examen blanc");
        filtreTypeComboBox.getSelectionModel().selectFirst();
        
        filtreStatutComboBox.getItems().addAll("Tous", "Planifiée", "Terminée", "Annulée");
        filtreStatutComboBox.getSelectionModel().selectFirst();
    }
    
    private void refreshComboBoxesData() {
        // Load moniteurs
        List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
        moniteurComboBox.setItems(FXCollections.observableArrayList(moniteurs));
        moniteurComboBox.setConverter(new StringConverter<Moniteur>() {
            @Override
            public String toString(Moniteur moniteur) {
                return moniteur != null ? moniteur.getNom() + " " + moniteur.getPrenom() : "";
            }
            
            @Override
            public Moniteur fromString(String string) {
                return null; // Not needed for combobox
            }
        });
        
        // Load candidats
        List<Candidat> candidats = candidatService.getAllCandidats();
        candidatComboBox.setItems(FXCollections.observableArrayList(candidats));
        candidatComboBox.setConverter(new StringConverter<Candidat>() {
            @Override
            public String toString(Candidat candidat) {
                return candidat != null ? candidat.getNom() + " " + candidat.getPrenom() : "";
            }
            
            @Override
            public Candidat fromString(String string) {
                return null; // Not needed for combobox
            }
        });
        
        // Load vehicules
        List<Vehicule> vehicules = vehiculeService.getAllVehicules();
        vehiculeComboBox.setItems(FXCollections.observableArrayList(vehicules));
        vehiculeComboBox.setConverter(new StringConverter<Vehicule>() {
            @Override
            public String toString(Vehicule vehicule) {
                return vehicule != null ? vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")" : "";
            }
            
            @Override
            public Vehicule fromString(String string) {
                return null; // Not needed for combobox
            }
        });
    }
    
    private void setupTableView() {
        // Configure table columns
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().toLocalDate().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))));
            
        heureColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))));
            
        dureeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDuree() + " min"));
            
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType()));
            
        moniteurColumn.setCellValueFactory(cellData -> {
            Moniteur moniteur = cellData.getValue().getMoniteur();
            return new SimpleStringProperty(moniteur.getNom() + " " + moniteur.getPrenom());
        });
            
        candidatColumn.setCellValueFactory(cellData -> {
            Candidat candidat = cellData.getValue().getCandidat();
            return new SimpleStringProperty(candidat.getNom() + " " + candidat.getPrenom());
        });
            
        vehiculeColumn.setCellValueFactory(cellData -> {
            Vehicule vehicule = cellData.getValue().getVehicule();
            return new SimpleStringProperty(vehicule != null ? 
                vehicule.getMarque() + " " + vehicule.getModele() : "N/A");
        });
            
        statutColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatut()));
            
        // Bind the table to the filtered list
        seancesTableView.setItems(filteredSeancesList);
    }
    
    private void setupListeners() {
        // Add listener to type combo box to enable/disable vehicle selection
        typeSeanceComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            boolean isConduite = "Conduite".equals(newVal);
            vehiculeComboBox.setDisable(!isConduite);
        });
    }
    
    private void loadSeances() {
        // Load all seances from the service
        seancesList.clear();
        seancesList.addAll(seanceService.getAllSeances());
        
        // Apply current filters
        applyFilters();
    }
    
    private void applyFilters() {
        filteredSeancesList.clear();
        
        // Get filter values
        String typeFilter = filtreTypeComboBox.getValue();
        String statutFilter = filtreStatutComboBox.getValue();
        LocalDate dateFilter = filtreDate.getValue();
        
        // Apply filters
        List<Seance> filtered = seancesList.stream()
            .filter(seance -> "Tous".equals(typeFilter) || seance.getType().equals(typeFilter))
            .filter(seance -> "Tous".equals(statutFilter) || seance.getStatut().equals(statutFilter))
            .filter(seance -> dateFilter == null || seance.getDate().toLocalDate().equals(dateFilter))
            .collect(Collectors.toList());
            
        filteredSeancesList.addAll(filtered);
    }
    
    @FXML
    private void handlePlanifierSeance(ActionEvent event) {
        // Validate input
        if (!validateInputs()) {
            return;
        }
        
        // Create new seance
        Seance seance = new Seance();
        
        // Set seance properties
        LocalDateTime dateTime = LocalDateTime.of(datePicker.getValue(), heureComboBox.getValue());
        seance.setDate(dateTime);
        seance.setDuree(dureeComboBox.getValue());
        seance.setType(typeSeanceComboBox.getValue());
        seance.setMoniteur(moniteurComboBox.getValue());
        seance.setCandidat(candidatComboBox.getValue());
        
        // Set vehicule only for driving sessions
        if ("Conduite".equals(typeSeanceComboBox.getValue())) {
            seance.setVehicule(vehiculeComboBox.getValue());
        }
        
        // Set default status
        seance.setStatut("Planifiée");
        
        // Save the seance
        boolean success = seanceService.addSeance(seance);
        
        if (success) {
            showSuccessAlert("Succès", "La séance a été planifiée avec succès");
            // Reset form
            resetPlanifierForm();
            // Reload seances
            loadSeances();
        } else {
            showErrorAlert("Erreur", "Impossible de planifier la séance");
        }
    }
    
    @FXML
    private void handleReinitialiser(ActionEvent event) {
        resetPlanifierForm();
    }
    
    @FXML
    private void handleFiltrer(ActionEvent event) {
        applyFilters();
    }
    
    @FXML
    private void handleResetFiltre(ActionEvent event) {
        filtreTypeComboBox.getSelectionModel().selectFirst();
        filtreStatutComboBox.getSelectionModel().selectFirst();
        filtreDate.setValue(null);
        applyFilters();
    }
    
    @FXML
    private void handleModifier(ActionEvent event) {
        Seance selectedSeance = seancesTableView.getSelectionModel().getSelectedItem();
        if (selectedSeance == null) {
            showErrorAlert("Sélection requise", "Veuillez sélectionner une séance à modifier");
            return;
        }
        
        if (!"Planifiée".equals(selectedSeance.getStatut())) {
            showErrorAlert("Modification impossible", "Seules les séances planifiées peuvent être modifiées");
            return;
        }
        
        // Fill the form with the selected seance data
        typeSeanceComboBox.setValue(selectedSeance.getType());
        datePicker.setValue(selectedSeance.getDate().toLocalDate());
        heureComboBox.setValue(selectedSeance.getDate().toLocalTime());
        dureeComboBox.setValue(selectedSeance.getDuree());
        moniteurComboBox.setValue(selectedSeance.getMoniteur());
        candidatComboBox.setValue(selectedSeance.getCandidat());
        
        if (selectedSeance.getVehicule() != null) {
            vehiculeComboBox.setValue(selectedSeance.getVehicule());
        }
        
        // Remove the selected seance
        seanceService.removeSeance(selectedSeance.getId());
        loadSeances();
    }
    
    @FXML
    private void handleAnnuler(ActionEvent event) {
        Seance selectedSeance = seancesTableView.getSelectionModel().getSelectedItem();
        if (selectedSeance == null) {
            showErrorAlert("Sélection requise", "Veuillez sélectionner une séance à annuler");
            return;
        }
        
        if (!"Planifiée".equals(selectedSeance.getStatut())) {
            showErrorAlert("Annulation impossible", "Seules les séances planifiées peuvent être annulées");
            return;
        }
        
        // Show confirmation alert
        boolean confirmed = showConfirmationAlert("Confirmation", 
            "Êtes-vous sûr de vouloir annuler cette séance?");
            
        if (confirmed) {
            selectedSeance.setStatut("Annulée");
            seanceService.updateSeance(selectedSeance);
            loadSeances();
        }
    }
    
    @FXML
    private void handleTerminer(ActionEvent event) {
        Seance selectedSeance = seancesTableView.getSelectionModel().getSelectedItem();
        if (selectedSeance == null) {
            showErrorAlert("Sélection requise", "Veuillez sélectionner une séance à marquer comme terminée");
            return;
        }
        
        if (!"Planifiée".equals(selectedSeance.getStatut())) {
            showErrorAlert("Action impossible", "Seules les séances planifiées peuvent être marquées comme terminées");
            return;
        }
        
        // Show confirmation alert
        boolean confirmed = showConfirmationAlert("Confirmation", 
            "Êtes-vous sûr de vouloir marquer cette séance comme terminée?");
            
        if (confirmed) {
            selectedSeance.setStatut("Terminée");
            seanceService.updateSeance(selectedSeance);
            loadSeances();
        }
    }
    
    private boolean validateInputs() {
        StringBuilder errors = new StringBuilder();
        
        if (typeSeanceComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un type de séance\n");
        }
        
        if (datePicker.getValue() == null) {
            errors.append("- Veuillez sélectionner une date\n");
        } else if (datePicker.getValue().isBefore(LocalDate.now())) {
            errors.append("- La date doit être aujourd'hui ou dans le futur\n");
        }
        
        if (heureComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner une heure\n");
        }
        
        if (dureeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner une durée\n");
        }
        
        if (moniteurComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un moniteur\n");
        }
        
        if (candidatComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un candidat\n");
        }
        
        if ("Conduite".equals(typeSeanceComboBox.getValue()) && vehiculeComboBox.getValue() == null) {
            errors.append("- Veuillez sélectionner un véhicule\n");
        }
        
        if (errors.length() > 0) {
            showErrorAlert("Formulaire invalide", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void resetPlanifierForm() {
        typeSeanceComboBox.setValue(null);
        datePicker.setValue(LocalDate.now());
        heureComboBox.setValue(null);
        dureeComboBox.setValue(null);
        moniteurComboBox.setValue(null);
        candidatComboBox.setValue(null);
        vehiculeComboBox.setValue(null);
        vehiculeComboBox.setDisable(true);
    }
    
    private void showSuccessAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private boolean showConfirmationAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        return alert.showAndWait().filter(ButtonType.OK::equals).isPresent();
    }
} 