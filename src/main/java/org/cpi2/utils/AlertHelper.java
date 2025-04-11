package org.cpi2.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * Utility class for displaying different types of alerts to the user.
 */
public class AlertHelper {

    /**
     * Display an information alert with the given title and content.
     *
     * @param title   The title of the alert
     * @param content The content message
     */
    public static void showInformation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Display an error alert with the given title and content.
     *
     * @param error
     * @param title   The title of the alert
     * @param content The content message
     */
    public static void showError(String error, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Display a warning alert with the given title and content.
     *
     * @param title   The title of the alert
     * @param content The content message
     */
    public static void showWarning(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Display a confirmation alert with the given title and content.
     *
     * @param title   The title of the alert
     * @param content The content message
     * @return true if OK was clicked, false otherwise
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().get() == ButtonType.OK;
    }
}
