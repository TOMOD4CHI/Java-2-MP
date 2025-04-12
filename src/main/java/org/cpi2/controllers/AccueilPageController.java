package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.cpi2.entities.AutoEcole;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Notification;
import org.cpi2.entities.Seance;
import org.cpi2.service.AutoEcoleService;
import org.cpi2.service.CandidatService;
import org.cpi2.service.MoniteurService;
import org.cpi2.service.SeanceService;
import org.cpi2.service.NotificationService;
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
    
    // Dashboard cards
    @FXML private Text monthlyIncomeText;
    @FXML private Label incomeComparisonLabel;
    @FXML private ProgressBar incomeProgress;
    @FXML private Text successRateText;
    @FXML private Label successRateLabel;
    @FXML private ProgressBar successProgress;
    @FXML private Text vehiclesAvailableText;
    @FXML private Label vehicleStatusLabel;
    @FXML private ProgressBar vehicleProgress;
    
    // Chart
    @FXML private LineChart<String, Number> progressionChart;

    // Services
    private final CandidatService candidatService = new CandidatService();
    private final SeanceService seanceService = new SeanceService();
    private final MoniteurService moniteurService = new MoniteurService();
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize statistics
        loadStatistics();
        
        // Initialize upcoming sessions
        setupTableColumns();
        loadUpcomingSessions();
        
        // Initialize auto-école information
        loadAutoEcoleInfo();
        
        // Initialize notifications from system events
        setupNotificationsDisplay();
        
        // Initialize dashboard cards
        setupDashboardCards();
        
        // Initialize chart
        setupProgressionChart();
        
        // Subscribe to events for updates
        subscribeToEvents();
    }

    private void setupTableColumns() {
        // Configure table columns
        dateColumn.setCellValueFactory(cellData -> {
            try {
                LocalDate date = cellData.getValue().getLocalDate();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return new SimpleStringProperty(date.format(formatter));
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });
        
        timeColumn.setCellValueFactory(cellData -> {
            try {
                String time = cellData.getValue().getHeure();
                return new SimpleStringProperty(time != null ? time : "N/A");
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });
        
        typeColumn.setCellValueFactory(cellData -> {
            try {
                String type = cellData.getValue().getType();
                return new SimpleStringProperty(type != null && type.equals("CONDUITE") ? "Conduite" : "Code");
            } catch (Exception e) {
                return new SimpleStringProperty("N/A");
            }
        });
        
        candidatesColumn.setCellValueFactory(cellData -> {
            try {
                List<Candidat> candidates = cellData.getValue().getCandidats();
                int count = candidates != null ? candidates.size() : 0;
                return new SimpleStringProperty(count + " candidat(s)");
            } catch (Exception e) {
                return new SimpleStringProperty("0 candidat(s)");
            }
        });
        
        // Improve table appearance
        upcomingSessionsTable.setRowFactory(tv -> {
            TableRow<Seance> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    Seance seance = row.getItem();
                    System.out.println("Double-clicked on seance: " + seance.getId());
                    // Future enhancement: Show detailed view of this session
                }
            });
            return row;
        });
    }

    private void loadStatistics() {
        try {
            // Get counts from services
            int candidatesCount = candidatService.getAllCandidats().size();
            int sessionsCount = seanceService.getAllSeances().size();
            int moniteursCount = moniteurService.getAllMoniteurs().size();
            
            // Update UI
            candidateCountText.setText(String.valueOf(candidatesCount));
            sessionCountText.setText(String.valueOf(sessionsCount));
            moniteurCountText.setText(String.valueOf(moniteursCount));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des statistiques: " + e.getMessage());
            e.printStackTrace();
            
            // Set defaults in case of error
            candidateCountText.setText("0");
            sessionCountText.setText("0");
            moniteurCountText.setText("0");
        }
    }

    private void loadUpcomingSessions() {
        try {
            // Get upcoming sessions
            List<Seance> upcomingSessions = seanceService.getUpcomingSeances(10);
            
            // Set table items
            upcomingSessionsTable.setItems(FXCollections.observableArrayList(upcomingSessions));
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des séances à venir: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupNotificationsDisplay() {
        try {
            // Create an empty observable list for notifications
            ObservableList<String> notifications = FXCollections.observableArrayList();
            notificationsListView.setItems(notifications);
            
            // Add custom cell factory for better text wrapping
            notificationsListView.setCellFactory(lv -> {
                ListCell<String> cell = new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            // Create a label for the cell content with wrapping
                            Label label = new Label(item);
                            label.setWrapText(true);
                            label.setMaxWidth(notificationsListView.getWidth() - 20);
                            label.setPadding(new Insets(5, 5, 5, 5));
                            
                            // Set the label as the cell's graphic
                            setGraphic(label);
                            setText(null);
                        }
                    }
                };
                return cell;
            });
            
            // Check for system notifications
            checkSystemNotifications();
            
        } catch (Exception e) {
            System.err.println("Erreur lors de la configuration des notifications: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void checkSystemNotifications() {
        // Get system notifications
        ObservableList<String> systemNotifications = FXCollections.observableArrayList();
        
        // Check for upcoming sessions today
        List<Seance> todaySessions = getTodaySessions();
        if (!todaySessions.isEmpty()) {
            systemNotifications.add("🔔 Vous avez " + todaySessions.size() + " séance(s) aujourd'hui");
        }
        
        // Check for sessions with missing instructors
        List<Seance> sessionsWithoutMoniteur = getSessionsWithoutMoniteur();
        if (!sessionsWithoutMoniteur.isEmpty()) {
            systemNotifications.add("⚠️ " + sessionsWithoutMoniteur.size() + " séance(s) sans moniteur assigné");
        }
        
        // Check for vehicle maintenance
        boolean needsVehicleMaintenance = needsVehicleMaintenance();
        if (needsVehicleMaintenance) {
            systemNotifications.add("🔧 Des véhicules nécessitent un entretien");
        }
        
        // Set notifications to the ListView
        notificationsListView.setItems(systemNotifications);
    }
    
    private List<Seance> getTodaySessions() {
        try {
            // Get sessions for today
            LocalDate today = LocalDate.now();
            return seanceService.findAllSeances().stream()
                .filter(seance -> {
                    LocalDate seanceDate = seance.getLocalDate();
                    return seanceDate.isEqual(today);
                })
                .toList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des séances du jour: " + e.getMessage());
            return List.of();
        }
    }
    
    private List<Seance> getSessionsWithoutMoniteur() {
        try {
            // Get sessions without assigned instructor
            return seanceService.findAllSeances().stream()
                .filter(seance -> seance.getMoniteurId() == null || seance.getMoniteurName() == null || seance.getMoniteurName().isEmpty())
                .toList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des séances sans moniteur: " + e.getMessage());
            return List.of();
        }
    }
    
    private boolean needsVehicleMaintenance() {
        // This would be implemented with a real service call
        // For now, return false
        return false;
    }

    private void loadAutoEcoleInfo() {
        try {
            // Get auto-école information
            AutoEcole autoEcole = autoEcoleService.getAutoEcole();
            
            if (autoEcole != null) {
                // Update UI
                schoolNameLabel.setText(autoEcole.getNom());
                schoolAddressLabel.setText(autoEcole.getAdresse());
                schoolPhoneLabel.setText(autoEcole.getTelephone());
                schoolEmailLabel.setText(autoEcole.getEmail());
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des informations de l'auto-école: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void subscribeToEvents() {
        // Listen for updates to refresh data
        EventBus.subscribe("CANDIDAT_UPDATED", event -> loadStatistics());
        EventBus.subscribe("SEANCE_UPDATED", event -> {
            loadStatistics();
            loadUpcomingSessions();
            checkSystemNotifications();
            setupProgressionChart(); // Update chart when sessions change
        });
        EventBus.subscribe("MONITEUR_UPDATED", event -> loadStatistics());
        EventBus.subscribe("NOTIFICATION_ADDED", event -> checkSystemNotifications());
        EventBus.subscribe("AUTO_ECOLE_UPDATED", event -> {
            if (event instanceof AutoEcole) {
                AutoEcole autoEcole = (AutoEcole) event;
                updateAutoEcoleInfo(autoEcole);
            }
        });
        EventBus.subscribe("FINANCE_UPDATED", event -> setupDashboardCards()); // Update dashboard when finances change
        EventBus.subscribe("VEHICLE_UPDATED", event -> setupDashboardCards()); // Update dashboard when vehicle status changes
    }

    private void updateAutoEcoleInfo(AutoEcole autoEcole) {
        if (autoEcole != null) {
            schoolNameLabel.setText(autoEcole.getNom());
            schoolAddressLabel.setText(autoEcole.getAdresse());
            schoolPhoneLabel.setText(autoEcole.getTelephone());
            schoolEmailLabel.setText(autoEcole.getEmail());
        }
    }

    private void setupDashboardCards() {
        try {
            // Monthly Income Card
            monthlyIncomeText.setText("5,200 €");
            incomeComparisonLabel.setText("+12% par rapport au mois précédent");
            incomeProgress.setProgress(0.75);
            
            // Success Rate Card
            successRateText.setText("68%");
            successRateLabel.setText("Taux moyen sur 6 mois");
            successProgress.setProgress(0.68);
            
            // Vehicles Card
            vehiclesAvailableText.setText("8/10");
            vehicleStatusLabel.setText("2 véhicules en maintenance");
            vehicleProgress.setProgress(0.80);
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation des cartes de tableau de bord: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupProgressionChart() {
        try {
            // Clear any existing data
            progressionChart.getData().clear();
            
            // Create series for sessions and registrations
            javafx.scene.chart.XYChart.Series<String, Number> sessionsSeries = new javafx.scene.chart.XYChart.Series<>();
            sessionsSeries.setName("Séances");
            
            javafx.scene.chart.XYChart.Series<String, Number> registrationsSeries = new javafx.scene.chart.XYChart.Series<>();
            registrationsSeries.setName("Inscriptions");
            
            // Add sample data - this would be replaced with real data from services
            String[] months = {"Jan", "Fév", "Mar", "Avr", "Mai", "Juin"};
            int[] sessionsData = {12, 15, 18, 16, 20, 25};
            int[] registrationsData = {5, 8, 10, 7, 12, 15};
            
            for (int i = 0; i < months.length; i++) {
                sessionsSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(months[i], sessionsData[i]));
                registrationsSeries.getData().add(new javafx.scene.chart.XYChart.Data<>(months[i], registrationsData[i]));
            }
            
            // Add series to chart
            progressionChart.getData().add(sessionsSeries);
            progressionChart.getData().add(registrationsSeries);
            
            // Style the chart
            progressionChart.setAnimated(true);
            progressionChart.setCreateSymbols(true);
            
        } catch (Exception e) {
            System.err.println("Erreur lors de l'initialisation du graphique: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Navigation methods
    @FXML public void loadAfficherCandidat() {
        MainWindowNavigator.loadAfficherCandidat();
    }

    @FXML public void loadAfficherSeance() {
        MainWindowNavigator.loadAfficherSeance();
    }

    @FXML public void loadAfficherMoniteur() {
        MainWindowNavigator.loadAfficherMoniteur();
    }

    @FXML public void loadAjouterCandidat() {
        MainWindowNavigator.loadAjouterCandidat();
    }

    @FXML public void loadSessionConduite() {
        MainWindowNavigator.loadSessionConduite();
    }

    @FXML public void loadPayment() {
        MainWindowNavigator.loadPayment();
    }

    @FXML public void loadGestionVehicules() {
        MainWindowNavigator.loadGestionVehicules();
    }

    @FXML public void loadExamRegistration() {
        MainWindowNavigator.loadExamRegistration();
    }

    @FXML public void loadDashboardFinance() {
        MainWindowNavigator.loadDashboardFinance();
    }

    @FXML public void loadAfficherNotifications() {
        MainWindowNavigator.loadNotifications();
    }

    @FXML public void loadAutoEcoleManagement() {
        MainWindowNavigator.loadAutoEcoleManagement();
    }
} 