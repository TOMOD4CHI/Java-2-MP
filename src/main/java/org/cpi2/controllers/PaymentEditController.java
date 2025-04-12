package org.cpi2.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.cpi2.controllers.Paiement.PaiementData;
import org.cpi2.entities.*;
import org.cpi2.service.CandidatService;
import org.cpi2.service.ExamenService;
import org.cpi2.service.InscriptionService;
import org.cpi2.service.PaiementService;
import org.cpi2.utils.AlertUtil;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ResourceBundle;

public class PaymentEditController implements Initializable {
    @FXML private AnchorPane mypane;
    
    @FXML private Label idLabel;
    @FXML private Label cinLabel;
    @FXML private Label candidatLabel;
    @FXML private Label typeLabel;
    @FXML private TextField montantField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private TextArea descriptionArea;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private PaiementData originalData;
    private final PaiementService paiementService = new PaiementService();
    private final CandidatService candidatService = new CandidatService();
    private final InscriptionService inscriptionService = new InscriptionService();
    private final ExamenService examenService = new ExamenService();
    private void setApplicationIcon(Stage stage) {
        try {


            stage.getIcons().clear();
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        } catch (Exception e) {
            e.printStackTrace();

            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> {
            if (mypane.getScene() != null && mypane.getScene().getWindow() != null) {
                Window window = mypane.getScene().getWindow();
            } else {
                System.err.println("Scene or Window is null.");
            }
        });

        modeComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(ModePaiement.values())
                        .map(Enum::name)
                        .toList()
        ));

        saveButton.setOnAction(this::handleSave);
        cancelButton.setOnAction(this::handleCancel);

        montantField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                montantField.setText(old);
            }
        });
    }
    
    public void initData(PaiementData data) {
        this.originalData = data;

        idLabel.setText(String.valueOf(data.getId()));
        cinLabel.setText(data.getCin());
        candidatLabel.setText(data.getCandidat());
        typeLabel.setText(data.getType());
        montantField.setText(String.valueOf(data.getMontant()));
        datePicker.setValue(data.getDate());
        modeComboBox.setValue(data.getMethode());
        descriptionArea.setText(data.getDescription());
    }
    
    @FXML
    private void handleSave(ActionEvent event) {
        if (!validateForm()) {
            return;
        }
        
        try {
            double montant = Double.parseDouble(montantField.getText());
            LocalDate date = datePicker.getValue();
            String mode = modeComboBox.getValue();
            String description = descriptionArea.getText();
            
            if (originalData.getType().equals("Examen")) {

                paiementService.update(new PaiementExamen(
                        StatutPaiement.COMPLETE,
                        originalData.getId(),
                        candidatService.getCandidatByCin(originalData.getCin()),
                        montant,
                        date,
                        ModePaiement.valueOf(mode),
                        examenService.getPendingExamen(originalData.getCin()),
                        description
                ));
            } else {

                Inscription inscription = inscriptionService.getActifInscirptionBycin(originalData.getCin()).get(0);
                
                paiementService.update(new PaiementInscription(
                        StatutPaiement.COMPLETE,
                        originalData.getId(),
                        candidatService.getCandidatByCin(originalData.getCin()),
                        montant,
                        date,
                        ModePaiement.valueOf(mode),
                        inscription,
                        originalData.getType(),
                        description
                ));

                try {
                    double reste = paiementService.calculerMontantRestant(inscription.getId());
                    if (reste <= 0) {
                        inscriptionService.updatePaymentStatus(inscription.getId(), true);
                    } else {
                        inscriptionService.updatePaymentStatus(inscription.getId(), false);
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
            }
            
            AlertUtil.showSuccess("Succès", "Paiement modifié avec succès");
            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors de la modification du paiement: " + e.getMessage());
        }
    }
    
    @FXML
    private void handleCancel(ActionEvent event) {
        closeWindow();
    }
    
    private boolean validateForm() {
        if (montantField.getText().isEmpty()) {
            AlertUtil.showError("Erreur", "Veuillez saisir le montant");
            return false;
        }
        
        if (datePicker.getValue() == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner une date");
            return false;
        }
        
        if (modeComboBox.getValue() == null) {
            AlertUtil.showError("Erreur", "Veuillez sélectionner le mode de paiement");
            return false;
        }
        
        return true;
    }
    
    private void closeWindow() {
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
} 
