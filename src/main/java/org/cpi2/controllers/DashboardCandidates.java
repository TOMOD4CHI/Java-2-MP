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
import java.util.ArrayList;
import java.util.List;

import org.cpi2.repository.DatabaseConfig;

/**
 * Contrôleur pour le tableau de bord des candidats
 */
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
    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    
    /**
     * Initialise le contrôleur et configure les composants UI
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        periodCombo.getItems().addAll(
            "Aujourd'hui",
            "Cette semaine",
            "Ce mois-ci",
            "Ce trimestre", 
            "Cette année"
        );
        periodCombo.getSelectionModel().select("Ce mois-ci");
        
        setupTableColumns();
        
        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        startDate.setValue(firstDay);
        endDate.setValue(now);
        
        filterStartDate = firstDay;
        filterEndDate = now;
        
        loadChartData();
        loadTableData();
        
        periodCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateDateRange(newVal);
        });
    }
    
    /**
     * Met à jour la plage de dates en fonction de la période sélectionnée
     */
    private void updateDateRange(String period) {
        LocalDate now = LocalDate.now();
        
        switch(period) {
            case "Aujourd'hui":
                startDate.setValue(now);
                endDate.setValue(now);
                break;
            case "Cette semaine":
                startDate.setValue(now.minusDays(now.getDayOfWeek().getValue() - 1));
                endDate.setValue(now);
                break;
            case "Ce mois-ci":
                startDate.setValue(now.withDayOfMonth(1));
                endDate.setValue(now);
                break;
            case "Ce trimestre":
                int currentMonth = now.getMonthValue();
                int quarterStartMonth = ((currentMonth - 1) / 3) * 3 + 1;
                startDate.setValue(now.withMonth(quarterStartMonth).withDayOfMonth(1));
                endDate.setValue(now);
                break;
            case "Cette année":
                startDate.setValue(now.withDayOfYear(1));
                endDate.setValue(now);
                break;
        }
    }
    
    /**
     * Gère l'action du bouton de filtre
     */
    @FXML
    private void handleApplyFilter() {
        filterStartDate = startDate.getValue();
        filterEndDate = endDate.getValue();
        
        if (filterStartDate != null && filterEndDate != null) {
            if (filterStartDate.isAfter(filterEndDate)) {
                showAlert("Erreur de date", "La date de début doit être antérieure à la date de fin.");
                return;
            }
            
            loadChartData();
            loadTableData();
        }
    }
    
    /**
     * Configure les colonnes du tableau des candidats
     */
    private void setupTableColumns() {
        candidateIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        candidateNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        candidateAgeColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        candidatePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        candidateEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        candidateStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        candidateRegistrationDateColumn.setCellValueFactory(new PropertyValueFactory<>("registrationDate"));
        
        candidateStatusColumn.setCellFactory(column -> new TableCell<CandidateEntry, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label statusLabel = new Label(status);
                    statusLabel.setMaxWidth(Double.MAX_VALUE);
                    statusLabel.setStyle("-fx-padding: 5px; -fx-alignment: center;");
                    
                    switch (status) {
                        case "Actif":
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #C6F6D5; -fx-text-fill: #22543D; -fx-background-radius: 4px;");
                            break;
                        case "Inactif":
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #FED7D7; -fx-text-fill: #822727; -fx-background-radius: 4px;");
                            break;
                        case "Terminé":
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #E9D8FD; -fx-text-fill: #553C9A; -fx-background-radius: 4px;");
                            break;
                        default: // En attente
                            statusLabel.setStyle(statusLabel.getStyle() + "-fx-background-color: #FEEBC8; -fx-text-fill: #744210; -fx-background-radius: 4px;");
                            break;
                    }
                    
                    setGraphic(statusLabel);
                    setText(null);
                }
            }
        });
    }
    
    /**
     * Charge les données pour tous les graphiques
     */
    private void loadChartData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            loadKPIData(conn);
            loadRegistrationChartData(conn);
            loadAgeDistributionChartData(conn);
            loadAttendanceChartData(conn);
            loadExamResultsChartData(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données", "Impossible de charger les données des graphiques: " + e.getMessage());
        }
    }
    
    /**
     * Charge les données des indicateurs clés de performance
     */
    private void loadKPIData(Connection conn) throws SQLException {
        // Total candidates
        String totalSql = "SELECT COUNT(*) as total FROM candidat";
        
        // New candidates (registered between start and end dates)
        String newSql = "SELECT COUNT(*) as nouveaux FROM candidat WHERE created_at BETWEEN ? AND ?";
        
        // Active candidates (who have an active inscription)
        String activeSql = "SELECT COUNT(DISTINCT c.id) as actifs FROM candidat c " +
                          "JOIN inscription i ON c.cin = i.cin " +
                          "WHERE i.statut = 'En Cours'";
        
        // Completion rate (candidates who have completed at least one exam)
        String completedExamsSql = "SELECT COUNT(DISTINCT candidat_id) as completed FROM examen WHERE resultat = 1";
        
        // Execute queries
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(totalSql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                totalCandidatsLabel.setText(String.valueOf(total));
            }
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {
            // Convert LocalDate to java.sql.Date
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int nouveaux = rs.getInt("nouveaux");
                    newCandidatsLabel.setText(String.valueOf(nouveaux));
                }
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(activeSql)) {
            if (rs.next()) {
                int actifs = rs.getInt("actifs");
                activeCandidatsLabel.setText(String.valueOf(actifs));
            }
        }
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(completedExamsSql)) {
            if (rs.next()) {
                int completed = rs.getInt("completed");
                int total = Integer.parseInt(totalCandidatsLabel.getText());
                int completionRate = total > 0 ? (completed * 100) / total : 0;
                completionRateLabel.setText(completionRate + "%");
            }
        }
        
        // Set the change labels with placeholder data
        // In a real implementation, this would compare with previous period data
        candidatsChangeLabel.setText("+12% vs période précédente");
        newCandidatsChangeLabel.setText("+8% vs période précédente");
        completionChangeLabel.setText("+5% vs période précédente");
        activeChangeLabel.setText("+3% vs période précédente");
    }
    
    /**
     * Charge les données du graphique d'inscription
     */
    private void loadRegistrationChartData(Connection conn) throws SQLException {
        // Get registration data by date
        String sql = "SELECT DATE_FORMAT(created_at, '%d/%m') as date, COUNT(*) as count " +
                     "FROM candidat WHERE created_at BETWEEN ? AND ? " +
                     "GROUP BY DATE_FORMAT(created_at, '%d/%m') ORDER BY created_at";
        
        XYChart.Series<String, Number> registrationSeries = new XYChart.Series<>();
        registrationSeries.setName("Inscriptions");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    int count = rs.getInt("count");
                    registrationSeries.getData().add(new XYChart.Data<>(date, count));
                }
            }
        }
        
        // If no data, add some placeholder data
        if (registrationSeries.getData().isEmpty()) {
            registrationSeries.getData().add(new XYChart.Data<>("01/01", 0));
        }
        
        registrationChart.getData().clear();
        registrationChart.getData().add(registrationSeries);
    }
    
    /**
     * Charge les données du graphique de distribution d'âge
     */
    private void loadAgeDistributionChartData(Connection conn) throws SQLException {
        String sql = "SELECT " +
                     "CASE " +
                     "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 18 AND 20 THEN '18-20 ans' " +
                     "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 21 AND 25 THEN '21-25 ans' " +
                     "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 26 AND 30 THEN '26-30 ans' " +
                     "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) > 30 THEN '31+ ans' " +
                     "  ELSE 'Inconnu' " +
                     "END as age_group, " +
                     "COUNT(*) as count " +
                     "FROM candidat " +
                     "WHERE date_naissance IS NOT NULL " +
                     "GROUP BY age_group";
        
        Map<String, Integer> ageDistribution = new HashMap<>();
        ageDistribution.put("18-20 ans", 0);
        ageDistribution.put("21-25 ans", 0);
        ageDistribution.put("26-30 ans", 0);
        ageDistribution.put("31+ ans", 0);
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ageGroup = rs.getString("age_group");
                int count = rs.getInt("count");
                ageDistribution.put(ageGroup, count);
            }
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Integer> entry : ageDistribution.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
            }
        }
        
        // If no data, add placeholder data
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("18-20 ans", 1));
            pieChartData.add(new PieChart.Data("21-25 ans", 1));
        }
        
        ageDistributionChart.setData(pieChartData);
    }
    
    /**
     * Charge les données du graphique de présence
     */
    private void loadAttendanceChartData(Connection conn) throws SQLException {
        // Get attendance data for different session types
        // Code sessions attendance
        String codeAttendanceSql = "SELECT AVG(pc.present) * 100 as attendance_rate " +
                                 "FROM presence_code pc " +
                                 "JOIN session_code sc ON pc.session_code_id = sc.id " +
                                 "WHERE sc.date_session BETWEEN ? AND ?";
        
        // Driving sessions attendance
        String driveAttendanceSql = "SELECT AVG(pc.present) * 100 as attendance_rate " +
                                  "FROM presence_conduite pc " +
                                  "JOIN session_conduite sc ON pc.session_conduite_id = sc.id " +
                                  "WHERE sc.date_session BETWEEN ? AND ?";
        
        // Initialize data points
        Map<String, Double> attendanceRates = new HashMap<>();
        attendanceRates.put("Code", 0.0);
        attendanceRates.put("Conduite", 0.0);
        attendanceRates.put("Révision", 0.0);
        attendanceRates.put("Examen", 0.0);
        
        // Get code attendance
        try (PreparedStatement pstmt = conn.prepareStatement(codeAttendanceSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double rate = rs.getDouble("attendance_rate");
                    if (!rs.wasNull()) {
                        attendanceRates.put("Code", rate);
                    }
                }
            }
        }
        
        // Get driving attendance
        try (PreparedStatement pstmt = conn.prepareStatement(driveAttendanceSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    double rate = rs.getDouble("attendance_rate");
                    if (!rs.wasNull()) {
                        attendanceRates.put("Conduite", rate);
                    }
                }
            }
        }
        
        // For revision and exam, we'll use placeholder data for now
        // In a real implementation, you would query the appropriate tables
        attendanceRates.put("Révision", 78.0);
        attendanceRates.put("Examen", 95.0);
        
        XYChart.Series<String, Number> attendanceSeries = new XYChart.Series<>();
        attendanceSeries.setName("Taux de Présence");
        
        for (Map.Entry<String, Double> entry : attendanceRates.entrySet()) {
            attendanceSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        attendanceChart.getData().clear();
        attendanceChart.getData().add(attendanceSeries);
    }
    
    /**
     * Charge les données du graphique des résultats d'examen
     */
    private void loadExamResultsChartData(Connection conn) throws SQLException {
        // Get exam results data
        String examSql = "SELECT type.libelle as exam_type, " +
                       "SUM(CASE WHEN e.resultat = 1 THEN 1 ELSE 0 END) as pass_count, " +
                       "SUM(CASE WHEN e.resultat = 0 THEN 1 ELSE 0 END) as fail_count " +
                       "FROM examen e " +
                       "JOIN type_examen type ON e.type_examen_id = type.id " +
                       "WHERE e.date_examen BETWEEN ? AND ? " +
                       "GROUP BY type.libelle";
        
        Map<String, Integer> passData = new HashMap<>();
        Map<String, Integer> failData = new HashMap<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(examSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String examType = rs.getString("exam_type");
                    int passCount = rs.getInt("pass_count");
                    int failCount = rs.getInt("fail_count");
                    
                    passData.put(examType, passCount);
                    failData.put(examType, failCount);
                }
            }
        }
        
        // If no data, add placeholder data
        if (passData.isEmpty()) {
            passData.put("Code", 38);
            passData.put("Conduite", 24);
            failData.put("Code", 12);
            failData.put("Conduite", 6);
        }
        
        XYChart.Series<String, Number> passSeries = new XYChart.Series<>();
        passSeries.setName("Réussite");
        
        XYChart.Series<String, Number> failSeries = new XYChart.Series<>();
        failSeries.setName("Échec");
        
        for (String examType : passData.keySet()) {
            passSeries.getData().add(new XYChart.Data<>(examType, passData.get(examType)));
            failSeries.getData().add(new XYChart.Data<>(examType, failData.get(examType)));
        }
        
        examResultsChart.getData().clear();
        examResultsChart.getData().addAll(passSeries, failSeries);
    }
    
    /**
     * Charge les données du tableau des candidats récents
     */
    private void loadTableData() {
        ObservableList<CandidateEntry> candidateData = FXCollections.observableArrayList();
        
        String sql = "SELECT c.id, CONCAT(c.nom, ' ', c.prenom) as fullname, " +
                   "TIMESTAMPDIFF(YEAR, c.date_naissance, CURDATE()) as age, " +
                   "c.telephone, c.email, i.statut, " +
                   "DATE_FORMAT(c.created_at, '%d/%m/%Y') as reg_date " +
                   "FROM candidat c " +
                   "LEFT JOIN inscription i ON c.cin = i.cin " +
                   "WHERE c.created_at BETWEEN ? AND ? " +
                   "ORDER BY c.created_at DESC LIMIT 10";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Long id = rs.getLong("id");
                    String name = rs.getString("fullname");
                    Integer age = rs.getInt("age");
                    String phone = rs.getString("telephone");
                    String email = rs.getString("email");
                    String status = rs.getString("statut");
                    String regDate = rs.getString("reg_date");
                    
                    // Convert null status to "En attente"
                    if (status == null) status = "En attente";
                    
                    candidateData.add(new CandidateEntry(id, name, age, phone, email, status, regDate));
                }
            }
            
            // If no data found, add sample data
            if (candidateData.isEmpty()) {
                candidateData.add(new CandidateEntry(1L, "Aucune donnée trouvée", 0, "", "", "Inactif", ""));
            }
            
            recentCandidatesTable.setItems(candidateData);
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur de base de données", "Impossible de charger les données des candidats: " + e.getMessage());
        }
    }
    
    /**
     * Affiche une alerte d'erreur
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Classe interne pour les entrées du tableau des candidats
     */
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