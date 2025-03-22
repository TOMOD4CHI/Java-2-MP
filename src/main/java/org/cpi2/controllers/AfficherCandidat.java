package org.cpi2.controllers;

import org.cpi2.entitties.Candidat;
import org.cpi2.service.CandidatService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.util.List;

public class AfficherCandidat  {

    @FXML
    private TableView<Candidat> candidatTable;

    @FXML
    private TableColumn<Candidat, String> nomColumn;

    @FXML
    private TableColumn<Candidat, String> prenomColumn;

    @FXML
    private TableColumn<Candidat, String> cinColumn;

    @FXML
    private TableColumn<Candidat, String> adresseColumn;

    @FXML
    private TableColumn<Candidat, String> telephoneColumn;

    private ObservableList<Candidat> candidatsList = FXCollections.observableArrayList();

    private final CandidatService candidatService = new CandidatService();

    public void initialize() {
        setupTableColumns();
        loadCandidats();
    }

    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
    }

    public void loadCandidats() {
        candidatsList.clear();

        // Fetch candidates from service
        List<Candidat> candidats = candidatService.getAllCandidats();
        candidatsList.addAll(candidats);

        // Set items to table
        candidatTable.setItems(candidatsList);
    }
}