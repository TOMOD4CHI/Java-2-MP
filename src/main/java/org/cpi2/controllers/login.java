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
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class login  {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Text actionTarget;
    @FXML private Pane logoContainer;


    public void initialize() {
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

    @FXML
    protected void handleLoginButtonAction(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();


        if (username.isEmpty() || password.isEmpty()) {
            actionTarget.setText("Username and password cannot be empty");
            return;
        }


        if (username.equals("user") && password.equals("pass")) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/MainWindow.fxml"));
                Parent mainRoot = loader.load();

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setTitle("Main Application");
                stage.setScene(new Scene(mainRoot));
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