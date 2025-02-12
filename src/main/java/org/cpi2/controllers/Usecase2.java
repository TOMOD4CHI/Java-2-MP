package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class Usecase2{

    @FXML private WebView mapView;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private TextField field1;
    @FXML private TextField field2;
    @FXML private TextField field3;
    @FXML private TextField field4;




        @FXML
        public void initialize() {
            WebEngine webEngine = mapView.getEngine();
            webEngine.loadContent(getMapHtml());

            // Connect Java with JavaScript
            webEngine.documentProperty().addListener((obs, oldDoc, newDoc) -> {
                if (newDoc != null) {
                    JSObject window = (JSObject) webEngine.executeScript("window");
                    window.setMember("javaConnector", this);
                }
            });
        }

        /** Called from JavaScript */
        public void updateCoordinates(String lat, String lng) {
            latitudeField.setText(lat);
            longitudeField.setText(lng);
        }

        private String getMapHtml() {
            return "<html>" +
                    "<head>" +
                    "   <link rel='stylesheet' href='https://unpkg.com/leaflet@1.7.1/dist/leaflet.css'/>" +
                    "   <script src='https://unpkg.com/leaflet@1.7.1/dist/leaflet.js'></script>" +
                    "</head>" +
                    "<body>" +
                    "   <div id='mapid' style='height: 600px;'></div>" +
                    "   <script>" +
                    "       var map = L.map('mapid').setView([37.276, 9.873], 13);" +
                    "       L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png').addTo(map);" +
                    "       map.on('click', function(e) {" +
                    "           var lat = e.latlng.lat.toFixed(6);" +
                    "           var lng = e.latlng.lng.toFixed(6);" +
                    "           console.log('Map Clicked: ', lat, lng);" +
                    "           window.javaConnector.updateCoordinates(lat, lng);" +
                    "       });" +
                    "   </script>" +
                    "</body>" +
                    "</html>";
        }

    @FXML
    private void handleSubmit(ActionEvent event) {
        // Ensure all fields are filled
        if (field1.getText().isEmpty() || field2.getText().isEmpty() ||
                field3.getText().isEmpty() || field4.getText().isEmpty() ||
                latitudeField.getText().isEmpty() || longitudeField.getText().isEmpty()) {

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill out all fields before submitting!");
            alert.showAndWait();
        } else {
            // Process the form data (store or send it)
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Form Submitted");
            alert.setHeaderText(null);
            alert.setContentText("Form submitted successfully!");
            alert.showAndWait();
        }
    }
}





