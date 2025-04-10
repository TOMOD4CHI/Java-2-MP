package org.cpi2.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class AlertUtil {
    public static void showError(String title, String content) {
        showAlert(Alert.AlertType.ERROR, title, content);
    }

    public static void showInfo(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    public static void showWarning(String title, String content) {
        showAlert(Alert.AlertType.WARNING, title, content);
    }

    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        
        return alert.showAndWait().get() == ButtonType.OK;
    }

    private static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 