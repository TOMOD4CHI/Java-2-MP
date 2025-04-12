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
import org.cpi2.utils.AlertUtil;


public class DashboardCandidates implements Initializable {

    @FXML private ComboBox<String> periodCombo;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;

    @FXML private Label totalCandidatsLabel;
    @FXML private Label newCandidatsLabel;
    @FXML private Label completionRateLabel;
    @FXML private Label activeCandidatsLabel;
    @FXML private Label candidatsChangeLabel;
    @FXML private Label newCandidatsChangeLabel;
    @FXML private Label completionChangeLabel;
    @FXML private Label activeChangeLabel;

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
    
    
    @FXML
    private void handleApplyFilter() {
        filterStartDate = startDate.getValue();
        filterEndDate = endDate.getValue();

        if (filterStartDate != null && filterEndDate != null && filterStartDate.isAfter(filterEndDate)) {
            AlertUtil.showError("Erreur de date", "La date de début doit être avant la date de fin");
            return;
        }

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
    
    
    private void loadChartData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            loadKPIData(conn);
            loadRegistrationChartData(conn);
            loadAgeDistributionChartData(conn);
            loadAttendanceChartData(conn);
            loadExamResultsChartData(conn);
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur de base de données", "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    
    private void loadKPIData(Connection conn) throws SQLException {

        String totalSql = "SELECT COUNT(*) as total FROM candidat";

        String newSql = "SELECT COUNT(*) as nouveaux FROM candidat WHERE created_at BETWEEN ? AND ?";

        String activeSql = "SELECT COUNT(DISTINCT c.id) as actifs FROM candidat c " +
                          "JOIN inscription i ON c.cin = i.cin " +
                          "WHERE i.statut = 'En Cours'";

        String completedExamsSql = "SELECT COUNT(DISTINCT candidat_id) as completed FROM examen WHERE resultat = 1";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(totalSql)) {
            if (rs.next()) {
                int total = rs.getInt("total");
                totalCandidatsLabel.setText(String.valueOf(total));
            }
        }
        
        try (PreparedStatement pstmt = conn.prepareStatement(newSql)) {

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


        candidatsChangeLabel.setText("+12% vs période précédente");
        newCandidatsChangeLabel.setText("+8% vs période précédente");
        completionChangeLabel.setText("+5% vs période précédente");
        activeChangeLabel.setText("+3% vs période précédente");
    }
    
    
    private void loadRegistrationChartData(Connection conn) throws SQLException {
        XYChart.Series<String, Number> registrationSeries = new XYChart.Series<>();
        registrationSeries.setName("Inscriptions");
        
        String sql = "SELECT DATE_FORMAT(date_inscription, '%d/%m') as date, COUNT(*) as count " +
                    "FROM inscription WHERE date_inscription BETWEEN ? AND ? " +
                    "GROUP BY DATE_FORMAT(date_inscription, '%d/%m') " +
                    "ORDER BY date_inscription";
        
        Map<String, Integer> registrationByDate = new HashMap<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    String date = rs.getString("date");
                    int count = rs.getInt("count");
                    registrationByDate.put(date, count);
                }
            }
        }
        
        if (!registrationByDate.isEmpty()) {
            for (Map.Entry<String, Integer> entry : registrationByDate.entrySet()) {
                registrationSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        } else {

            registrationSeries.getData().add(new XYChart.Data<>(filterStartDate.format(DateTimeFormatter.ofPattern("dd/MM")), 0));
        }
        
        registrationChart.getData().clear();
        registrationChart.getData().add(registrationSeries);
    }
    
    
    private void loadAgeDistributionChartData(Connection conn) throws SQLException {
        Map<String, Integer> ageGroups = new HashMap<>();
        ageGroups.put("18-20 ans", 0);
        ageGroups.put("21-25 ans", 0);
        ageGroups.put("26-30 ans", 0);
        ageGroups.put("31-40 ans", 0);
        ageGroups.put("41+ ans", 0);
        
        String sql = "SELECT " +
                   "CASE " +
                   "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 18 AND 20 THEN '18-20 ans' " +
                   "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 21 AND 25 THEN '21-25 ans' " +
                   "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 26 AND 30 THEN '26-30 ans' " +
                   "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) BETWEEN 31 AND 40 THEN '31-40 ans' " +
                   "  WHEN TIMESTAMPDIFF(YEAR, date_naissance, CURDATE()) > 40 THEN '41+ ans' " +
                   "  ELSE NULL " +
                   "END as age_group, " +
                   "COUNT(*) as count " +
                   "FROM candidat " +
                   "WHERE date_naissance IS NOT NULL " +
                   "GROUP BY age_group " +
                   "HAVING age_group IS NOT NULL";
        
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String ageGroup = rs.getString("age_group");
                int count = rs.getInt("count");
                if (ageGroup != null) {
                    ageGroups.put(ageGroup, count);
                }
            }
        }
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        boolean hasData = false;
        
        for (Map.Entry<String, Integer> entry : ageGroups.entrySet()) {
            if (entry.getValue() > 0) {
                pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
                hasData = true;
            }
        }
        
        if (!hasData) {

            pieChartData.add(new PieChart.Data("Aucune donnée", 1));
        }
        
        ageDistributionChart.setData(pieChartData);
    }
    
    
    private void loadAttendanceChartData(Connection conn) throws SQLException {

        Map<String, Integer> attendanceData = new HashMap<>();
        attendanceData.put("Code", 0);
        attendanceData.put("Conduite", 0);

        String codeSql = "SELECT " +
                        "COUNT(*) as total, " +
                        "SUM(present) as present_count " +
                        "FROM presence_code " +
                        "JOIN session_code ON presence_code.session_code_id = session_code.id " +
                        "WHERE session_code.date_session BETWEEN ? AND ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(codeSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int presentCount = rs.getInt("present_count");
                    
                    if (total > 0) {
                        double rate = ((double) presentCount / total) * 100;
                        attendanceData.put("Code", (int) rate);
                    }
                }
            }
        } catch (SQLException e) {

            System.out.println("Warning: Could not get code attendance data: " + e.getMessage());
        }

        String conduiteSql = "SELECT " +
                            "COUNT(*) as total, " +
                            "SUM(present) as present_count " +
                            "FROM presence_conduite " +
                            "JOIN session_conduite ON presence_conduite.session_conduite_id = session_conduite.id " +
                            "WHERE session_conduite.date_session BETWEEN ? AND ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(conduiteSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int total = rs.getInt("total");
                    int presentCount = rs.getInt("present_count");
                    
                    if (total > 0) {
                        double rate = ((double) presentCount / total) * 100;
                        attendanceData.put("Conduite", (int) rate);
                    }
                }
            }
        } catch (SQLException e) {

            System.out.println("Warning: Could not get driving attendance data: " + e.getMessage());
        }

        if (attendanceData.get("Conduite") == 0) {
            String seanceSql = "SELECT " +
                             "COUNT(*) as total, " +
                             "COUNT(CASE WHEN statut = 'Complétée' THEN 1 END) as present_count " +
                             "FROM seance " +
                             "WHERE date BETWEEN ? AND ? " +
                             "AND type = 'Conduite'";
            
            try (PreparedStatement pstmt = conn.prepareStatement(seanceSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        int total = rs.getInt("total");
                        int presentCount = rs.getInt("present_count");
                        
                        if (total > 0) {
                            double rate = ((double) presentCount / total) * 100;
                            attendanceData.put("Conduite", (int) rate);
                        }
                    }
                }
            } catch (SQLException e) {

                System.out.println("Warning: Could not get driving attendance data from seance: " + e.getMessage());
            }
        }
        
        XYChart.Series<String, Number> attendanceSeries = new XYChart.Series<>();
        attendanceSeries.setName("Taux de présence (%)");
        
        for (Map.Entry<String, Integer> entry : attendanceData.entrySet()) {
            attendanceSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        
        attendanceChart.getData().clear();
        attendanceChart.getData().add(attendanceSeries);
    }
    
    
    private void loadExamResultsChartData(Connection conn) throws SQLException {
        Map<String, Integer> passData = new HashMap<>();
        passData.put("Code", 0);
        passData.put("Conduite", 0);
        
        Map<String, Integer> failData = new HashMap<>();
        failData.put("Code", 0);
        failData.put("Conduite", 0);

        String codeSql = "SELECT " +
                       "COUNT(CASE WHEN e.resultat = 1 THEN 1 END) as pass_count, " +
                       "COUNT(CASE WHEN e.resultat = 0 THEN 1 END) as fail_count " +
                       "FROM examen e " +
                       "JOIN type_examen te ON e.type_examen_id = te.id " +
                       "WHERE e.date_examen BETWEEN ? AND ? " +
                       "AND te.libelle = 'Code'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(codeSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int passCount = rs.getInt("pass_count");
                    int failCount = rs.getInt("fail_count");
                    
                    passData.put("Code", passCount);
                    failData.put("Code", failCount);
                }
            }
        }

        String drivingSql = "SELECT " +
                          "COUNT(CASE WHEN e.resultat = 1 THEN 1 END) as pass_count, " +
                          "COUNT(CASE WHEN e.resultat = 0 THEN 1 END) as fail_count " +
                          "FROM examen e " +
                          "JOIN type_examen te ON e.type_examen_id = te.id " +
                          "WHERE e.date_examen BETWEEN ? AND ? " +
                          "AND te.libelle = 'Conduite'";
        
        try (PreparedStatement pstmt = conn.prepareStatement(drivingSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int passCount = rs.getInt("pass_count");
                    int failCount = rs.getInt("fail_count");
                    
                    passData.put("Conduite", passCount);
                    failData.put("Conduite", failCount);
                }
            }
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

                    if (status == null) status = "En attente";
                    
                    candidateData.add(new CandidateEntry(id, name, age, phone, email, status, regDate));
                }
            }

            if (candidateData.isEmpty()) {
                candidateData.add(new CandidateEntry(1L, "Aucune donnée trouvée", 0, "", "", "Inactif", ""));
            }
            
            recentCandidatesTable.setItems(candidateData);
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur de base de données", "Impossible de charger les données des candidats: " + e.getMessage());
        }
    }
    

    
    
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
