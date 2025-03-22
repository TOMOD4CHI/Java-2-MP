package org.cpi2;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/fxmls/login.fxml"));
        Scene scene = new Scene(root, 800, 1100);
        primaryStage.setTitle("System Auto-École");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/images/app_icon.png")));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
