package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.cpi2.entitties.TypePermis;
import org.cpi2.entitties.Vehicule;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class GestionVehicule  {

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

    // Déclaration des données temporaires (à remplacer par service)
    private ObservableList<Vehicule> vehiculesList = FXCollections.observableArrayList();
    private Vehicule vehiculeSelected;
    private boolean isEditMode = false;


    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialisation des colonnes
        immatriculationCol.setCellValueFactory(new PropertyValueFactory<>("immatriculation"));
        typeVehiculeCol.setCellValueFactory(new PropertyValueFactory<>("typeVehicule"));
        marqueModeleCol.setCellValueFactory(cellData -> cellData.getValue().marqueModeleProperty());
        kilometrageCol.setCellValueFactory(new PropertyValueFactory<>("kilometrage"));
        prochainEntretienCol.setCellValueFactory(new PropertyValueFactory<>("prochainEntretien"));

        // Initialisation des types de véhicules
        typeVehiculeCombo.getItems().addAll("Voiture", "Moto", "Camion");

        // Chargement des données de test
        chargerDonneesTest();

        // Afficher le nombre total de véhicules
        mettreAJourTotal();

        // Désactiver les boutons de gestion jusqu'à sélection
        activerBoutons(false);

        // Ajouter un écouteur pour la sélection de véhicule
        vehiculesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            this.vehiculeSelected = newSelection;
            activerBoutons(newSelection != null);
        });

        // Configuration du formulaire
        formPane.setExpanded(false);
    }

    /**
     * Charge des données de test pour la démo
     */
    private void chargerDonneesTest() {
        // Données de test en attendant l'implémentation du service
        vehiculesList.add(new Vehicule("AB-123-CD", "Voiture", "Renault", "Clio", 45000, LocalDate.now().plusMonths(3)));
        vehiculesList.add(new Vehicule("EF-456-GH", "Voiture", "Peugeot", "308", 72000, LocalDate.now().plusMonths(1)));
        vehiculesList.add(new Vehicule("IJ-789-KL", "Moto", "Honda", "CBR", 15000, LocalDate.now().plusMonths(6)));
        vehiculesList.add(new Vehicule("MN-012-OP", "Camion", "Volvo", "FH16", 120000, LocalDate.now().minusMonths(1)));

        vehiculesTable.setItems(vehiculesList);
    }

    /**
     * Met à jour l'affichage du nombre total de véhicules
     */
    private void mettreAJourTotal() {
        totalVehiculesLabel.setText("Total: " + vehiculesList.size() + " véhicules");
    }

    /**
     * Active/désactive les boutons d'action selon la sélection
     */
    private void activerBoutons(boolean actif) {
        modifierBtn.setDisable(!actif);
        supprimerBtn.setDisable(!actif);
        detailsBtn.setDisable(!actif);
    }

    /**
     * Gère l'action d'ajout d'un véhicule
     */
    @FXML
    private void handleAjouterVehicule(ActionEvent event) {
        isEditMode = false;
        viderChamps();
        formPane.setExpanded(true);
        statusLabel.setText("Ajout d'un nouveau véhicule");
    }

    /**
     * Gère l'action de modification d'un véhicule
     */
    @FXML
    private void handleModifierVehicule(ActionEvent event) {
        if (vehiculeSelected == null) return;

        isEditMode = true;
        remplirChamps(vehiculeSelected);
        formPane.setExpanded(true);
        statusLabel.setText("Modification du véhicule " + vehiculeSelected.getImmatriculation());
    }

    /**
     * Gère l'action de suppression d'un véhicule
     */
    @FXML
    private void handleSupprimerVehicule(ActionEvent event) {
        if (vehiculeSelected == null) return;

        // Demande de confirmation
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le véhicule " + vehiculeSelected.getImmatriculation());
        alert.setContentText("Êtes-vous sûr de vouloir supprimer ce véhicule ?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Suppression du véhicule
                vehiculesList.remove(vehiculeSelected);
                mettreAJourTotal();
                statusLabel.setText("Véhicule supprimé");
                vehiculeSelected = null;
                activerBoutons(false);
            }
        });
    }

    /**
     * Gère l'action pour voir les détails d'un véhicule
     */
    @FXML
    private void handleVoirDetails(ActionEvent event) {
        if (vehiculeSelected == null) return;

        // TODO: Implémenter l'affichage des détails (fenêtre ou navigation)
        statusLabel.setText("Affichage des détails du véhicule " + vehiculeSelected.getImmatriculation());
    }

    /**
     * Gère l'action d'annulation du formulaire
     */
    @FXML
    private void handleAnnuler(ActionEvent event) {
        formPane.setExpanded(false);
        viderChamps();
        statusLabel.setText("Opération annulée");
    }

    /**
     * Gère l'action d'enregistrement du véhicule
     */
    @FXML
    private void handleEnregistrer(ActionEvent event) {
        if (!validerFormulaire()) {
            return;
        }

        // Création ou mise à jour du véhicule
        if (isEditMode && vehiculeSelected != null) {
            // Mise à jour
            vehiculeSelected.setImmatriculation(immatriculationField.getText());
            vehiculeSelected.setTypePermis(TypePermis.valueOf(typeVehiculeCombo.getValue()));
            vehiculeSelected.setMarque(marqueField.getText());
            vehiculeSelected.setModele(modeleField.getText());
            vehiculeSelected.setKilometrageTotal(Integer.parseInt(kilometrageField.getText()));
            vehiculeSelected.setDateMiseEnService(dateMiseServicePicker.getValue());

            statusLabel.setText("Véhicule modifié avec succès");
        } else {
            // Création
            Vehicule nouveauVehicule = new Vehicule(
                    immatriculationField.getText(),
                    typeVehiculeCombo.getValue(),
                    marqueField.getText(),
                    modeleField.getText(),
                    Integer.parseInt(kilometrageField.getText()),
                    LocalDate.now().plusMonths(3) // Date d'entretien par défaut (à remplacer)
            );
            nouveauVehicule.setDateMiseEnService(dateMiseServicePicker.getValue());

            vehiculesList.add(nouveauVehicule);
            mettreAJourTotal();
            statusLabel.setText("Véhicule ajouté avec succès");
        }

        // Rafraîchir la table
        vehiculesTable.refresh();

        // Fermer le formulaire
        formPane.setExpanded(false);
        viderChamps();
    }

    /**
     * Valide les champs du formulaire avant enregistrement
     */
    private boolean validerFormulaire() {
        StringBuilder erreurs = new StringBuilder();

        if (immatriculationField.getText().isEmpty()) {
            erreurs.append("L'immatriculation est obligatoire\n");
        }

        if (typeVehiculeCombo.getValue() == null) {
            erreurs.append("Le type de véhicule est obligatoire\n");
        }

        if (marqueField.getText().isEmpty()) {
            erreurs.append("La marque est obligatoire\n");
        }

        if (modeleField.getText().isEmpty()) {
            erreurs.append("Le modèle est obligatoire\n");
        }

        if (kilometrageField.getText().isEmpty()) {
            erreurs.append("Le kilométrage est obligatoire\n");
        } else {
            try {
                int km = Integer.parseInt(kilometrageField.getText());
                if (km < 0) {
                    erreurs.append("Le kilométrage doit être positif\n");
                }
            } catch (NumberFormatException e) {
                erreurs.append("Le kilométrage doit être un nombre\n");
            }
        }

        if (dateMiseServicePicker.getValue() == null) {
            erreurs.append("La date de mise en service est obligatoire\n");
        }

        if (erreurs.length() > 0) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText("Merci de corriger les erreurs suivantes:");
            alert.setContentText(erreurs.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    /**
     * Remplit les champs avec les données du véhicule sélectionné
     */
    private void remplirChamps(Vehicule vehicule) {
        immatriculationField.setText(vehicule.getImmatriculation());
        typeVehiculeCombo.setValue(String.valueOf(vehicule.getTypePermis()));
        marqueField.setText(vehicule.getMarque());
        modeleField.setText(vehicule.getModele());
        kilometrageField.setText(String.valueOf(vehicule.getKilometrageAvantEntretien()));
        dateMiseServicePicker.setValue(vehicule.getDateMiseEnService());
    }

    /**
     * Vide tous les champs du formulaire
     */
    private void viderChamps() {
        immatriculationField.clear();
        typeVehiculeCombo.setValue(null);
        marqueField.clear();
        modeleField.clear();
        kilometrageField.clear();
        dateMiseServicePicker.setValue(null);
    }
}