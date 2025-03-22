package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cpi2.entitties.TypePermis;
import org.cpi2.entitties.Vehicule;
import org.cpi2.service.VehiculeService;

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
            typeVehiculeCombo.getItems().add(type.getCode());
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
            return new SimpleStringProperty(typePermis != null ? typePermis.getCode() : "");
        });
        marqueModeleCol.setCellValueFactory(cellData -> cellData.getValue().marqueModeleProperty());
        kilometrageCol.setCellValueFactory(new PropertyValueFactory<>("kilometrageTotal"));
        prochainEntretienCol.setCellValueFactory(new PropertyValueFactory<>("dateMiseEnService"));
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

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le véhicule " + vehiculeSelected.getImmatriculation());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce véhicule ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (vehiculeService.supprimerVehicule(vehiculeSelected.getId())) {
                    vehiculesList.remove(vehiculeSelected);
                    mettreAJourTotal();
                    statusLabel.setText("Véhicule supprimé");
                    vehiculeSelected = null;
                    activerBoutons(false);
                } else {
                    showErrorAlert("Erreur de suppression", "Impossible de supprimer le véhicule.");
                }
            }
        });
    }

    @FXML
    private void handleVoirDetails(ActionEvent event) {
        if (vehiculeSelected == null) return;
        
        statusLabel.setText("Affichage des détails du véhicule " + vehiculeSelected.getImmatriculation());
        // TODO: Navigate to details view or open details dialog
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
        boolean success;
        
        if (isEditMode && vehiculeSelected != null) {
            vehicule = vehiculeSelected;
            updateVehiculeFromForm(vehicule);
            success = vehiculeService.mettreAJourVehicule(vehicule);
            if (success) {
                statusLabel.setText("Véhicule modifié avec succès");
                int index = vehiculesList.indexOf(vehiculeSelected);
                if (index >= 0) {
                    vehiculesList.set(index, vehicule);
                }
            }
        } else {
            vehicule = createVehiculeFromForm();
            success = vehiculeService.enregistrerVehicule(vehicule);
            if (success) {
                vehiculesList.add(vehicule);
                statusLabel.setText("Véhicule ajouté avec succès");
            }
        }

        if (success) {
            formPane.setExpanded(false);
            viderChamps();
            vehiculesTable.refresh();
            mettreAJourTotal();
        } else {
            showErrorAlert("Erreur", "Impossible d'enregistrer le véhicule. Vérifiez les données saisies.");
        }
    }

    private Vehicule createVehiculeFromForm() {
        TypePermis typePermis = getTypePermisFromCombo();
        
        return new Vehicule(
            immatriculationField.getText(),
            marqueField.getText(),
            modeleField.getText(),
            typePermis,
            dateMiseServicePicker.getValue(),
            Integer.parseInt(kilometrageField.getText())
        );
    }
    
    private void updateVehiculeFromForm(Vehicule vehicule) {
        vehicule.setImmatriculation(immatriculationField.getText());
        vehicule.setTypePermis(getTypePermisFromCombo());
        vehicule.setMarque(marqueField.getText());
        vehicule.setModele(modeleField.getText());
        vehicule.setKilometrageTotal(Integer.parseInt(kilometrageField.getText()));
        vehicule.setDateMiseEnService(dateMiseServicePicker.getValue());
    }
    
    private TypePermis getTypePermisFromCombo() {
        String typeCode = typeVehiculeCombo.getValue();
        for (TypePermis type : TypePermis.values()) {
            if (type.getCode().equals(typeCode)) {
                return type;
            }
        }
        return null;
    }

    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (immatriculationField.getText().isEmpty()) {
            erreurs.append("L'immatriculation est obligatoire.\n");
        }

        if (typeVehiculeCombo.getValue() == null) {
            erreurs.append("Le type de véhicule est obligatoire.\n");
        }

        if (marqueField.getText().isEmpty()) {
            erreurs.append("La marque est obligatoire.\n");
        }

        if (modeleField.getText().isEmpty()) {
            erreurs.append("Le modèle est obligatoire.\n");
        }

        if (kilometrageField.getText().isEmpty()) {
            erreurs.append("Le kilométrage est obligatoire.\n");
        } else {
            try {
                int kilometrage = Integer.parseInt(kilometrageField.getText());
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
            showErrorAlert("Erreurs de validation", erreurs.toString());
            return false;
        }

        return true;
    }

    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void remplirChamps(Vehicule vehicule) {
        immatriculationField.setText(vehicule.getImmatriculation());
        
        TypePermis type = vehicule.getTypePermis();
        if (type != null) {
            typeVehiculeCombo.setValue(type.getCode());
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
}