package org.cpi2.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.SessionCode;
import org.cpi2.utils.AlertUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RemplirSeance {

    // FXML Components
    @FXML private DatePicker dateFilter;
    @FXML private Button searchBtn;
    @FXML private Button resetBtn;

    @FXML private TableView<SessionCode> seancesTable;
    @FXML private TableColumn<SessionCode, String> dateColumn;
    @FXML private TableColumn<SessionCode, String> heureColumn;
    @FXML private TableColumn<SessionCode, String> moniteurColumn;
    @FXML private TableColumn<SessionCode, String> placesColumn;
    @FXML private Label selectedSessionLabel;

    @FXML private TableView<CandidatWrapper> candidatsTable;
    @FXML private TableColumn<CandidatWrapper, Boolean> selectColumn;
    @FXML private TableColumn<CandidatWrapper, String> nomColumn;
    @FXML private TableColumn<CandidatWrapper, String> prenomColumn;

    @FXML private Label selectedCountLabel;
    @FXML private Button selectAllBtn;
    @FXML private Button deselectAllBtn;

    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    // Data
    private ObservableList<SessionCode> seances;
    private FilteredList<SessionCode> filteredSeances;
    private ObservableList<CandidatWrapper> candidats;
    private SessionCode selectedSeance;

    public void handleCancel(ActionEvent actionEvent) {
    }

    public void handleSave(ActionEvent actionEvent) {

    }

    // Wrapper class for Candidat to add checkbox functionality
    public class CandidatWrapper {
        private final Candidat candidat;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public CandidatWrapper(Candidat candidat) {
            this.candidat = candidat;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        public String getNom() {
            return candidat.getNom();
        }

        public String getPrenom() {
            return candidat.getPrenom();
        }

        public int getId() {
            return Math.toIntExact(candidat.getId());
        }
    }

    // Placeholder service methods
    private List<SessionCode> getSeances() {
        // This would come from your service
        List<SessionCode> result = new ArrayList<>();

        // Sample data
        for (int i = 1; i <= 5; i++) {
            SessionCode seance = new SessionCode();
            seance.setId((long) i);
            seance.setDateSession(LocalDate.now().plusDays(i));
            seance.setHeureSession(LocalTime.of(9 + i, 0));

            Moniteur moniteur = new Moniteur();
            moniteur.setId((long) i);
            moniteur.setNom("Nom" + i);
            moniteur.setPrenom("Prénom" + i);
            seance.setMoniteur(moniteur);

            seance.setCapaciteMax(10);


            result.add(seance);
        }

        return result;
    }

    private List<Candidat> getCandidats() {
        // This would come from your service
        List<Candidat> result = new ArrayList<>();

        // Sample data
        for (int i = 1; i <= 10; i++) {
            Candidat candidat = new Candidat();
            candidat.setId((long) i);
            candidat.setNom("Candidat" + i);
            candidat.setPrenom("Prénom" + i);
            candidat.setEmail("email" + i + "@example.com");
            candidat.setTelephone("06000000" + i);


            result.add(candidat);
        }

        return result;
    }

    private int getInscriptionsCount(int seanceId) {
        // This would come from your service
        // For now, return random value between 0 and 7
        return new Random().nextInt(8);
    }

    private void saveInscriptions(int seanceId, List<Integer> candidatIds) {
        // This would save to your database
        System.out.println("Saving inscriptions for session " + seanceId);
        System.out.println("Candidates: " + candidatIds);
    }


    public void initialize() {
        // Initialize collections
        seances = FXCollections.observableArrayList(getSeances());
        filteredSeances = new FilteredList<>(seances, p -> true);
        seancesTable.setItems(filteredSeances);

        // Configure séances table columns
        dateColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return new SimpleStringProperty(cellData.getValue().getDateSession().format(formatter));
        });

        heureColumn.setCellValueFactory(cellData -> {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return new SimpleStringProperty(cellData.getValue().getHeureSession().format(formatter));
        });

        moniteurColumn.setCellValueFactory(cellData -> {
            Moniteur moniteur = cellData.getValue().getMoniteur();
            return new SimpleStringProperty(moniteur.getNom() + " " + moniteur.getPrenom());
        });

        placesColumn.setCellValueFactory(cellData -> {
            SessionCode seance = cellData.getValue();
            int inscriptions = getInscriptionsCount(Math.toIntExact(seance.getId()));
            int places = seance.getCapaciteMax() - inscriptions;
            return new SimpleStringProperty(places + "/" + seance.getCapaciteMax());
        });

        // Row selection listener for sessions
        seancesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedSeance = newSelection;
                updateSelectedSeanceInfo();
                loadCandidats();
                saveBtn.setDisable(false);
            } else {
                selectedSeance = null;
                selectedSessionLabel.setText("Aucune séance sélectionnée");
                saveBtn.setDisable(true);
            }
        });

        // Configure candidats table
        selectColumn.setCellValueFactory(cellData -> cellData.getValue().selectedProperty());
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);

        nomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNom()));
        prenomColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPrenom()));

        // Make candidats table editable for checkboxes
        candidatsTable.setEditable(true);

        // Disable save button until a session is selected
        saveBtn.setDisable(true);
    }

    private void updateSelectedSeanceInfo() {
        if (selectedSeance == null) {
            selectedSessionLabel.setText("Aucune séance sélectionnée");
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        selectedSessionLabel.setText(String.format(
                "Séance du %s à %s - Moniteur: %s %s",
                selectedSeance.getDateSession().format(dateFormatter),
                selectedSeance.getHeureSession().format(timeFormatter),
                selectedSeance.getMoniteur().getNom(),
                selectedSeance.getMoniteur().getPrenom()
        ));
    }

    private void loadCandidats() {
        List<Candidat> allCandidats = getCandidats();
        candidats = FXCollections.observableArrayList();

        for (Candidat candidat : allCandidats) {
            CandidatWrapper wrapper = new CandidatWrapper(candidat);

            // Add selection listener to update count
            wrapper.selectedProperty().addListener((obs, oldVal, newVal) -> {
                updateSelectedCount();
            });

            candidats.add(wrapper);
        }

        candidatsTable.setItems(candidats);
        updateSelectedCount();
    }

    private void updateSelectedCount() {
        if (candidats == null) return;

        long count = candidats.stream().filter(CandidatWrapper::isSelected).count();
        selectedCountLabel.setText(String.valueOf(count));
    }

    @FXML
    private void rechercherSeances() {
        filteredSeances.setPredicate(seance -> {
            if (dateFilter.getValue() == null) {
                return true;
            }
            return seance.getDateSession().equals(dateFilter.getValue());
        });
    }

    @FXML
    private void resetFilters() {
        dateFilter.setValue(null);
        filteredSeances.setPredicate(p -> true);
    }

    @FXML
    private void selectAllCandidats() {
        for (CandidatWrapper wrapper : candidats) {
            wrapper.setSelected(true);
        }
        updateSelectedCount();
    }

    @FXML
    private void deselectAllCandidats() {
        for (CandidatWrapper wrapper : candidats) {
            wrapper.setSelected(false);
        }
        updateSelectedCount();
    }

    @FXML
    private void cancel() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void registerCandidates() {
        if (selectedSeance == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une séance");
            return;
        }

        // Get list of selected candidates
        List<Integer> selectedCandidatIds = new ArrayList<>();
        for (CandidatWrapper wrapper : candidats) {
            if (wrapper.isSelected()) {
                selectedCandidatIds.add(wrapper.getId());
            }
        }

        if (selectedCandidatIds.isEmpty()) {
            AlertUtil.showInfo("Attention", "Aucun candidat sélectionné");
            return;
        }

        // Check if selection exceeds capacity
        int inscriptions = getInscriptionsCount(Math.toIntExact(selectedSeance.getId()));
        int placesDisponibles = selectedSeance.getCapaciteMax() - inscriptions;

        if (selectedCandidatIds.size() > placesDisponibles) {
            boolean confirm = AlertUtil.showConfirmation(
                    "Dépassement de capacité",
                    "Vous avez sélectionné " + selectedCandidatIds.size() + " candidats pour " +
                            placesDisponibles + " places disponibles. Voulez-vous continuer ?"
            );

            if (!confirm) {
                return;
            }
        }

        // Save inscriptions
        saveInscriptions(Math.toIntExact(selectedSeance.getId()), selectedCandidatIds);

        AlertUtil.showInfo("Succès", "Les inscriptions ont été enregistrées avec succès");
        resetSelection();
    }

    private void resetSelection() {
        seancesTable.getSelectionModel().clearSelection();
        selectedSeance = null;
        selectedSessionLabel.setText("Aucune séance sélectionnée");
        candidats.clear();
        updateSelectedCount();
        saveBtn.setDisable(true);
    }


}