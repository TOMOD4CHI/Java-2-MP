package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Seance;
import org.cpi2.service.SeanceService;
import org.cpi2.utils.AlertUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SeanceConduite {

    @FXML private WebView mapView;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private ComboBox<String> vehiculeCombo;
    @FXML private ComboBox<String> moniteurCombo;
    @FXML private ComboBox<String> candidatCombo;
    @FXML private TextField kilometrage;
    @FXML private DatePicker date;
    @FXML private TextField temps;

    @FXML private Label candidatError;
    @FXML private Label moniteurError;
    @FXML private Label vehiculeError;
    @FXML private Label kilometrageError;
    @FXML private Label dateError;
    @FXML private Label tempsError;
    @FXML private Label latitudeError;
    @FXML private Label longitudeError;

    private final SeanceService seanceService = new SeanceService();

    // CSS class for inputs with errors
    private static final String ERROR_STYLE_CLASS = "error-field";

    @FXML
    public void initialize() {
        // Initialize the map
        WebEngine webEngine = mapView.getEngine();
        webEngine.loadContent(getMapHtml());

        webEngine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", this);
            }
        });

        // Load the combo boxes with data
        loadCandidats();
        loadMoniteurs();
        loadVehicules();

        // Set default date to today
        date.setValue(LocalDate.now());

        // Setup validation
        setupValidation();
    }

    private void loadCandidats() {
        // Charger les candidats depuis la base de données
        org.cpi2.service.CandidatService candidatService = new org.cpi2.service.CandidatService();
        List<Candidat> candidatsList = candidatService.getAllCandidats();

        ObservableList<String> candidats = FXCollections.observableArrayList();

        if (candidatsList.isEmpty()) {
            AlertUtil.showError("Erreur", "Aucun candidat trouvé dans la base de données.");
        } else {
            for (org.cpi2.entities.Candidat candidat : candidatsList) {
                candidats.add(candidat.getId() + " - " + candidat.getNom() + " " + candidat.getPrenom());
            }
        }

        candidatCombo.setItems(candidats);
    }

    private void loadMoniteurs() {
        // Charger les moniteurs depuis la base de données
        org.cpi2.service.MoniteurService moniteurService = new org.cpi2.service.MoniteurService();
        List<org.cpi2.entities.Moniteur> moniteursList = moniteurService.getAllMoniteurs();

        ObservableList<String> moniteurs = FXCollections.observableArrayList();

        if (moniteursList.isEmpty()) {

            AlertUtil.showWarning( "Erreur", "Aucun moniteur trouvé dans la base de données.");
        } else {
            // Ajouter les moniteurs à la liste déroulante
            for (org.cpi2.entities.Moniteur moniteur : moniteursList) {
                moniteurs.add(moniteur.getId() + " - " + moniteur.getNom() + " " + moniteur.getPrenom());
            }
        }

        moniteurCombo.setItems(moniteurs);
    }

    private void loadVehicules() {
        // Charger les véhicules depuis la base de données
        org.cpi2.service.VehiculeService vehiculeService = new org.cpi2.service.VehiculeService();
        List<org.cpi2.entities.Vehicule> vehiculesList = vehiculeService.getAllVehicules();

        ObservableList<String> vehicules = FXCollections.observableArrayList();

        if (vehiculesList.isEmpty()) {
            AlertUtil.showWarning( "Aucun véhicule", "Aucun véhicule trouvé dans la base de données.");
        } else {
            for (org.cpi2.entities.Vehicule vehicule : vehiculesList) {
                vehicules.add(vehicule.getId() + " - " + vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")");
            }
        }

        vehiculeCombo.setItems(vehicules);
    }

    public void updateCoordinates(String lat, String lng) {
        latitudeField.setText(lat);
        longitudeField.setText(lng);

        // Clear any error styling and messages when coordinates are updated
        removeErrorStyle(latitudeField);
        removeErrorStyle(longitudeField);
        latitudeError.setVisible(false);
        longitudeError.setVisible(false);
    }

    private String getMapHtml() {
        return "<html>" +
                "<head>" +
                "   <link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css'/>" +
                "   <script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                "   <style>#mapid { height: 100vh; width: 100%; }</style>" +
                "</head>" +
                "<body style='margin:0;padding:0;'>" +
                "   <div id='mapid'></div>" +
                "   <script>" +
                "       var map = L.map('mapid').setView([36.8, 10.2], 11);" +  // Centered on Tunisia
                "       L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "           attribution: '&copy; OpenStreetMap contributors'" +
                "       }).addTo(map);" +
                "       var marker;" +
                "       map.on('click', function(e) {" +
                "           var lat = e.latlng.lat.toFixed(6);" +
                "           var lng = e.latlng.lng.toFixed(6);" +
                "           if (marker) { map.removeLayer(marker); }" +
                "           marker = L.marker([lat, lng]).addTo(map)" +
                "               .bindPopup('Lieu de séance sélectionné<br>Coordonnées: ' + lat + ', ' + lng)" +
                "               .openPopup();" +
                "           window.javaConnector.updateCoordinates(lat, lng);" +
                "       });" +
                "   </script>" +
                "</body>" +
                "</html>";
    }

    // Add error styling to a Control
    private void addErrorStyle(Control control) {
        if (!control.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            control.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    // Remove error styling from a Control
    private void removeErrorStyle(Control control) {
        control.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    // Reset styling and errors for all form fields
    private void resetAllStyles() {
        // Remove error styling from all controls
        removeErrorStyle(candidatCombo);
        removeErrorStyle(moniteurCombo);
        removeErrorStyle(vehiculeCombo);
        removeErrorStyle(kilometrage);
        removeErrorStyle(date);
        removeErrorStyle(temps);
        removeErrorStyle(latitudeField);
        removeErrorStyle(longitudeField);

        // Hide all error messages
        candidatError.setVisible(false);
        moniteurError.setVisible(false);
        vehiculeError.setVisible(false);
        kilometrageError.setVisible(false);
        dateError.setVisible(false);
        tempsError.setVisible(false);
        latitudeError.setVisible(false);
        longitudeError.setVisible(false);
    }

    @FXML
    private void cancelAction() {
        // Clear all fields (reset form)
        candidatCombo.setValue(null);
        moniteurCombo.setValue(null);
        vehiculeCombo.setValue(null);
        kilometrage.clear();
        date.setValue(LocalDate.now());
        temps.clear();
        latitudeField.clear();
        longitudeField.clear();

        // Reset the map marker
        WebEngine webEngine = mapView.getEngine();
        webEngine.executeScript("if (marker) { map.removeLayer(marker); marker = null; }");

        // Reset all styling and error messages
        resetAllStyles();
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        // Reset all styles and error messages before validation
        resetAllStyles();

        // Check and validate all fields
        boolean hasErrors = false;

        // Validate candidat
        if (candidatCombo.getValue() == null) {
            candidatError.setText("Veuillez sélectionner un candidat");
            candidatError.setVisible(true);
            addErrorStyle(candidatCombo);
            hasErrors = true;
        }

        // Validate moniteur
        if (moniteurCombo.getValue() == null) {
            moniteurError.setText("Veuillez sélectionner un moniteur");
            moniteurError.setVisible(true);
            addErrorStyle(moniteurCombo);
            hasErrors = true;
        }

        // Validate véhicule
        if (vehiculeCombo.getValue() == null) {
            vehiculeError.setText("Veuillez sélectionner un véhicule");
            vehiculeError.setVisible(true);
            addErrorStyle(vehiculeCombo);
            hasErrors = true;
        }

        // Validate kilométrage
        if (kilometrage.getText().trim().isEmpty()) {
            kilometrageError.setText("Le kilométrage est obligatoire");
            kilometrageError.setVisible(true);
            addErrorStyle(kilometrage);
            hasErrors = true;
        } else {
            try {
                double km = Double.parseDouble(kilometrage.getText());
                if (km < 0) {
                    kilometrageError.setText("Le kilométrage doit être un nombre positif");
                    kilometrageError.setVisible(true);
                    addErrorStyle(kilometrage);
                    hasErrors = true;
                }
            } catch (NumberFormatException e) {
                kilometrageError.setText("Le kilométrage doit être un nombre");
                kilometrageError.setVisible(true);
                addErrorStyle(kilometrage);
                hasErrors = true;
            }
        }

        // Validate date
        if (date.getValue() == null) {
            dateError.setText("La date est obligatoire");
            dateError.setVisible(true);
            addErrorStyle(date);
            hasErrors = true;
        } else if (date.getValue().isBefore(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le passé");
            dateError.setVisible(true);
            addErrorStyle(date);
            hasErrors = true;
        }

        // Validate temps (HH:MM)
        if (temps.getText().trim().isEmpty()) {
            tempsError.setText("L'heure est obligatoire");
            tempsError.setVisible(true);
            addErrorStyle(temps);
            hasErrors = true;
        } else {
            String timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
            if (!temps.getText().matches(timeRegex)) {
                tempsError.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
                tempsError.setVisible(true);
                addErrorStyle(temps);
                hasErrors = true;
            }
        }

        // Validate coordinates
        if (latitudeField.getText().trim().isEmpty()) {
            latitudeError.setText("Veuillez sélectionner un lieu sur la carte");
            latitudeError.setVisible(true);
            addErrorStyle(latitudeField);
            hasErrors = true;
        }

        if (longitudeField.getText().trim().isEmpty()) {
            longitudeError.setText("Veuillez sélectionner un lieu sur la carte");
            longitudeError.setVisible(true);
            addErrorStyle(longitudeField);
            hasErrors = true;
        }

        // If there are validation errors, stop here
        if (hasErrors) {
            return;
        }

        try {
            // Extract IDs from selected values (format: "1 - Name")
            Long candidatId = Long.parseLong(candidatCombo.getValue().split(" - ")[0]);
            Long moniteurId = Long.parseLong(moniteurCombo.getValue().split(" - ")[0]);
            Long vehiculeId = Long.parseLong(vehiculeCombo.getValue().split(" - ")[0]);

            // Validate and parse kilometrage
            double km = Double.parseDouble(kilometrage.getText());

            // Create Seance object
            Seance seance = new Seance();
            seance.setType("Conduite");
            seance.setCandidatId(candidatId);
            seance.setMoniteurId(moniteurId);
            seance.setVehiculeId(vehiculeId);

            // Format date as string (yyyy-MM-dd)
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            seance.setDate(date.getValue().format(formatter));

            seance.setTemps(temps.getText());
            seance.setKilometrage(km);
            seance.setLatitude(Double.parseDouble(latitudeField.getText()));
            seance.setLongitude(Double.parseDouble(longitudeField.getText()));

            // Vérifier si le moniteur existe avant de sauvegarder
            org.cpi2.service.MoniteurService moniteurService = new org.cpi2.service.MoniteurService();
            if (!moniteurService.getMoniteurById(moniteurId).isPresent()) {

                AlertUtil.showError("Erreur",
                        "Le moniteur sélectionné n'existe pas dans la base de données. Veuillez ajouter le moniteur avant de planifier une séance.");
                return;
            }

            // Vérifier si le candidat existe avant de sauvegarder
            org.cpi2.service.CandidatService candidatService = new org.cpi2.service.CandidatService();
            if (!candidatService.getCandidatById(candidatId).isPresent()) {

                AlertUtil.showError("Erreur",
                        "Le candidat sélectionné n'existe pas dans la base de données. Veuillez ajouter le candidat avant de planifier une séance.");
                return;
            }

            // Vérifier si le véhicule existe avant de sauvegarder
            if (vehiculeId != null) {
                org.cpi2.service.VehiculeService vehiculeService = new org.cpi2.service.VehiculeService();
                if (!vehiculeService.getVehiculeById(vehiculeId).isPresent()) {

                    AlertUtil.showError("Erreur",
                            "Le véhicule sélectionné n'existe pas dans la base de données. Veuillez ajouter le véhicule avant de planifier une séance.");
                    return;
                }
            }

            // Save to database
            boolean success = seanceService.saveSeance(seance);

            if (success) {
                AlertUtil.showSuccess( "Succès",
                        "La séance de conduite a été planifiée avec succès!");
                cancelAction(); // Clear the form
            } else {

                AlertUtil.showError("Erreur",
                        "Échec de la planification de la séance de conduite!");
            }

        } catch (Exception e) {

            AlertUtil.showError("Erreur",
                    "Une erreur s'est produite: " + e.getMessage());
        }
    }

    private void setupValidation() {
        candidatCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                candidatError.setText("Veuillez sélectionner un candidat");
                candidatError.setVisible(true);
                addErrorStyle(candidatCombo);
            } else {
                candidatError.setVisible(false);
                removeErrorStyle(candidatCombo);
            }
        });

        // Moniteur validation
        moniteurCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                moniteurError.setText("Veuillez sélectionner un moniteur");
                moniteurError.setVisible(true);
                addErrorStyle(moniteurCombo);
            } else {
                moniteurError.setVisible(false);
                removeErrorStyle(moniteurCombo);
            }
        });

        // Véhicule validation
        vehiculeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                vehiculeError.setText("Veuillez sélectionner un véhicule");
                vehiculeError.setVisible(true);
                addErrorStyle(vehiculeCombo);
            } else {
                vehiculeError.setVisible(false);
                removeErrorStyle(vehiculeCombo);
            }
        });

        // Kilométrage validation
        kilometrage.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                kilometrageError.setText("Le kilométrage est obligatoire");
                kilometrageError.setVisible(true);
                addErrorStyle(kilometrage);
            } else {
                try {
                    double km = Double.parseDouble(newVal);
                    if (km < 0) {
                        kilometrageError.setText("Le kilométrage doit être un nombre positif");
                        kilometrageError.setVisible(true);
                        addErrorStyle(kilometrage);
                    } else {
                        kilometrageError.setVisible(false);
                        removeErrorStyle(kilometrage);
                    }
                } catch (NumberFormatException e) {
                    kilometrageError.setText("Le kilométrage doit être un nombre");
                    kilometrageError.setVisible(true);
                    addErrorStyle(kilometrage);
                }
            }
        });

        // Date validation
        date.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                dateError.setText("La date est obligatoire");
                dateError.setVisible(true);
                addErrorStyle(date);
            } else if (newVal.isBefore(LocalDate.now())) {
                dateError.setText("La date ne peut pas être dans le passé");
                dateError.setVisible(true);
                addErrorStyle(date);
            } else {
                dateError.setVisible(false);
                removeErrorStyle(date);
            }
        });

        // Temps validation
        temps.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                tempsError.setText("L'heure est obligatoire");
                tempsError.setVisible(true);
                addErrorStyle(temps);
            } else {
                try {
                    LocalTime.parse(newVal, DateTimeFormatter.ofPattern("HH:mm"));
                    tempsError.setVisible(false);
                    removeErrorStyle(temps);
                } catch (Exception e) {
                    tempsError.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
                    tempsError.setVisible(true);
                    addErrorStyle(temps);
                }
            }
        });

    }
}