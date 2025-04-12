package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
    @FXML private Pane illustrationContainer;
    
    private final AuthenticationService authService = new AuthenticationService();


    public void initialize() {
        ImageView logoImageView = (ImageView) illustrationContainer.getChildren().get(0);

        logoImageView.setOpacity(0);
        logoImageView.setRotate(-90);

        FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1.5), logoImageView);
        fadeTransition.setFromValue(0);
        fadeTransition.setToValue(1);

        RotateTransition rotateTransition = new RotateTransition(Duration.seconds(1.5), logoImageView);
        rotateTransition.setFromAngle(-90);
        rotateTransition.setToAngle(0);

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.getChildren().addAll(fadeTransition, rotateTransition);

        parallelTransition.play();

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

        if (logoContainer != null) {
            logoContainer.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    Platform.runLater(() -> {
                        Stage stage = (Stage) newScene.getWindow();
                        if (stage != null) {
                            stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
                        }
                    });
                }
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

        actionTarget.setText("");

        if (username.isEmpty()) {
            actionTarget.setText("Le nom d'utilisateur ne peut pas etre vide");
            return;
        }
        
        if (password.isEmpty()) {
            actionTarget.setText("le mot de passe ne peut pas etre vide ");
            return;
        }
        
        if (password.length() < 4) {
            actionTarget.setText("mot de passe doit contenir au moins 4 caractéres");
            return;
        }

        if (authService.authenticate(username, password)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainWindow.fxml"));
                Parent mainRoot = loader.load();
                
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                double width = stage.getWidth();
                double height = stage.getHeight();

                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));

                Scene scene = new Scene(mainRoot, width, height);
                stage.setTitle("Main Application");
                stage.setScene(scene);
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                actionTarget.setText("Error loading main window");
            }
        } else {
            actionTarget.setText("mot de passe ou nom d'utilisateur incorrecte");
        }
    }
}
