package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import org.cpi2.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ConfirmerSeance implements Initializable {

    @FXML private DatePicker filterDate;
    @FXML private ChoiceBox<String> statusFilter;
    
    @FXML private TableView<SeanceEntry> seancesTable;
    @FXML private TableColumn<SeanceEntry, String> dateColumn;
    @FXML private TableColumn<SeanceEntry, String> heureColumn;
    @FXML private TableColumn<SeanceEntry, String> dureeColumn;
    @FXML private TableColumn<SeanceEntry, String> candidatColumn;
    @FXML private TableColumn<SeanceEntry, String> moniteurColumn;
    @FXML private TableColumn<SeanceEntry, String> vehiculeColumn;
    @FXML private TableColumn<SeanceEntry, String> lieuColumn;
    @FXML private TableColumn<SeanceEntry, String> statusColumn;
    @FXML private TableColumn<SeanceEntry, Void> actionsColumn;
    
    @FXML private VBox detailPane;
    @FXML private Label detailDateLabel;
    @FXML private Label detailHeureLabel;
    @FXML private Label detailDureeLabel;
    @FXML private Label detailCandidatLabel;
    @FXML private Label detailMoniteurLabel;
    @FXML private Label detailVehiculeLabel;
    @FXML private Label detailLieuLabel;
    @FXML private Label detailStatusLabel;
    
    @FXML private ComboBox<String> statusComboBox;
    @FXML private TextArea notesArea;
    @FXML private HBox kilometrageBox;
    @FXML private TextField kmFinField;
    @FXML private Button updateButton;
    
    private ObservableList<SeanceEntry> seanceData = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private SeanceEntry selectedSeance;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        statusFilter.getItems().addAll(
            "Toutes les séances",
            "En attente",
            "Confirmées",
            "Terminées",
            "Annulées"
        );
        statusFilter.setValue("Toutes les séances");
        
        statusComboBox.getItems().addAll(
            "Confirmée",
            "Terminée",
            "Annulée"
        );

        setupTableColumns();
        setupActionsColumn();

        loadSampleData();

        updateButton.setDisable(true);

        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {

            kilometrageBox.setVisible("Terminée".equals(newVal));
            kilometrageBox.setManaged("Terminée".equals(newVal));

            updateButton.setDisable(newVal == null);
        });

        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                filterSeances();
            }
        });
    }
    
    private void setupTableColumns() {
        dateColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDate()));
        
        heureColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getHeure()));
        
        dureeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getDuree() + " min"));
        
        candidatColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCandidat()));
        
        moniteurColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getMoniteur()));
        
        vehiculeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getVehicule()));
        
        lieuColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLieu()));
        
        statusColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getStatus()));

        statusColumn.setCellFactory(column -> new TableCell<SeanceEntry, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(status);
                    
                    switch (status) {
                        case "En attente":
                            setTextFill(Color.ORANGE);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "Confirmée":
                            setTextFill(Color.GREEN);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "Terminée":
                            setTextFill(Color.BLUE);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        case "Annulée":
                            setTextFill(Color.RED);
                            setStyle("-fx-font-weight: bold;");
                            break;
                        default:
                            setTextFill(Color.BLACK);
                            setStyle("");
                    }
                }
            }
        });

        setupActionsColumn();

        seancesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                showDetails(newSelection);
            }
        });

        seancesTable.setItems(seanceData);
    }
    
    private void setupActionsColumn() {
        Callback<TableColumn<SeanceEntry, Void>, TableCell<SeanceEntry, Void>> cellFactory = 
            new Callback<>() {
                @Override
                public TableCell<SeanceEntry, Void> call(final TableColumn<SeanceEntry, Void> param) {
                    return new TableCell<>() {
                        private final Button viewBtn = new Button("Voir");
                        private final Button confirmBtn = new Button("Confirmer");
                        private final HBox pane = new HBox(5, viewBtn, confirmBtn);
                        
                        {
                            viewBtn.getStyleClass().addAll("button", "action-button", "small-button");
                            confirmBtn.getStyleClass().addAll("button", "submit-button", "small-button");
                            pane.setAlignment(Pos.CENTER);
                            
                            viewBtn.setOnAction(event -> {
                                SeanceEntry seance = getTableView().getItems().get(getIndex());
                                showDetails(seance);
                            });
                            
                            confirmBtn.setOnAction(event -> {
                                SeanceEntry seance = getTableView().getItems().get(getIndex());
                                showDetails(seance);
                                statusComboBox.setValue("Confirmée");
                            });
                        }
                        
                        @Override
                        public void updateItem(Void item, boolean empty) {
                            super.updateItem(item, empty);
                            
                            if (empty) {
                                setGraphic(null);
                            } else {
                                SeanceEntry seance = getTableView().getItems().get(getIndex());

                                confirmBtn.setVisible("En attente".equals(seance.getStatus()));
                                confirmBtn.setManaged("En attente".equals(seance.getStatus()));
                                
                                setGraphic(pane);
                            }
                        }
                    };
                }
            };
        
        actionsColumn.setCellFactory(cellFactory);
    }
    
    private void loadSampleData() {

        seanceData.addAll(
            new SeanceEntry("25/01/2024", "10:00", 60, "Ahmed Salah", "Karim Mrad", "Peugeot 208", "Centre Ville", "En attente"),
            new SeanceEntry("25/01/2024", "14:30", 60, "Nadia Mejri", "Hichem Ben Ali", "Renault Clio", "Lac 1", "Confirmée"),
            new SeanceEntry("24/01/2024", "09:00", 90, "Mohamed Karim", "Karim Mrad", "Peugeot 208", "Marsa", "Terminée"),
            new SeanceEntry("26/01/2024", "11:00", 60, "Fatma Ben Salem", "Sarah Mansour", "Volkswagen Golf", "Menzah 6", "En attente"),
            new SeanceEntry("23/01/2024", "16:00", 60, "Sami Ferchichi", "Hichem Ben Ali", "Renault Clio", "Centre Ville", "Annulée"),
            new SeanceEntry("27/01/2024", "08:30", 120, "Lina Trabelsi", "Sarah Mansour", "Volkswagen Golf", "La Soukra", "En attente")
        );
    }
    
    private void showDetails(SeanceEntry seance) {
        selectedSeance = seance;

        detailDateLabel.setText(seance.getDate());
        detailHeureLabel.setText(seance.getHeure());
        detailDureeLabel.setText(seance.getDuree() + " min");
        detailCandidatLabel.setText(seance.getCandidat());
        detailMoniteurLabel.setText(seance.getMoniteur());
        detailVehiculeLabel.setText(seance.getVehicule());
        detailLieuLabel.setText(seance.getLieu());
        detailStatusLabel.setText(seance.getStatus());

        switch (seance.getStatus()) {
            case "En attente":
                detailStatusLabel.setTextFill(Color.ORANGE);
                break;
            case "Confirmée":
                detailStatusLabel.setTextFill(Color.GREEN);
                break;
            case "Terminée":
                detailStatusLabel.setTextFill(Color.BLUE);
                break;
            case "Annulée":
                detailStatusLabel.setTextFill(Color.RED);
                break;
            default:
                detailStatusLabel.setTextFill(Color.BLACK);
        }

        statusComboBox.setValue(null);
        notesArea.clear();
        kmFinField.clear();

        detailPane.setVisible(true);
    }
    
    @FXML
    private void handleApplyFilter() {
        filterSeances();
    }
    
    @FXML
    private void handleResetFilter() {
        filterDate.setValue(null);
        statusFilter.setValue("Toutes les séances");
        filterSeances();
    }
    
    private void filterSeances() {


        LocalDate date = filterDate.getValue();
        String status = statusFilter.getValue();
        
        String message = "Filtrage des séances";
        if (date != null) {
            message += " du " + date.format(dateFormatter);
        }
        
        if (!"Toutes les séances".equals(status)) {
            message += " avec statut: " + status;
        }

        AlertUtil.showInfo( "Filtre appliqué", message);
    }
    
    @FXML
    private void handleClose() {

        detailPane.setVisible(false);

        seancesTable.getSelectionModel().clearSelection();
        selectedSeance = null;
    }
    
    @FXML
    private void handleUpdate() {
        if (selectedSeance == null || statusComboBox.getValue() == null) {
            return;
        }
        
        String newStatus = statusComboBox.getValue();

        if ("Terminée".equals(newStatus) && !validateKilometrage()) {
            return;
        }

        selectedSeance.setStatus(newStatus);

        seancesTable.refresh();

        detailStatusLabel.setText(newStatus);

        switch (newStatus) {
            case "Confirmée":
                detailStatusLabel.setTextFill(Color.GREEN);
                break;
            case "Terminée":
                detailStatusLabel.setTextFill(Color.BLUE);
                break;
            case "Annulée":
                detailStatusLabel.setTextFill(Color.RED);
                break;
            default:
                detailStatusLabel.setTextFill(Color.BLACK);
        }

        AlertUtil.showInfo( "Statut mis à jour",
                "Le statut de la séance a été mis à jour avec succès.");

        statusComboBox.setValue(null);
        notesArea.clear();
        kmFinField.clear();
        updateButton.setDisable(true);
    }
    
    private boolean validateKilometrage() {
        if (kmFinField.getText().trim().isEmpty()) {
            AlertUtil.showError( "Erreur de validation",
                    "Veuillez saisir le kilométrage final du véhicule.");
            return false;
        }
        
        try {
            int km = Integer.parseInt(kmFinField.getText().trim());
            if (km <= 0) {
                AlertUtil.showError( "Erreur de validation",
                        "Le kilométrage doit être un nombre positif.");
                return false;
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur de validation",
                    "Le kilométrage doit être un nombre valide.");
            return false;
        }
        
        return true;
    }

    public static class SeanceEntry {
        private final String date;
        private final String heure;
        private final int duree;
        private final String candidat;
        private final String moniteur;
        private final String vehicule;
        private final String lieu;
        private String status;
        
        public SeanceEntry(String date, String heure, int duree, String candidat, 
                           String moniteur, String vehicule, String lieu, String status) {
            this.date = date;
            this.heure = heure;
            this.duree = duree;
            this.candidat = candidat;
            this.moniteur = moniteur;
            this.vehicule = vehicule;
            this.lieu = lieu;
            this.status = status;
        }
        
        public String getDate() { return date; }
        public String getHeure() { return heure; }
        public int getDuree() { return duree; }
        public String getCandidat() { return candidat; }
        public String getMoniteur() { return moniteur; }
        public String getVehicule() { return vehicule; }
        public String getLieu() { return lieu; }
        public String getStatus() { return status; }
        
        public void setStatus(String status) {
            this.status = status;
        }
    }
} 

