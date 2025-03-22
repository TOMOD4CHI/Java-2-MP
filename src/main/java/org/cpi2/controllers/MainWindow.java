package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cpi2.entitties.AutoEcole;
import org.cpi2.service.AutoEcoleService;

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
    // Fullscreen button removed as requested

    private Stage stage;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createLogoAnimation();
        loadAutoEcoleInfo();
        
        // We'll set up the stage properties after the scene is fully loaded
        // using Platform.runLater to ensure the scene is ready
        javafx.application.Platform.runLater(() -> {
            try {
                Stage stage = (Stage) contentArea.getScene().getWindow();
                stage.setResizable(true);
                stage.setMinWidth(800);
                stage.setMinHeight(600);
                stage.setMaximized(false);
                
                // Set default size - wider as requested
                stage.setWidth(1280);
                stage.setHeight(800);
                
                // Set application icon
                setApplicationIcon(stage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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
     * Loads auto-école information and displays it in the footer
     */
    private void loadAutoEcoleInfo() {
        try {
            // Get the auto-école information from the database
            AutoEcole autoEcole = autoEcoleService.getAutoEcole();
            
            if (autoEcole != null) {
                // Set the footer labels with auto-école information
                ecoleNameLabel.setText(autoEcole.getNom());
                ecoleAddressLabel.setText(autoEcole.getAdresse());
                ecoleTelLabel.setText("Tél: " + autoEcole.getTelephone());
                ecoleEmailLabel.setText("Email: " + autoEcole.getEmail());
                
                // Load the logo if it exists
                if (autoEcole.getLogo() != null && !autoEcole.getLogo().isEmpty()) {
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
                        e.printStackTrace();
                    }
                } else {
                    // Use default logo if no logo path is specified
                    logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                }
            } else {
                // Set default values if no auto-école is found
                ecoleNameLabel.setText("Auto-École");
                ecoleAddressLabel.setText("Adresse non définie");
                ecoleTelLabel.setText("Tél: Non défini");
                ecoleEmailLabel.setText("Email: Non défini");
                logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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