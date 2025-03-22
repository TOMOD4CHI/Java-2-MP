package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entitties.AutoEcole;
import org.cpi2.service.AutoEcoleService;

import java.io.*;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ManageEcole implements Initializable {

    @FXML private Label nomLabel;
    @FXML private Label adresseLabel;
    @FXML private Label telephoneLabel;
    @FXML private Label emailLabel;
    @FXML private Label directeurLabel;
    
    @FXML private ImageView logoImageView;
    
    @FXML private VBox formContainer;
    
    @FXML private TextField nomField;
    @FXML private TextField adresseField;
    @FXML private TextField telephoneField;
    @FXML private TextField emailField;
    @FXML private TextField directeurField;
    
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private AutoEcole currentAutoEcole;
    private File selectedLogoFile;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadAutoEcole();
    }
    
    private void loadAutoEcole() {
        // Get the first/only auto-école
        try {
            currentAutoEcole = autoEcoleService.getAutoEcole();
            
            if (currentAutoEcole != null) {
                updateLabels();
                
                // Load logo if it exists
                if (currentAutoEcole.getLogo() != null && !currentAutoEcole.getLogo().isEmpty()) {
                    try {
                        File logoFile = new File(currentAutoEcole.getLogo());
                        if (logoFile.exists()) {
                            Image logoImage = new Image(logoFile.toURI().toString());
                            logoImageView.setImage(logoImage);
                        }
                    } catch (Exception e) {
                        // If there's an error loading the logo, use the default one
                        System.err.println("Error loading logo: " + e.getMessage());
                    }
                }
            } else {
                // If no auto-école exists, show the form to create one
                showForm(true);
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Impossible de charger les informations de l'auto-école", e.getMessage());
        }
    }
    
    private void updateLabels() {
        if (currentAutoEcole != null) {
            nomLabel.setText(currentAutoEcole.getNom());
            adresseLabel.setText(currentAutoEcole.getAdresse());
            telephoneLabel.setText(currentAutoEcole.getTelephone());
            emailLabel.setText(currentAutoEcole.getEmail());
            directeurLabel.setText(currentAutoEcole.getDirecteur());
        }
    }
    
    private void populateFields() {
        if (currentAutoEcole != null) {
            nomField.setText(currentAutoEcole.getNom());
            adresseField.setText(currentAutoEcole.getAdresse());
            telephoneField.setText(currentAutoEcole.getTelephone());
            emailField.setText(currentAutoEcole.getEmail());
            directeurField.setText(currentAutoEcole.getDirecteur());
        }
    }
    
    private void showForm(boolean show) {
        formContainer.setVisible(show);
        formContainer.setManaged(show);
        
        if (show) {
            populateFields();
        }
    }
    
    @FXML
    private void handleModifier() {
        showForm(true);
    }
    
    @FXML
    private void handleAnnuler() {
        showForm(false);
        selectedLogoFile = null;
    }
    
    @FXML
    private void handleSave() {
        // Validate input
        if (!validateInput()) {
            return;
        }
        
        try {
            // Update or create auto-école
            if (currentAutoEcole == null) {
                currentAutoEcole = new AutoEcole();
            }
            
            currentAutoEcole.setNom(nomField.getText().trim());
            currentAutoEcole.setAdresse(adresseField.getText().trim());
            currentAutoEcole.setTelephone(telephoneField.getText().trim());
            currentAutoEcole.setEmail(emailField.getText().trim());
            currentAutoEcole.setDirecteur(directeurField.getText().trim());
            
            // Handle logo file if selected
            if (selectedLogoFile != null) {
                currentAutoEcole.setLogo(selectedLogoFile.getAbsolutePath());
            }
            
            // Save to database
            boolean success = currentAutoEcole.getId() == 0 ? 
                    autoEcoleService.saveAutoEcole(currentAutoEcole) : 
                    autoEcoleService.updateAutoEcole(currentAutoEcole);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Auto-école enregistrée", "Les informations ont été enregistrées avec succès.");
                
                showForm(false);
                updateLabels();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Échec de l'enregistrement", "Impossible d'enregistrer les informations de l'auto-école.");
            }
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                    "Échec de l'enregistrement", e.getMessage());
        }
    }
    
    @FXML
    private void handleChangeLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        
        // Get the Stage
        Stage stage = (Stage) logoImageView.getScene().getWindow();
        
        // Show open file dialog
        selectedLogoFile = fileChooser.showOpenDialog(stage);
        
        if (selectedLogoFile != null) {
            try {
                Image logoImage = new Image(selectedLogoFile.toURI().toString());
                logoImageView.setImage(logoImage);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Impossible de charger l'image", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handleExport() {
        if (currentAutoEcole == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", 
                    "Aucune information", "Il n'y a pas d'informations à sauvegarder.");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sauvegarder les informations");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers texte", "*.txt")
        );
        fileChooser.setInitialFileName("AutoEcole_" + currentAutoEcole.getNom().replace(" ", "_") + ".txt");
        
        // Get the Stage
        Stage stage = (Stage) logoImageView.getScene().getWindow();
        
        // Show save file dialog
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                writer.println("Informations de l'Auto-École");
                writer.println("===========================");
                writer.println("Nom: " + currentAutoEcole.getNom());
                writer.println("Adresse: " + currentAutoEcole.getAdresse());
                writer.println("Téléphone: " + currentAutoEcole.getTelephone());
                writer.println("Email: " + currentAutoEcole.getEmail());
                writer.println("Directeur: " + currentAutoEcole.getDirecteur());
                
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "Sauvegarde réussie", "Les informations ont été sauvegardées dans " + file.getName());
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Échec de la sauvegarde", e.getMessage());
            }
        }
    }
    
    @FXML
    private void handlePrint() {
        // In a real application, this would use PrinterJob to print the information
        showAlert(Alert.AlertType.INFORMATION, "Impression", 
                "Fonctionnalité d'impression", "L'impression sera implémentée dans une future version.");
    }
    
    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (nomField.getText().trim().isEmpty()) {
            errors.append("- Le nom est obligatoire\n");
        }
        
        if (adresseField.getText().trim().isEmpty()) {
            errors.append("- L'adresse est obligatoire\n");
        }
        
        if (telephoneField.getText().trim().isEmpty()) {
            errors.append("- Le numéro de téléphone est obligatoire\n");
        } else if (!telephoneField.getText().trim().matches("\\d{8}")) {
            errors.append("- Le numéro de téléphone doit contenir 8 chiffres\n");
        }
        
        if (emailField.getText().trim().isEmpty()) {
            errors.append("- L'email est obligatoire\n");
        } else if (!emailField.getText().trim().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            errors.append("- Format d'email invalide\n");
        }
        
        if (directeurField.getText().trim().isEmpty()) {
            errors.append("- Le nom du directeur est obligatoire\n");
        }
        
        if (errors.length() > 0) {
            showAlert(Alert.AlertType.ERROR, "Erreurs de validation", 
                    "Veuillez corriger les erreurs suivantes:", errors.toString());
            return false;
        }
        
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 