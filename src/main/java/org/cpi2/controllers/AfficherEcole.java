package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cpi2.entitties.AutoEcole;
import org.cpi2.service.AutoEcoleService;
import javafx.scene.effect.DropShadow;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class AfficherEcole {
    @FXML private TableView<AutoEcole> ecoleTableView;
    @FXML private TableColumn<AutoEcole, Long> idColumn;
    @FXML private TableColumn<AutoEcole, String> nomColumn;
    @FXML private TableColumn<AutoEcole, String> adresseColumn;
    @FXML private TableColumn<AutoEcole, String> telephoneColumn;
    @FXML private TableColumn<AutoEcole, String> emailColumn;
    @FXML private ImageView logoImageView;
    @FXML private VBox contentContainer;
    @FXML private HBox detailContainer;
    @FXML private StackPane rootPane;

    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private final ObservableList<AutoEcole> ecoleList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Set up table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        telephoneColumn.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        // Apply fade-in animation to the whole view
        applyFadeInAnimation(rootPane, 300);
        
        // Load auto-écoles from the database
        loadAutoEcoles();

        // Add selection listener to display the logo
        ecoleTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayLogo(newSelection);
            }
        });
    }
    
    private void applyFadeInAnimation(javafx.scene.Node node, double durationMillis) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(durationMillis), node);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    private void applyScaleAnimation(javafx.scene.Node node, double durationMillis) {
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(durationMillis), node);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.play();
    }
    
    private void applyTranslateAnimation(javafx.scene.Node node, double durationMillis, double fromY) {
        TranslateTransition translateTransition = new TranslateTransition(Duration.millis(durationMillis), node);
        translateTransition.setFromY(fromY);
        translateTransition.setToY(0);
        translateTransition.play();
    }

    public void loadAutoEcoles() {
        try {
            ecoleList.clear();
            List<AutoEcole> ecoles = autoEcoleService.findAllAutoEcoles();
            ecoleList.addAll(ecoles);
            
            if (ecoles.isEmpty()) {
                System.out.println("No auto-écoles found in database");
            } else {
                System.out.println("Found " + ecoles.size() + " auto-écoles in database");
                for (AutoEcole ecole : ecoles) {
                    System.out.println("Auto-école: " + ecole.getId() + " - " + ecole.getNom());
                }
            }
            
            ecoleList.addAll(ecoles);
            ecoleTableView.setItems(ecoleList);
            
            // Apply slide-in animation to the table
            applyTranslateAnimation(ecoleTableView, 500, 50);
            applyFadeInAnimation(ecoleTableView, 500);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les auto-écoles: " + e.getMessage());
        }
    }

    private void displayLogo(AutoEcole autoEcole) {
        String logoPath = autoEcole.getLogo();
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                File logoFile = new File(logoPath);
                if (logoFile.exists()) {
                    Image logoImage = new Image(logoFile.toURI().toString());
                    logoImageView.setImage(logoImage);
                    
                    // Apply scale animation to the logo
                    applyScaleAnimation(logoImageView, 300);
                } else {
                    System.out.println("Logo file does not exist: " + logoPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading logo: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleAjouterEcole(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/ajouterEcole.fxml"));
            Parent root = loader.load();

            // Apply animation to the dialog
            applyFadeInAnimation(root, 300);

            Stage stage = new Stage();
            stage.setTitle("Ajouter une école");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            // Reload the list after adding
            loadAutoEcoles();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre d'ajout: " + e.getMessage());
        }
    }

    @FXML
    public void handleModifierEcole(ActionEvent event) {
        AutoEcole selectedEcole = ecoleTableView.getSelectionModel().getSelectedItem();
        if (selectedEcole == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une école à modifier.");
            return;
        }

        try {
            // Load manageEcole view which has inline editing capability
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/manageEcole.fxml"));
            Parent root = loader.load();
            
            applyFadeInAnimation(root, 300);
            
            ManageEcole controller = loader.getController();
            controller.loadAutoEcole(selectedEcole.getId());
            controller.handleModifier(); // Automatically switch to edit mode
            
            Stage stage = new Stage();
            stage.setTitle("Modifier les informations de l'école");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            
            // Update the list when the window is closed
            stage.setOnHidden(e -> loadAutoEcoles());
            
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir la fenêtre de modification: " + e.getMessage());
        }
    }

    @FXML
    public void handleSupprimerEcole(ActionEvent event) {
        AutoEcole selectedEcole = ecoleTableView.getSelectionModel().getSelectedItem();
        if (selectedEcole == null) {
            showAlert(Alert.AlertType.WARNING, "Sélection requise", "Veuillez sélectionner une école à supprimer.");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmation de suppression");
        confirmAlert.setHeaderText(null);
        confirmAlert.setContentText("Êtes-vous sûr de vouloir supprimer l'école " + selectedEcole.getNom() + " ?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean success = autoEcoleService.deleteAutoEcole(selectedEcole.getId());
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", "L'école a été supprimée avec succès.");
                loadAutoEcoles();
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression de l'école.");
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}