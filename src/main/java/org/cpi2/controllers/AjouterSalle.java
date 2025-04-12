
package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.cpi2.entities.Salle;
import org.cpi2.service.SalleService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.ValidationUtils;

import java.util.regex.Pattern;

public class AjouterSalle {
    @FXML private TextField nomSalleField;
    @FXML private TextField numeroSalleField;
    @FXML private TextField capaciteField;
    @FXML private TextArea notesTextArea;
    @FXML private Button annulerButton;
    @FXML private Button enregistrerButton;
    
    @FXML private Label nomSalleError;
    @FXML private Label numeroSalleError;
    @FXML private Label capaciteError;
    
    private final SalleService salleService = new SalleService();

    private static final Pattern NUMBER_PATTERN = Pattern.compile("^[0-9]+$");

    @FXML
    public void initialize() {

        nomSalleField.setPromptText("Entrez le nom de la salle");
        numeroSalleField.setPromptText("Entrez le numéro de salle");
        capaciteField.setPromptText("Entrez la capacité de la salle");
        notesTextArea.setPromptText("Entrez toute information supplémentaire concernant la salle");

        setupValidation();
    }
    
    private void setupValidation() {

        ValidationUtils.addValidation(nomSalleField, 
            text -> !text.trim().isEmpty(), 
            "Le nom de la salle est obligatoire", 1);
        ValidationUtils.addValidation(nomSalleField, 
            text -> text.length() >= 3, 
            "Le nom de la salle doit contenir au moins 3 caractères", 2);

        ValidationUtils.addValidation(numeroSalleField, 
            text -> !text.trim().isEmpty(), 
            "Le numéro de salle est obligatoire", 1);
        ValidationUtils.addValidation(numeroSalleField, 
            text -> NUMBER_PATTERN.matcher(text).matches(), 
            "Le numéro de salle doit contenir uniquement des chiffres", 2);

        ValidationUtils.addValidation(capaciteField, 
            text -> !text.trim().isEmpty(), 
            "La capacité est obligatoire", 1);
        ValidationUtils.addValidation(capaciteField, 
            text -> {
                try {
                    int capacite = Integer.parseInt(text);
                    return capacite > 0;
                } catch (NumberFormatException e) {
                    return false;
                }
            }, 
            "La capacité doit être un nombre entier positif", 2);
    }
    
    @FXML
    public void handleCloseButtonAction(ActionEvent actionEvent) {

        Stage stage = (Stage) annulerButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void handleAnnulerButtonAction(ActionEvent actionEvent) {
        clearForm();
    }

    @FXML
    public void handleEnregistrerButtonAction(ActionEvent actionEvent) {

        if (ValidationUtils.hasAnyErrors()) {
            AlertUtil.showError("Erreur de validation", "Veuillez corriger les erreurs avant d'enregistrer.");
            return;
        }
        
        try {

            String nomSalle = nomSalleField.getText().trim();
            String numeroSalle = numeroSalleField.getText().trim();
            int capacite = Integer.parseInt(capaciteField.getText().trim());
            String notes = notesTextArea.getText().trim();

            if (salleService.getSalleByNumero(numeroSalle).isPresent()) {
                AlertUtil.showError("Erreur", "Une salle avec le numéro " + numeroSalle + " existe déjà.");
                return;
            }

            Salle salle = new Salle();
            salle.setNom(nomSalle);
            salle.setNumero(numeroSalle);
            salle.setCapacite(capacite);
            salle.setNotes(notes);

            boolean success = salleService.addSalle(salle);
            
            if (success) {
                AlertUtil.showSuccess("Succès", "La salle a été ajoutée avec succès!");
                clearForm();
            } else {
                AlertUtil.showError("Erreur", "Échec de l'ajout de la salle.");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showError("Erreur", "La capacité doit être un nombre entier.");
        } catch (Exception e) {
            AlertUtil.showError("Erreur", "Une erreur est survenue: " + e.getMessage());
        }
    }
    
    private void clearForm() {
        nomSalleField.clear();
        numeroSalleField.clear();
        capaciteField.clear();
        notesTextArea.clear();
        
        ValidationUtils.clearValidation(nomSalleField);
        ValidationUtils.clearValidation(numeroSalleField);
        ValidationUtils.clearValidation(capaciteField);
    }
}

