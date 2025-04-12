package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entities.AutoEcole;
import org.cpi2.service.AutoEcoleService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.ValidationUtils;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

public class ModifierEcole implements Initializable {

    @FXML public TextField nomField;
    @FXML public TextField adresseField;
    @FXML public TextField telephoneField;
    @FXML public TextField emailField;
    @FXML public ImageView logoImageView;
    @FXML public Label validationMessageLabel;

    @FXML public Label nomError;
    @FXML public Label adresseError;
    @FXML public Label telephoneError;
    @FXML public Label emailError;

    private Image logoImage;
    private String logoPath;
    private final AutoEcoleService autoEcoleService = new AutoEcoleService();
    private AutoEcole currentAutoEcole;
    private AutoEcole originalAutoEcole; // For reset functionality

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        nomField.setPromptText("Entrez le nom de l'école");
        adresseField.setPromptText("Entrez l'adresse complète");
        telephoneField.setPromptText("Entrez le numéro (8 chiffres)");
        emailField.setPromptText("exemple@domaine.com");
        
        setupValidation();
    }

    private void setupValidation() {

        ValidationUtils.addValidation(nomField, 
            name -> name != null && !name.isEmpty() && NAME_PATTERN.matcher(name).matches(),
            "Le nom de l'école ne doit contenir que des lettres et des espaces", 1);

        ValidationUtils.addValidation(adresseField,
            address -> address != null && address.length() >= 10,
            "L'adresse doit contenir au moins 10 caractères", 1);

        ValidationUtils.addValidation(telephoneField,
            phone -> phone != null && PHONE_PATTERN.matcher(phone).matches(),
            "Le numéro de téléphone doit contenir exactement 8 chiffres", 1);

        ValidationUtils.addValidation(emailField,
            email -> email != null && EMAIL_PATTERN.matcher(email).matches(),
            "L'adresse email n'est pas valide", 1);
        

    }

    public void setAutoEcoleToModify(AutoEcole autoEcole) {
        if (autoEcole != null) {
            this.currentAutoEcole = autoEcole;

            this.originalAutoEcole = new AutoEcole(
                    autoEcole.getId(),
                    autoEcole.getNom(),
                    autoEcole.getAdresse(),
                    autoEcole.getTelephone(),
                    autoEcole.getEmail(),
                    autoEcole.getLogo()
            );

            populateFormFields();
        }
    }

    private void populateFormFields() {

        ValidationUtils.clearValidation(nomField);
        ValidationUtils.clearValidation(adresseField);
        ValidationUtils.clearValidation(telephoneField);
        ValidationUtils.clearValidation(emailField);

        if (nomError != null) nomError.setText("");
        if (adresseError != null) adresseError.setText("");
        if (telephoneError != null) telephoneError.setText("");
        if (emailError != null) emailError.setText("");

        nomField.setText(currentAutoEcole.getNom());
        adresseField.setText(currentAutoEcole.getAdresse());
        telephoneField.setText(currentAutoEcole.getTelephone());
        emailField.setText(currentAutoEcole.getEmail());

        styleFieldsForEditing();

        logoPath = currentAutoEcole.getLogo();
        if (logoPath != null && !logoPath.isEmpty()) {
            try {
                File logoFile = new File(logoPath);
                if (logoFile.exists()) {
                    logoImage = new Image(logoFile.toURI().toString());
                    logoImageView.setImage(logoImage);
                    logoImageView.getStyleClass().add("logo-image");

                    logoImageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.color(0, 0, 0, 0.3)));
                }
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de charger le logo: " + e.getMessage());
            }
        } else {
            logoImageView.setImage(null);
        }
    }

    private void styleFieldsForEditing() {

        nomField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getNom())) {
                nomField.getStyleClass().add("modified-field");
            } else {
                nomField.getStyleClass().removeAll("modified-field");
            }
        });

        adresseField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getAdresse())) {
                adresseField.getStyleClass().add("modified-field");
            } else {
                adresseField.getStyleClass().removeAll("modified-field");
            }
        });

        telephoneField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getTelephone())) {
                telephoneField.getStyleClass().add("modified-field");
            } else {
                telephoneField.getStyleClass().removeAll("modified-field");
            }
        });

        emailField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalAutoEcole.getEmail())) {
                emailField.getStyleClass().add("modified-field");
            } else {
                emailField.getStyleClass().removeAll("modified-field");
            }
        });
    }

    public void handleLogoUpload(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        fileChooser.setTitle("Choisir une image");
        Stage stage = (Stage) logoImageView.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {

                Path logoDir = Paths.get("logos");
                if (!Files.exists(logoDir)) {
                    Files.createDirectories(logoDir);
                }

                String fileName = System.currentTimeMillis() + "_" + file.getName();
                Path targetPath = logoDir.resolve(fileName);
                Files.copy(file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                logoPath = targetPath.toString();

                logoImage = new Image(file.toURI().toString());
                logoImageView.setImage(logoImage);
                logoImageView.getStyleClass().add("logo-image");

                logoImageView.setEffect(new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.color(0, 0, 0, 0.3)));
            } catch (Exception e) {
                AlertUtil.showError("Erreur", "Impossible de charger l'image: " + e.getMessage());
            }
        }
    }

    public void handleModifierEcole(ActionEvent event) {

        if (ValidationUtils.hasAnyErrors()) {
            return;
        }

        String nom = nomField.getText().trim();
        String adresse = adresseField.getText().trim();
        String telephone = telephoneField.getText().trim();
        String email = emailField.getText().trim();

        if (nom.isEmpty() || adresse.isEmpty() || telephone.isEmpty() || email.isEmpty()) {
            AlertUtil.showError("Erreur de validation", "Tous les champs sont obligatoires.");
            return;
        }

        currentAutoEcole.setNom(nom);
        currentAutoEcole.setAdresse(adresse);
        currentAutoEcole.setTelephone(telephone);
        currentAutoEcole.setEmail(email);
        currentAutoEcole.setLogo(logoPath);

        boolean success = autoEcoleService.updateAutoEcole(currentAutoEcole);

        if (success) {
            AlertUtil.showSuccess("Succès", "L'école a été modifiée avec succès!");

            originalAutoEcole = new AutoEcole(
                    currentAutoEcole.getId(),
                    currentAutoEcole.getNom(),
                    currentAutoEcole.getAdresse(),
                    currentAutoEcole.getTelephone(),
                    currentAutoEcole.getEmail(),
                    currentAutoEcole.getLogo()
            );

            nomField.getStyleClass().removeAll("modified-field");
            adresseField.getStyleClass().removeAll("modified-field");
            telephoneField.getStyleClass().removeAll("modified-field");
            emailField.getStyleClass().removeAll("modified-field");
        } else {
            AlertUtil.showError("Erreur", "Échec de la modification de l'école.");
        }
    }

    public void handleReset(ActionEvent event) {
        if (originalAutoEcole != null) {

            nomField.setText(originalAutoEcole.getNom());
            adresseField.setText(originalAutoEcole.getAdresse());
            telephoneField.setText(originalAutoEcole.getTelephone());
            emailField.setText(originalAutoEcole.getEmail());

            if (originalAutoEcole.getLogo() != null && !originalAutoEcole.getLogo().isEmpty()) {
                try {
                    File logoFile = new File(originalAutoEcole.getLogo());
                    if (logoFile.exists()) {
                        logoImage = new Image(logoFile.toURI().toString());
                        logoImageView.setImage(logoImage);
                    }
                } catch (Exception e) {
                    logoImageView.setImage(null);
                }
            } else {
                logoImageView.setImage(null);
            }
            
            logoPath = originalAutoEcole.getLogo();

            nomField.getStyleClass().removeAll("modified-field");
            adresseField.getStyleClass().removeAll("modified-field");
            telephoneField.getStyleClass().removeAll("modified-field");
            emailField.getStyleClass().removeAll("modified-field");

            ValidationUtils.clearValidation(nomField);
            ValidationUtils.clearValidation(adresseField);
            ValidationUtils.clearValidation(telephoneField);
            ValidationUtils.clearValidation(emailField);
        }
    }

    public void handleCancel(ActionEvent event) {

        Stage stage = (Stage) nomField.getScene().getWindow();
        stage.close();
    }
}

