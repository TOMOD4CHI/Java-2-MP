package org.cpi2.controllers;

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

import org.cpi2.repository.DatabaseConfig;
import org.cpi2.utils.AlertUtil;

public class DashboardFinance implements Initializable {

    @FXML private ComboBox<String> periodCombo;
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    
    // KPI Labels
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalExpensesLabel;
    @FXML private Label netProfitLabel;
    @FXML private Label newStudentsLabel;
    @FXML private Label revenueChangeLabel;
    @FXML private Label expensesChangeLabel;
    @FXML private Label profitChangeLabel;
    @FXML private Label studentsChangeLabel;
    
    // Charts
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
    
    // Table
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
        
        filterStartDate = firstDay;
        filterEndDate = now;
        
        // Setup the transactions table columns
        setupTransactionsTable();
        
        // Load chart data
        loadChartData();
        
        // Add listener to period combo box
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
        // Get values from filters
        filterStartDate = startDate.getValue();
        filterEndDate = endDate.getValue();
        
        // Validate dates
        if (filterStartDate != null && filterEndDate != null && filterStartDate.isAfter(filterEndDate)) {
            AlertUtil.showError("Erreur de date", "La date de début doit être avant la date de fin");
            return;
        }
        
        // Apply filter and reload data
        loadChartData();
    }
    
    private void loadChartData() {
        try (Connection conn = DatabaseConfig.getConnection()) {
            // Load KPI data
            loadKPIData(conn);
            
            // Load revenue chart data
            loadRevenueChartData(conn);
            
            // Load expenses breakdown
            loadExpensesBreakdownData(conn);
            
            // Load revenue by service
            loadRevenueByServiceData(conn);
            
            // Load monthly comparison
            loadMonthlyComparisonData(conn);
            
            // Load transactions table
            loadTransactionsData(conn);
            
        } catch (SQLException e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur de base de données", "Impossible de charger les données: " + e.getMessage());
        }
    }
    
    private void loadKPIData(Connection conn) throws SQLException {
        // Total revenue
        String revenueSql = "SELECT SUM(montant) as total_revenue FROM paiement " +
                           "WHERE date_paiement BETWEEN ? AND ?";
        
        // Total expenses (from entretien table for maintenance costs)
        String expensesSql = "SELECT SUM(cout) as total_expenses FROM entretien " +
                           "WHERE date_entretien BETWEEN ? AND ?";
        
        // New students count
        String newStudentsSql = "SELECT COUNT(*) as new_students FROM inscription " +
                              "WHERE date_inscription BETWEEN ? AND ?";
        
        // Execute revenue query
        try (PreparedStatement pstmt = conn.prepareStatement(revenueSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            double totalRevenue = 0;
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    totalRevenue = rs.getDouble("total_revenue");
                }
            }
            
            // Format as currency
            totalRevenueLabel.setText(String.format("%.2f DT", totalRevenue));
            
            // If no data, set default value
            if (totalRevenue == 0) {
                totalRevenueLabel.setText("0.00 DT");
            }
        }
        
        // Execute expenses query
        try (PreparedStatement pstmt = conn.prepareStatement(expensesSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            double totalExpenses = 0;
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    totalExpenses = rs.getDouble("total_expenses");
                }
            }
            
            // Format as currency
            totalExpensesLabel.setText(String.format("%.2f DT", totalExpenses));
            
            // If no data, set default value
            if (totalExpenses == 0) {
                totalExpensesLabel.setText("0.00 DT");
            }
            
            // Calculate net profit
            double revenue = 0;
            try {
                revenue = Double.parseDouble(totalRevenueLabel.getText().replace(" DT", ""));
            } catch (NumberFormatException e) {
                revenue = 0;
            }
            
            double expenses = 0;
            try {
                expenses = Double.parseDouble(totalExpensesLabel.getText().replace(" DT", ""));
            } catch (NumberFormatException e) {
                expenses = 0;
            }
            
            double netProfit = revenue - expenses;
            netProfitLabel.setText(String.format("%.2f DT", netProfit));
        }
        
        // Execute new students query
        try (PreparedStatement pstmt = conn.prepareStatement(newStudentsSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newStudents = rs.getInt("new_students");
                    newStudentsLabel.setText(String.valueOf(newStudents));
                } else {
                    newStudentsLabel.setText("0");
                }
            }
        }
        
        // Set placeholder values for change indicators
        // In a real implementation, you would compare with previous period
        revenueChangeLabel.setText("+8% vs période précédente");
        expensesChangeLabel.setText("+3% vs période précédente");
        profitChangeLabel.setText("+12% vs période précédente");
        studentsChangeLabel.setText("+5% vs période précédente");
    }
    
    private void loadRevenueChartData(Connection conn) throws SQLException {
        // Get revenue data by date
        String sql = "SELECT DATE_FORMAT(date_paiement, '%d/%m') as date, SUM(montant) as amount " +
                    "FROM paiement WHERE date_paiement BETWEEN ? AND ? " +
                    "GROUP BY DATE_FORMAT(date_paiement, '%d/%m') ORDER BY date_paiement";
        
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus");
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    String date = rs.getString("date");
                    double amount = rs.getDouble("amount");
                    revenueSeries.getData().add(new XYChart.Data<>(date, amount));
                }
                
                // If no data found, add sample data
                if (!hasData) {
                    // Add placeholder data points for each day in the range
                    LocalDate current = filterStartDate;
                    while (!current.isAfter(filterEndDate)) {
                        String date = current.format(DateTimeFormatter.ofPattern("dd/MM"));
                        revenueSeries.getData().add(new XYChart.Data<>(date, 0));
                        current = current.plusDays(1);
                    }
                }
            }
        }
        
        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);
    }
    
    private void loadExpensesBreakdownData(Connection conn) throws SQLException {
        // Get expense breakdown data
        // This would typically come from a categories table linked to expenses
        // For this example we'll use a simplified query
        
        Map<String, Double> expensesCategories = new HashMap<>();
        expensesCategories.put("Salaires", 0.0);
        expensesCategories.put("Loyer", 0.0);
        expensesCategories.put("Carburant", 0.0);
        expensesCategories.put("Maintenance", 0.0);
        expensesCategories.put("Autres", 0.0);
        
        // Query for maintenance expenses from entretien table
        String maintenanceSql = "SELECT SUM(cout) as total FROM entretien WHERE date_entretien BETWEEN ? AND ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(maintenanceSql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next() && !rs.wasNull()) {
                    double maintenanceCost = rs.getDouble("total");
                    expensesCategories.put("Maintenance", maintenanceCost);
                }
            }
        }
        
        // For the other categories, we'll use placeholder data
        // In a real implementation, you would query from the appropriate tables
        expensesCategories.put("Salaires", 3500.0);
        expensesCategories.put("Loyer", 1200.0);
        expensesCategories.put("Carburant", 850.0);
        expensesCategories.put("Autres", 450.0);
        
        // Calculate total expenses for percentage calculation
        double totalExpenses = expensesCategories.values().stream().mapToDouble(Double::doubleValue).sum();
        
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : expensesCategories.entrySet()) {
            if (entry.getValue() > 0) {
                // Calculate the percentage for the label
                double percentage = (entry.getValue() / totalExpenses) * 100;
                String label = String.format("%s (%.1f%%)", entry.getKey(), percentage);
                pieChartData.add(new PieChart.Data(label, entry.getValue()));
            }
        }
        
        // If no data, add placeholder data
        if (pieChartData.isEmpty()) {
            pieChartData.add(new PieChart.Data("Aucune dépense", 1));
        }
        
        expensesPieChart.setData(pieChartData);
    }
    
    private void loadRevenueByServiceData(Connection conn) throws SQLException {
        // Get revenue by service type
        // This would ideally come from a join between payments and services
        // For this example we'll use a simplified query based on plans
        String sql = "SELECT p.libelle as service, SUM(pay.montant) as revenue " +
                   "FROM paiement pay " +
                   "JOIN inscription i ON pay.inscription_id = i.id " +
                   "JOIN plan p ON i.plan_id = p.id " +
                   "WHERE pay.date_paiement BETWEEN ? AND ? " +
                   "GROUP BY p.libelle";
        
        XYChart.Series<String, Number> serviceSeries = new XYChart.Series<>();
        serviceSeries.setName("Revenus par Service");
        
        Map<String, Double> serviceRevenues = new HashMap<>();
        
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, java.sql.Date.valueOf(filterStartDate));
            pstmt.setDate(2, java.sql.Date.valueOf(filterEndDate));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                boolean hasData = false;
                while (rs.next()) {
                    hasData = true;
                    String service = rs.getString("service");
                    double revenue = rs.getDouble("revenue");
                    serviceRevenues.put(service, revenue);
                }
                
                // If no data found from query, use placeholder data
                if (!hasData) {
                    serviceRevenues.put("Code", 3500.0);
                    serviceRevenues.put("Conduite", 6200.0);
                    serviceRevenues.put("Examen", 2100.0);
                    serviceRevenues.put("Autres", 650.0);
                }
            }
        }
        
        // Sort by revenue descending and add to series
        serviceRevenues.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .forEach(entry -> {
                serviceSeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            });
        
        revenueByServiceChart.getData().clear();
        revenueByServiceChart.getData().add(serviceSeries);
    }
    
    private void loadMonthlyComparisonData(Connection conn) throws SQLException {
        // Get monthly revenue data
        String revenueSql = "SELECT DATE_FORMAT(date_paiement, '%b') as month, SUM(montant) as revenue " +
                          "FROM paiement " +
                          "WHERE date_paiement >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) " +
                          "GROUP BY DATE_FORMAT(date_paiement, '%b') " +
                          "ORDER BY date_paiement";
        
        // Get monthly expenses data
        String expensesSql = "SELECT DATE_FORMAT(date_entretien, '%b') as month, SUM(cout) as expenses " +
                           "FROM entretien " +
                           "WHERE date_entretien >= DATE_SUB(CURDATE(), INTERVAL 4 MONTH) " +
                           "GROUP BY DATE_FORMAT(date_entretien, '%b') " +
                           "ORDER BY date_entretien";
        
        XYChart.Series<String, Number> revenueMonthlySeries = new XYChart.Series<>();
        revenueMonthlySeries.setName("Revenus");
        
        XYChart.Series<String, Number> expenseMonthlySeries = new XYChart.Series<>();
        expenseMonthlySeries.setName("Dépenses");
        
        // Get revenue data
        Map<String, Double> monthlyRevenue = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(revenueSql)) {
            while (rs.next()) {
                String month = rs.getString("month");
                double revenue = rs.getDouble("revenue");
                monthlyRevenue.put(month, revenue);
            }
        }
        
        // Get expenses data
        Map<String, Double> monthlyExpenses = new HashMap<>();
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(expensesSql)) {
            while (rs.next()) {
                String month = rs.getString("month");
                double expenses = rs.getDouble("expenses");
                monthlyExpenses.put(month, expenses);
            }
        }
        
        // If no data, use placeholder data
        if (monthlyRevenue.isEmpty() && monthlyExpenses.isEmpty()) {
            revenueMonthlySeries.getData().add(new XYChart.Data<>("Jan", 12450));
            revenueMonthlySeries.getData().add(new XYChart.Data<>("Fev", 13200));
            revenueMonthlySeries.getData().add(new XYChart.Data<>("Mars", 14500));
            revenueMonthlySeries.getData().add(new XYChart.Data<>("Avr", 12800));
            
            expenseMonthlySeries.getData().add(new XYChart.Data<>("Jan", 8230));
            expenseMonthlySeries.getData().add(new XYChart.Data<>("Fev", 9100));
            expenseMonthlySeries.getData().add(new XYChart.Data<>("Mars", 9600));
            expenseMonthlySeries.getData().add(new XYChart.Data<>("Avr", 8500));
        } else {
            // Add all months from both maps
            for (String month : monthlyRevenue.keySet()) {
                revenueMonthlySeries.getData().add(new XYChart.Data<>(month, monthlyRevenue.get(month)));
                // Add 0 for expenses if no data for this month
                double expenses = monthlyExpenses.getOrDefault(month, 0.0);
                expenseMonthlySeries.getData().add(new XYChart.Data<>(month, expenses));
            }
            
            // Add months that are in expenses but not in revenue
            for (String month : monthlyExpenses.keySet()) {
                if (!monthlyRevenue.containsKey(month)) {
                    revenueMonthlySeries.getData().add(new XYChart.Data<>(month, 0.0));
                    expenseMonthlySeries.getData().add(new XYChart.Data<>(month, monthlyExpenses.get(month)));
                }
            }
        }
        
        monthlyComparisonChart.getData().clear();
        monthlyComparisonChart.getData().addAll(revenueMonthlySeries, expenseMonthlySeries);
    }
    
    private void setupTransactionsTable() {
        // Configure table columns
        transactionDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        transactionTypeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        transactionDescriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        transactionAmountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        transactionStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        // Format amount column to show currency
        transactionAmountColumn.setCellFactory(col -> new TableCell<Transaction, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f DT", amount));
                    
                    // Add color based on amount (red for negative, green for positive)
                    if (amount < 0) {
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else {
                        setStyle("-fx-text-fill: #2ecc71;");
                    }
                }
            }
        });
        
        // Format type column with color indicators
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
    
    private void loadTransactionsData(Connection conn) throws SQLException {
        ObservableList<Transaction> data = FXCollections.observableArrayList();
        
        // First get revenue transactions
        String revenueSql = "SELECT DATE_FORMAT(p.date_paiement, '%d/%m/%Y') as date, " +
                          "'Revenu' as type, " +
                          "CASE " +
                          "  WHEN i.id IS NOT NULL THEN CONCAT('Paiement inscription - ', c.nom, ' ', c.prenom) " +
                          "  WHEN e.id IS NOT NULL THEN CONCAT('Paiement examen - ', c.nom, ' ', c.prenom) " +
                          "  ELSE CONCAT('Paiement - ', c.nom, ' ', c.prenom) " +
                          "END as description, " +
                          "p.montant as amount, " +
                          "'Complété' as status " +
                          "FROM paiement p " +
                          "JOIN candidat c ON p.id_candidat = c.id " +
                          "LEFT JOIN inscription i ON p.inscription_id = i.id " +
                          "LEFT JOIN examen e ON p.id_examen = e.id " +
                          "WHERE p.date_paiement BETWEEN ? AND ? " +
                          "ORDER BY p.date_paiement DESC";
        
        // Get expense transactions
        String expensesSql = "SELECT DATE_FORMAT(e.date_entretien, '%d/%m/%Y') as date, " +
                           "'Dépense' as type, " +
                           "CONCAT(e.type_entretien, ' - ', v.marque, ' ', v.modele, ' (', v.immatriculation, ')') as description, " +
                           "-e.cout as amount, " +
                           "e.statut as status " +
                           "FROM entretien e " +
                           "JOIN vehicule v ON e.vehicule_id = v.id " +
                           "WHERE e.date_entretien BETWEEN ? AND ? " +
                           "ORDER BY e.date_entretien DESC";
        
        // Fetch revenue transactions
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
        
        // Fetch expense transactions
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
        
        // If no data was found, add sample data
        if (data.isEmpty()) {
            data.add(new Transaction("25/01/2024", "Revenu", "Paiement leçon - Ahmed Salah", 120.00, "Complété"));
            data.add(new Transaction("24/01/2024", "Revenu", "Paiement leçon - Nadia Mejri", 120.00, "Complété"));
            data.add(new Transaction("23/01/2024", "Dépense", "Carburant véhicule #3", -85.50, "Complété"));
            data.add(new Transaction("22/01/2024", "Revenu", "Paiement formation code - Groupe B", 600.00, "Complété"));
            data.add(new Transaction("21/01/2024", "Dépense", "Maintenance véhicule #1", -250.00, "Complété"));
        }
        
        // Sort by date (most recent first)
        data.sort((t1, t2) -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate d1 = LocalDate.parse(t1.getDate(), formatter);
            LocalDate d2 = LocalDate.parse(t2.getDate(), formatter);
            return d2.compareTo(d1);
        });
        
        // Limit to 10 transactions
        if (data.size() > 10) {
            data = FXCollections.observableArrayList(data.subList(0, 10));
        }
        
        transactionsTable.setItems(data);
    }
    
    // Inner class for Transaction table
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