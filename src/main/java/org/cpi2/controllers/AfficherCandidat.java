package org.cpi2.controllers;

import org.cpi2.entitties.Candidat;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

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

    private CandidatService candidatService;




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

        /* Fetch candidates from service
        List<Candidat> candidats = candidatService.getAllCandidats();
        candidatsList.addAll(candidats);

        // Set items to table
        candidatTable.setItems(candidatsList);*/

    }

    // Example service class stub (in real application, you would have a separate service class)
    private class CandidatService {
        public List<Candidat> getAllCandidats() {
            // This would be implemented to fetch data from your data source
            // For demonstration, returning an empty list
            return List.of();
        }
    }
}