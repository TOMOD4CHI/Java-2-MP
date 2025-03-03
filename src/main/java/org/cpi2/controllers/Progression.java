package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.time.LocalDate;

public class Progression {

    @FXML private TextField candidatIdField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField typePermisField;
    @FXML private Label dateInscriptionLabel;
    @FXML private Label statutLabel;
    
    @FXML private Label totalSeancesCodeLabel;
    @FXML private Label totalSeancesConduiteLabel;
    @FXML private Label seancesCodeCompletesLabel;
    @FXML private Label seancesConduiteCompletesLabel;
    
    @FXML private ProgressBar progressionTotaleBar;
    @FXML private Label progressionPourcentageLabel;
    
    @FXML private TableView<Seance> seancesTableView;
    @FXML private TableColumn<Seance, String> typeSeanceColumn;
    @FXML private TableColumn<Seance, LocalDate> dateSeanceColumn;
    @FXML private TableColumn<Seance, String> statutSeanceColumn;
    
    @FXML private PieChart progressionPieChart;
    @FXML private BarChart<String, Number> progressionBarChart;
    
    @FXML private ComboBox<String> periodeComboBox;

    @FXML
    public void initialize() {
        // Initialize the period comboBox
        periodeComboBox.getItems().addAll(
            "Dernière semaine",
            "Dernier mois",
            "Derniers 3 mois",
            "Tout"
        );
        periodeComboBox.getSelectionModel().select("Tout");
        
        // Setup mock data for charts
        setupMockData();
    }

    @FXML
    private void rechercheCandidatAction() {
        // In a real app, this would search for the candidate in the database
        // For this example, we'll just populate with mock data
        if (!candidatIdField.getText().isEmpty()) {
            loadCandidatInfo();
        }
    }
    
    @FXML
    private void refreshChartsAction() {
        // This would refresh the charts based on the selected period
        setupMockData();
    }
    
    @FXML
    private void exportPdfAction() {
        // This would export the progression data to a PDF file
    }
    
    private void loadCandidatInfo() {
        // Mock data
        nomField.setText("Dupont");
        prenomField.setText("Jean");
        typePermisField.setText("B");
        dateInscriptionLabel.setText("15/01/2024");
        statutLabel.setText("EN COURS");
        
        totalSeancesCodeLabel.setText("10");
        totalSeancesConduiteLabel.setText("20");
        seancesCodeCompletesLabel.setText("8");
        seancesConduiteCompletesLabel.setText("12");
        
        double progress = (8.0 + 12.0) / (10.0 + 20.0);
        progressionTotaleBar.setProgress(progress);
        progressionPourcentageLabel.setText(String.format("%.1f%%", progress * 100));
        
        // Update charts
        setupMockData();
    }
    // ce la va etre remplir avec des informations du base de donnees
    private void setupMockData() {
        // Setup Pie Chart data
        progressionPieChart.getData().clear();
        progressionPieChart.getData().addAll(
            new PieChart.Data("Séances Code Complètes", 8),
            new PieChart.Data("Séances Code Restantes", 2),
            new PieChart.Data("Séances Conduite Complètes", 12),
            new PieChart.Data("Séances Conduite Restantes", 8)
        );
        
        // Setup Bar Chart data
        progressionBarChart.getData().clear();
        
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        series1.setName("Code");
        series1.getData().add(new XYChart.Data<>("Planifiées", 10));
        series1.getData().add(new XYChart.Data<>("Complétées", 8));
        
        XYChart.Series<String, Number> series2 = new XYChart.Series<>();
        series2.setName("Conduite");
        series2.getData().add(new XYChart.Data<>("Planifiées", 20));
        series2.getData().add(new XYChart.Data<>("Complétées", 12));
        
        progressionBarChart.getData().addAll(series1, series2);
    }
    
    // Inner class to represent a seance (for TableView)
    public static class Seance {
        private String type;
        private LocalDate date;
        private String statut;
        
        public Seance(String type, LocalDate date, String statut) {
            this.type = type;
            this.date = date;
            this.statut = statut;
        }
        
        public String getType() { return type; }
        public LocalDate getDate() { return date; }
        public String getStatut() { return statut; }
    }
}
