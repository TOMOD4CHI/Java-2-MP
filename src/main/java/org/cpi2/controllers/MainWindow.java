package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private ImageView backgroundImage;
    @FXML private ImageView logoImageView;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        createLogoAnimation();
    }

    private void createLogoAnimation() {
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1.5), logoImageView);
        rotateTransition.setFromAngle(-180);
        rotateTransition.setToAngle(0);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.5), logoImageView);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), logoImageView);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(0.6);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(rotateTransition, scaleTransition, fadeTransition);
        parallelTransition.play();
    }

    @FXML public void loadAjouterEcole(){loadViewWithTransition("/fxmls/AjouterEcole.fxml"); };
    @FXML public void loadModifierEcole(){loadViewWithTransition("/fxmls/ModifierEcole.fxml"); }


    @FXML public void loadAjouterCandidat() { loadViewWithTransition("/fxmls/AjouterCandidat.fxml"); }
    @FXML public void loadModifierCandidat() { loadViewWithTransition("/fxmls/ModifierCandidat.fxml"); }
    @FXML public void loadDocuments() { loadViewWithTransition("/fxmls/documents.fxml"); }
    @FXML public void loadProgression() { loadViewWithTransition("/fxmls/progression.fxml"); }
    @FXML public void loadExamRegistration() { loadViewWithTransition("/fxmls/examRegistration.fxml"); }
    @FXML public void loadExam() { loadViewWithTransition("/fxmls/passExam.fxml"); }

    @FXML public void loadSessionConduite() { loadViewWithTransition("/fxmls/SeanceConduite.fxml"); }
    @FXML public void loadSessionCode() { loadViewWithTransition("/fxmls/SeanceCode.fxml"); }
    @FXML public void loadMoniteurDispo() { loadViewWithTransition("/fxmls/MoniteurDispo.fxml"); }
    @FXML public void loadConfirmPresence() { loadViewWithTransition("/fxmls/ConfirmPresence.fxml"); }
    @FXML public void loadManageSchedules() { loadViewWithTransition("/fxmls/ManageSchedules.fxml"); }

    @FXML public void loadVehicles() { loadViewWithTransition("/fxmls/Vehicles.fxml"); }
    @FXML public void loadMaintenance() { loadViewWithTransition("/fxmls/Maintenance.fxml"); }
    @FXML public void loadKilometrage() { loadViewWithTransition("/fxmls/Kilometrage.fxml"); }
    @FXML public void loadNotifications() { loadViewWithTransition("/fxmls/Notifications.fxml"); }

    @FXML public void loadPayment() { loadViewWithTransition("/fxmls/Payment.fxml"); }
    @FXML public void loadInvoice() { loadViewWithTransition("/fxmls/Invoice.fxml"); }
    @FXML public void loadPaymentHistory() { loadViewWithTransition("/fxmls/PaymentHistory.fxml"); }


    @FXML public void loadDashboardFinance() { loadViewWithTransition("/fxmls/DashboardFinance.fxml"); }
    @FXML public void loadDashboardCandidates() { loadViewWithTransition("/fxmls/DashboardCandidates.fxml"); }
    @FXML public void loadDashboardVehicles() { loadViewWithTransition("/fxmls/DashboardVehicles.fxml"); }

    private void loadViewWithTransition(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();

            // Apply fade-in effect
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), view);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            fadeIn.play();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML private ImageView monImageView;


    @FXML
    public void loadMain() {
        contentArea.getChildren().clear();

        if (!contentArea.getChildren().contains(logoImageView)) {
            contentArea.getChildren().add(logoImageView);
        }

        createLogoAnimation();
    }
    public void loadAjouterSalle(ActionEvent actionEvent) {
    }

    public void loadModifierSalle(ActionEvent actionEvent) {
    }

    public void loadAjouterMoniteur() {loadViewWithTransition("/fxmls/ajouterMoniteur.fxml");
    }

    public void loadModifierMoniteur() {loadViewWithTransition("/fxmls/ModifierMoniteur.fxml");
    }
}