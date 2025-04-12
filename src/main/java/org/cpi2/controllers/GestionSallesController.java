package org.cpi2.controllers;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.cpi2.entities.Salle;
import org.cpi2.service.SalleService;
import org.cpi2.utils.AlertUtil;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class GestionSallesController {

    @FXML private TableView<Salle> salleTableView;
    @FXML private TableColumn<Salle, Long> idColumn;
    @FXML private TableColumn<Salle, String> nomColumn;
    @FXML private TableColumn<Salle, String> numeroColumn;
    @FXML private TableColumn<Salle, Integer> capaciteColumn;
    @FXML private TableColumn<Salle, Void> actionsColumn;
    
    @FXML private TextField searchField;
    @FXML private Label formTitle;
    @FXML private TextField nomField;
    @FXML private TextField numeroField;
    @FXML private TextField capaciteField;
    @FXML private TextArea notesTextArea;
    
    @FXML private Label nomError;
    @FXML private Label numeroError;
    @FXML private Label capaciteError;
    
    @FXML private Button clearButton;
    @FXML private Button saveButton;
    @FXML private Button refreshButton;
    
    private final SalleService salleService = new SalleService();
    private ObservableList<Salle> sallesList = FXCollections.observableArrayList();
    private FilteredList<Salle> filteredSalles;
    
    private Long currentSalleId = null;
    private boolean isEditing = false;
    
    @FXML
    public void initialize() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        capaciteColumn.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        
        setupActionsColumn();
        
        // Setup filtered list
        filteredSalles = new FilteredList<>(sallesList, p -> true);
        salleTableView.setItems(filteredSalles);
        
        // Setup search functionality
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredSalles.setPredicate(createPredicate(newValue));
        });
        
        // Apply button styling
        refreshButton.getStyleClass().setAll("action-button", "small-button");
        
        // Setup error labels
        setupErrorLabels();
        
        // Load initial data
        loadSalles();
    }
    
    private void setupActionsColumn() {
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");
            private final HBox pane = new HBox(5, editButton, deleteButton);
            
            {
                // Configure buttons with application-wide styles
                editButton.getStyleClass().addAll("primary-button", "small-button");
                deleteButton.getStyleClass().addAll("secondary-button", "small-button");
                pane.setAlignment(Pos.CENTER);
                
                // Set button actions
                editButton.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    handleEditSalle(salle);
                });
                
                deleteButton.setOnAction(event -> {
                    Salle salle = getTableView().getItems().get(getIndex());
                    handleDeleteSalle(salle);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(pane);
                }
            }
        });
    }
    
    private void setupErrorLabels() {
        nomError.getStyleClass().add("error-label");
        numeroError.getStyleClass().add("error-label");
        capaciteError.getStyleClass().add("error-label");
    }
    
    private void loadSalles() {
        try {
            List<Salle> salles = salleService.getAllSalles();
            sallesList.setAll(salles);
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Erreur lors du chargement des salles: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleRefreshAction() {
        loadSalles();
        clearForm();
    }
    
    @FXML
    private void handleClearAction() {
        clearForm();
    }
    
    @FXML
    private void handleSaveAction() {
        if (validateForm()) {
            try {
                String nom = nomField.getText().trim();
                String numero = numeroField.getText().trim();
                int capacite = Integer.parseInt(capaciteField.getText().trim());
                String notes = notesTextArea.getText().trim();
                
                Salle salle = new Salle(nom, numero, capacite, notes);
                
                boolean success;
                if (isEditing) {
                    salle.setId(currentSalleId);
                    success = salleService.updateSalle(salle);
                    if (success) {
                        AlertUtil.showSuccess("Succès", "Salle mise à jour avec succès");
                    } else {
                        AlertUtil.showError("Erreur", "Échec de la mise à jour de la salle");
                    }
                } else {
                    success = salleService.addSalle(salle);
                    if (success) {
                        AlertUtil.showSuccess("Succès", "Salle ajoutée avec succès");
                    } else {
                        AlertUtil.showError("Erreur", "Échec de l'ajout de la salle");
                    }
                }
                
                if (success) {
                    clearForm();
                    loadSalles();
                }
            } catch (NumberFormatException e) {
                capaciteError.setText("La capacité doit être un nombre entier");
                capaciteError.setVisible(true);
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
            }
        }
    }
    
    private void handleEditSalle(Salle salle) {
        formTitle.setText("Modifier une Salle");
        isEditing = true;
        currentSalleId = salle.getId();
        
        nomField.setText(salle.getNom());
        numeroField.setText(salle.getNumero());
        capaciteField.setText(String.valueOf(salle.getCapacite()));
        notesTextArea.setText(salle.getNotes());
        
        saveButton.setText("Mettre à jour");
    }
    
    private void handleDeleteSalle(Salle salle) {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirmation de suppression");
        confirmation.setHeaderText("Supprimer la salle");
        confirmation.setContentText("Êtes-vous sûr de vouloir supprimer la salle " + salle.getNom() + " (" + salle.getNumero() + ") ?");
        
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = salleService.deleteSalle(salle.getId());
            if (success) {
                AlertUtil.showSuccess("Succès", "Salle supprimée avec succès");
                loadSalles();
                
                // If we were editing this salle, clear the form
                if (isEditing && currentSalleId != null && currentSalleId.equals(salle.getId())) {
                    clearForm();
                }
            } else {
                AlertUtil.showError("Erreur", "Échec de la suppression de la salle");
            }
        }
    }
    
    private void clearForm() {
        formTitle.setText("Ajouter une Salle");
        isEditing = false;
        currentSalleId = null;
        
        nomField.clear();
        numeroField.clear();
        capaciteField.clear();
        notesTextArea.clear();
        
        nomError.setVisible(false);
        numeroError.setVisible(false);
        capaciteError.setVisible(false);
        
        saveButton.setText("Enregistrer");
    }
    
    private boolean validateForm() {
        boolean isValid = true;
        
        // Validate nom
        if (nomField.getText().trim().isEmpty()) {
            nomError.setText("Le nom est obligatoire");
            nomError.setVisible(true);
            isValid = false;
        } else if (nomField.getText().trim().length() < 3) {
            nomError.setText("Le nom doit contenir au moins 3 caractères");
            nomError.setVisible(true);
            isValid = false;
        } else {
            nomError.setVisible(false);
        }
        
        // Validate numero
        if (numeroField.getText().trim().isEmpty()) {
            numeroError.setText("Le numéro est obligatoire");
            numeroError.setVisible(true);
            isValid = false;
        } else {
            // Check if numero is unique (when adding or changing numero)
            if (!isEditing || !numeroField.getText().equals(salleService.getSalleById(currentSalleId).map(Salle::getNumero).orElse(""))) {
                Optional<Salle> existingSalle = salleService.getSalleByNumero(numeroField.getText().trim());
                if (existingSalle.isPresent()) {
                    numeroError.setText("Ce numéro est déjà utilisé");
                    numeroError.setVisible(true);
                    isValid = false;
                } else {
                    numeroError.setVisible(false);
                }
            } else {
                numeroError.setVisible(false);
            }
        }
        
        // Validate capacite
        if (capaciteField.getText().trim().isEmpty()) {
            capaciteError.setText("La capacité est obligatoire");
            capaciteError.setVisible(true);
            isValid = false;
        } else {
            try {
                int capacite = Integer.parseInt(capaciteField.getText().trim());
                if (capacite <= 0) {
                    capaciteError.setText("La capacité doit être supérieure à 0");
                    capaciteError.setVisible(true);
                    isValid = false;
                } else {
                    capaciteError.setVisible(false);
                }
            } catch (NumberFormatException e) {
                capaciteError.setText("La capacité doit être un nombre entier");
                capaciteError.setVisible(true);
                isValid = false;
            }
        }
        
        return isValid;
    }
    
    private Predicate<Salle> createPredicate(String searchText) {
        if (searchText == null || searchText.isEmpty()) {
            return p -> true;
        }
        
        String lowerCaseSearchText = searchText.toLowerCase();
        
        return salle -> 
            salle.getNom().toLowerCase().contains(lowerCaseSearchText) ||
            salle.getNumero().toLowerCase().contains(lowerCaseSearchText) ||
            String.valueOf(salle.getCapacite()).contains(lowerCaseSearchText);
    }
} 