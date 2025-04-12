package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.cpi2.entities.AutoEcole;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Seance;
import org.cpi2.repository.DashboardRepository;
import org.cpi2.service.DashboardService;
import org.cpi2.utils.EventBus;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class AccueilPageController implements Initializable {

    @FXML private Text candidateCountText;
    @FXML private Text sessionCountText;
    @FXML private Text moniteurCountText;
    @FXML private TableView<Seance> upcomingSessionsTable;
    @FXML private TableColumn<Seance, String> dateColumn;
    @FXML private TableColumn<Seance, String> timeColumn;
    @FXML private TableColumn<Seance, String> typeColumn;
    @FXML private TableColumn<Seance, String> candidatesColumn;
    @FXML private ListView<String> notificationsListView;
    @FXML private Label schoolNameLabel;
    @FXML private Label schoolAddressLabel;
    @FXML private Label schoolPhoneLabel;
    @FXML private Label schoolEmailLabel;
    @FXML private Text monthlyIncomeText;
    @FXML private Label incomeComparisonLabel;
    @FXML private ProgressBar incomeProgress;
    @FXML private Text successRateText;
    @FXML private Label successRateLabel;
    @FXML private ProgressBar successProgress;
    @FXML private Text vehiclesAvailableText;
    @FXML private Label vehicleStatusLabel;
    @FXML private ProgressBar vehicleProgress;
    @FXML private LineChart<String, Number> progressionChart;

    private final DashboardService dashboardService;
    public AccueilPageController() {
        this.dashboardService = new DashboardService();
    }

    public AccueilPageController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadStatistics();
        loadUpcomingSessions();
        loadAutoEcoleInfo();
        setupNotificationsDisplay();
        setupDashboardCards();
        setupProgressionChart();
        subscribeToEvents();
    }

    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> {
            try {
                LocalDate date = cellData.getValue().getLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(date.format(formatter));
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });

        timeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getHeure() != null ? cellData.getValue().getHeure() : "N/A"));

        typeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(
                cellData.getValue().getType() != null && cellData.getValue().getType().equals("CONDUITE") ? "Conduite" : "Code"));

        candidatesColumn.setCellValueFactory(cellData -> {
            try {
                List<Candidat> candidates = cellData.getValue().getCandidats();
                int count = candidates != null ? candidates.size() : 0;
                return new SimpleStringProperty(count + " candidat(s)");
            } catch (Exception e) {
                return new SimpleStringProperty("0 candidat(s)");
            }
        });

        upcomingSessionsTable.setRowFactory(tv -> {
            TableRow<Seance> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Seance seance = row.getItem();
                    System.out.println("Double-clicked on seance: " + seance.getId());
                }
            });
            return row;
        });
    }

    private void loadStatistics() {
        try {
            candidateCountText.setText(String.valueOf(dashboardService.getCandidatesCount()));
            sessionCountText.setText(String.valueOf(dashboardService.getSessionsCount()));
            moniteurCountText.setText(String.valueOf(dashboardService.getMoniteursCount()));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            candidateCountText.setText("0");
            sessionCountText.setText("0");
            moniteurCountText.setText("0");
        }
    }

    private void loadUpcomingSessions() {
        try {
            List<Seance> upcomingSessions = dashboardService.getUpcomingSessions(10);
            upcomingSessionsTable.setItems(FXCollections.observableArrayList(upcomingSessions));
            System.out.println("Displaying " + upcomingSessions.size() + " upcoming sessions in table");
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances à venir: " + e.getMessage());
            upcomingSessionsTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void loadAutoEcoleInfo() {
        try {
            AutoEcole autoEcole = dashboardService.getAutoEcoleInfo();
            if (autoEcole != null) {
                schoolNameLabel.setText(autoEcole.getNom());
                schoolAddressLabel.setText(autoEcole.getAdresse());
                schoolPhoneLabel.setText(autoEcole.getTelephone());
                schoolEmailLabel.setText(autoEcole.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des informations de l'auto-école: " + e.getMessage());
        }
    }

    private void setupNotificationsDisplay() {
        try {
            notificationsListView.setCellFactory(lv -> {
                ListCell<String> cell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            Label label = new Label(item);
                            label.setWrapText(true);
                            label.setMaxWidth(notificationsListView.getWidth() - 20);
                            label.setPadding(new Insets(5, 5, 5, 5));
                            setGraphic(label);
                            setText(null);
                        }
                    }
                };
                return cell;
            });

            checkSystemNotifications();
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des notifications: " + e.getMessage());
        }
    }
    
    private void checkSystemNotifications() {
        try {
            List<String> notifications = dashboardService.getSystemNotifications();
            ObservableList<String> systemNotifications = FXCollections.observableArrayList(notifications);
            if (systemNotifications.isEmpty()) {
                systemNotifications.add("✅ Tout est en ordre. Aucune notification importante.");
            }
            notificationsListView.setItems(systemNotifications);
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification des notifications: " + e.getMessage());
            notificationsListView.setItems(FXCollections.observableArrayList(
                    "⚠️ Impossible de récupérer les notifications. Veuillez rafraîchir."));
        }
    }

    private void setupDashboardCards() {
        try {
            double monthlyIncome = dashboardService.getMonthlyIncome();
            monthlyIncomeText.setText(String.format("%,.0f", monthlyIncome));
            double changePercent = dashboardService.getIncomeChangePercent();
            incomeComparisonLabel.setText(String.format("%+.1f%% par rapport au mois précédent", changePercent));
            incomeProgress.setProgress(Math.min(1.0, monthlyIncome / 10000));

            double successRate = dashboardService.getExamSuccessRate();
            successRateText.setText(String.format("%.1f%%", successRate));
            successRateLabel.setText("Taux moyen sur 6 mois");
            successProgress.setProgress(successRate / 100);

            int[] vehicleCounts = dashboardService.getVehicleCounts();
            int availableVehicles = vehicleCounts[0];
            int totalVehicles = vehicleCounts[1];
            vehiclesAvailableText.setText(availableVehicles + "/" + totalVehicles);
            int inMaintenance = totalVehicles - availableVehicles;
            vehicleStatusLabel.setText(inMaintenance + " véhicule" + (inMaintenance > 1 ? "s" : "") + " en maintenance");
            vehicleProgress.setProgress(totalVehicles > 0 ? (double) availableVehicles / totalVehicles : 0);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des cartes du tableau de bord: " + e.getMessage());
        }
    }
    
    private void setupProgressionChart() {
        try {
            progressionChart.getData().clear();
            XYChart.Series<String, Number> sessionsSeries = new XYChart.Series<>();
            sessionsSeries.setName("Séances");
            XYChart.Series<String, Number> registrationsSeries = new XYChart.Series<>();
            registrationsSeries.setName("Inscriptions");

            String[] months = dashboardService.getLastSixMonths();
            int[] sessionsData = dashboardService.getSessionsPerMonth();
            int[] registrationsData = dashboardService.getRegistrationsPerMonth();

            for (int i = 0; i < months.length; i++) {
                sessionsSeries.getData().add(new XYChart.Data<>(months[i], sessionsData[i]));
                registrationsSeries.getData().add(new XYChart.Data<>(months[i], registrationsData[i]));
            }

            progressionChart.getData().addAll(sessionsSeries, registrationsSeries);
            progressionChart.setAnimated(true);
            progressionChart.setCreateSymbols(true);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du graphique: " + e.getMessage());
        }
    }

    private void subscribeToEvents() {
        EventBus.subscribe("CANDIDAT_UPDATED", event -> loadStatistics());
        EventBus.subscribe("SEANCE_UPDATED", event -> {
            loadStatistics();
            loadUpcomingSessions();
            checkSystemNotifications();
            setupProgressionChart();
        });
        EventBus.subscribe("MONITEUR_UPDATED", event -> loadStatistics());
        EventBus.subscribe("NOTIFICATION_ADDED", event -> checkSystemNotifications());
        EventBus.subscribe("autoecole_UPDATED", event -> {
            if (event instanceof AutoEcole autoEcole) {
                schoolNameLabel.setText(autoEcole.getNom());
                schoolAddressLabel.setText(autoEcole.getAdresse());
                schoolPhoneLabel.setText(autoEcole.getTelephone());
                schoolEmailLabel.setText(autoEcole.getEmail());
            }
        });
        EventBus.subscribe("FINANCE_UPDATED", event -> setupDashboardCards());
        EventBus.subscribe("VEHICLE_UPDATED", event -> setupDashboardCards());
    }

    @FXML public void loadAfficherCandidat() { MainWindowNavigator.loadAfficherCandidat(); }
    @FXML public void loadAfficherSeance() { MainWindowNavigator.loadAfficherSeance(); }
    @FXML public void loadAfficherMoniteur() { MainWindowNavigator.loadAfficherMoniteur(); }
    @FXML public void loadAjouterCandidat() { MainWindowNavigator.loadAjouterCandidat(); }
    @FXML public void loadSessionConduite() { MainWindowNavigator.loadSessionConduite(); }
    @FXML public void loadPayment() { MainWindowNavigator.loadPayment(); }
    @FXML public void loadGestionVehicules() { MainWindowNavigator.loadGestionVehicules(); }
    @FXML public void loadExamRegistration() { MainWindowNavigator.loadExamRegistration(); }
    @FXML public void loadDashboardFinance() { MainWindowNavigator.loadDashboardFinance(); }
    @FXML public void loadAfficherNotifications() { MainWindowNavigator.loadNotifications(); }
    @FXML public void loadAutoEcoleManagement() { MainWindowNavigator.loadAutoEcoleManagement(); }
}