package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardVehicles implements Initializable {

    @FXML private ComboBox<String> filterCombo;
    
    // KPI Labels
    @FXML private Label totalVehiclesLabel;
    @FXML private Label availableVehiclesLabel;
    @FXML private Label maintenanceDueLabel;
    @FXML private Label totalKmLabel;
    
    // Charts
    @FXML private PieChart vehicleUsageChart;
    
    @FXML private BarChart<String, Number> maintenanceHistoryChart;
    @FXML private CategoryAxis maintenanceMonthAxis;
    @FXML private NumberAxis maintenanceCountAxis;
    
    // Tables
    @FXML private TableView<VehicleEntry> vehiclesTable;
    @FXML private TableColumn<VehicleEntry, Long> vehicleIdColumn;
    @FXML private TableColumn<VehicleEntry, String> vehicleModelColumn;
    @FXML private TableColumn<VehicleEntry, String> vehiclePlateColumn;
    @FXML private TableColumn<VehicleEntry, Double> vehicleKmColumn;
    @FXML private TableColumn<VehicleEntry, String> vehicleStatusColumn;
    @FXML private TableColumn<VehicleEntry, String> vehicleLastMaintenanceColumn;
    @FXML private TableColumn<VehicleEntry, String> vehicleNextMaintenanceColumn;
    
    @FXML private TableView<MaintenanceAlertEntry> maintenanceAlertsTable;
    @FXML private TableColumn<MaintenanceAlertEntry, String> alertVehicleColumn;
    @FXML private TableColumn<MaintenanceAlertEntry, String> alertTypeColumn;
    @FXML private TableColumn<MaintenanceAlertEntry, String> alertDueDateColumn;
    @FXML private TableColumn<MaintenanceAlertEntry, String> alertStatusColumn;
    @FXML private TableColumn<MaintenanceAlertEntry, String> alertPriorityColumn;
    @FXML private TableColumn<MaintenanceAlertEntry, Void> alertActionsColumn;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the filter combobox
        filterCombo.getItems().addAll(
            "Tous les véhicules",
            "Véhicules disponibles",
            "Véhicules en entretien",
            "Véhicules hors service"
        );
        filterCombo.getSelectionModel().select("Tous les véhicules");
        
        // Set up the table columns
        setupVehicleTableColumns();
        setupAlertTableColumns();
        
        // Load data
        loadChartData();
        loadTableData();
    }
    
    @FXML
    private void handleApplyFilter() {
        // Get filter value and reload data
        String filter = filterCombo.getValue();
        loadTableData();
    }
    
    private void setupVehicleTableColumns() {
        vehicleIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        vehicleModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        vehiclePlateColumn.setCellValueFactory(new PropertyValueFactory<>("plate"));
        vehicleKmColumn.setCellValueFactory(new PropertyValueFactory<>("km"));
        vehicleStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        vehicleLastMaintenanceColumn.setCellValueFactory(new PropertyValueFactory<>("lastMaintenance"));
        vehicleNextMaintenanceColumn.setCellValueFactory(new PropertyValueFactory<>("nextMaintenance"));
        
        // Format km column to show units
        vehicleKmColumn.setCellFactory(col -> new TableCell<VehicleEntry, Double>() {
            @Override
            protected void updateItem(Double km, boolean empty) {
                super.updateItem(km, empty);
                if (empty || km == null) {
                    setText(null);
                } else {
                    setText(String.format("%.0f km", km));
                }
            }
        });
        
        // Format status column with color indicators
        vehicleStatusColumn.setCellFactory(col -> new TableCell<VehicleEntry, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Disponible":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "En entretien":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "Hors service":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
    }
    
    private void setupAlertTableColumns() {
        alertVehicleColumn.setCellValueFactory(new PropertyValueFactory<>("vehicle"));
        alertTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        alertDueDateColumn.setCellValueFactory(new PropertyValueFactory<>("dueDate"));
        alertStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        alertPriorityColumn.setCellValueFactory(new PropertyValueFactory<>("priority"));
        
        // Format priority column with color indicators
        alertPriorityColumn.setCellFactory(col -> new TableCell<MaintenanceAlertEntry, String>() {
            @Override
            protected void updateItem(String priority, boolean empty) {
                super.updateItem(priority, empty);
                if (empty || priority == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(priority);
                    switch (priority) {
                        case "Haute":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "Moyenne":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "Basse":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
        
        // Add action buttons to the alerts table
        alertActionsColumn.setCellFactory(col -> new TableCell<MaintenanceAlertEntry, Void>() {
            private final Button scheduleBtn = new Button("Planifier");
            private final Button ignoreBtn = new Button("Ignorer");
            
            {
                scheduleBtn.getStyleClass().add("small-button");
                ignoreBtn.getStyleClass().add("small-button");
                
                scheduleBtn.setOnAction(event -> {
                    MaintenanceAlertEntry alert = getTableView().getItems().get(getIndex());
                    // Handle scheduling maintenance
                    System.out.println("Schedule maintenance for: " + alert.getVehicle());
                });
                
                ignoreBtn.setOnAction(event -> {
                    MaintenanceAlertEntry alert = getTableView().getItems().get(getIndex());
                    // Handle ignoring alert
                    System.out.println("Ignore alert for: " + alert.getVehicle());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, scheduleBtn, ignoreBtn);
                    setGraphic(buttons);
                }
            }
        });
    }
    
    private void loadChartData() {
        // Vehicle usage chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Conduite", 65),
            new PieChart.Data("Entretien", 15),
            new PieChart.Data("Inactif", 20)
        );
        vehicleUsageChart.setData(pieChartData);
        
        // Maintenance history chart data
        XYChart.Series<String, Number> maintenanceSeries = new XYChart.Series<>();
        maintenanceSeries.setName("Entretiens");
        
        maintenanceSeries.getData().add(new XYChart.Data<>("Jan", 4));
        maintenanceSeries.getData().add(new XYChart.Data<>("Fév", 3));
        maintenanceSeries.getData().add(new XYChart.Data<>("Mar", 5));
        maintenanceSeries.getData().add(new XYChart.Data<>("Avr", 2));
        maintenanceSeries.getData().add(new XYChart.Data<>("Mai", 6));
        maintenanceSeries.getData().add(new XYChart.Data<>("Juin", 3));
        
        maintenanceHistoryChart.getData().clear();
        maintenanceHistoryChart.getData().add(maintenanceSeries);
        
        // Update KPI labels with sample data
        totalVehiclesLabel.setText("12");
        availableVehiclesLabel.setText("8");
        maintenanceDueLabel.setText("3");
        totalKmLabel.setText("157,340 km");
    }
    
    private void loadTableData() {
        // Sample vehicle data
        ObservableList<VehicleEntry> vehicleData = FXCollections.observableArrayList(
            new VehicleEntry(1L, "Peugeot 208", "134 TU 1234", 45231.0, "Disponible", "15/04/2023", "15/04/2024"),
            new VehicleEntry(2L, "Renault Clio", "134 TU 5678", 78562.0, "Disponible", "22/03/2023", "22/03/2024"),
            new VehicleEntry(3L, "Citroën C3", "134 TU 9012", 32145.0, "En entretien", "10/01/2023", "10/01/2024"),
            new VehicleEntry(4L, "Peugeot 308", "134 TU 3456", 92345.0, "Disponible", "05/02/2023", "05/02/2024"),
            new VehicleEntry(5L, "Renault Mégane", "134 TU 7890", 48732.0, "Hors service", "18/06/2023", "18/06/2024"),
            new VehicleEntry(6L, "Citroën C4", "134 TU 1357", 65432.0, "Disponible", "30/05/2023", "30/05/2024")
        );
        
        vehiclesTable.setItems(vehicleData);
        
        // Sample maintenance alerts data
        ObservableList<MaintenanceAlertEntry> alertData = FXCollections.observableArrayList(
            new MaintenanceAlertEntry("Peugeot 308", "Vidange", "03/06/2023", "En attente", "Haute"),
            new MaintenanceAlertEntry("Renault Clio", "Freins", "10/06/2023", "En attente", "Moyenne"),
            new MaintenanceAlertEntry("Citroën C3", "Pneus", "15/06/2023", "Planifié", "Moyenne"),
            new MaintenanceAlertEntry("Peugeot 208", "Filtre à air", "20/06/2023", "En attente", "Basse")
        );
        
        maintenanceAlertsTable.setItems(alertData);
    }
    
    // Inner class for vehicle table entries
    public static class VehicleEntry {
        private final Long id;
        private final String model;
        private final String plate;
        private final Double km;
        private final String status;
        private final String lastMaintenance;
        private final String nextMaintenance;
        
        public VehicleEntry(Long id, String model, String plate, Double km, 
                            String status, String lastMaintenance, String nextMaintenance) {
            this.id = id;
            this.model = model;
            this.plate = plate;
            this.km = km;
            this.status = status;
            this.lastMaintenance = lastMaintenance;
            this.nextMaintenance = nextMaintenance;
        }
        
        public Long getId() { return id; }
        public String getModel() { return model; }
        public String getPlate() { return plate; }
        public Double getKm() { return km; }
        public String getStatus() { return status; }
        public String getLastMaintenance() { return lastMaintenance; }
        public String getNextMaintenance() { return nextMaintenance; }
    }
    
    // Inner class for maintenance alert entries
    public static class MaintenanceAlertEntry {
        private final String vehicle;
        private final String type;
        private final String dueDate;
        private final String status;
        private final String priority;
        
        public MaintenanceAlertEntry(String vehicle, String type, String dueDate, 
                                    String status, String priority) {
            this.vehicle = vehicle;
            this.type = type;
            this.dueDate = dueDate;
            this.status = status;
            this.priority = priority;
        }
        
        public String getVehicle() { return vehicle; }
        public String getType() { return type; }
        public String getDueDate() { return dueDate; }
        public String getStatus() { return status; }
        public String getPriority() { return priority; }
    }
} 