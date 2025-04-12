package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cpi2.entities.TypePermis;
import org.cpi2.entities.Vehicule;
import org.cpi2.service.VehiculeService;
import org.cpi2.utils.AlertUtil;

import java.lang.reflect.Type;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class GestionVehicule implements Initializable {


    @FXML
    private TableView<Vehicule> vehiculesTable;

    @FXML
    private TableColumn<Vehicule, String> immatriculationCol;

    @FXML
    private TableColumn<Vehicule, String> typeVehiculeCol;

    @FXML
    private TableColumn<Vehicule, String> marqueModeleCol;

    @FXML
    private TableColumn<Vehicule, Integer> kilometrageCol;

    @FXML
    private TableColumn<Vehicule, LocalDate> prochainEntretienCol;

    @FXML
    private TextField rechercheField;

    @FXML
    private Button ajouterBtn;

    @FXML
    private Button modifierBtn;

    @FXML
    private Button supprimerBtn;

    @FXML
    private Button detailsBtn;

    @FXML
    private TitledPane formPane;

    @FXML
    private TextField immatriculationField;

    @FXML
    private ComboBox<String> typeVehiculeCombo;

    @FXML
    private TextField marqueField;

    @FXML
    private TextField modeleField;

    @FXML
    private TextField kilometrageField;

    @FXML
    private DatePicker dateMiseServicePicker;

    @FXML
    private Button annulerBtn;

    @FXML
    private Button enregistrerBtn;

    @FXML
    private Label statusLabel;

    @FXML
    private Label totalVehiculesLabel;

    private ObservableList<Vehicule> vehiculesList = FXCollections.observableArrayList();
    private Vehicule vehiculeSelected;
    private boolean isEditMode = false;
    private VehiculeService vehiculeService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        vehiculeService = new VehiculeService();
        
        initializeTableColumns();
        loadVehicules();
        
        typeVehiculeCombo.getItems().clear();
        for (TypePermis type : TypePermis.values()) {
            typeVehiculeCombo.getItems().add(type.name()+" : ("+type.getTypeVehicule()+")");
        }

        activerBoutons(false);

        vehiculesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            this.vehiculeSelected = newSelection;
            activerBoutons(newSelection != null);
        });

        formPane.setExpanded(false);
    }
    
    private void initializeTableColumns() {
        immatriculationCol.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        typeVehiculeCol.setCellValueFactory(cellData -> {
            TypePermis typePermis = cellData.getValue().getTypePermis();
            return new SimpleStringProperty(typePermis != null ? typePermis.getTypeVehicule() : "");
        });
        marqueModeleCol.setCellValueFactory(cellData -> cellData.getValue().marqueModeleProperty());
        kilometrageCol.setCellValueFactory(new PropertyValueFactory<>("kilometrageTotal"));
        prochainEntretienCol.setCellValueFactory(new PropertyValueFactory<>("dateProchainEntretien"));
    }

    private void loadVehicules() {
        vehiculesList.clear();
        vehiculesList.addAll(vehiculeService.getAllVehicules());
        vehiculesTable.setItems(vehiculesList);
        mettreAJourTotal();
    }

    private void mettreAJourTotal() {
        totalVehiculesLabel.setText("Total: " + vehiculesList.size() + " véhicules");
    }

    private void activerBoutons(boolean actif) {
        modifierBtn.setDisable(!actif);
        supprimerBtn.setDisable(!actif);
        detailsBtn.setDisable(!actif);
    }


    @FXML
    private void handleAjouterVehicule(ActionEvent event) {
        isEditMode = false;
        viderChamps();
        formPane.setExpanded(true);
        statusLabel.setText("Ajout d'un nouveau véhicule");
    }

    @FXML
    private void handleModifierVehicule(ActionEvent event) {
        if (vehiculeSelected == null) return;

        isEditMode = true;
        remplirChamps(vehiculeSelected);
        formPane.setExpanded(true);
        statusLabel.setText("Modification du véhicule " + vehiculeSelected.getImmatriculation());
    }

    @FXML
    private void handleSupprimerVehicule(ActionEvent event) {
        if (vehiculeSelected == null) return;

        boolean confirmation = AlertUtil.showConfirmation(
            "Confirmation de suppression",
            "Supprimer le véhicule " + vehiculeSelected.getImmatriculation() +
            "Êtes-vous sûr de vouloir supprimer ce véhicule ?"
        );

        if (confirmation) {
                if (vehiculeService.supprimerVehicule((int) vehiculeSelected.getId())) {
                    vehiculesList.remove(vehiculeSelected);
                    mettreAJourTotal();
                    statusLabel.setText("Véhicule supprimé");
                    vehiculeSelected = null;
                    activerBoutons(false);
                } else {
                    AlertUtil.showError("Erreur de suppression", "Impossible de supprimer le véhicule.");
                }
            }
    }

    @FXML
    private void handleVoirDetails(ActionEvent event) {
        if (vehiculeSelected == null) return;
        AlertUtil.showError("Erreur", "Fonctionnalité non implémentée :) ");
        statusLabel.setText("Affichage des détails du véhicule " + vehiculeSelected.getImmatriculation());

    }

    @FXML
    private void handleAnnuler(ActionEvent event) {
        formPane.setExpanded(false);
        viderChamps();
        statusLabel.setText("Opération annulée");
    }

    @FXML
    private void handleEnregistrer(ActionEvent event) {
        if (!validerFormulaire()) {
            return;
        }

        Vehicule vehicule;
        boolean success = false;
        
        if (isEditMode && vehiculeSelected != null) {
                Vehicule oldVehicule = vehiculeSelected;
                Vehicule newVehicule = new Vehicule();
                updateVehiculeFromForm(newVehicule);
                success = vehiculeService.modifierVehicule(oldVehicule,newVehicule);
                if (success) {
                    vehiculesTable.refresh();
                    statusLabel.setText("Véhicule modifié avec succès");
                } else {
                    AlertUtil.showError("Erreur de modification", "Impossible de modifier le véhicule. Vérifiez les données saisies.");
                }
        } else {
                vehicule = createVehiculeFromForm();
                success = vehiculeService.ajouterVehicule(vehicule);
                if (success) {
                    vehiculesList.add(vehicule);
                    statusLabel.setText("Véhicule ajouté avec succès");
                }
                else {
                    AlertUtil.showError("Erreur d'ajout", "Impossible d'ajouter le véhicule. Vérifiez les données saisies.");
                }
        }

        if (success) {
            formPane.setExpanded(false);
            viderChamps();
            vehiculesTable.refresh();
            mettreAJourTotal();
        }
    }

    private Vehicule createVehiculeFromForm() {
        TypePermis typePermis = getTypePermisFromCombo();
        
        Vehicule vehicule = new Vehicule();
        vehicule.setImmatriculation(immatriculationField.getText().trim());
        vehicule.setMarque(marqueField.getText().trim());
        vehicule.setModele(modeleField.getText().trim());
        vehicule.setTypePermis(typePermis);
        vehicule.setDateMiseEnService(dateMiseServicePicker.getValue());
        
        try {
            vehicule.setKilometrageTotal(Integer.parseInt(kilometrageField.getText()));
        } catch (NumberFormatException e) {
            vehicule.setKilometrageTotal(0);
        }

        vehicule.setKilometrageProchainEntretien(vehicule.getKilometrageTotal() + vehiculeService.getNextKilometrageByTypeVehicule(typePermis.getTypeVehicule()));
        vehicule.setDateProchainEntretien(LocalDate.now().plusMonths(6));
        vehicule.setAnnee(dateMiseServicePicker.getValue().getYear());
        vehicule.setStatut("Disponible");
        
        return vehicule;
    }
    
    private void updateVehiculeFromForm(Vehicule vehicule) {
        vehicule.setImmatriculation(immatriculationField.getText().trim());
        vehicule.setTypePermis(getTypePermisFromCombo());
        vehicule.setMarque(marqueField.getText().trim());
        vehicule.setModele(modeleField.getText().trim());
        vehicule.setAnnee(dateMiseServicePicker.getValue().getYear());
        
        try {
            vehicule.setKilometrageTotal(Integer.parseInt(kilometrageField.getText()));
        } catch (NumberFormatException e) {

        }
        
        vehicule.setDateMiseEnService(dateMiseServicePicker.getValue());
    }
    
    private TypePermis getTypePermisFromCombo() {
        return TypePermis.valueOf(typeVehiculeCombo.getValue().substring(0,1));
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (immatriculationField.getText().trim().isEmpty()) {
            erreurs.append("L'immatriculation est obligatoire.\n");
        }

        if (typeVehiculeCombo.getValue() == null) {
            erreurs.append("Le type de véhicule est obligatoire.\n");
        }

        if (marqueField.getText().trim().isEmpty()) {
            erreurs.append("La marque est obligatoire.\n");
        }

        if (modeleField.getText().trim().isEmpty()) {
            erreurs.append("Le modèle est obligatoire.\n");
        }

        if (kilometrageField.getText().trim().isEmpty()) {
            erreurs.append("Le kilométrage est obligatoire.\n");
        } else {
            try {
                int kilometrage = Integer.parseInt(kilometrageField.getText().trim());
                if (kilometrage < 0) {
                    erreurs.append("Le kilométrage doit être positif.\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("Le kilométrage doit être un nombre entier.\n");
            }
        }

        if (dateMiseServicePicker.getValue() == null) {
            erreurs.append("La date de mise en service est obligatoire.\n");
        } else if (dateMiseServicePicker.getValue().isAfter(LocalDate.now())) {
            erreurs.append("La date de mise en service ne peut pas être dans le futur.\n");
        }

        if (erreurs.length() > 0) {
            AlertUtil.showError("Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    private void remplirChamps(Vehicule vehicule) {
        immatriculationField.setText(vehicule.getImmatriculation());
        
        TypePermis type = vehicule.getTypePermis();
        if (type != null) {
            typeVehiculeCombo.setValue(type.name());
        }
        
        marqueField.setText(vehicule.getMarque());
        modeleField.setText(vehicule.getModele());
        kilometrageField.setText(String.valueOf(vehicule.getKilometrageTotal()));
        dateMiseServicePicker.setValue(vehicule.getDateMiseEnService());
    }

    private void viderChamps() {
        immatriculationField.clear();
        typeVehiculeCombo.setValue(null);
        marqueField.clear();
        modeleField.clear();
        kilometrageField.clear();
        dateMiseServicePicker.setValue(null);
    }

    public void handleRechercher() {
        String recherche = rechercheField.getText().trim().toLowerCase();
        if (recherche.isEmpty()) {
            vehiculesTable.setItems(vehiculesList);
        } else {
            ObservableList<Vehicule> filteredList = FXCollections.observableArrayList();
            for (Vehicule vehicule : vehiculesList) {
                if (vehicule.getImmatriculation().toLowerCase().contains(recherche) ||
                    vehicule.getMarque().toLowerCase().contains(recherche) ||
                    vehicule.getModele().toLowerCase().contains(recherche)) {
                    filteredList.add(vehicule);
                }
            }
            vehiculesTable.setItems(filteredList);
        }
    }
}

