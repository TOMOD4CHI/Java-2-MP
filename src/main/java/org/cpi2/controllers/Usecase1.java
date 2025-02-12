package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

public class Usecase1 {

    @FXML private TextField nameField;
    @FXML private TextField ageField;
    @FXML private ComboBox<String> genderComboBox;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private Button cancelButton;
    @FXML private Button confirmButton;

    // Action for the Cancel button
    @FXML
    private void cancelAction() {
        // Clear all fields (reset form)
        nameField.clear();
        ageField.clear();
        genderComboBox.getSelectionModel().clearSelection();
        emailField.clear();
        phoneField.clear();
    }

    // Action for the Confirm button
    @FXML
    private void confirmAction() {
        // Example validation (you can add more logic as needed)
        if (nameField.getText().isEmpty() || ageField.getText().isEmpty() ||
                genderComboBox.getSelectionModel().isEmpty() || emailField.getText().isEmpty() ||
                phoneField.getText().isEmpty()) {

            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Validation Error");
            alert.setHeaderText(null);
            alert.setContentText("Please fill out all fields!");
            alert.showAndWait();
        } else {
            // Here you can handle the form submission logic (e.g., saving to a database)
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Form Submitted");
            alert.setHeaderText(null);
            alert.setContentText("Candidate added successfully!");
            alert.showAndWait();

            // Optionally clear fields after submission
            cancelAction();
        }
    }
}
