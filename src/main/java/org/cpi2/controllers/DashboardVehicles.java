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
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;

import org.cpi2.repository.DatabaseConfig;
import org.cpi2.utils.AlertUtil;

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
    private String currentFilter = "Tous les véhicules";
    
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
        loadDashboardData();
    }
    
    @FXML
    private void handleApplyFilter() {
        // Get filter value and reload data
        currentFilter = filterCombo.getValue();
        loadDashboardData();
    }
    
    private void loadDashboardData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Load KPI data
            loadKPIData(conn);
            
            // Load chart data
            loadVehicleUsageChart(conn);
            loadMaintenanceHistoryChart(conn);
            
            // Load table data
            loadVehiclesTableData(conn);
            loadMaintenanceAlertsData(conn);
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur de base de données", "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    private void loadKPIData(Connection conn) throws SQLException {
        // Total vehicles count
        String totalSql = "SELECT COUNT(*) as total FROM vehicule";
        
        // Available vehicles count
        String availableSql = "SELECT COUNT(*) as available FROM vehicule WHERE statut = 'Disponible'";
        
        // Maintenance due count (vehicles with maintenance soon due)
        String maintenanceDueSql = "SELECT COUNT(*) as due FROM vehicule " +
                                 "WHERE kilometrage_total >= (kilometrage_prochain_entretien - 500) " +
                                 "OR (date_prochain_entretien IS NOT NULL AND date_prochain_entretien <= DATE_ADD(CURDATE(), INTERVAL 30 DAY))";
        
        // Total kilometers
        String totalKmSql = "SELECT SUM(kilometrage_total) as total_km FROM vehicule";
        
        // Execute queries
        try (Statement stmt = conn.createStatement()) {
            // Total vehicles
            try (ResultSet rs = stmt.executeQuery(totalSql)) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    totalVehiclesLabel.setText(String.valueOf(total));
                } else {
                    totalVehiclesLabel.setText("0");
                }
            }
            
            // Available vehicles
            try (ResultSet rs = stmt.executeQuery(availableSql)) {
                if (rs.next()) {
                    int available = rs.getInt("available");
                    availableVehiclesLabel.setText(String.valueOf(available));
                } else {
                    availableVehiclesLabel.setText("0");
                }
            }
            
            // Maintenance due
            try (ResultSet rs = stmt.executeQuery(maintenanceDueSql)) {
                if (rs.next()) {
                    int due = rs.getInt("due");
                    maintenanceDueLabel.setText(String.valueOf(due));
                } else {
                    maintenanceDueLabel.setText("0");
                }
            }
            
            // Total kilometers
            try (ResultSet rs = stmt.executeQuery(totalKmSql)) {
                if (rs.next()) {
                    int totalKm = rs.getInt("total_km");
                    totalKmLabel.setText(String.format("%,d km", totalKm));
                } else {
                    totalKmLabel.setText("0 km");
                }
            }
        }
    }
    
    private void loadVehicleUsageChart(Connection conn) throws SQLException {
        // Query to get vehicle status distribution
        String sql = "SELECT statut, COUNT(*) as count FROM vehicule GROUP BY statut";
        
        Map<String, Integer> statusCounts = new HashMap<>();
        statusCounts.put("Disponible", 0);
        statusCounts.put("En entretien", 0);
        statusCounts.put("Hors service", 0);
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String status = rs.getString("statut");
                int count = rs.getInt("count");
                statusCounts.put(status, count);
            }
        }
        
        // Prepare pie chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        
        for (Map.Entry<String, Integer> entry : statusCounts.entrySet()) {
            if (entry.getValue() > 0) {
                String label = entry.getKey();
                int count = entry.getValue();
                
                // Change label for pie chart
                if (label.equals("Disponible")) {
                    label = "Conduite";
                } else if (label.equals("En entretien")) {
                    label = "Entretien";
                } else if (label.equals("Hors service")) {
                    label = "Inactif";
                }
                
                pieChartData.add(new PieChart.Data(label, count));
            }
        }
        
        // If no data, add placeholder data
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Conduite", 65));
            pieChartData.add(new PieChart.Data("Entretien", 15));
            pieChartData.add(new PieChart.Data("Inactif", 20));
        }
        
        vehicleUsageChart.setData(pieChartData);
    }
    
    private void loadMaintenanceHistoryChart(Connection conn) throws SQLException {
        // Get maintenance history by month
        String sql = "SELECT DATE_FORMAT(date_entretien, '%b') as month, COUNT(*) as count " +
                   "FROM entretien " +
                   "WHERE date_entretien >= DATE_SUB(CURDATE(), INTERVAL 6 MONTH) " +
                   "GROUP BY DATE_FORMAT(date_entretien, '%b') " +
                   "ORDER BY date_entretien";
        
        XYChart.Series<String, Number> maintenanceSeries = new XYChart.Series<>();
        maintenanceSeries.setName("Entretiens");
        
        Map<String, Integer> monthCounts = new HashMap<>();
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String month = rs.getString("month");
                int count = rs.getInt("count");
                monthCounts.put(month, count);
            }
        }
        
        // If data was found, add it to the series
        if (!monthCounts.isEmpty()) {
            for (Map.Entry<String, Integer> entry : monthCounts.entrySet()) {
                maintenanceSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        } else {
            // Otherwise use placeholder data
            maintenanceSeries.getData().add(new XYChart.Data<>("Jan", 4));
            maintenanceSeries.getData().add(new XYChart.Data<>("Fév", 3));
            maintenanceSeries.getData().add(new XYChart.Data<>("Mar", 5));
            maintenanceSeries.getData().add(new XYChart.Data<>("Avr", 2));
            maintenanceSeries.getData().add(new XYChart.Data<>("Mai", 6));
            maintenanceSeries.getData().add(new XYChart.Data<>("Juin", 3));
        }
        
        maintenanceHistoryChart.getData().clear();
        maintenanceHistoryChart.getData().add(maintenanceSeries);
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
    
    private void loadVehiclesTableData(Connection conn) throws SQLException {
        ObservableList<VehicleEntry> vehicleData = FXCollections.observableArrayList();
        
        // SQL query depends on filter
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT v.id, CONCAT(v.marque, ' ', v.modele) as vehicle_name, ")
                 .append("v.immatriculation, v.kilometrage_total, v.statut, ")
                 .append("(SELECT DATE_FORMAT(MAX(e.date_entretien), '%d/%m/%Y') ")
                 .append("FROM entretien e WHERE e.vehicule_id = v.id) as last_maintenance, ")
                 .append("DATE_FORMAT(v.date_prochain_entretien, '%d/%m/%Y') as next_maintenance ")
                 .append("FROM vehicule v");
        
        // Add filter condition if needed
        if (!currentFilter.equals("Tous les véhicules")) {
            if (currentFilter.equals("Véhicules disponibles")) {
                sqlBuilder.append(" WHERE v.statut = 'Disponible'");
            } else if (currentFilter.equals("Véhicules en entretien")) {
                sqlBuilder.append(" WHERE v.statut = 'En entretien'");
            } else if (currentFilter.equals("Véhicules hors service")) {
                sqlBuilder.append(" WHERE v.statut = 'Hors service'");
            }
        }
        
        sqlBuilder.append(" ORDER BY v.id");
        
        // Execute the query
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlBuilder.toString())) {
            
            while (rs.next()) {
                long id = rs.getLong("id");
                String model = rs.getString("vehicle_name");
                String plate = rs.getString("immatriculation");
                double km = rs.getDouble("kilometrage_total");
                String status = rs.getString("statut");
                String lastMaintenance = rs.getString("last_maintenance");
                String nextMaintenance = rs.getString("next_maintenance");
                
                // Handle null values
                if (lastMaintenance == null) lastMaintenance = "Non disponible";
                if (nextMaintenance == null) nextMaintenance = "Non planifié";
                
                vehicleData.add(new VehicleEntry(id, model, plate, km, status, lastMaintenance, nextMaintenance));
            }
        }
        
        // If no data found, add sample data
        if (vehicleData.isEmpty()) {
            vehicleData.add(new VehicleEntry(1L, "Peugeot 208", "134 TU 1234", 45231.0, "Disponible", "15/04/2023", "15/04/2024"));
            vehicleData.add(new VehicleEntry(2L, "Renault Clio", "134 TU 5678", 78562.0, "Disponible", "22/03/2023", "22/03/2024"));
            vehicleData.add(new VehicleEntry(3L, "Citroën C3", "134 TU 9012", 32145.0, "En entretien", "10/01/2023", "10/01/2024"));
        }
        
        vehiclesTable.setItems(vehicleData);
    }
    
    private void loadMaintenanceAlertsData(Connection conn) throws SQLException {
        ObservableList<MaintenanceAlertEntry> alertData = FXCollections.observableArrayList();
        
        // Query for upcoming maintenance alerts
        String sql = "SELECT CONCAT(v.marque, ' ', v.modele) as vehicle_name, " +
                   "CASE " +
                   "  WHEN v.kilometrage_total >= (v.kilometrage_prochain_entretien - 500) THEN 'Vidange' " +
                   "  WHEN v.date_prochaine_visite_technique <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'Visite technique' " +
                   "  WHEN v.date_expiration_assurance <= DATE_ADD(CURDATE(), INTERVAL 30 DAY) THEN 'Assurance' " +
                   "  ELSE 'Maintenance générale' " +
                   "END as alert_type, " +
                   "CASE " +
                   "  WHEN v.date_prochain_entretien IS NOT NULL THEN DATE_FORMAT(v.date_prochain_entretien, '%d/%m/%Y') " +
                   "  WHEN v.date_prochaine_visite_technique IS NOT NULL THEN DATE_FORMAT(v.date_prochaine_visite_technique, '%d/%m/%Y') " +
                   "  WHEN v.date_expiration_assurance IS NOT NULL THEN DATE_FORMAT(v.date_expiration_assurance, '%d/%m/%Y') " +
                   "  ELSE 'Non défini' " +
                   "END as due_date, " +
                   "'En attente' as status, " +
                   "CASE " +
                   "  WHEN v.kilometrage_total >= v.kilometrage_prochain_entretien THEN 'Haute' " +
                   "  WHEN v.kilometrage_total >= (v.kilometrage_prochain_entretien - 300) THEN 'Moyenne' " +
                   "  ELSE 'Basse' " +
                   "END as priority " +
                   "FROM vehicule v " +
                   "WHERE v.kilometrage_total >= (v.kilometrage_prochain_entretien - 500) " +
                   "OR (v.date_prochain_entretien IS NOT NULL AND v.date_prochain_entretien <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)) " +
                   "OR (v.date_prochaine_visite_technique IS NOT NULL AND v.date_prochaine_visite_technique <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)) " +
                   "OR (v.date_expiration_assurance IS NOT NULL AND v.date_expiration_assurance <= DATE_ADD(CURDATE(), INTERVAL 30 DAY))";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String vehicle = rs.getString("vehicle_name");
                String type = rs.getString("alert_type");
                String dueDate = rs.getString("due_date");
                String status = rs.getString("status");
                String priority = rs.getString("priority");
                
                alertData.add(new MaintenanceAlertEntry(vehicle, type, dueDate, status, priority));
            }
        }
        
        // If no data found, add sample data
        if (alertData.isEmpty()) {
            alertData.add(new MaintenanceAlertEntry("Peugeot 308", "Vidange", "03/06/2023", "En attente", "Haute"));
            alertData.add(new MaintenanceAlertEntry("Renault Clio", "Freins", "10/06/2023", "En attente", "Moyenne"));
            alertData.add(new MaintenanceAlertEntry("Citroën C3", "Pneus", "15/06/2023", "Planifié", "Moyenne"));
        }
        
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