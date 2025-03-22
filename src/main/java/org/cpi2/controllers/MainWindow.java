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
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private ImageView logoImage;
    @FXML private Button mainButton;

    private Stage stage;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createLogoAnimation();
    }

    private void createLogoAnimation() {
        // Simpler logo animation with just subtle fade-in
        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.0), logoImage);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(0.95);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);
        
        // Add subtle scale for a gentle entrance
        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.0), logoImage);
        scaleTransition.setFromX(0.95);
        scaleTransition.setFromY(0.95);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);
        
        ParallelTransition parallelTransition = new ParallelTransition(fadeTransition, scaleTransition);
        parallelTransition.play();
    }

    @FXML
    public void toggleFullscreen() {
        if (stage == null) {
            stage = (Stage) mainButton.getScene().getWindow();
        }
        
        boolean isFullScreen = stage.isFullScreen();
        stage.setFullScreen(!isFullScreen);
        
        // Update button text based on fullscreen state
        if (isFullScreen) {
            mainButton.setText("Plein écran");
        } else {
            mainButton.setText("Quitter plein écran");
        }
    }

    @FXML public void loadAjouterEcole() {loadViewWithTransition("/fxmls/ajouterEcole.fxml"); }
    @FXML public void loadModifierEcole() {loadViewWithTransition("/fxmls/modifierEcole.fxml"); }
    @FXML public void loadManageEcole() {loadViewWithTransition("/fxmls/manageEcole.fxml"); }


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
    @FXML public void loadGestionSeances() { loadViewWithTransition("/fxmls/gestionSeances.fxml"); }

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
        if (!contentArea.getChildren().contains(logoImage)) {
            contentArea.getChildren().add(logoImage);
            createLogoAnimation();
        }
    }
    
    public void loadModifierSalle(ActionEvent actionEvent) {
    }
}