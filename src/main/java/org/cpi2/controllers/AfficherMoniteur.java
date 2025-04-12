package org.cpi2.controllers;

import org.cpi2.entities.Moniteur;
import org.cpi2.entities.TypePermis;
import org.cpi2.service.MoniteurService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;



public class AfficherMoniteur {

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
    private MoniteurService moniteurService;

    public void initialize() {
        moniteurService = new MoniteurService();
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

    @FXML
    public void loadMoniteurs() {
        moniteursList.clear();
        List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
        moniteursList.addAll(moniteurs);
        moniteurTable.setItems(moniteursList);
    }
}


