package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import org.cpi2.entities.Candidat;
import org.cpi2.service.ProgressionService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.ProgressionReportGenerator;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


public class Progression {

    @FXML private ComboBox<CandidatItem> candidatComboBox;
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
    
    @FXML private PieChart progressionPieChart;
    @FXML private BarChart<String, Number> progressionBarChart;
    
    @FXML private ComboBox<String> periodeComboBox;
    
    private final ProgressionService progressionService = new ProgressionService();
    private Long currentCandidatId = null;
    private Map<Long, String> candidatsMap = new HashMap<>();

    @FXML
    public void initialize() {

        periodeComboBox.getItems().addAll("Semaine", "Mois", "Année");
        periodeComboBox.setValue("Mois");

        periodeComboBox.setOnAction(event -> {
            if (currentCandidatId != null) {
                updateChartsByPeriod(periodeComboBox.getValue());
            }
        });

        setupCandidatComboBox();

        clearFields();
    }
    
    private void setupCandidatComboBox() {

        candidatsMap = progressionService.getAllCandidatsForComboBox();
        ObservableList<CandidatItem> candidatItems = FXCollections.observableArrayList();

        for (Map.Entry<Long, String> entry : candidatsMap.entrySet()) {
            candidatItems.add(new CandidatItem(entry.getKey(), entry.getValue()));
        }

        candidatComboBox.setItems(candidatItems);

        candidatComboBox.setConverter(new StringConverter<CandidatItem>() {
            @Override
            public String toString(CandidatItem item) {
                return item != null ? item.getDisplayName() : "";
            }
            
            @Override
            public CandidatItem fromString(String string) {
                return null; // Not needed for combo box
            }
        });

        candidatComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                currentCandidatId = newVal.getId();
                loadCandidatInfo(currentCandidatId);
            }
        });
    }

    @FXML
    private void rechercheCandidatAction() {
        CandidatItem selectedCandidat = candidatComboBox.getValue();
        if (selectedCandidat == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner un candidat");
            return;
        }
        
        currentCandidatId = selectedCandidat.getId();
        loadCandidatInfo(currentCandidatId);
    }
    

    @FXML
    private void exportPdfAction() {

        if (currentCandidatId == null) {
            AlertUtil.showError("Erreur", "Veuillez d'abord charger les données d'un candidat");
            return;
        }
        
        try {

            Map<String, Object> progressionData = new HashMap<>();

            progressionData.put("nom", nomField.getText());
            progressionData.put("prenom", prenomField.getText());
            progressionData.put("typePermis", typePermisField.getText());
            progressionData.put("dateInscription", dateInscriptionLabel.getText());
            progressionData.put("statut", statutLabel.getText());

            progressionData.put("totalSeancesCode", Integer.parseInt(totalSeancesCodeLabel.getText()));
            progressionData.put("totalSeancesConduite", Integer.parseInt(totalSeancesConduiteLabel.getText()));
            progressionData.put("seancesCodeCompletes", Integer.parseInt(seancesCodeCompletesLabel.getText()));
            progressionData.put("seancesConduiteCompletes", Integer.parseInt(seancesConduiteCompletesLabel.getText()));

            double progressionTotale = progressionTotaleBar.getProgress();
            progressionData.put("progressionTotale", progressionTotale);

            String pdfPath = ProgressionReportGenerator.generateProgressionReport(progressionData);
            
            if (pdfPath != null) {

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText("Rapport de progression généré");
                alert.setContentText("Le rapport a été enregistré sous:\n" + pdfPath);

                ButtonType openFileButton = new ButtonType("Ouvrir le fichier");
                ButtonType openDirButton = new ButtonType("Ouvrir le dossier");
                ButtonType closeButton = ButtonType.CLOSE;
                
                alert.getButtonTypes().setAll(openFileButton, openDirButton, closeButton);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == openFileButton) {

                        File pdfFile = new File(pdfPath);
                        if (pdfFile.exists()) {
                            Desktop.getDesktop().open(pdfFile);
                        }
                    } else if (result.get() == openDirButton) {

                        File pdfDirectory = new File(pdfPath).getParentFile();
                        if (pdfDirectory.exists()) {
                            Desktop.getDesktop().open(pdfDirectory);
                        }
                    }
                }
            } else {
                AlertUtil.showError("Erreur", "Une erreur est survenue lors de la génération du PDF");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }
    
    private void loadCandidatInfo(Long candidatId) {

        Map<String, Object> progressionData = progressionService.getCandidatProgression(candidatId);
        
        if (progressionData.isEmpty()) {
            AlertUtil.showError("Erreur", "Impossible de charger les données de progression");
            return;
        }

        nomField.setText((String) progressionData.get("nom"));
        prenomField.setText((String) progressionData.get("prenom"));
        typePermisField.setText((String) progressionData.get("typePermis"));
        dateInscriptionLabel.setText((String) progressionData.get("dateInscription"));
        statutLabel.setText((String) progressionData.get("statut"));
        
        totalSeancesCodeLabel.setText(String.valueOf(progressionData.get("totalSeancesCode")));
        totalSeancesConduiteLabel.setText(String.valueOf(progressionData.get("totalSeancesConduite")));
        seancesCodeCompletesLabel.setText(String.valueOf(progressionData.get("seancesCodeCompletes")));
        seancesConduiteCompletesLabel.setText(String.valueOf(progressionData.get("seancesConduiteCompletes")));
        
        double progress = (double) progressionData.get("progressionTotale");
        progressionTotaleBar.setProgress(progress);
        progressionPourcentageLabel.setText(String.format("%.1f%%", progress * 100));

        updateCharts(candidatId);
    }

    private void updateCharts(Long candidatId) {

        int seancesCodeCompletes = Integer.parseInt(seancesCodeCompletesLabel.getText());
        int totalSeancesCode = Integer.parseInt(totalSeancesCodeLabel.getText());
        int seancesCodeRestantes = totalSeancesCode - seancesCodeCompletes;
        
        int seancesConduiteCompletes = Integer.parseInt(seancesConduiteCompletesLabel.getText());
        int totalSeancesConduite = Integer.parseInt(totalSeancesConduiteLabel.getText());
        int seancesConduiteRestantes = totalSeancesConduite - seancesConduiteCompletes;
        
        progressionPieChart.getData().clear();
        progressionPieChart.getData().addAll(
            new PieChart.Data("Séances Code Complètes", seancesCodeCompletes),
            new PieChart.Data("Séances Code Restantes", seancesCodeRestantes),
            new PieChart.Data("Séances Conduite Complètes", seancesConduiteCompletes),
            new PieChart.Data("Séances Conduite Restantes", seancesConduiteRestantes)
        );

        updateChartsByPeriod(periodeComboBox.getValue());
    }
    
    private void updateChartsByPeriod(String period) {
        if (currentCandidatId == null) return;
        
        Map<String, Integer> periodData = progressionService.getSessionsByPeriod(currentCandidatId, period);
        
        progressionBarChart.getData().clear();
        
        XYChart.Series<String, Number> codeSeries = new XYChart.Series<>();
        codeSeries.setName("Code");
        
        XYChart.Series<String, Number> drivingSeries = new XYChart.Series<>();
        drivingSeries.setName("Conduite");

        for (Map.Entry<String, Integer> entry : periodData.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            
            if (key.startsWith("Code-")) {
                String label = key.substring(5); // Remove 'Code-' prefix
                codeSeries.getData().add(new XYChart.Data<>(label, value));
            } else if (key.startsWith("Conduite-")) {
                String label = key.substring(9); // Remove 'Conduite-' prefix
                drivingSeries.getData().add(new XYChart.Data<>(label, value));
            }
        }
        
        progressionBarChart.getData().addAll(codeSeries, drivingSeries);
    }
    
    private void clearFields() {
        nomField.clear();
        prenomField.clear();
        typePermisField.clear();
        dateInscriptionLabel.setText("-");
        statutLabel.setText("-");
        
        totalSeancesCodeLabel.setText("0");
        totalSeancesConduiteLabel.setText("0");
        seancesCodeCompletesLabel.setText("0");
        seancesConduiteCompletesLabel.setText("0");
        
        progressionTotaleBar.setProgress(0);
        progressionPourcentageLabel.setText("0.0%");
        
        progressionPieChart.getData().clear();
        progressionBarChart.getData().clear();
        
        currentCandidatId = null;
    }
    
    
    private static class CandidatItem {
        private final Long id;
        private final String displayName;
        
        public CandidatItem(Long id, String displayName) {
            this.id = id;
            this.displayName = displayName;
        }
        
        public Long getId() {
            return id;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}

