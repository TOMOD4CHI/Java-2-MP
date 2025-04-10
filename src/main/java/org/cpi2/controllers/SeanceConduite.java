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

import java.time.LocalDate;
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
    
    private final SeanceService seanceService = new SeanceService();
    
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
    }
    
    private void loadCandidats() {
        // Charger les candidats depuis la base de données
        org.cpi2.service.CandidatService candidatService = new org.cpi2.service.CandidatService();
        List<Candidat> candidatsList = candidatService.getAllCandidats();
        
        ObservableList<String> candidats = FXCollections.observableArrayList();
        
        if (candidatsList.isEmpty()) {
            // Afficher un message d'avertissement si aucun candidat n'est trouvé
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun candidat");
            alert.setHeaderText(null);
            alert.setContentText("Aucun candidat n'est disponible dans la base de données. Veuillez ajouter des candidats avant de planifier des séances.");
            alert.show();
        } else {
            // Ajouter les candidats à la liste déroulante
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
            // Afficher un message d'avertissement si aucun moniteur n'est trouvé
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun moniteur");
            alert.setHeaderText(null);
            alert.setContentText("Aucun moniteur n'est disponible dans la base de données. Veuillez ajouter des moniteurs avant de planifier des séances.");
            alert.show();
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
            // Afficher un message d'avertissement si aucun véhicule n'est trouvé
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Aucun véhicule");
            alert.setHeaderText(null);
            alert.setContentText("Aucun véhicule n'est disponible dans la base de données. Veuillez ajouter des véhicules avant de planifier des séances.");
            alert.show();
        } else {
            // Ajouter les véhicules à la liste déroulante
            for (org.cpi2.entities.Vehicule vehicule : vehiculesList) {
                vehicules.add(vehicule.getId() + " - " + vehicule.getMarque() + " " + vehicule.getModele() + " (" + vehicule.getImmatriculation() + ")");
            }
        }
        
        vehiculeCombo.setItems(vehicules);
    }

    public void updateCoordinates(String lat, String lng) {
        latitudeField.setText(lat);
        longitudeField.setText(lng);
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
    }

    @FXML
    private void handleSubmit(ActionEvent event) {
        // Validate all fields
        if (candidatCombo.getValue() == null || moniteurCombo.getValue() == null || 
            vehiculeCombo.getValue() == null || kilometrage.getText().isEmpty() ||
            date.getValue() == null || temps.getText().isEmpty() ||
            latitudeField.getText().isEmpty() || longitudeField.getText().isEmpty()) {

            showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                     "Veuillez remplir tous les champs avant de soumettre!");
            return;
        }
        
        try {
            // Extract IDs from selected values (format: "1 - Name")
            Long candidatId = Long.parseLong(candidatCombo.getValue().split(" - ")[0]);
            Long moniteurId = Long.parseLong(moniteurCombo.getValue().split(" - ")[0]);
            Long vehiculeId = Long.parseLong(vehiculeCombo.getValue().split(" - ")[0]);
            
            // Validate and parse kilometrage
            double km;
            try {
                km = Double.parseDouble(kilometrage.getText());
                if (km < 0) {
                    showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                             "Le kilométrage ne peut pas être négatif!");
                    return;
                }
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "Le kilométrage doit être un nombre!");
                return;
            }
            
            // Validate time format (HH:MM)
            String timeRegex = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
            if (!temps.getText().matches(timeRegex)) {
                showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                         "L'heure doit être au format HH:MM!");
                return;
            }
            
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
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Le moniteur sélectionné n'existe pas dans la base de données. Veuillez ajouter le moniteur avant de planifier une séance.");
                return;
            }
            
            // Vérifier si le candidat existe avant de sauvegarder
            org.cpi2.service.CandidatService candidatService = new org.cpi2.service.CandidatService();
            if (!candidatService.getCandidatById(candidatId).isPresent()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Le candidat sélectionné n'existe pas dans la base de données. Veuillez ajouter le candidat avant de planifier une séance.");
                return;
            }
            
            // Vérifier si le véhicule existe avant de sauvegarder
            if (vehiculeId != null) {
                org.cpi2.service.VehiculeService vehiculeService = new org.cpi2.service.VehiculeService();
                if (!vehiculeService.getVehiculeById(vehiculeId).isPresent()) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", 
                            "Le véhicule sélectionné n'existe pas dans la base de données. Veuillez ajouter le véhicule avant de planifier une séance.");
                    return;
                }
            }
            
            // Save to database
            boolean success = seanceService.saveSeance(seance);
            
            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Succès", 
                        "La séance de conduite a été planifiée avec succès!");
                cancelAction(); // Clear the form
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", 
                        "Échec de la planification de la séance de conduite!");
            }
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", 
                     "Une erreur s'est produite: " + e.getMessage());
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





