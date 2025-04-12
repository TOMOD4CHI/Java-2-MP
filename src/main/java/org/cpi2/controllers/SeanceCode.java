package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.Salle;
import org.cpi2.entities.SessionCode;
import org.cpi2.entities.TypePermis;
import org.cpi2.entities.TypeSession;
import org.cpi2.service.CandidatService;
import org.cpi2.service.MoniteurService;
import org.cpi2.service.SalleService;
import org.cpi2.service.SessionService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeanceCode {
    @FXML private ComboBox<String> candidatCombo;
    @FXML private TextField capfield;
    @FXML private DatePicker datefield;
    @FXML private TextField tempsfield;
    @FXML private TextField moniteurfield;
    @FXML private ComboBox<String> salleCombo;
    
    @FXML private Label candidatError;
    @FXML private Label dateError;
    @FXML private Label tempsError;
    @FXML private Label moniteurError;
    @FXML private Label salleError;

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
    private final CandidatService candidatService = new CandidatService();
    private final SalleService salleService = new SalleService();
    private Moniteur selectedMoniteur;
    private Long selectedCandidatId;
    private Long selectedSalleId;

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
        
        // Set placeholders for input fields
        candidatCombo.setPromptText("Sélectionner un candidat");
        capfield.setPromptText("Nombre de places");
        tempsfield.setPromptText("Format: HH:mm");
        moniteurfield.setPromptText("Sélectionnez un moniteur");
        salleCombo.setPromptText("Sélectionner une salle");
        
        // Setup validation
        setupValidation();

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

        // Load data from services
        loadMoniteurs();
        loadCandidats();
        loadSalles();
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
    
    private void loadCandidats() {
        try {
            List<Candidat> candidats = candidatService.getAllCandidats();
            ObservableList<String> candidatItems = FXCollections.observableArrayList();
            
            if (candidats == null || candidats.isEmpty()) {
                AlertUtil.showWarning("Aucun candidat", "Aucun candidat trouvé dans la base de données.");
            } else {
                for (Candidat candidat : candidats) {
                    candidatItems.add(candidat.getId() + " - " + candidat.getNom() + " " + candidat.getPrenom());
                }
                candidatCombo.setItems(candidatItems);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des candidats: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors du chargement des candidats: " + e.getMessage());
        }
    }
    
    private void loadSalles() {
        try {
            List<Salle> salles = salleService.getAllSalles();
            ObservableList<String> salleItems = FXCollections.observableArrayList();
            
            if (salles == null || salles.isEmpty()) {
                AlertUtil.showWarning("Aucune salle", "Aucune salle trouvée dans la base de données.");
            } else {
                for (Salle salle : salles) {
                    salleItems.add(salle.getId() + " - " + salle.getNom() + " (" + salle.getNumero() + ")");
                }
                salleCombo.setItems(salleItems);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des salles: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors du chargement des salles: " + e.getMessage());
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
            // Get candidat ID
            String candidatValue = candidatCombo.getValue();
            Long candidatId = Long.parseLong(candidatValue.split(" - ")[0]);
            
            // Parse capacity
            int capacite = Integer.parseInt(capfield.getText());
            
            // Get date
            LocalDate date = datefield.getValue();
            
            // Parse time
            LocalTime time = LocalTime.parse(tempsfield.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            
            // Get salle
            String salleValue = salleCombo.getValue();
            String salleName = salleValue.split(" - ")[1].split("\\(")[0];
            
            // Create session code
            SessionCode sessionCode = new SessionCode();
            sessionCode.setDateSession(date);
            sessionCode.setHeureSession(time);
            sessionCode.setMoniteur(selectedMoniteur);
            sessionCode.setCapaciteMax(capacite);
            sessionCode.setSalle(salleName);
            sessionCode.setTypeSession(TypeSession.CODE);
            
            // Définir un plan valide (par exemple, le plan 1 pour le code)
            sessionCode.setPlanId(1);
            
            // Définir une durée par défaut si non spécifiée (60 minutes)
            if (sessionCode.getDuree() <= 0) {
                sessionCode.setDuree(60);
            }
            
            // Vérifier si le candidat existe
            if (!candidatService.getCandidatById(candidatId).isPresent()) {
                AlertUtil.showError("Erreur", "Le candidat sélectionné n'existe pas dans la base de données.");
                return;
            }
            
            // Save session
            boolean saved = sessionService.saveSessionCode(sessionCode);
            
            if (saved) {
                AlertUtil.showSuccess("Planification", "Séance de code planifiée avec succès");
                clearFields();
            } else {
                AlertUtil.showError("Erreur", "Erreur lors de la planification de la séance");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showConfirmation("Validation", "La capacité doit être un nombre entier");
        } catch (Exception e) {
            System.err.println("Erreur lors de la planification: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors de la planification: " + e.getMessage());
        }
    }

    private void clearFields() {
        candidatCombo.setValue(null);
        capfield.clear();
        datefield.setValue(LocalDate.now());
        tempsfield.clear();
        moniteurfield.clear();
        salleCombo.setValue(null);
        selectedMoniteur = null;
        selectedCandidatId = null;
        selectedSalleId = null;
        moniteurTableView.getSelectionModel().clearSelection();
        
        // Masquer les messages d'erreur
        candidatError.setVisible(false);
        dateError.setVisible(false);
        tempsError.setVisible(false);
        moniteurError.setVisible(false);
        salleError.setVisible(false);
        
        // Supprimer les styles de validation
        candidatCombo.getStyleClass().remove("error-field");
        candidatCombo.getStyleClass().remove("valid-field");
        capfield.getStyleClass().remove("error-field");
        capfield.getStyleClass().remove("valid-field");
        datefield.getStyleClass().remove("error-field");
        datefield.getStyleClass().remove("valid-field");
        tempsfield.getStyleClass().remove("error-field");
        tempsfield.getStyleClass().remove("valid-field");
        moniteurfield.getStyleClass().remove("error-field");
        moniteurfield.getStyleClass().remove("valid-field");
        salleCombo.getStyleClass().remove("error-field");
        salleCombo.getStyleClass().remove("valid-field");
    }

    @FXML
    private void handleCancel() {
        // Reset form instead of closing the window
        clearFields();
    }

    private void setupValidation() {
        // Réinitialiser les labels d'erreur
        candidatError.setVisible(false);
        dateError.setVisible(false);
        tempsError.setVisible(false);
        moniteurError.setVisible(false);
        salleError.setVisible(false);
        
        // Candidat validation
        candidatCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                candidatError.setText("Veuillez sélectionner un candidat");
                candidatError.setVisible(true);
                candidatCombo.getStyleClass().remove("valid-field");
                if (!candidatCombo.getStyleClass().contains("error-field")) {
                    candidatCombo.getStyleClass().add("error-field");
                }
            } else {
                candidatError.setVisible(false);
                candidatCombo.getStyleClass().remove("error-field");
                if (!candidatCombo.getStyleClass().contains("valid-field")) {
                    candidatCombo.getStyleClass().add("valid-field");
                }
            }
        });
        
        // Capacité validation
        capfield.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                candidatError.setText("La capacité est obligatoire");
                candidatError.setVisible(true);
                capfield.getStyleClass().remove("valid-field");
                if (!capfield.getStyleClass().contains("error-field")) {
                    capfield.getStyleClass().add("error-field");
                }
            } else {
                try {
                    int cap = Integer.parseInt(newVal);
                    if (cap <= 0) {
                        candidatError.setText("La capacité doit être un nombre entier positif");
                        candidatError.setVisible(true);
                        capfield.getStyleClass().remove("valid-field");
                        if (!capfield.getStyleClass().contains("error-field")) {
                            capfield.getStyleClass().add("error-field");
                        }
                    } else {
                        candidatError.setVisible(false);
                        capfield.getStyleClass().remove("error-field");
                        if (!capfield.getStyleClass().contains("valid-field")) {
                            capfield.getStyleClass().add("valid-field");
                        }
                    }
                } catch (NumberFormatException e) {
                    candidatError.setText("La capacité doit être un nombre entier positif");
                    candidatError.setVisible(true);
                    capfield.getStyleClass().remove("valid-field");
                    if (!capfield.getStyleClass().contains("error-field")) {
                        capfield.getStyleClass().add("error-field");
                    }
                }
            }
        });
        
        // Date validation
        datefield.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                dateError.setText("La date est obligatoire");
                dateError.setVisible(true);
                datefield.getStyleClass().remove("valid-field");
                if (!datefield.getStyleClass().contains("error-field")) {
                    datefield.getStyleClass().add("error-field");
                }
            } else if (newVal.isBefore(LocalDate.now())) {
                dateError.setText("La date ne peut pas être dans le passé");
                dateError.setVisible(true);
                datefield.getStyleClass().remove("valid-field");
                if (!datefield.getStyleClass().contains("error-field")) {
                    datefield.getStyleClass().add("error-field");
                }
            } else {
                dateError.setVisible(false);
                datefield.getStyleClass().remove("error-field");
                if (!datefield.getStyleClass().contains("valid-field")) {
                    datefield.getStyleClass().add("valid-field");
                }
            }
        });
        
        // Temps validation
        tempsfield.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                tempsError.setText("L'heure est obligatoire");
                tempsError.setVisible(true);
                tempsfield.getStyleClass().remove("valid-field");
                if (!tempsfield.getStyleClass().contains("error-field")) {
                    tempsfield.getStyleClass().add("error-field");
                }
            } else {
                try {
                    LocalTime.parse(newVal, DateTimeFormatter.ofPattern("HH:mm"));
                    tempsError.setVisible(false);
                    tempsfield.getStyleClass().remove("error-field");
                    if (!tempsfield.getStyleClass().contains("valid-field")) {
                        tempsfield.getStyleClass().add("valid-field");
                    }
                } catch (Exception e) {
                    tempsError.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
                    tempsError.setVisible(true);
                    tempsfield.getStyleClass().remove("valid-field");
                    if (!tempsfield.getStyleClass().contains("error-field")) {
                        tempsfield.getStyleClass().add("error-field");
                    }
                }
            }
        });
        
        // Moniteur validation
        moniteurfield.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                moniteurError.setText("Veuillez sélectionner un moniteur");
                moniteurError.setVisible(true);
                moniteurfield.getStyleClass().remove("valid-field");
                if (!moniteurfield.getStyleClass().contains("error-field")) {
                    moniteurfield.getStyleClass().add("error-field");
                }
            } else {
                moniteurError.setVisible(false);
                moniteurfield.getStyleClass().remove("error-field");
                if (!moniteurfield.getStyleClass().contains("valid-field")) {
                    moniteurfield.getStyleClass().add("valid-field");
                }
            }
        });
        
        // Salle validation
        salleCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                salleError.setText("Veuillez sélectionner une salle");
                salleError.setVisible(true);
                salleCombo.getStyleClass().remove("valid-field");
                if (!salleCombo.getStyleClass().contains("error-field")) {
                    salleCombo.getStyleClass().add("error-field");
                }
            } else {
                salleError.setVisible(false);
                salleCombo.getStyleClass().remove("error-field");
                if (!salleCombo.getStyleClass().contains("valid-field")) {
                    salleCombo.getStyleClass().add("valid-field");
                }
            }
        });
    }
    
    private boolean validateInputs() {
        // Vérifier les entrées manuellement
        boolean hasErrors = false;
        
        // Vérifier le candidat
        if (candidatCombo.getValue() == null) {
            candidatError.setText("Veuillez sélectionner un candidat");
            candidatError.setVisible(true);
            hasErrors = true;
        }
        
        // Vérifier la capacité
        if (capfield.getText().trim().isEmpty()) {
            candidatError.setText("La capacité est obligatoire");
            candidatError.setVisible(true);
            hasErrors = true;
        } else {
            try {
                int cap = Integer.parseInt(capfield.getText());
                if (cap <= 0) {
                    candidatError.setText("La capacité doit être un nombre entier positif");
                    candidatError.setVisible(true);
                    hasErrors = true;
                }
            } catch (NumberFormatException e) {
                candidatError.setText("La capacité doit être un nombre entier positif");
                candidatError.setVisible(true);
                hasErrors = true;
            }
        }
        
        // Vérifier la date
        if (datefield.getValue() == null) {
            dateError.setText("La date est obligatoire");
            dateError.setVisible(true);
            hasErrors = true;
        } else if (datefield.getValue().isBefore(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le passé");
            dateError.setVisible(true);
            hasErrors = true;
        }
        
        // Vérifier l'heure
        if (tempsfield.getText().trim().isEmpty()) {
            tempsError.setText("L'heure est obligatoire");
            tempsError.setVisible(true);
            hasErrors = true;
        } else {
            try {
                LocalTime.parse(tempsfield.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                tempsError.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
                tempsError.setVisible(true);
                hasErrors = true;
            }
        }
        
        // Vérifier le moniteur
        if (moniteurfield.getText().trim().isEmpty() || selectedMoniteur == null) {
            moniteurError.setText("Veuillez sélectionner un moniteur");
            moniteurError.setVisible(true);
            hasErrors = true;
        }
        
        // Vérifier la salle
        if (salleCombo.getValue() == null) {
            salleError.setText("Veuillez sélectionner une salle");
            salleError.setVisible(true);
            hasErrors = true;
        }
        
        return !hasErrors;
    }

}