package org.cpi2.controllers;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;

import java.io.IOException;

public class MainWindow {

    @FXML private StackPane contentArea;
    @FXML private Label profileLabel, coursesLabel, paymentsLabel, exampleLabel;
    @FXML private VBox profileSubmenu, coursesSubmenu, paymentsSubmenu, exampleSubmenu;
    @FXML private Label profileArrow, coursesArrow, paymentsArrow, exampleArrow;

    @FXML
    public void initialize() {
        setupMenuToggle(profileLabel, profileSubmenu, profileArrow);
        setupMenuToggle(coursesLabel, coursesSubmenu, coursesArrow);
        setupMenuToggle(paymentsLabel, paymentsSubmenu, paymentsArrow);
        setupMenuToggle(exampleLabel, exampleSubmenu, exampleArrow);
    }

    private void setupMenuToggle(Label menuLabel, VBox submenu, Label arrow) {
        menuLabel.setOnMouseClicked(event -> {
            boolean isVisible = submenu.isVisible();
            if (isVisible) {
                fadeOut(submenu);
                arrow.setText("\uD83D\uDC64--▶");
            } else {
                fadeIn(submenu);
                arrow.setText("\uD83D\uDC64--▼");
            }
        });
    }

    private void fadeIn(VBox submenu) {
        submenu.setManaged(true);
        submenu.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), submenu);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    private void fadeOut(VBox submenu) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), submenu);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            submenu.setVisible(false);
            submenu.setManaged(false);
        });
        ft.play();
    }

    public void loadUsecase1(MouseEvent mouseEvent) {
        // Load the Usecase1 FXML file dynamically
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Usecase1.fxml"));
        StackPane usecase1Content = null; // Load Usecase1.fxml into a StackPane
        try {
            usecase1Content = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Replace the content area (center of the BorderPane) with the Usecase1 content
        contentArea.getChildren().clear();  // Clear any existing content in the center
        contentArea.getChildren().add(usecase1Content);  // Add Usecase1 content to the center
    }
    public void loadUsecase2(MouseEvent mouseEvent) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxmls/Usecase2.fxml"));
        Parent usecase2Content;
        try {
            usecase2Content = loader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        contentArea.getChildren().clear();  // Clear existing content
        contentArea.getChildren().add(usecase2Content);  // Add Usecase2 content
    }

}
