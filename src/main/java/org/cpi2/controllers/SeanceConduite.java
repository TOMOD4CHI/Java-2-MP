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
    @FXML private TextField quartierField;
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
    @FXML private Label quartierError;

    private final SeanceService seanceService = new SeanceService();

    private static final String ERROR_STYLE_CLASS = "error-field";

    @FXML
    public void initialize() {
        WebEngine webEngine = mapView.getEngine();
        webEngine.loadContent(getMapHtml());

        webEngine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
            if (newDoc != null) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaConnector", this);
            }
        });

        loadCandidats();
        loadMoniteurs();
        loadVehicules();

        date.setValue(LocalDate.now());

        setupValidation();
    }

    private void loadCandidats() {
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
        org.cpi2.service.MoniteurService moniteurService = new org.cpi2.service.MoniteurService();
        List<org.cpi2.entities.Moniteur> moniteursList = moniteurService.getAllMoniteurs();

        ObservableList<String> moniteurs = FXCollections.observableArrayList();

        if (moniteursList.isEmpty()) {
            AlertUtil.showWarning("Erreur", "Aucun moniteur trouvé dans la base de données.");
        } else {
            for (org.cpi2.entities.Moniteur moniteur : moniteursList) {
                moniteurs.add(moniteur.getId() + " - " + moniteur.getNom() + " " + moniteur.getPrenom());
            }
        }

        moniteurCombo.setItems(moniteurs);
    }

    private void loadVehicules() {
        org.cpi2.service.VehiculeService vehiculeService = new org.cpi2.service.VehiculeService();
        List<org.cpi2.entities.Vehicule> vehiculesList = vehiculeService.getAllVehicules();

        ObservableList<String> vehicules = FXCollections.observableArrayList();

        if (vehiculesList.isEmpty()) {
            AlertUtil.showWarning("Aucun véhicule", "Aucun véhicule trouvé dans la base de données.");
        } else {
            for (org.cpi2.entities.Vehicule vehicule : vehiculesList) {
                vehicules.add(vehicule.getId() + " - " + vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")");
            }
        }

        vehiculeCombo.setItems(vehicules);
    }

    public void updateCoordinates(String lat, String lng, String address) {
        latitudeField.setText(lat);
        longitudeField.setText(lng);
        quartierField.setText(address);

        removeErrorStyle(latitudeField);
        removeErrorStyle(longitudeField);
        removeErrorStyle(quartierField);
        latitudeError.setVisible(false);
        longitudeError.setVisible(false);
        quartierError.setVisible(false);
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
                "       var map = L.map('mapid').setView([36.8, 10.2], 11);" +
                "       L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {" +
                "           attribution: '&copy; OpenStreetMap contributors'" +
                "       }).addTo(map);" +
                "       var marker;" +
                "       var debounceTimer;" +
                "       var lastCoords = null;" +

                "       var geocodeCache = {};" +

                "       map.on('click', function(e) {" +
                "           var lat = e.latlng.lat.toFixed(6);" +
                "           var lng = e.latlng.lng.toFixed(6);" +
                "           var coordKey = lat + ',' + lng;" +

                "           if (marker) { map.removeLayer(marker); }" +
                "           marker = L.marker([lat, lng]).addTo(map)" +
                "               .bindPopup('Lieu de séance sélectionné<br>Coordonnées: ' + lat + ', ' + lng)" +
                "               .openPopup();" +

                "           window.javaConnector.updateCoordinates(lat, lng, 'Chargement...');" +

                "           if (geocodeCache[coordKey]) {" +
                "               window.javaConnector.updateCoordinates(lat, lng, geocodeCache[coordKey]);" +
                "               return;" +
                "           }" +

                "           clearTimeout(debounceTimer);" +
                "           debounceTimer = setTimeout(function() {" +
                "               getAddress(lat, lng, coordKey);" +
                "           }, 300);" +
                "       });" +

                "       function getAddress(lat, lng, coordKey) {" +
                "           var url = 'https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&zoom=18&addressdetails=1';" +

                "           var options = {" +
                "               headers: {" +
                "                   'Accept-Language': 'fr, en;q=0.8'," +
                "                   'User-Agent': 'JavaFX Driving School Application'" +
                "               }" +
                "           };" +

                "           fetch(url, options)" +
                "               .then(response => response.json())" +
                "               .then(data => {" +
                "                   var address = '';" +
                "                   if (data.address) {" +
                "                       if (data.address.road) {" +
                "                           address = data.address.road;" +
                "                       } else if (data.address.suburb) {" +
                "                           address = data.address.suburb;" +
                "                       } else if (data.address.neighbourhood) {" +
                "                           address = data.address.neighbourhood;" +
                "                       } else if (data.address.city_district) {" +
                "                           address = data.address.city_district;" +
                "                       } else if (data.address.city) {" +
                "                           address = data.address.city;" +
                "                       } else {" +
                "                           address = data.display_name.split(',')[0];" +
                "                       }" +
                "                   } else {" +
                "                       address = 'Emplacement non identifié';" +
                "                   }" +

                "                   geocodeCache[coordKey] = address;" +
                "                   window.javaConnector.updateCoordinates(lat, lng, address);" +
                "               })" +
                "               .catch(error => {" +
                "                   console.error('Erreur lors de la récupération de l\\'adresse:', error);" +
                "                   window.javaConnector.updateCoordinates(lat, lng, 'Emplacement non identifié');" +
                "               });" +
                "       }" +
                "   </script>" +
                "</body>" +
                "</html>";
    }

    private void addErrorStyle(Control control) {
        if (!control.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            control.getStyleClass().add(ERROR_STYLE_CLASS);
        }
    }

    private void removeErrorStyle(Control control) {
        control.getStyleClass().remove(ERROR_STYLE_CLASS);
    }

    private void resetAllStyles() {
        removeErrorStyle(candidatCombo);
        removeErrorStyle(moniteurCombo);
        removeErrorStyle(vehiculeCombo);
        removeErrorStyle(kilometrage);
        removeErrorStyle(date);
        removeErrorStyle(temps);
        removeErrorStyle(latitudeField);
        removeErrorStyle(longitudeField);
        removeErrorStyle(quartierField);

        candidatError.setVisible(false);
        moniteurError.setVisible(false);
        vehiculeError.setVisible(false);
        kilometrageError.setVisible(false);
        dateError.setVisible(false);
        tempsError.setVisible(false);
        latitudeError.setVisible(false);
        longitudeError.setVisible(false);
        quartierError.setVisible(false);
    }

    @FXML
    private void cancelAction() {
        candidatCombo.setValue(null);
        moniteurCombo.setValue(null);
        vehiculeCombo.setValue(null);
        kilometrage.clear();
        date.setValue(LocalDate.now());
        temps.clear();
        latitudeField.clear();
        longitudeField.clear();
        quartierField.clear();

        WebEngine webEngine = mapView.getEngine();
        webEngine.executeScript("if (marker) { map.removeLayer(marker); marker = null; }");

        resetAllStyles();
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        resetAllStyles();

        boolean hasErrors = false;

        if (candidatCombo.getValue() == null) {
            candidatError.setText("Veuillez sélectionner un candidat");
            candidatError.setVisible(true);
            addErrorStyle(candidatCombo);
            hasErrors = true;
        }

        if (moniteurCombo.getValue() == null) {
            moniteurError.setText("Veuillez sélectionner un moniteur");
            moniteurError.setVisible(true);
            addErrorStyle(moniteurCombo);
            hasErrors = true;
        }

        if (vehiculeCombo.getValue() == null) {
            vehiculeError.setText("Veuillez sélectionner un véhicule");
            vehiculeError.setVisible(true);
            addErrorStyle(vehiculeCombo);
            hasErrors = true;
        }

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

        if (quartierField.getText().trim().isEmpty()) {
            quartierError.setText("Le nom de la rue est obligatoire");
            quartierError.setVisible(true);
            addErrorStyle(quartierField);
            hasErrors = true;
        }

        if (hasErrors) {
            return;
        }

        try {
            Long candidatId = Long.parseLong(candidatCombo.getValue().split(" - ")[0]);
            Long moniteurId = Long.parseLong(moniteurCombo.getValue().split(" - ")[0]);
            Long vehiculeId = Long.parseLong(vehiculeCombo.getValue().split(" - ")[0]);

            double km = Double.parseDouble(kilometrage.getText());

            Seance seance = new Seance();
            seance.setType("Conduite");
            seance.setCandidatId(candidatId);
            seance.setMoniteurId(moniteurId);
            seance.setVehiculeId(vehiculeId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            seance.setDate(date.getValue().format(formatter));

            seance.setTemps(temps.getText());
            seance.setKilometrage(km);
            seance.setLatitude(Double.parseDouble(latitudeField.getText()));
            seance.setLongitude(Double.parseDouble(longitudeField.getText()));
            seance.setQuartier(quartierField.getText().trim());

            org.cpi2.service.MoniteurService moniteurService = new org.cpi2.service.MoniteurService();
            if (!moniteurService.getMoniteurById(moniteurId).isPresent()) {
                AlertUtil.showError("Erreur",
                        "Le moniteur sélectionné n'existe pas dans la base de données. Veuillez ajouter le moniteur avant de planifier une séance.");
                return;
            }

            org.cpi2.service.CandidatService candidatService = new org.cpi2.service.CandidatService();
            if (!candidatService.getCandidatById(candidatId).isPresent()) {
                AlertUtil.showError("Erreur",
                        "Le candidat sélectionné n'existe pas dans la base de données. Veuillez ajouter le candidat avant de planifier une séance.");
                return;
            }

            if (vehiculeId != null) {
                org.cpi2.service.VehiculeService vehiculeService = new org.cpi2.service.VehiculeService();
                if (!vehiculeService.getVehiculeById(vehiculeId).isPresent()) {
                    AlertUtil.showError("Erreur",
                            "Le véhicule sélectionné n'existe pas dans la base de données. Veuillez ajouter le véhicule avant de planifier une séance.");
                    return;
                }
            }

            boolean success = seanceService.saveSeance(seance);

            if (success) {

                if (seance.getId() != null) {
                    AlertUtil.showSuccess("Succès",
                            "La séance de conduite a été planifiée avec succès et la présence du candidat a été enregistrée!");
                } else {
                    AlertUtil.showSuccess("Succès partiel",
                            "La séance de conduite a été planifiée, mais l'enregistrement de présence pourrait ne pas avoir été effectué.");
                }
                cancelAction();
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

        quartierField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                quartierError.setText("Le nom de la rue est obligatoire");
                quartierError.setVisible(true);
                addErrorStyle(quartierField);
            } else {
                quartierError.setVisible(false);
                removeErrorStyle(quartierField);
            }
        });
    }
}


