package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cpi2.entities.Examen;
import org.cpi2.service.CandidatService;
import org.cpi2.service.ExamenService;
import org.cpi2.utils.AlertUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class PassExam {

    @FXML
    private TextField idCandidatField;

    @FXML
    private TextField idExamenField;

    @FXML
    private ComboBox<String> resultatComboBox;

    @FXML
    private DatePicker dateFilter;

    @FXML private TableView<Exam> examensTable;
    @FXML private TableColumn<Exam, String> idColumn;
    @FXML private TableColumn<Exam, String> typeColumn;
    @FXML private TableColumn<Exam, LocalDate> dateColumn;
    @FXML private TableColumn<Exam, String> candidatColumn;

    @FXML
    private Label selectedExamLabel;

    @FXML
    private TextArea commentaireArea;

    @FXML
    private Button saveButton;

    @FXML
    private Button cancelButton;

    private final ExamenService examenService = new ExamenService();
    private final CandidatService candidatService = new CandidatService();
    private ObservableList<Exam> examensList;

    @FXML
    public void initialize() {

        if (resultatComboBox.getItems().isEmpty()) {
            resultatComboBox.getItems().addAll("Réussi", "Échoué");
        }
        setupTableView();


        
        loadExamens();
        addListeners();

    }
    public void loadExamens() {
        examensList = FXCollections.observableArrayList();
        for (Examen examen : examenService.getAllPendingExamens()) {
            examensList.add(new Exam(
                    examen.getCandidat().getCin(),
                    examen.getId().toString(),
                    examen.getType().name(),
                    examen.getDate().toString(),
                    examen.getCandidat().getNom() + " " + examen.getCandidat().getPrenom(),
                    examen.getResultat() != null ? examen.getResultat().toString() : "En Cours"
            ));
        }
        if (!examensList.isEmpty()) {
            examensTable.setItems(examensList);
            examensTable.getSortOrder().add(dateColumn);
            dateColumn.setSortType(TableColumn.SortType.DESCENDING);
            examensTable.sort();
        }
    }
    private void setupTableView() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("idExamen"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("typeExamen"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateExamen"));
        candidatColumn.setCellValueFactory(new PropertyValueFactory<>("candidat"));
    }

    private void addListeners() {
        examensTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateExameninfo(newSelection);
            }
        });
    }


    public void updateExameninfo(Exam selectedExamen) {
        if (selectedExamen != null) {
            idCandidatField.setText(selectedExamen.getCin());
            idExamenField.setText(selectedExamen.getIdExamen());
            selectedExamLabel.setText("Sélectionné: " + selectedExamen.getTypeExamen());
        }
    }


    @FXML
    public void handleSave(ActionEvent event) {
        String candidatId = idCandidatField.getText();
        String examenId = idExamenField.getText();
        String resultat = resultatComboBox.getValue();
        String commentaire = commentaireArea.getText();

        if (candidatId == null || candidatId.trim().isEmpty() ||
                examenId == null || examenId.trim().isEmpty() ||
                resultat == null) {
            AlertUtil.showError("Erreur", "Veuillez remplir tous les champs obligatoires");
            return;
        }
        if (candidatService.findByCin(candidatId).isEmpty()) {
            AlertUtil.showError("Erreur", "Candidat non trouvé");
            return;
        }
        if (examenService.getExamenById(Long.parseLong(examenId)).isEmpty()) {
            AlertUtil.showError("Erreur", "Examen non trouvé");
            return;
        }
        if (commentaire.length() > 255) {
            AlertUtil.showError("Erreur", "Le commentaire ne doit pas dépasser 255 caractères");
            return;
        }

        Examen examen = examenService.getExamenById(Long.parseLong(examenId)).get();
        examen.setResultat(resultat.equals("Réussi"));
        examen.setCommentaire(commentaire);
        if(examenService.updateExamen(examen)) {
            AlertUtil.showSuccess( "Success", "Examen mis à jour avec succès");
            loadExamens();
            examensTable.setItems(examensList);
        } else {
            AlertUtil.showError("Erreur", "Échec de la mise à jour de l'examen");
            return;
        }

        clearFields();
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        clearFields();
    }

    private void clearFields() {
        idCandidatField.clear();
        idExamenField.clear();
        resultatComboBox.setValue(null);
        commentaireArea.clear();
    }

    @FXML
    public void rechercherExamens() {
        LocalDate selectedDate = dateFilter.getValue();
        if (selectedDate != null) {
            ObservableList<Exam> filteredList = FXCollections.observableArrayList();
            filteredList = examensList.stream().filter(
                    examen -> examen.getDateExamen().equals(selectedDate.toString())
            ).collect(Collectors.toCollection(FXCollections::observableArrayList));
            filteredList.sort(Comparator.comparing(Exam::getDateExamen));
            examensTable.setItems(filteredList);
        } else {
            examensList.sort(Comparator.comparing(Exam::getDateExamen));
            examensTable.setItems(examensList);
        }
    }

    public void resetFilters() {
        dateFilter.setValue(null);
        examensTable.setItems(null);
    }

    public static class Exam {
        private String idExamen;
        private String typeExamen;
        private String dateExamen;
        private String candidat;
        private String resultat;
        private String cin;

        public Exam(String cin,String idExamen, String typeExamen, String dateExamen, String candidat, String resultat) {
            this.cin = cin;
            this.idExamen = idExamen;
            this.typeExamen = typeExamen;
            this.dateExamen = dateExamen;
            this.candidat = candidat;
            this.resultat = resultat;
        }

        public String getIdExamen() {
            return idExamen;
        }

        public void setIdExamen(String idExamen) {
            this.idExamen = idExamen;
        }

        public String getTypeExamen() {
            return typeExamen;
        }

        public void setTypeExamen(String typeExamen) {
            this.typeExamen = typeExamen;
        }

        public String getDateExamen() {
            return dateExamen;
        }

        public void setDateExamen(String dateExamen) {
            this.dateExamen = dateExamen;
        }

        public String getCandidat() {
            return candidat;
        }

        public void setCandidat(String candidat) {
            this.candidat = candidat;
        }

        public String getResultat() {
            return resultat;
        }

        public void setResultat(String resultat) {
            this.resultat = resultat;
        }
        public String getCin() {
            return cin;
        }
        public void setCin(String cin) {
            this.cin = cin;
        }
    }
}

