package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.cpi2.service.AuthenticationService;

import java.io.IOException;

public class login  {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Text actionTarget;
    @FXML private Pane logoContainer;
    
    private final AuthenticationService authService = new AuthenticationService();


    public void initialize() {
        setupLogoAnimation();
        
        // Set up input validation listeners
        if (usernameField != null) {
            usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateInput();
            });
        }
        
        if (passwordField != null) {
            passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
                validateInput();
            });
        }
    }
    
    private void setupLogoAnimation() {
        if (logoContainer == null) return;
        
        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1.5), logoContainer);
        rotateTransition.setFromAngle(-180);
        rotateTransition.setToAngle(0);
        rotateTransition.setInterpolator(Interpolator.EASE_OUT);

        ScaleTransition scaleTransition = new ScaleTransition(Duration.seconds(1.5), logoContainer);
        scaleTransition.setFromX(0.8);
        scaleTransition.setFromY(0.8);
        scaleTransition.setToX(1.0);
        scaleTransition.setToY(1.0);
        scaleTransition.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), logoContainer);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(0.7);
        fadeTransition.setInterpolator(Interpolator.EASE_OUT);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(rotateTransition, scaleTransition, fadeTransition);
        parallelTransition.play();
    }
    
    private void validateInput() {
        if (actionTarget != null) {
            actionTarget.setText("");
        }
    }

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        if (usernameField == null || passwordField == null || actionTarget == null) {
            System.err.println("Error: UI components not properly initialized");
            return;
        }
        
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Clear previous error message
        actionTarget.setText("");
        
        // Validate inputs
        if (username.isEmpty()) {
            actionTarget.setText("Username cannot be empty");
            return;
        }
        
        if (password.isEmpty()) {
            actionTarget.setText("Password cannot be empty");
            return;
        }
        
        if (password.length() < 4) {
            actionTarget.setText("Password must be at least 4 characters");
            return;
        }

        // Authenticate against database
        if (authService.authenticate(username, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainWindow.fxml"));
                Parent mainRoot = loader.load();
                
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                
                // Get current window dimensions to maintain consistency
                double width = stage.getWidth();
                double height = stage.getHeight();
                
                // Set app icon using local resource
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                
                // Create new scene with same dimensions
                Scene scene = new Scene(mainRoot, width, height);
                stage.setTitle("Main Application");
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                actionTarget.setText("Error loading main window");
            }
        } else {
            actionTarget.setText("Invalid username or password");
        }
    }
}