package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;

public class MainWindow {

    @FXML private StackPane contentArea;
    @FXML private ImageView backgroundImage;


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

    @FXML
    public void loadMain() {
        contentArea.getChildren().clear();
         // Restore background when returning to main
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
