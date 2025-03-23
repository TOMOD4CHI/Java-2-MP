package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cpi2.entities.AutoEcole;
import org.cpi2.service.AutoEcoleService;
import org.cpi2.utils.EventBus;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImageView;
    @FXML private Label ecoleNameLabel;
    @FXML private Label ecoleAddressLabel;
    @FXML private Label ecoleTelLabel;
    @FXML private Label ecoleEmailLabel;
    @FXML private Label userNameLabel;
    @FXML private Button logoutButton;
    // Fullscreen button removed as requested

    private Stage stage;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Load auto-école information for the footer
        loadAutoEcoleInfo();
        
        // Load the logo
        loadLogo();
        
        // Create logo animation
        createLogoAnimation();
        
        // Subscribe to auto-école update events
        EventBus.subscribe("AUTO_ECOLE_UPDATED", data -> {
            if (data instanceof AutoEcole) {
                updateFooterInfo((AutoEcole) data);
            }
        });
        
        // We'll set up the stage properties after the scene is fully loaded
        Platform.runLater(this::setupMainWindow);
    }
    
    private void setupMainWindow() {
        try {
            // Wait until the scene is available
            if (contentArea.getScene() == null) {
                // If scene is not available yet, try again later
                Platform.runLater(this::setupMainWindow);
                return;
            }
            
            // Get the stage from the scene
            Stage stage = (Stage) contentArea.getScene().getWindow();
            if (stage == null) {
                // If window is not available yet, try again later
                Platform.runLater(this::setupMainWindow);
                return;
            }
            
            // Set window properties for 1920x1080 screen
            stage.setResizable(true);
            stage.setMinWidth(800);
            stage.setMinHeight(600);
            stage.setMaximized(false);
            
            stage.setHeight(825); 
            
            // Set application icon
            setApplicationIcon(stage);
            
            // Center the window on screen
            stage.centerOnScreen();
        } catch (Exception e) {
            System.err.println("Error setting up main window: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Loads auto-école information and displays it in the footer
     */
    private void loadAutoEcoleInfo() {
        try {
            // Get the auto-école information from the database
            AutoEcole autoEcole = autoEcoleService.getAutoEcole();
            
            if (autoEcole != null) {
                updateFooterInfo(autoEcole);
            }
        } catch (Exception e) {
            System.err.println("Error loading auto-école information: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Updates the footer information with the provided auto-école data
     * This method can be called from other controllers when auto-école information changes
     * @param autoEcole The updated auto-école information
     */
    public void updateFooterInfo(AutoEcole autoEcole) {
        if (autoEcole != null) {
            // Set the footer labels with auto-école information
            ecoleNameLabel.setText(autoEcole.getNom());
            ecoleAddressLabel.setText(autoEcole.getAdresse());
            ecoleTelLabel.setText("Tél: " + autoEcole.getTelephone());
            ecoleEmailLabel.setText("Email: " + autoEcole.getEmail());
            
            // Also update the logo if it has changed
            if (autoEcole.getLogo() != null && !autoEcole.getLogo().isEmpty()) {
                try {
                    File logoFile = new File(autoEcole.getLogo());
                    if (logoFile.exists()) {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        logoImageView.setImage(logoImage);
                    }
                } catch (Exception e) {
                    System.err.println("Error updating logo: " + e.getMessage());
                }
            }
        }
    }
    
    private void loadLogo() {
        try {
            AutoEcole autoEcole = autoEcoleService.getAutoEcole();
            if (autoEcole != null && autoEcole.getLogo() != null && !autoEcole.getLogo().isEmpty()) {
                try {
                    File logoFile = new File(autoEcole.getLogo());
                    if (logoFile.exists()) {
                        Image logoImage = new Image(logoFile.toURI().toString());
                        logoImageView.setImage(logoImage);
                    } else {
                        // Use default logo if file doesn't exist
                        logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                    }
                } catch (Exception e) {
                    // Use default logo if there's an error loading the custom logo
                    logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                    System.err.println("Error loading logo image: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                // Use default logo if no logo path is specified
                logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            }
        } catch (Exception e) {
            // Use default logo if there's any error
            logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            System.err.println("Error in loadLogo method: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createLogoAnimation() {
        // Simpler logo animation with just subtle fade-in
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.0), logoImageView);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(0.75);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        
        // Add subtle scale for a gentle entrance
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.0), logoImageView);
        scaleTransition.setFromX(0.95);
        scaleTransition.setFromY(0.95);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    // Fullscreen toggle functionality removed as requested

    @FXML public void loadAutoEcoleManagement() {loadViewWithTransition("/fxmls/AutoEcoleManagement.fxml"); }


    @FXML public void loadAjouterCandidat() { loadViewWithTransition("/fxmls/AjouterCandidat.fxml"); }
    @FXML public void loadModifierCandidat() { loadViewWithTransition("/fxmls/ModifierCandidat.fxml"); }
    @FXML public void loadDocuments() { loadViewWithTransition("/fxmls/Documents.fxml"); }
    @FXML public void loadRemplirSeance() { loadViewWithTransition("/fxmls/RemplirSeance.fxml"); }
    @FXML public void loadAfficherSeance() { loadViewWithTransition("/fxmls/AfficherSeance.fxml"); }
    @FXML public void loadConfirmPresence() { loadViewWithTransition("/fxmls/ConfirmPresence.fxml"); }
    @FXML public void loadProgression() { loadViewWithTransition("/fxmls/Progression.fxml"); }
    @FXML public void loadExamRegistration() { loadViewWithTransition("/fxmls/ExamRegistration.fxml"); }
    @FXML public void loadExam() { loadViewWithTransition("/fxmls/Exam.fxml"); }
    @FXML public void loadGestionVehicules() { loadViewWithTransition("/fxmls/GestionVehicules.fxml"); }
    @FXML public void loadDashboardCandidates() { loadViewWithTransition("/fxmls/DashboardCandidates.fxml"); }
    @FXML public void loadDashboardVehicles() { loadViewWithTransition("/fxmls/DashboardVehicles.fxml"); }
    @FXML public void loadDashboardFinance() { loadViewWithTransition("/fxmls/DashboardFinance.fxml"); }
    @FXML public void loadAfficherCandidat() { loadViewWithTransition("/fxmls/AfficherCandidat.fxml"); }
    @FXML public void loadPayment() { loadViewWithTransition("/fxmls/Paiement.fxml"); }
    @FXML public void loadAjouterMoniteur() { loadViewWithTransition("/fxmls/AjouterMoniteur.fxml"); }
    @FXML public void loadModifierMoniteur() { loadViewWithTransition("/fxmls/ModifierMoniteur.fxml"); }
    @FXML public void loadAfficherMoniteur() { loadViewWithTransition("/fxmls/AfficherMoniteur.fxml"); }
    @FXML public void loadSessionConduite() { loadViewWithTransition("/fxmls/SeanceConduite.fxml"); }
    @FXML public void loadSessionCode() { loadViewWithTransition("/fxmls/SeanceCode.fxml"); }
    @FXML public void loadSuiviEntretiens() { loadViewWithTransition("/fxmls/SuiviEntretiens.fxml"); }
    @FXML public void loadMiseAJourKilometrage() { loadViewWithTransition("/fxmls/MiseAJourKilometrage.fxml"); }
    @FXML public void loadNotifications() { loadViewWithTransition("/fxmls/Notifications.fxml"); }
    @FXML public void loadInvoice() { loadViewWithTransition("/fxmls/Invoice.fxml"); }
    @FXML public void loadPaymentHistory() { loadViewWithTransition("/fxmls/PaymentHistory.fxml"); }
    
    /**
     * Handles the logout action when the logout button is clicked
     * Returns the user to the login screen
     */
    @FXML
    public void logout() {
        try {
            // Load the login screen
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/login.fxml"));
            Parent loginRoot = loader.load();
            
            // Get the current stage
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            
            // Create a new scene with the login screen
            Scene scene = new Scene(loginRoot, stage.getWidth(), stage.getHeight());
            stage.setTitle("Login");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading login screen: " + e.getMessage());
        }
    }

    private void loadViewWithTransition(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Create a fade-out transition for current content
            if (!contentArea.getChildren().isEmpty()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea.getChildren().get(0));
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);
                    
                    // Apply multiple animations for an engaging entrance
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), view);
                    fadeIn.setFromValue(0);
                    fadeIn.setToValue(1);
                    
                    ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), view);
                    scaleIn.setFromX(0.95);
                    scaleIn.setFromY(0.95);
                    scaleIn.setToX(1.0);
                    scaleIn.setToY(1.0);
                    
                    ParallelTransition parallelTransition = new ParallelTransition();
                    parallelTransition.getChildren().addAll(fadeIn, scaleIn);
                    parallelTransition.play();
                });
                fadeOut.play();
            } else {
                contentArea.getChildren().add(view);
                
                // Initial load (no existing content to transition from)
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), view);
                fadeIn.setFromValue(0);
                fadeIn.setToValue(1);
                
                ScaleTransition scaleIn = new ScaleTransition(Duration.millis(300), view);
                scaleIn.setFromX(0.95);
                scaleIn.setFromY(0.95);
                scaleIn.setToX(1.0);
                scaleIn.setToY(1.0);
                
                ParallelTransition parallelTransition = new ParallelTransition();
                parallelTransition.getChildren().addAll(fadeIn, scaleIn);
                parallelTransition.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private ImageView monImageView;

    public void loadMain() {
        contentArea.getChildren().clear();
        
        // Add logo back if it's not already in the contentArea
        if (!contentArea.getChildren().contains(logoImageView)) {
            contentArea.getChildren().add(logoImageView);
            createLogoAnimation();
        }
    }
    
    public void loadModifierSalle(ActionEvent actionEvent) {
    }
    
    /**
     * Sets the application icon based on the auto-école logo or default icon
     */
    private void setApplicationIcon(Stage stage) {
        try {
            // Get the auto-école information from the database
            AutoEcole autoEcole = autoEcoleService.getAutoEcole();
            
            if (autoEcole != null && autoEcole.getLogo() != null && !autoEcole.getLogo().isEmpty()) {
                File logoFile = new File(autoEcole.getLogo());
                if (logoFile.exists()) {
                    stage.getIcons().clear();
                    stage.getIcons().add(new Image(logoFile.toURI().toString()));
                    return;
                }
            }
            
            // Use default app icon if no auto-école logo is available
            stage.getIcons().clear();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        } catch (Exception e) {
            e.printStackTrace();
            // Use default app icon if there's an error
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        }
    }
}