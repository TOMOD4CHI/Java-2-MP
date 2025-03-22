package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

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
        
        // Initialize data
        loadChartData();
        setupTransactionsTable();
    }
    
    @FXML
    private void handleApplyFilter() {
        // Get values from filters
        String period = periodCombo.getValue();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();
        
        // Validate dates
        if (start != null && end != null && start.isAfter(end)) {
            showAlert("Erreur de date", "La date de début doit être avant la date de fin");
            return;
        }
        
        // Apply filter and reload data
        loadChartData();
    }
    
    private void loadChartData() {
        // Load Revenue Chart Data (Area Chart)
        XYChart.Series<String, Number> revenueSeries = new XYChart.Series<>();
        revenueSeries.setName("Revenus");
        
        // Sample data - would be replaced with actual data from database
        revenueSeries.getData().add(new XYChart.Data<>("1 Jan", 2500));
        revenueSeries.getData().add(new XYChart.Data<>("5 Jan", 3200));
        revenueSeries.getData().add(new XYChart.Data<>("10 Jan", 2800));
        revenueSeries.getData().add(new XYChart.Data<>("15 Jan", 3900));
        revenueSeries.getData().add(new XYChart.Data<>("20 Jan", 3600));
        revenueSeries.getData().add(new XYChart.Data<>("25 Jan", 4100));
        revenueSeries.getData().add(new XYChart.Data<>("30 Jan", 4500));
        
        revenueChart.getData().clear();
        revenueChart.getData().add(revenueSeries);
        
        // Load Expense Breakdown (Pie Chart)
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList(
            new PieChart.Data("Salaires", 45),
            new PieChart.Data("Loyer", 20),
            new PieChart.Data("Carburant", 15),
            new PieChart.Data("Maintenance", 10),
            new PieChart.Data("Autres", 10)
        );
        expensesPieChart.setData(pieChartData);
        
        // Load Revenue by Service (Bar Chart)
        XYChart.Series<String, Number> serviceSeries = new XYChart.Series<>();
        serviceSeries.setName("Revenus par Service");
        
        serviceSeries.getData().add(new XYChart.Data<>("Code", 3500));
        serviceSeries.getData().add(new XYChart.Data<>("Conduite", 6200));
        serviceSeries.getData().add(new XYChart.Data<>("Examen", 2100));
        serviceSeries.getData().add(new XYChart.Data<>("Autres", 650));
        
        revenueByServiceChart.getData().clear();
        revenueByServiceChart.getData().add(serviceSeries);
        
        // Load Monthly Comparison (Bar Chart)
        XYChart.Series<String, Number> revenueMonthlySeries = new XYChart.Series<>();
        revenueMonthlySeries.setName("Revenus");
        
        revenueMonthlySeries.getData().add(new XYChart.Data<>("Jan", 12450));
        revenueMonthlySeries.getData().add(new XYChart.Data<>("Fev", 13200));
        revenueMonthlySeries.getData().add(new XYChart.Data<>("Mars", 14500));
        revenueMonthlySeries.getData().add(new XYChart.Data<>("Avr", 12800));
        
        XYChart.Series<String, Number> expenseMonthlySeries = new XYChart.Series<>();
        expenseMonthlySeries.setName("Dépenses");
        
        expenseMonthlySeries.getData().add(new XYChart.Data<>("Jan", 8230));
        expenseMonthlySeries.getData().add(new XYChart.Data<>("Fev", 9100));
        expenseMonthlySeries.getData().add(new XYChart.Data<>("Mars", 9600));
        expenseMonthlySeries.getData().add(new XYChart.Data<>("Avr", 8500));
        
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
                }
            }
        });
        
        // Add sample data
        ObservableList<Transaction> data = FXCollections.observableArrayList(
            new Transaction("25/01/2024", "Revenu", "Paiement leçon - Ahmed Salah", 120.00, "Complété"),
            new Transaction("24/01/2024", "Revenu", "Paiement leçon - Nadia Mejri", 120.00, "Complété"),
            new Transaction("23/01/2024", "Dépense", "Carburant véhicule #3", -85.50, "Complété"),
            new Transaction("22/01/2024", "Revenu", "Paiement formation code - Groupe B", 600.00, "Complété"),
            new Transaction("21/01/2024", "Dépense", "Maintenance véhicule #1", -250.00, "Complété"),
            new Transaction("20/01/2024", "Revenu", "Paiement leçon - Mohamed Karim", 120.00, "Complété"),
            new Transaction("19/01/2024", "Revenu", "Paiement examen pratique - Fatma Ben Salem", 150.00, "Complété"),
            new Transaction("18/01/2024", "Dépense", "Salaires moniteurs", -3200.00, "Complété"),
            new Transaction("17/01/2024", "Dépense", "Loyer local", -1200.00, "Complété"),
            new Transaction("16/01/2024", "Revenu", "Paiement leçon - Sami Ferchichi", 120.00, "Complété")
        );
        
        transactionsTable.setItems(data);
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
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