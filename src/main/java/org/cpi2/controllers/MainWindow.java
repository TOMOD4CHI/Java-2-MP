package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow {

    @FXML private StackPane contentArea;
    @FXML private ImageView backgroundImage;



    @FXML public void loadInscription() { loadViewWithTransition("/fxmls/Inscription.fxml"); }
    @FXML public void loadDocuments() { loadViewWithTransition("/fxmls/Documents.fxml"); }
    @FXML public void loadProgression() { loadViewWithTransition("/fxmls/Progression.fxml"); }
    @FXML public void loadExamRegistration() { loadViewWithTransition("/fxmls/ExamRegistration.fxml"); }
    @FXML public void loadExam() { loadViewWithTransition("/fxmls/Exam.fxml"); }

    @FXML public void loadSessionConduite() { loadViewWithTransition("/fxmls/Usecase2.fxml"); }
    @FXML public void loadSessionCode() { loadViewWithTransition("/fxmls/SessionCode.fxml"); }
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

    @FXML public void loadClassrooms() { loadViewWithTransition("/fxmls/Classrooms.fxml"); }
    @FXML public void loadInstructors() { loadViewWithTransition("/fxmls/Instructors.fxml"); }

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
}
