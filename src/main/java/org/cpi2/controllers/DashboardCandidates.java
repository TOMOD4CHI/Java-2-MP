package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.cpi2.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class DashboardCandidates implements Initializable {

    @FXML private ComboBox<String> periodCombo;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    
    // KPI Labels
    @FXML private Label totalCandidatsLabel;
    @FXML private Label newCandidatsLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label activeCandidatsLabel;
    @FXML private Label candidatsChangeLabel;
    @FXML private Label newCandidatsChangeLabel;
    @FXML private Label completionChangeLabel;
    @FXML private Label activeChangeLabel;
    
    // Charts
    @FXML private LineChart<String, Number> registrationChart;
    @FXML private CategoryAxis registrationDateAxis;
    @FXML private NumberAxis registrationCountAxis;
    
    @FXML private PieChart ageDistributionChart;
    
    @FXML private BarChart<String, Number> attendanceChart;
    @FXML private CategoryAxis attendanceTypeAxis;
    @FXML private NumberAxis attendanceRateAxis;
    
    @FXML private StackedBarChart<String, Number> examResultsChart;
    @FXML private CategoryAxis examTypeAxis;
    @FXML private NumberAxis examResultsAxis;
    
    // Table
    @FXML private TableView<CandidateEntry> recentCandidatesTable;
    @FXML private TableColumn<CandidateEntry, Long> candidateIdColumn;
    @FXML private TableColumn<CandidateEntry, String> candidateNameColumn;
    @FXML private TableColumn<CandidateEntry, Integer> candidateAgeColumn;
    @FXML private TableColumn<CandidateEntry, String> candidatePhoneColumn;
    @FXML private TableColumn<CandidateEntry, String> candidateEmailColumn;
    @FXML private TableColumn<CandidateEntry, String> candidateStatusColumn;
    @FXML private TableColumn<CandidateEntry, String> candidateRegistrationDateColumn;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize the period combobox
        periodCombo.getItems().addAll(
            "Aujourd'hui",
            "Cette semaine",
            "Ce mois-ci",
            "Ce trimestre", 
            "Cette année"
        );
        periodCombo.getSelectionModel().select("Ce mois-ci");
        
        // Initialize date pickers with current month
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        startDate.setValue(firstDay);
        endDate.setValue(now);
        
        // Set up the table columns
        setupTableColumns();
        
        // Load data
        loadChartData();
        loadTableData();
    }
    
    @FXML
    private void handleApplyFilter() {
        // Get values from filters
        String period = periodCombo.getValue();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        
        // Validate dates
        if (start != null && end != null && start.isAfter(end)) {
            AlertUtil.showError("Erreur de date", "La date de début doit être avant la date de fin");
            return;
        }
        
        // Apply filter and reload data
        loadChartData();
        loadTableData();
    }
    
    private void setupTableColumns() {
        candidateIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        candidateAgeColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        candidatePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        candidateEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        candidateStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        candidateRegistrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        
        // Format status column with color indicators
        candidateStatusColumn.setCellFactory(col -> new TableCell<CandidateEntry, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    switch (status) {
                        case "Actif":
                            setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                            break;
                        case "En attente":
                            setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
                            break;
                        case "Inactif":
                            setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                            break;
                        case "Terminé":
                            setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
                            break;
                        default:
                            setStyle("");
                            break;
                    }
                }
            }
        });
    }
    
    private void loadChartData() {
        // Registration chart data
        XYChart.Series<String, Number> registrationSeries = new XYChart.Series<>();
        registrationSeries.setName("Inscriptions");
        
        registrationSeries.getData().add(new XYChart.Data<>("01/01", 8));
        registrationSeries.getData().add(new XYChart.Data<>("05/01", 12));
        registrationSeries.getData().add(new XYChart.Data<>("10/01", 7));
        registrationSeries.getData().add(new XYChart.Data<>("15/01", 14));
        registrationSeries.getData().add(new XYChart.Data<>("20/01", 9));
        registrationSeries.getData().add(new XYChart.Data<>("25/01", 11));
        registrationSeries.getData().add(new XYChart.Data<>("30/01", 15));
        
        registrationChart.getData().clear();
        registrationChart.getData().add(registrationSeries);
        
        // Age distribution chart data
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("18-20 ans", 45),
            new PieChart.Data("21-25 ans", 30),
            new PieChart.Data("26-30 ans", 15),
            new PieChart.Data("31+ ans", 10)
        );
        ageDistributionChart.setData(pieChartData);
        
        // Attendance chart data
        XYChart.Series<String, Number> attendanceSeries = new XYChart.Series<>();
        attendanceSeries.setName("Taux de Présence");
        
        attendanceSeries.getData().add(new XYChart.Data<>("Code", 85));
        attendanceSeries.getData().add(new XYChart.Data<>("Conduite", 92));
        attendanceSeries.getData().add(new XYChart.Data<>("Révision", 78));
        attendanceSeries.getData().add(new XYChart.Data<>("Examen", 95));
        
        attendanceChart.getData().clear();
        attendanceChart.getData().add(attendanceSeries);
        
        // Exam results chart data
        XYChart.Series<String, Number> passSeries = new XYChart.Series<>();
        passSeries.setName("Réussite");
        
        passSeries.getData().add(new XYChart.Data<>("Code", 38));
        passSeries.getData().add(new XYChart.Data<>("Conduite", 24));
        
        XYChart.Series<String, Number> failSeries = new XYChart.Series<>();
        failSeries.setName("Échec");
        
        failSeries.getData().add(new XYChart.Data<>("Code", 12));
        failSeries.getData().add(new XYChart.Data<>("Conduite", 6));
        
        examResultsChart.getData().clear();
        examResultsChart.getData().addAll(passSeries, failSeries);
        
        // Update KPI labels with sample data
        totalCandidatsLabel.setText("156");
        newCandidatsLabel.setText("24");
        completionRateLabel.setText("78%");
        activeCandidatsLabel.setText("87");
        
        candidatsChangeLabel.setText("+12% vs période précédente");
        newCandidatsChangeLabel.setText("+8% vs période précédente");
        completionChangeLabel.setText("+5% vs période précédente");
        activeChangeLabel.setText("+3% vs période précédente");
    }
    
    private void loadTableData() {
        // Sample candidate data
        ObservableList<CandidateEntry> candidateData = FXCollections.observableArrayList(
            new CandidateEntry(1L, "Ahmed Ben Ali", 22, "+216 98 765 432", "ahmed@example.com", "Actif", "15/01/2023"),
            new CandidateEntry(2L, "Fatma Trabelsi", 19, "+216 55 432 109", "fatma@example.com", "Actif", "20/01/2023"),
            new CandidateEntry(3L, "Mohamed Sassi", 25, "+216 21 876 543", "mohamed@example.com", "En attente", "22/01/2023"),
            new CandidateEntry(4L, "Ines Miled", 28, "+216 94 285 760", "ines@example.com", "Actif", "23/01/2023"),
            new CandidateEntry(5L, "Sami Khalifa", 20, "+216 58 123 476", "sami@example.com", "Inactif", "25/01/2023"),
            new CandidateEntry(6L, "Nour Mejri", 24, "+216 26 987 012", "nour@example.com", "Terminé", "28/01/2023"),
            new CandidateEntry(7L, "Karim Ben Salah", 30, "+216 97 654 321", "karim@example.com", "Actif", "30/01/2023")
        );
        
        recentCandidatesTable.setItems(candidateData);
    }
    
    // Inner class for candidate table entries
    public static class CandidateEntry {
        private final Long id;
        private final String name;
        private final Integer age;
        private final String phone;
        private final String email;
        private final String status;
        private final String registrationDate;
        
        public CandidateEntry(Long id, String name, Integer age, String phone, 
                             String email, String status, String registrationDate) {
            this.id = id;
            this.name = name;
            this.age = age;
            this.phone = phone;
            this.email = email;
            this.status = status;
            this.registrationDate = registrationDate;
        }
        
        public Long getId() { return id; }
        public String getName() { return name; }
        public Integer getAge() { return age; }
        public String getPhone() { return phone; }
        public String getEmail() { return email; }
        public String getStatus() { return status; }
        public String getRegistrationDate() { return registrationDate; }
    }
} 