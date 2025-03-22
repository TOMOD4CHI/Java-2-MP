package org.cpi2.controllers;

import org.cpi2.entitties.Moniteur;
import org.cpi2.entitties.TypePermis;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

public class AfficherMoniteur{

    @FXML
    private TableView<Moniteur> moniteurTable;

    @FXML
    private TableColumn<Moniteur, String> nomColumn;

    @FXML
    private TableColumn<Moniteur, String> prenomColumn;

    @FXML
    private TableColumn<Moniteur, String> cinColumn;

    @FXML
    private TableColumn<Moniteur, String> adresseColumn;

    @FXML
    private TableColumn<Moniteur, String> telephoneColumn;

    @FXML
    private TableColumn<Moniteur, LocalDate> dateEmbaucheColumn;

    @FXML
    private TableColumn<Moniteur, Set<TypePermis>> specialitesColumn;

    private ObservableList<Moniteur> moniteursList = FXCollections.observableArrayList();

    // Service to fetch moniteurs data
    private MoniteurService moniteurService;




    public void initialize() {
        setupTableColumns();
        loadMoniteurs();
    }

    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        cinColumn.setCellValueFactory(new PropertyValueFactory<>("cin"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        dateEmbaucheColumn.setCellValueFactory(new PropertyValueFactory<>("dateEmbauche"));
        specialitesColumn.setCellValueFactory(new PropertyValueFactory<>("specialites"));

        // Custom cell factory for date formatting
        dateEmbaucheColumn.setCellFactory(column -> new TableCell<>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText(null);
                } else {
                    setText(formatter.format(date));
                }
            }
        });

        // Custom cell factory for specialites formatting
        specialitesColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Set<TypePermis> specialites, boolean empty) {
                super.updateItem(specialites, empty);
                if (empty || specialites == null || specialites.isEmpty()) {
                    setText(null);
                } else {
                    setText(specialites.stream()
                            .map(TypePermis::name)
                            .collect(Collectors.joining(", ")));
                }
            }
        });
    }

    public void loadMoniteurs() {
        // Clear previous data
        moniteursList.clear();

        /* Fetch moniteurs from service
        List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
        moniteursList.addAll(moniteurs);

        // Set items to table
        moniteurTable.setItems(moniteursList);*/
    }

    // Example service class stub (in real application, you would have a separate service class)
    private class MoniteurService {
        public List<Moniteur> getAllMoniteurs() {
            // This would be implemented to fetch data from your data source
            // For demonstration, returning an empty list
            return List.of();
        }
    }
}