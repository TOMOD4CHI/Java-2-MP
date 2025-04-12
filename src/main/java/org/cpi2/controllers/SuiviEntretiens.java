package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import org.cpi2.entities.Entretien;
import org.cpi2.entities.Vehicule;
import org.cpi2.service.EntretienService;
import org.cpi2.service.VehiculeService;
import org.cpi2.utils.AlertUtil;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class SuiviEntretiens implements Initializable {
    // FXML components
    @FXML private ComboBox<Vehicule> vehiculeComboBox;
    @FXML private Button refreshBtn;

    @FXML private Label immatriculationLabel;
    @FXML private Label marqueModeleLabel;
    @FXML private Label kilometrageLabel;
    @FXML private Label dernierEntretienLabel;

    @FXML private TableView<Entretien> entretiensTable;
    @FXML private TableColumn<Entretien, LocalDate> dateCol;
    @FXML private TableColumn<Entretien, String> typeCol;
    @FXML private TableColumn<Entretien, String> descriptionCol;
    @FXML private TableColumn<Entretien, Double> coutCol;
    @FXML private TableColumn<Entretien, String> statutCol;

    @FXML private Button voirFactureBtn;

    @FXML private DatePicker dateEntretienPicker;
    @FXML private ComboBox<String> typeEntretienCombo;
    @FXML private TextArea descriptionArea;
    @FXML private TextField coutField;
    @FXML private TextField factureField;

    @FXML private Button annulerEntretienBtn;
    @FXML private Button enregistrerEntretienBtn;

    @FXML private Label coutTotalLabel;
    @FXML private Label nbPlanifiesLabel;
    @FXML private Label coutMoyenLabel;

    @FXML private Label statusEntretienLabel;
    @FXML private Label totalEntretiensLabel;

    private final VehiculeService vehiculeService = new VehiculeService();
    private final EntretienService entretienService = new EntretienService();

    private ObservableList<Vehicule> vehicules;
    private ObservableList<Entretien> entretiens;
    private Entretien entretienEnCours;
    private Vehicule selectedVehicule;
    private boolean modeAjout = true;

    /**
     * Initializes the controller.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initializeData();
        setupVehiculeComboBox();
        setupTableView();
        setupTypeEntretienCombo();
        setupButtonStates();
        addListeners();
        updateUIState();
    }

    /**
     * Initializes the data model.
     */
    private void initializeData() {
        vehicules = FXCollections.observableArrayList();
        entretiens = FXCollections.observableArrayList();
        loadSampleData();
    }

    /**
     * Loads sample data for demonstration.
     */
    private void loadSampleData() {
        for(org.cpi2.entities.Vehicule vehicule: vehiculeService.getAllVehicules()) {
            vehicules.add(new Vehicule(vehicule.getImmatriculation(), vehicule.getMarque() +" "+vehicule.getModele(), vehicule.getKilometrageTotal(),vehicule.getDateDerniereVisiteTechnique()));
        }
    }

    /**
     * Sets up the vehicle combo box.
     */
    private void setupVehiculeComboBox() {
        vehiculeComboBox.setItems(vehicules);
        vehiculeComboBox.setCellFactory(param -> new ListCell<Vehicule>() {
            @Override
            protected void updateItem(Vehicule item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getMarqueModele() + " (" + item.getImmatriculation() + ")");
                }
            }
        });
        vehiculeComboBox.setButtonCell(vehiculeComboBox.getCellFactory().call(null));
    }

    /**
     * Sets up the table view.
     */
    private void setupTableView() {
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        coutCol.setCellValueFactory(new PropertyValueFactory<>("cout"));
        statutCol.setCellValueFactory(new PropertyValueFactory<>("statut"));

        entretiensTable.setItems(entretiens);
    }

    /**
     * Sets up the type of maintenance combo box.
     */
    private void setupTypeEntretienCombo() {
        ObservableList<String> types = FXCollections.observableArrayList(
                "Vignette",
                "Assurance",
                "Visite Technique",
                "Vidange",
                "Lavage",
                "Autre Maitenance"
        );
        typeEntretienCombo.setItems(types);
    }

    /**
     * Sets up the initial button states.
     */
    private void setupButtonStates() {
    }

    /**
     * Adds listeners to UI components.
     */
    private void addListeners() {
        vehiculeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectedVehicule = vehiculeComboBox.getSelectionModel().getSelectedItem();
                updateVehiculeInfo(newVal);
                loadEntretiens(newVal);
            }
        });

        entretiensTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            updateButtonsState(newVal != null);
        });

        coutField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                coutField.setText(oldVal);
            }
        });

        refreshBtn.setOnAction(e -> refreshData());
    }

    /**
     * Updates the vehicle information in the UI.
     */
    private void updateVehiculeInfo(Vehicule vehicule) {
        immatriculationLabel.setText(vehicule.getImmatriculation());
        marqueModeleLabel.setText(vehicule.getMarqueModele());
        kilometrageLabel.setText(String.valueOf(vehicule.getKilometrage()) + " km");
        dernierEntretienLabel.setText(vehicule.getDernierEntretien() != null ? vehicule.getDernierEntretien().toString() : "Aucun entretien");
    }

    /**
     * Loads maintenance records for the selected vehicle.
     */
    private void loadEntretiens(Vehicule vehicule) {
        entretiens.clear();
        for(org.cpi2.entities.Entretien entretien: entretienService.getEntretienByVehiculeImm(vehicule.getImmatriculation())) {
            Entretien entretien_table = new Entretien(entretien.getDateEntretien(), entretien.getTypeEntretien(), entretien.getDescription(), entretien.getCout(), entretien.isDone());
            entretien_table.setId(entretien.getId());
            entretiens.add(entretien_table);
        }

        updateStatistics();
        totalEntretiensLabel.setText("Total: " + entretiens.size() + " entretiens");
    }

    /**
     * Updates button states based on selection.
     */
    private void updateButtonsState(boolean hasSelection) {
    }

    /**
     * Updates the UI state.
     */
    private void updateUIState() {
        resetFormFields();
        coutTotalLabel.setText("NA");
        nbPlanifiesLabel.setText("NA");
        statusEntretienLabel.setText("Prêt");
    }

    /**
     * Resets form fields.
     */
    private void resetFormFields() {
        dateEntretienPicker.setValue(LocalDate.now());
        typeEntretienCombo.getSelectionModel().clearSelection();
        descriptionArea.clear();
        coutField.clear();
        factureField.clear();
    }

    /**
     * Updates statistics about maintenance.
     */
    private void updateStatistics() {
        if (entretiens.isEmpty()) {
            return;
        }

        double total = entretiens.stream().filter(entretien -> !entretien.isDone()).mapToDouble(Entretien::getCout).sum();
        int count = entretiens.stream().filter(e -> !e.isDone()).toList().size();

        coutTotalLabel.setText(String.format("%.2f DT", total));
        nbPlanifiesLabel.setText(String.valueOf(count));
    }

    /**
     * Refreshes data from the source.
     */
    private void refreshData() {
        Vehicule selectedVehicule = vehiculeComboBox.getSelectionModel().getSelectedItem();
        if (selectedVehicule != null) {
            loadEntretiens(selectedVehicule);
        }
        updateUIState();
    }

    /**
     * Validates form fields.
     * @return true if all fields are valid, false otherwise
     */
    private boolean validateForm() {
        StringBuilder errorMessage = new StringBuilder();

        if (dateEntretienPicker.getValue() == null) {
            errorMessage.append("- Veuillez sélectionner une date.\n");
        }

        if (typeEntretienCombo.getValue() == null || typeEntretienCombo.getValue().trim().isEmpty()) {
            errorMessage.append("- Veuillez sélectionner un type d'entretien.\n");
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            errorMessage.append("- Veuillez saisir une description.\n");
        }

        if (coutField.getText().trim().isEmpty()) {
            errorMessage.append("- Veuillez saisir un coût.\n");
        } else {
            try {
                Double.parseDouble(coutField.getText());
            } catch (NumberFormatException e) {
                errorMessage.append("- Le coût doit être un nombre valide.\n");
            }
        }

        if (errorMessage.length() > 0) {
            AlertUtil.showError("Erreur de validation", "Veuillez corriger les erreurs suivantes : " + errorMessage.toString());
            return false;
        }

        return true;
    }

    /**
     * Handles adding a new maintenance record.
     */
    @FXML
    public void handleAjouterEntretien(ActionEvent actionEvent) {
        modeAjout = true;
        entretienEnCours = null;
        resetFormFields();

        statusEntretienLabel.setText("Ajout d'un nouvel entretien");
    }

    /**
     * Handles viewing an invoice.
     */
    @FXML
    public void handleVoirFacture(ActionEvent actionEvent) {
    }

    /**
     * Handles choosing an invoice file.
     */
    @FXML
    public void handleChoisirFacture(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner une facture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"),
                new FileChooser.ExtensionFilter("PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(factureField.getScene().getWindow());
        if (file != null) {
            factureField.setText(file.getAbsolutePath());
        }
    }

    /**
     * Handles canceling maintenance record editing.
     */
    @FXML
    public void handleAnnulerEntretien(ActionEvent actionEvent) {
        resetFormFields();

        statusEntretienLabel.setText("Opération annulée");
    }

    /**
     * Handles saving a maintenance record.
     */
    @FXML
    public void handleEnregistrerEntretien(ActionEvent actionEvent) {
        if (!validateForm()) {
            return;
        }
        if(selectedVehicule == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner un véhicule.");
            return;
        }

        try {
            LocalDate date = dateEntretienPicker.getValue();
            String type = typeEntretienCombo.getValue();
            String description = descriptionArea.getText();
            double cout = Double.parseDouble(coutField.getText());
            String facture = factureField.getText();
            boolean success = false;

            if (modeAjout) {
                Entretien nouvelEntretien = new Entretien(date, type, description, cout, (!date.isAfter(LocalDate.now())));
                org.cpi2.entities.Entretien entretien = new org.cpi2.entities.Entretien(vehiculeService.getVehiculeByImmatriculation(selectedVehicule.getImmatriculation()).get().getId(), date, type, selectedVehicule.getKilometrage(), cout, facture,nouvelEntretien.isDone(),description);
                success = entretienService.createEntretien(entretien);
                if (!success) {
                    AlertUtil.showError("Erreur", "Impossible d'ajouter l'entretien.");
                    return;
                }
                statusEntretienLabel.setText("Nouvel entretien ajouté");
            } else if (entretienEnCours != null) {
                Entretien selectedEntretien = entretiensTable.getSelectionModel().getSelectedItem();
                entretienEnCours.setDate(date);
                entretienEnCours.setType(type);
                entretienEnCours.setDescription(description);
                entretienEnCours.setCout(cout);

                entretiensTable.refresh();
                statusEntretienLabel.setText("Entretien modifié");
            }

            refreshData();
            updateStatistics();
            totalEntretiensLabel.setText("Total: " + entretiens.size() + " entretiens");
            resetFormFields();

        } catch (Exception e) {
            AlertUtil.showError("Error","Erreur lors de l'enregistrement\n" + e.getMessage());
        }
        updateStatistics();
        updateVehiculeInfo(Vehicule.from(vehiculeService.getVehiculeByImmatriculation(selectedVehicule.getImmatriculation()).get()));
    }

    /**
     * Handles deleting a maintenance record.
     */
    @FXML
    public void handleSupprimerEntretien(ActionEvent actionEvent) {
        Entretien selectedEntretien = entretiensTable.getSelectionModel().getSelectedItem();
        if (selectedEntretien != null) {
            AlertUtil.showConfirmation( "Confirmer la suppression" , "Êtes-vous sûr de vouloir supprimer cet entretien ?" );
        }
    }

    /**
     * Vehicule class for data model.
     */
    public static class Vehicule {
        private String immatriculation;
        private String marqueModele;
        private int kilometrage;
        private LocalDate dernier_entretien;

        public Vehicule(String immatriculation, String marqueModele, int kilometrage,LocalDate dernier_entretien) {
            this.immatriculation = immatriculation;
            this.marqueModele = marqueModele;
            this.kilometrage = kilometrage;
            this.dernier_entretien=dernier_entretien;
        }

        public String getImmatriculation() {
            return immatriculation;
        }

        public String getMarqueModele() {
            return marqueModele;
        }

        public int getKilometrage() {
            return kilometrage;
        }

        public LocalDate getDernierEntretien() {
            return dernier_entretien;
        }
        public static Vehicule from(org.cpi2.entities.Vehicule vehicule) {
            return new Vehicule(vehicule.getImmatriculation(), vehicule.getMarque() + " " + vehicule.getModele(), vehicule.getKilometrageTotal(),vehicule.getDateDerniereVisiteTechnique());
        }
    }
    public void handleMarquerTermine(ActionEvent actionEvent) {

        Entretien selectedEntretien = entretiensTable.getSelectionModel().getSelectedItem();
        if (selectedEntretien != null && !selectedEntretien.isDone()) {
            if(entretienService.markEntretienAsDone(selectedEntretien.id))
            {
                statusEntretienLabel.setText("Entretien Terminer avec succeés");
                selectedEntretien.setStatut(true);
                entretiensTable.refresh();
                statusEntretienLabel.setText("Terminer");
            }
            else {
                AlertUtil.showError("Erreur","Erreur lors de la modification");
            }
            updateStatistics();
            updateVehiculeInfo(Vehicule.from(vehiculeService.getVehiculeByImmatriculation(selectedVehicule.getImmatriculation()).get()));
        }else{
            AlertUtil.showError("Erreur","Veuillez sélectionner un entretien à marquer comme terminé.");
        }
    }

    /**
     * Entretien (Maintenance) class for data model.
     */
    public static class Entretien {
        private int id;
        private LocalDate date;
        private String type;
        private String description;
        private double cout;
        private String statut;

        public Entretien(LocalDate date, String type, String description, double cout, boolean statut) {
            this.date = date;
            this.type = type;
            this.description = description;
            this.cout = cout;
            this.statut = statut? "Terminer" : "Planifier";
        }
        public int getId() {
            return id;
        }
        public void setId(int id) {
            this.id = id;
        }

        public String getStatut() {
            return statut;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public double getCout() {
            return cout;
        }

        public void setCout(double cout) {
            this.cout = cout;
        }

        public boolean isDone() {
            return Objects.equals(statut, "Terminer");
        }

        public void setStatut(boolean statut) {
            this.statut = statut? "Terminer" : "Planifier";
        }
    }
}