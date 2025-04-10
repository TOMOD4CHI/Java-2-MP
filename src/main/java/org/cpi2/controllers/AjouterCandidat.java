package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.cpi2.entities.CoursePlan;
import org.cpi2.entities.Inscription;
import org.cpi2.service.CandidatService;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.TypePermis;
import org.cpi2.service.InscriptionService;
import org.cpi2.utils.ValidationUtils;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

public class AjouterCandidat {
    private final CandidatService candidatService = new CandidatService();
    private final InscriptionService inscriptionService = new InscriptionService();

    // Validation patterns
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9]{8}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-zÀ-ÿ\\s]+$");
    private static final Pattern CIN_PATTERN = Pattern.compile("^[0-9]{8,}$");

    private boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    private boolean isValidName(String name) {
        return name != null && NAME_PATTERN.matcher(name).matches();
    }

    private boolean isValidCIN(String cin) {
        return cin != null && CIN_PATTERN.matcher(cin).matches();
    }
    
    private boolean isValidAddress(String address, int minLength) {
        return address != null && address.length() >= minLength;
    }

    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField cinField;
    @FXML private ComboBox<String> typeComboBox;
    @FXML private TextField addressField;
    @FXML private TextField phoneField;
    @FXML private TextField emailField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    @FXML
    private void initialize() {
        typeComboBox.setPrefWidth(200);
        typeComboBox.getItems().clear();
        for (CoursePlan plan : CoursePlan.values()) {
            typeComboBox.getItems().add(plan.name());
        }
        if (!typeComboBox.getItems().isEmpty()) {
            typeComboBox.getSelectionModel().selectFirst();
        }
        
        // Set placeholders for input fields
        nomField.setPromptText("Entrez le nom");
        prenomField.setPromptText("Entrez le prénom");
        cinField.setPromptText("Entrez le CIN (8 chiffres minimum)");
        addressField.setPromptText("Entrez l'adresse complète");
        phoneField.setPromptText("Entrez le numéro (8 chiffres)");
        emailField.setPromptText("exemple@domaine.com");
        
        // Setup real-time validation
        setupValidation();
    }
    
    private void setupValidation() {
        // Nom validation
        ValidationUtils.addValidation(nomField, 
            text -> !text.trim().isEmpty(), 
            "Le nom est obligatoire", 1);
        ValidationUtils.addValidation(nomField, 
            this::isValidName, 
            "Le nom ne doit contenir que des lettres et des espaces", 2);
            
        // Prénom validation
        ValidationUtils.addValidation(prenomField, 
            text -> !text.trim().isEmpty(), 
            "Le prénom est obligatoire", 1);
        ValidationUtils.addValidation(prenomField, 
            this::isValidName, 
            "Le prénom ne doit contenir que des lettres et des espaces", 2);
            
        // CIN validation
        ValidationUtils.addValidation(cinField, 
            text -> !text.trim().isEmpty(), 
            "Le CIN est obligatoire", 1);
        ValidationUtils.addValidation(cinField, 
            this::isValidCIN, 
            "Le CIN doit contenir au moins 8 chiffres et ne doit contenir que des chiffres", 2);
            
        // Type permis validation
        ValidationUtils.<String>addValidation(typeComboBox, 
            value -> value != null, 
            "Veuillez sélectionner un type de permis", 1);
            
        // Address validation
        ValidationUtils.addValidation(addressField, 
            text -> !text.trim().isEmpty(), 
            "L'adresse est obligatoire", 1);
        ValidationUtils.addValidation(addressField, 
            text -> isValidAddress(text, 10), 
            "L'adresse doit contenir au moins 10 caractères", 2);
            
        // Phone validation
        ValidationUtils.addValidation(phoneField, 
            text -> !text.trim().isEmpty(), 
            "Le numéro de téléphone est obligatoire", 1);
        ValidationUtils.addValidation(phoneField, 
            this::isValidPhone, 
            "Le numéro de téléphone doit contenir exactement 8 chiffres", 2);
            
        // Email validation (only if not empty)
        ValidationUtils.addValidation(emailField, 
            text -> text.trim().isEmpty() || isValidEmail(text), 
            "L'adresse email n'est pas valide", 1);
    }

    @FXML
    private void cancelAction() {
        // Effacer les champs
        nomField.clear();
        prenomField.clear();
        cinField.clear();
        typeComboBox.getSelectionModel().clearSelection();
        addressField.clear();
        phoneField.clear();
        emailField.clear();
        
        // Effacer les validations
        ValidationUtils.clearValidation(nomField);
        ValidationUtils.clearValidation(prenomField);
        ValidationUtils.clearValidation(cinField);
        ValidationUtils.clearValidation(typeComboBox);
        ValidationUtils.clearValidation(addressField);
        ValidationUtils.clearValidation(phoneField);
        ValidationUtils.clearValidation(emailField);
    }

    @FXML
    private void confirmAction() {
        // Retrieve all form field values
        String nom = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String cin = cinField.getText().trim();
        String address = addressField.getText().trim();
        String phone = phoneField.getText().trim();
        String email = emailField.getText().trim();
        String typePermis = typeComboBox.getValue();
        String cycle = "Mensuel";//this is the solution until the fields are added to the form
        //String cycle = cycleComboBox.getValue();


        // Check required fields
        if (nom.isEmpty() || prenom.isEmpty() || cin.isEmpty() || 
            address.isEmpty() || phone.isEmpty() || typePermis == null) {
            // This should be caught by the validation, but just in case
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Merci de remplir tous les champs obligatoires!");
            alert.showAndWait();
            return;
        }

        try {
            Inscription inscription = new Inscription();
            Candidat candidat = new Candidat();
            candidat.setNom(nom);
            candidat.setPrenom(prenom);
            candidat.setCin(cin);
            candidat.setAdresse(address);
            candidat.setTelephone(phone);
            candidat.setEmail(email);

            //TODO: Add more fields to choose payment cycle obligatory
            //these are the options : "Mensuel", "Trimestriel", "Hebdomadaire","Totale"

            inscription.setCin(candidat.getCin());
            inscription.setPaymentStatus(false);
            inscription.setStatus("En Cours");
            inscription.setPaymentCycle(cycle);
            inscription.setInscriptioDate(Date.valueOf(LocalDate.now()));
            inscription.setnextPaymentDate(Date.valueOf(LocalDate.now()));
            inscription.setPlan(CoursePlan.valueOf(typeComboBox.getValue()));


            if (candidatService.addCandidat(candidat)) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Le candidat a été ajouté avec succès!");
            } else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Une erreur est survenue lors de l'ajout du candidat.");
                alert.showAndWait();
            }
            List<Inscription> activeinscriptions = inscriptionService.getActifInscirptionBycin(cin);
            if (activeinscriptions.isEmpty()) {
                //can be paired with type of documents rather than type permis for more realistic scenarios
                TypePermis requiredPermis = CoursePlan.requiredTypePermis(CoursePlan.valueOf(typePermis));
                if(requiredPermis == null ||candidatService.findCandidatsByTypePermis(requiredPermis.name()).contains(cin)){
                    if(inscriptionService.saveInscription(inscription)){
                        Alert alert = new Alert(AlertType.INFORMATION);
                        alert.setTitle("Succès");
                        alert.setHeaderText(null);
                        alert.setContentText("L'inscription a été ajoutée avec succès!");
                        alert.showAndWait();
                        cancelAction();
                    } else {
                        Alert alert = new Alert(AlertType.ERROR);
                        alert.setTitle("Erreur");
                        alert.setHeaderText(null);
                        alert.setContentText("Une erreur est survenue lors de l'ajout de l'inscription.");
                        alert.showAndWait();
                    }

                }
                else {
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Erreur");
                    alert.setHeaderText(null);
                    alert.setContentText("Le candidat n'est pas eligible car il n'avais pas le document suivant : "+requiredPermis.getDescription()+".");
                    alert.showAndWait();
                }
            }
            else {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Erreur");
                alert.setHeaderText(null);
                alert.setContentText("Le candidat a déjà une inscription en cours : \n "+activeinscriptions.get(0).getPlan().getDescription()+" .");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText(null);
            alert.setContentText("Une erreur est survenue: " + e.getMessage());
            //for debugging
            e.printStackTrace();
            alert.showAndWait();
        }
    }
}
