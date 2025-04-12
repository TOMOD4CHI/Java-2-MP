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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
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


    private Stage stage;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        loadAutoEcoleInfo();

        loadLogo();

        createLogoAnimation();

        EventBus.subscribe("AUTO_ECOLE_UPDATED", data -> {
            if (data instanceof AutoEcole) {
                updateFooterInfo((AutoEcole) data);
            }
        });

        Platform.runLater(this::setupMainWindow);
    }

    private void setupMainWindow() {
        try {

            if (contentArea.getScene() == null) {

                Platform.runLater(this::setupMainWindow);
                return;
            }

            Stage stage = (Stage) contentArea.getScene().getWindow();
            if (stage == null) {
                Platform.runLater(this::setupMainWindow);
                return;
            }

            Screen screen = Screen.getPrimary();
            javafx.geometry.Rectangle2D bounds = screen.getVisualBounds();

            stage.setX(bounds.getMinX());
            stage.setY(bounds.getMinY());
            stage.setWidth(bounds.getWidth());
            stage.setHeight(bounds.getHeight());

            stage.setMaximized(true);
            stage.setResizable(true);

            setApplicationIcon(stage);

        } catch (Exception e) {
            System.err.println("Error setting up main window: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private void loadAutoEcoleInfo() {
        try {

            AutoEcole autoEcole = autoEcoleService.getAutoEcole();

            if (autoEcole != null) {
                updateFooterInfo(autoEcole);
            }
        } catch (Exception e) {
            System.err.println("Error loading auto-école information: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    public void updateFooterInfo(AutoEcole autoEcole) {
        if (autoEcole != null) {

            ecoleNameLabel.setText(autoEcole.getNom());
            ecoleAddressLabel.setText(autoEcole.getAdresse());
            ecoleTelLabel.setText("Tél: " + autoEcole.getTelephone());
            ecoleEmailLabel.setText("Email: " + autoEcole.getEmail());

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

                        logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                    }
                } catch (Exception e) {

                    logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                    System.err.println("Error loading logo image: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {

                logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            }
        } catch (Exception e) {

            logoImageView.setImage(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
            System.err.println("Error in loadLogo method: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createLogoAnimation() {

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.0), logoImageView);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(0.75);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.0), logoImageView);
        scaleTransition.setFromX(0.95);
        scaleTransition.setFromY(0.95);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(scaleTransition, fadeTransition);

        parallelTransition.setOnFinished(event -> {
            loadAccueilPage();
        });

        parallelTransition.play();
    }


    @FXML public void loadAutoEcoleManagement() {loadViewWithTransition("/fxmls/AutoEcoleManagement.fxml"); }

    @FXML public void loadAjouterSalle() { loadViewWithTransition("/fxmls/GestionSalles.fxml"); }
    @FXML public void loadAjouterCandidat() { loadViewWithTransition("/fxmls/AjouterCandidat.fxml"); }
    @FXML public void loadModifierCandidat() { loadViewWithTransition("/fxmls/ModifierCandidat.fxml"); }
    @FXML public void loadDocuments() { loadViewWithTransition("/fxmls/Documents.fxml"); }
    @FXML public void loadRemplirSeance() { loadViewWithTransition("/fxmls/RemplirSeance.fxml"); }
    @FXML public void loadAfficherSeance() { loadViewWithTransition("/fxmls/AfficherSeance.fxml"); }
    @FXML public void loadProgression() { loadViewWithTransition("/fxmls/Progression.fxml"); }
    @FXML public void loadExamRegistration() { loadViewWithTransition("/fxmls/ExamRegistration.fxml"); }
    @FXML public void loadExam() { loadViewWithTransition("/fxmls/passExam.fxml"); }
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
    @FXML public void loadNotifications() { loadViewWithTransition("/fxmls/Notifications.fxml"); }
    @FXML public void loadInvoice() { loadViewWithTransition("/fxmls/Invoice.fxml"); }
    @FXML public void loadPaymentHistory() { loadViewWithTransition("/fxmls/PaymentHistory.fxml"); }

    
    @FXML
    public void logout() {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/login.fxml"));
            Parent loginRoot = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
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

            if (!contentArea.getChildren().isEmpty()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), contentArea.getChildren().get(0));
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    contentArea.getChildren().clear();
                    contentArea.getChildren().add(view);

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

    
    private void loadAccueilPage() {
        try {

            MainWindowNavigator.setMainWindowController(this);

            loadViewWithTransition("/fxmls/AccueilPage.fxml");
        } catch (Exception e) {
            System.err.println("Error loading welcome page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    @FXML
    public void loadMain() {
        try {

            contentArea.getChildren().clear();

            contentArea.getChildren().add(logoImageView);

            createLogoAnimation();
        } catch (Exception e) {
            System.err.println("Error returning to main screen: " + e.getMessage());
            e.printStackTrace();

            loadAccueilPage();
        }
    }

    public void loadModifierSalle(ActionEvent actionEvent) {
    }

    
    private void setApplicationIcon(Stage stage) {
        try {

            AutoEcole autoEcole = autoEcoleService.getAutoEcole();

            if (autoEcole != null && autoEcole.getLogo() != null && !autoEcole.getLogo().isEmpty()) {
                File logoFile = new File(autoEcole.getLogo());
                if (logoFile.exists()) {
                    stage.getIcons().clear();
                    stage.getIcons().add(new Image(logoFile.toURI().toString()));
                    return;
                }
            }

            stage.getIcons().clear();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        } catch (Exception e) {
            e.printStackTrace();

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        }
    }
}


