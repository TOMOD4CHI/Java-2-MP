package org.cpi2.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import org.cpi2.repository.DatabaseConfig;
import org.cpi2.service.EntretienService;
import org.cpi2.utils.AlertUtil;

public class DashboardFinance implements Initializable {

    @FXML private ComboBox<String> periodCombo;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;

    @FXML private Label totalRevenueLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netProfitLabel;
    @FXML private Label newStudentsLabel;
    @FXML private Label revenueChangeLabel;
    @FXML private Label expensesChangeLabel;
    @FXML private Label profitChangeLabel;
    @FXML private Label studentsChangeLabel;

    @FXML private AreaChart<String, Number> revenueChart;
    @FXML private CategoryAxis revenueDateAxis;
    @FXML private NumberAxis revenueValueAxis;
    
    @FXML private PieChart expensesPieChart;
    
    @FXML private BarChart<String, Number> revenueByServiceChart;
    @FXML private CategoryAxis serviceAxis;
    @FXML private NumberAxis serviceRevenueAxis;
    
    @FXML private BarChart<String, Number> monthlyComparisonChart;
    @FXML private CategoryAxis monthAxis;
    @FXML private NumberAxis monthlyValueAxis;

    @FXML private TableView<Transaction> transactionsTable;
    @FXML private TableColumn<Transaction, String> transactionDateColumn;
    @FXML private TableColumn<Transaction, String> transactionTypeColumn;
    @FXML private TableColumn<Transaction, String> transactionDescriptionColumn;
    @FXML private TableColumn<Transaction, Double> transactionAmountColumn;
    @FXML private TableColumn<Transaction, String> transactionStatusColumn;

    private LocalDate filterStartDate;
    private LocalDate filterEndDate;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        LocalDate now = LocalDate.now();
        LocalDate firstDay = now.withDayOfMonth(1);
        startDate.setValue(firstDay);
        endDate.setValue(now);
        
        filterStartDate = firstDay;
        filterEndDate = now;

        setupTransactionsTable();

        initializeEmptyCharts();

        Platform.runLater(() -> {
            try {
                loadChartData();
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtil.showError("Erreur de chargement", "Impossible de charger les données: " + e.getMessage());
            }
        });

        periodCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateDateRange(newVal);
        });
    }
    
    private void initializeEmptyCharts() {

        XYChart.Series<String, Number> emptySeries = new XYChart.Series<>();
        emptySeries.setName("Chargement...");
        revenueChart.getData().add(emptySeries);

        ObservableList<PieChart.Data> emptyPieData = FXCollections.observableArrayList();
        emptyPieData.add(new PieChart.Data("Chargement...", 1));
        expensesPieChart.setData(emptyPieData);

        XYChart.Series<String, Number> emptyServiceSeries = new XYChart.Series<>();
        emptyServiceSeries.setName("Chargement...");
        revenueByServiceChart.getData().add(emptyServiceSeries);

        XYChart.Series<String, Number> emptyMonthlySeries = new XYChart.Series<>();
        emptyMonthlySeries.setName("Chargement...");
        monthlyComparisonChart.getData().add(emptyMonthlySeries);
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
                int startMonth = ((currentMonth - 1) / 3) * 3 + 1;
                startDate.setValue(LocalDate.of(now.getYear(), startMonth, 1));
                endDate.setValue(now);
                break;
            case "Cette année":
                startDate.setValue(LocalDate.of(now.getYear(), 1, 1));
                endDate.setValue(now);
                break;
        }
    }
    
    @FXML
    private void handleApplyFilter() {

        filterStartDate = startDate.getValue();
        filterEndDate = endDate.getValue();

        if (filterStartDate == null || filterEndDate == null) {
            AlertUtil.showError("Erreur de date", "Veuillez sélectionner des dates valides");
            return;
        }
        
        if (filterStartDate.isAfter(filterEndDate)) {
            AlertUtil.showError("Erreur de date", "La date de début doit être avant la date de fin");
            return;
        }

        totalRevenueLabel.setText("Chargement...");
        totalExpensesLabel.setText("Chargement...");
        netProfitLabel.setText("Chargement...");
        newStudentsLabel.setText("Chargement...");

        Platform.runLater(() -> {
            try {
                loadChartData();
            } catch (Exception e) {
                e.printStackTrace();

                Platform.runLater(() -> {
                    totalRevenueLabel.setText("0.00 DT");
                    totalExpensesLabel.setText("0.00 DT");
                    netProfitLabel.setText("0.00 DT");
                    newStudentsLabel.setText("0");
                });
            }
        });
    }
    
    private void loadChartData() {

        List<Exception> criticalErrors = new ArrayList<>();


        try {
            loadKPIData();
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                totalRevenueLabel.setText("0.00 DT");
                totalExpensesLabel.setText("0.00 DT");
                netProfitLabel.setText("0.00 DT");
                newStudentsLabel.setText("0");
                revenueChangeLabel.setText("--% vs période précédente");
                expensesChangeLabel.setText("--% vs période précédente");
                profitChangeLabel.setText("--% vs période précédente");
                studentsChangeLabel.setText("--% vs période précédente");
            });
        }
        
        try {
            loadRevenueChartData();
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                revenueChart.getData().clear();
                XYChart.Series<String, Number> emptySeries = new XYChart.Series<>();
                emptySeries.setName("Revenus");
                revenueChart.getData().add(emptySeries);
            });
        }
        
        try {
            loadExpensesBreakdownData();
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                ObservableList<PieChart.Data> emptyData = FXCollections.observableArrayList();
                emptyData.add(new PieChart.Data("Aucune dépense", 1));
                expensesPieChart.setData(emptyData);
            });
        }
        
        try {
            loadRevenueByServiceData();
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                revenueByServiceChart.getData().clear();
                XYChart.Series<String, Number> emptySeries = new XYChart.Series<>();
                emptySeries.setName("Revenus par Service");
                revenueByServiceChart.getData().add(emptySeries);
            });
        }
        
        try {
            loadMonthlyComparisonData();
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                monthlyComparisonChart.getData().clear();
                XYChart.Series<String, Number> emptySeries = new XYChart.Series<>();
                emptySeries.setName("Comparaison Mensuelle");
                monthlyComparisonChart.getData().add(emptySeries);
            });
        }
        
        try {
            loadTransactionsData();
        } catch (Exception e) {
            e.printStackTrace();

            Platform.runLater(() -> {
                transactionsTable.setItems(FXCollections.observableArrayList());
            });
        }

        if (!criticalErrors.isEmpty()) {
            Platform.runLater(() -> {
                AlertUtil.showError("Erreur", "Erreur critique lors du chargement des données financières.");
            });
        }
    }
    
    private void loadKPIData() {


        String revenueSql = "SELECT SUM(montant) as total_revenue FROM paiement " +
                           "WHERE date_paiement BETWEEN ? AND ?";

        String expensesSql = "SELECT SUM(cout) as total_expenses FROM entretien " +
                           "WHERE date_entretien BETWEEN ? AND ?";

        String newStudentsSql = "SELECT COUNT(*) as new_students FROM inscription " +
                              "WHERE date_inscription BETWEEN ? AND ?";

        LocalDate prevPeriodEndDate = filterStartDate.minusDays(1);
        int daysBetween = (int) java.time.temporal.ChronoUnit.DAYS.between(filterStartDate, filterEndDate);
        LocalDate prevPeriodStartDate = prevPeriodEndDate.minusDays(daysBetween);
        
        double totalRevenue = 0;
        double prevRevenue = 0;
        double totalExpenses = 0;
        double prevExpenses = 0;
        int newStudents = 0;
        int prevStudents = 0;


        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(revenueSql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    totalRevenue = rs.getDouble("total_revenue");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(revenueSql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(prevPeriodStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(prevPeriodEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    prevRevenue = rs.getDouble("total_revenue");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(expensesSql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    totalExpenses = rs.getDouble("total_expenses");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        try {
            EntretienService entretienService = new EntretienService();
            double serviceTotalCost = entretienService.getTotalCost(filterStartDate, filterEndDate);
            
            if (serviceTotalCost > 0) {
                totalExpenses = serviceTotalCost;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(expensesSql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(prevPeriodStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(prevPeriodEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    prevExpenses = rs.getDouble("total_expenses");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        try {
            EntretienService entretienService = new EntretienService();
            double prevServiceCost = entretienService.getTotalCost(prevPeriodStartDate, prevPeriodEndDate);
            
            if (prevServiceCost > 0) {
                prevExpenses = prevServiceCost;
            }
        } catch (Exception e) {
            e.printStackTrace();

        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(newStudentsSql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    newStudents = rs.getInt("new_students");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(newStudentsSql)) {
            
            pstmt.setDate(1, java.sql.Date.valueOf(prevPeriodStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(prevPeriodEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    prevStudents = rs.getInt("new_students");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM moniteur")) {
            
            if (rs.next()) {
                int monitorCount = rs.getInt("total");
                if (monitorCount > 0) {
                    double salaireMoyen = 1000.0; // Average salary per monitor in DT

                    double totalSalaires = monitorCount * salaireMoyen;

                    int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(filterStartDate, filterEndDate) + 1;
                    double periodRatio = daysInPeriod / 30.0; // Approximate days in a month
                    double salaryForPeriod = totalSalaires * periodRatio;

                    totalExpenses += salaryForPeriod;

                    int prevDaysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(prevPeriodStartDate, prevPeriodEndDate) + 1;
                    double prevPeriodRatio = prevDaysInPeriod / 30.0;
                    double salaryForPrevPeriod = totalSalaires * prevPeriodRatio;
                    
                    prevExpenses += salaryForPrevPeriod;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }

        double loyerMensuel = 1200.0; // Monthly rent
        try {
            int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(filterStartDate, filterEndDate) + 1;
            double periodRatio = daysInPeriod / 30.0; // Approximate days in a month
            totalExpenses += (loyerMensuel * periodRatio);
            
            int prevDaysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(prevPeriodStartDate, prevPeriodEndDate) + 1;
            double prevPeriodRatio = prevDaysInPeriod / 30.0;
            prevExpenses += (loyerMensuel * prevPeriodRatio);
        } catch (Exception e) {
            e.printStackTrace();

        }

        final double finalTotalRevenue = totalRevenue;
        final double finalTotalExpenses = totalExpenses;
        final double finalPrevRevenue = prevRevenue;
        final double finalPrevExpenses = prevExpenses;
        final int finalNewStudents = newStudents;
        final int finalPrevStudents = prevStudents;
        
        Platform.runLater(() -> {

            totalRevenueLabel.setText(String.format("%.2f DT", finalTotalRevenue));

            totalExpensesLabel.setText(String.format("%.2f DT", -finalTotalExpenses));

            newStudentsLabel.setText(String.valueOf(finalNewStudents));

            double netProfit = finalTotalRevenue - finalTotalExpenses;
            netProfitLabel.setText(String.format("%.2f DT", netProfit));

            if (finalPrevRevenue > 0) {
                double revenueChange = ((finalTotalRevenue - finalPrevRevenue) / finalPrevRevenue) * 100;
                String direction = revenueChange >= 0 ? "+" : "";
                revenueChangeLabel.setText(String.format("%s%.1f%% vs période précédente", direction, revenueChange));
            } else {
                revenueChangeLabel.setText("--% vs période précédente");
            }

            if (finalPrevExpenses > 0) {
                double expensesChange = ((finalTotalExpenses - finalPrevExpenses) / finalPrevExpenses) * 100;

                String direction = expensesChange <= 0 ? "+" : "";
                String changeText = String.format("%s%.1f%% vs période précédente", direction, Math.abs(expensesChange));
                expensesChangeLabel.setText(changeText);
            } else {
                expensesChangeLabel.setText("--% vs période précédente");
            }

            double prevProfit = finalPrevRevenue - finalPrevExpenses;
            if (prevProfit != 0) {
                double profitChange = ((netProfit - prevProfit) / Math.abs(prevProfit)) * 100;
                String direction = profitChange >= 0 ? "+" : "";
                profitChangeLabel.setText(String.format("%s%.1f%% vs période précédente", direction, profitChange));
            } else {
                profitChangeLabel.setText("--% vs période précédente");
            }

            if (finalPrevStudents > 0) {
                double studentsChange = ((double) (finalNewStudents - finalPrevStudents) / finalPrevStudents) * 100;
                String direction = studentsChange >= 0 ? "+" : "";
                studentsChangeLabel.setText(String.format("%s%.1f%% vs période précédente", direction, studentsChange));
            } else {
                studentsChangeLabel.setText("--% vs période précédente");
            }
        });
    }
    
    private void loadRevenueChartData() throws SQLException {

        String sql = "SELECT DATE_FORMAT(date_paiement, '%d/%m') as date, SUM(montant) as amount " +
                    "FROM paiement WHERE date_paiement BETWEEN ? AND ? " +
                    "GROUP BY DATE_FORMAT(date_paiement, '%d/%m') ORDER BY date_paiement";
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus");
        
        try (Connection conn = DatabaseConfig.getConnection()) {

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                
                Map<String, Double> revenueByDate = new HashMap<>();
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String date = rs.getString("date");
                        double amount = rs.getDouble("amount");
                        revenueByDate.put(date, amount);
                    }
                }

                LocalDate current = filterStartDate;
                while (!current.isAfter(filterEndDate)) {
                    String dateStr = current.format(DateTimeFormatter.ofPattern("dd/MM"));
                    if (!revenueByDate.containsKey(dateStr)) {
                        revenueByDate.put(dateStr, 0.0);
                    }
                    current = current.plusDays(1);
                }

                revenueByDate.entrySet().stream()
                    .sorted((e1, e2) -> {

                        String[] parts1 = e1.getKey().split("/");
                        String[] parts2 = e2.getKey().split("/");

                        int val1 = Integer.parseInt(parts1[1]) * 100 + Integer.parseInt(parts1[0]);
                        int val2 = Integer.parseInt(parts2[1]) * 100 + Integer.parseInt(parts2[0]);
                        
                        return val1 - val2;
                    })
                    .forEach(entry -> {
                        revenueSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    });
            }
        }

        Platform.runLater(() -> {
            revenueChart.getData().clear();
            revenueChart.getData().add(revenueSeries);
        });
    }
    
    private void loadExpensesBreakdownData() throws SQLException {

        Map<String, Double> expensesCategories = new HashMap<>();
        expensesCategories.put("Salaires", 0.0);
        expensesCategories.put("Loyer", 0.0);
        expensesCategories.put("Carburant", 0.0);
        expensesCategories.put("Maintenance", 0.0);
        expensesCategories.put("Autres", 0.0);
        
        try (Connection conn = DatabaseConfig.getConnection()) {

            String maintenanceSql = "SELECT type_entretien, SUM(cout) as total FROM entretien " +
                                  "WHERE date_entretien BETWEEN ? AND ? " +
                                  "GROUP BY type_entretien";
            
            try (PreparedStatement pstmt = conn.prepareStatement(maintenanceSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("type_entretien");
                        double amount = rs.getDouble("total");
                        
                        if (type != null) {
                            if (type.equalsIgnoreCase("Vidange")) {
                                expensesCategories.put("Maintenance", expensesCategories.get("Maintenance") + amount);
                            } else if (type.equalsIgnoreCase("Carburant")) {
                                expensesCategories.put("Carburant", expensesCategories.get("Carburant") + amount);
                            } else {
                                expensesCategories.put("Autres", expensesCategories.get("Autres") + amount);
                            }
                        }
                    }
                }
            }

            String monitorCountSql = "SELECT COUNT(*) as total FROM moniteur";
            double salaireMoyen = 1000.0; // Average monthly salary per monitor
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(monitorCountSql)) {
                if (rs.next()) {
                    int monitorCount = rs.getInt("total");
                    if (monitorCount > 0) {

                        double totalSalaires = monitorCount * salaireMoyen;

                        int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(filterStartDate, filterEndDate) + 1;
                        double periodRatio = daysInPeriod / 30.0; // Approximate days in a month
                        
                        expensesCategories.put("Salaires", totalSalaires * periodRatio);
                    }
                }
            }

            double loyerMensuel = 1200.0; // Monthly rent
            int daysInPeriod = (int) java.time.temporal.ChronoUnit.DAYS.between(filterStartDate, filterEndDate) + 1;
            double periodRatio = daysInPeriod / 30.0; // Approximate days in a month
            expensesCategories.put("Loyer", loyerMensuel * periodRatio);
        }

        expensesCategories.entrySet().removeIf(entry -> entry.getValue() <= 0);

        double totalExpenses = expensesCategories.values().stream().mapToDouble(Double::doubleValue).sum();

        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : expensesCategories.entrySet()) {
            if (entry.getValue() > 0) {
                double percentage = (entry.getValue() / totalExpenses) * 100;
                String label = String.format("%s (%.1f%%)", entry.getKey(), percentage);
                pieChartData.add(new PieChart.Data(label, entry.getValue()));
            }
        }

        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Aucune dépense", 1));
        }

        Platform.runLater(() -> {
            expensesPieChart.setData(pieChartData);
        });
    }
    
    private void loadRevenueByServiceData() throws SQLException {
        XYChart.Series<String, Number> serviceSeries = new XYChart.Series<>();
        serviceSeries.setName("Revenus par Service");
        
        Map<String, Double> serviceRevenues = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {

            boolean hasData = false;

            String planSql = "SELECT p.libelle as service, SUM(pay.montant) as revenue " +
                           "FROM paiement pay " +
                           "JOIN inscription i ON pay.inscription_id = i.id " +
                           "JOIN plan p ON i.plan_id = p.id " +
                           "WHERE pay.date_paiement BETWEEN ? AND ? " +
                           "GROUP BY p.libelle";
            
            try (PreparedStatement pstmt = conn.prepareStatement(planSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        String service = rs.getString("service");
                        double revenue = rs.getDouble("revenue");
                        if (service != null && !service.trim().isEmpty() && revenue > 0) {
                            serviceRevenues.put(service, revenue);
                            hasData = true;
                        }
                    }
                }
            } catch (SQLException e) {

                e.printStackTrace();
            }

            if (!hasData) {
                String typeSql = "SELECT COALESCE(type_paiement, 'Paiement général') as service, " +
                               "SUM(montant) as revenue " +
                               "FROM paiement " +
                               "WHERE date_paiement BETWEEN ? AND ? " +
                               "GROUP BY type_paiement";
                
                try (PreparedStatement pstmt = conn.prepareStatement(typeSql)) {
                    pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                    pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String service = rs.getString("service");
                            double revenue = rs.getDouble("revenue");
                            if (revenue > 0) {
                                serviceRevenues.put(service, revenue);
                                hasData = true;
                            }
                        }
                    }
                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }

            if (!hasData) {
                String inscriptionSql = "SELECT 'Formation' as service, SUM(montant) as revenue " +
                                      "FROM paiement " +
                                      "WHERE date_paiement BETWEEN ? AND ? " +
                                      "AND inscription_id IS NOT NULL " +
                                      "GROUP BY 'Formation'";
                
                try (PreparedStatement pstmt = conn.prepareStatement(inscriptionSql)) {
                    pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                    pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double revenue = rs.getDouble("revenue");
                            if (revenue > 0) {
                                serviceRevenues.put("Formation", revenue);
                                hasData = true;
                            }
                        }
                    }
                } catch (SQLException e) {

                    e.printStackTrace();
                }
                
                String examenSql = "SELECT 'Examen' as service, SUM(montant) as revenue " +
                                 "FROM paiement " +
                                 "WHERE date_paiement BETWEEN ? AND ? " +
                                 "AND id_examen IS NOT NULL " +
                                 "GROUP BY 'Examen'";
                
                try (PreparedStatement pstmt = conn.prepareStatement(examenSql)) {
                    pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                    pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double revenue = rs.getDouble("revenue");
                            if (revenue > 0) {
                                serviceRevenues.put("Examen", revenue);
                                hasData = true;
                            }
                        }
                    }
                } catch (SQLException e) {

                    e.printStackTrace();
                }
                
                String otherSql = "SELECT 'Autre service' as service, SUM(montant) as revenue " +
                                "FROM paiement " +
                                "WHERE date_paiement BETWEEN ? AND ? " +
                                "AND inscription_id IS NULL " +
                                "AND id_examen IS NULL " +
                                "GROUP BY 'Autre service'";
                
                try (PreparedStatement pstmt = conn.prepareStatement(otherSql)) {
                    pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                    pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double revenue = rs.getDouble("revenue");
                            if (revenue > 0) {
                                serviceRevenues.put("Autre service", revenue);
                                hasData = true;
                            }
                        }
                    }
                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }

            if (!hasData) {
                String totalSql = "SELECT SUM(montant) as total_revenue " +
                                "FROM paiement " +
                                "WHERE date_paiement BETWEEN ? AND ?";
                
                try (PreparedStatement pstmt = conn.prepareStatement(totalSql)) {
                    pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                    pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                    
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            double totalRevenue = rs.getDouble("total_revenue");
                            if (totalRevenue > 0) {

                                serviceRevenues.put("Formation", totalRevenue * 0.55);  // 55%
                                serviceRevenues.put("Conduite", totalRevenue * 0.30);   // 30%
                                serviceRevenues.put("Examen", totalRevenue * 0.15);     // 15%
                                hasData = true;
                            }
                        }
                    }
                }
            }
        }

        if (!serviceRevenues.isEmpty()) {
            serviceRevenues.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    serviceSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                });
        } else {

            serviceSeries.getData().add(new XYChart.Data<>("Aucune donnée", 0));
        }

        Platform.runLater(() -> {
            revenueByServiceChart.getData().clear();
            revenueByServiceChart.getData().add(serviceSeries);
        });
    }
    
    private void loadMonthlyComparisonData() throws SQLException {
        XYChart.Series<String, Number> revenueMonthlySeries = new XYChart.Series<>();
        revenueMonthlySeries.setName("Revenus");
        
        XYChart.Series<String, Number> expenseMonthlySeries = new XYChart.Series<>();
        expenseMonthlySeries.setName("Dépenses");
        
        XYChart.Series<String, Number> profitMonthlySeries = new XYChart.Series<>();
        profitMonthlySeries.setName("Profit Net");

        Map<String, Double> monthlyRevenues = new HashMap<>();
        Map<String, Double> monthlyExpenses = new HashMap<>();
        
        try (Connection conn = DatabaseConfig.getConnection()) {

            String revenueSql = "SELECT DATE_FORMAT(date_paiement, '%Y-%m') as yearMonth, " +
                              "DATE_FORMAT(date_paiement, '%b') as monthName, " +
                              "SUM(montant) as revenue " +
                              "FROM paiement " +
                              "WHERE date_paiement >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) " +
                              "GROUP BY yearMonth, monthName " +
                              "ORDER BY yearMonth";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(revenueSql)) {
                while (rs.next()) {
                    String monthName = rs.getString("monthName");
                    double revenue = rs.getDouble("revenue");
                    monthlyRevenues.put(monthName, revenue);
                }
            }

            String expensesSql = "SELECT DATE_FORMAT(date_entretien, '%Y-%m') as yearMonth, " +
                               "DATE_FORMAT(date_entretien, '%b') as monthName, " +
                               "SUM(cout) as expenses " +
                               "FROM entretien " +
                               "WHERE date_entretien >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) " +
                               "GROUP BY yearMonth, monthName " +
                               "ORDER BY yearMonth";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(expensesSql)) {
                while (rs.next()) {
                    String monthName = rs.getString("monthName");
                    double expenses = rs.getDouble("expenses");
                    monthlyExpenses.put(monthName, expenses);
                }
            }

            String monitorCountSql = "SELECT COUNT(*) as total FROM moniteur";
            double salaireMoyen = 1000.0; // Average salary per monitor per month
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(monitorCountSql)) {
                if (rs.next()) {
                    int monitorCount = rs.getInt("total");
                    if (monitorCount > 0) {
                        double monthlyMonitorSalaries = monitorCount * salaireMoyen;

                        LocalDate now = LocalDate.now();
                        for (int i = 0; i < 4; i++) {
                            LocalDate monthDate = now.minusMonths(i);
                            String monthName = monthDate.getMonth().toString().substring(0, 3);

                            monthlyExpenses.merge(monthName, monthlyMonitorSalaries, Double::sum);
                        }
                    }
                }
            }

            double monthlyRent = 1200.0;
            LocalDate now = LocalDate.now();
            for (int i = 0; i < 4; i++) {
                LocalDate monthDate = now.minusMonths(i);
                String monthName = monthDate.getMonth().toString().substring(0, 3);

                monthlyExpenses.merge(monthName, monthlyRent, Double::sum);
            }
        }

        Set<String> allMonths = new HashSet<>();
        allMonths.addAll(monthlyRevenues.keySet());
        allMonths.addAll(monthlyExpenses.keySet());

        LocalDate now = LocalDate.now();
        Map<String, Integer> monthOrder = new HashMap<>();
        List<String> monthNames = new ArrayList<>();
        
        for (int i = 3; i >= 0; i--) { // Show last 4 months in order
            LocalDate monthDate = now.minusMonths(i);
            String monthName = monthDate.getMonth().toString().substring(0, 3);
            monthNames.add(monthName);

            allMonths.add(monthName);

            monthOrder.put(monthName, 3-i); // 0,1,2,3 order value
        }

        for (String month : monthNames) {
            double revenue = monthlyRevenues.getOrDefault(month, 0.0);
            double expenses = monthlyExpenses.getOrDefault(month, 0.0);
            double profit = revenue - expenses;
            
            revenueMonthlySeries.getData().add(new XYChart.Data<>(month, revenue));
            expenseMonthlySeries.getData().add(new XYChart.Data<>(month, -expenses)); // Negate expenses for display
            profitMonthlySeries.getData().add(new XYChart.Data<>(month, profit));
        }

        Platform.runLater(() -> {
            monthlyComparisonChart.getData().clear();
            monthlyComparisonChart.getData().addAll(revenueMonthlySeries, expenseMonthlySeries, profitMonthlySeries);
        });
    }
    
    private void setupTransactionsTable() {

        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        transactionAmountColumn.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", amount));

                    if (amount < 0) {
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71;");
                    }
                }
            }
        });

        transactionTypeColumn.setCellFactory(col -> new TableCell<Transaction, String>() {
            @Override
            protected void updateItem(String type, boolean empty) {
                super.updateItem(type, empty);
                if (empty || type == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(type);
                    if ("Revenu".equals(type)) {
                        setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
                    } else if ("Dépense".equals(type)) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }
    
    private void loadTransactionsData() throws SQLException {
        ObservableList<Transaction> data = FXCollections.observableArrayList();
        
        try (Connection conn = DatabaseConfig.getConnection()) {

            String revenueSql = "SELECT DATE_FORMAT(p.date_paiement, '%d/%m/%Y') as date, " +
                              "'Revenu' as type, " +
                              "CASE " +
                              "  WHEN i.id IS NOT NULL THEN CONCAT('Paiement inscription - ', COALESCE(c.nom, ''), ' ', COALESCE(c.prenom, '')) " +
                              "  WHEN p.id_examen IS NOT NULL THEN CONCAT('Paiement examen - ', COALESCE(c.nom, ''), ' ', COALESCE(c.prenom, '')) " +
                              "  ELSE CONCAT('Paiement - ', COALESCE(c.nom, ''), ' ', COALESCE(c.prenom, '')) " +
                              "END as description, " +
                              "p.montant as amount, " +
                              "'Complété' as status " +
                              "FROM paiement p " +
                              "LEFT JOIN candidat c ON p.id_candidat = c.id " +
                              "LEFT JOIN inscription i ON p.inscription_id = i.id " +
                              "WHERE p.date_paiement BETWEEN ? AND ? " +
                              "ORDER BY p.date_paiement DESC " +
                              "LIMIT 20";

            String expensesSql = "SELECT DATE_FORMAT(e.date_entretien, '%d/%m/%Y') as date, " +
                               "'Dépense' as type, " +
                               "CONCAT(COALESCE(e.type_entretien, 'Entretien'), ' - ', COALESCE(v.marque, ''), ' ', COALESCE(v.modele, ''), IF(v.immatriculation IS NOT NULL, CONCAT(' (', v.immatriculation, ')'), '')) as description, " +
                               "-e.cout as amount, " +
                               "CASE WHEN e.statut = 1 THEN 'Complété' ELSE 'En attente' END as status " +
                               "FROM entretien e " +
                               "LEFT JOIN vehicule v ON e.vehicule_id = v.id " +
                               "WHERE e.date_entretien BETWEEN ? AND ? " +
                               "ORDER BY e.date_entretien DESC " +
                               "LIMIT 20";

            try (PreparedStatement pstmt = conn.prepareStatement(revenueSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        data.add(new Transaction(
                            rs.getString("date"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }

            try (PreparedStatement pstmt = conn.prepareStatement(expensesSql)) {
                pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
                pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
                
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        data.add(new Transaction(
                            rs.getString("date"),
                            rs.getString("type"),
                            rs.getString("description"),
                            rs.getDouble("amount"),
                            rs.getString("status")
                        ));
                    }
                }
            }

            LocalDate now = LocalDate.now();
            LocalDate currentMonth = now.withDayOfMonth(now.lengthOfMonth());

            if ((currentMonth.isAfter(filterStartDate) && currentMonth.isBefore(filterEndDate)) 
                    || currentMonth.isEqual(filterStartDate) || currentMonth.isEqual(filterEndDate)) {

                data.add(new Transaction(
                    currentMonth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    "Dépense",
                    "Loyer mensuel",
                    -1200.0, // Fixed monthly rent
                    "Complété"
                ));

                String monitorSql = "SELECT id, nom, prenom FROM moniteur";
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(monitorSql)) {
                    
                    while (rs.next()) {
                        String nom = rs.getString("nom");
                        String prenom = rs.getString("prenom");
                        
                        data.add(new Transaction(
                            currentMonth.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            "Dépense",
                            "Salaire - " + prenom + " " + nom,
                            -1000.0, // Average salary
                            "Complété"
                        ));
                    }
                }
            }
        }


        if (!data.isEmpty()) {
            data.sort((t1, t2) -> {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate d1 = LocalDate.parse(t1.getDate(), formatter);
                LocalDate d2 = LocalDate.parse(t2.getDate(), formatter);
                return d2.compareTo(d1);
            });

            if (data.size() > 10) {
                data = FXCollections.observableArrayList(data.subList(0, 10));
            }
        }

        ObservableList<Transaction> finalData = data;
        Platform.runLater(() -> {
            transactionsTable.setItems(finalData);
        });
    }

    public static class Transaction {
        private final String date;
        private final String type;
        private final String description;
        private final double amount;
        private final String status;
        
        public Transaction(String date, String type, String description, double amount, String status) {
            this.date = date;
            this.type = type;
            this.description = description;
            this.amount = amount;
            this.status = status;
        }
        
        public String getDate() { return date; }
        public String getType() { return type; }
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public String getStatus() { return status; }
    }
} 
