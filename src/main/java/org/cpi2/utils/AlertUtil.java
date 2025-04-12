package org.cpi2.utils;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.InputStream;
import java.util.Objects;

public class AlertUtil {

    // Color constants
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String ERROR_COLOR = "#F44336";
    private static final String WARNING_COLOR = "#FF9800";
    private static final String INFO_COLOR = "#2196F3";
    private static final String CONFIRM_COLOR = "#673AB7";

    // Font constants
    private static final String FONT_FAMILY = "Segoe UI";
    private static final double TITLE_FONT_SIZE = 18;
    private static final double CONTENT_FONT_SIZE = 14;
    private static final double ICON_SIZE = 48;

    // Load FontAwesome font
    private static Font fontAwesome;
    static {
        try {
            InputStream is = AlertUtil.class.getResourceAsStream("/fonts/fontawesome-solid-900.ttf");
            if (is != null) {
                fontAwesome = Font.loadFont(is, 48);
            }
        } catch (Exception e) {
            System.err.println("Failed to load FontAwesome font: " + e.getMessage());
        }
    }

    public static void showError(String title, String content) {
        showModernAlert(AlertType.ERROR, title, content);
    }

    public static void showInfo(String title, String content) {
        showModernAlert(AlertType.INFORMATION, title, content);
    }

    public static void showWarning(String title, String content) {
        showModernAlert(AlertType.WARNING, title, content);
    }

    public static void showSuccess(String title, String content) {
        showModernAlert(AlertType.SUCCESS, title, content);
    }

    public static boolean showConfirmation(String title, String content) {
        return showModernConfirmation(title, content);
    }

    public static void showAlert(Alert.AlertType type, String title, String content) {
        AlertType modernType;
        switch (type) {
            case ERROR:
                modernType = AlertType.ERROR;
                break;
            case WARNING:
                modernType = AlertType.WARNING;
                break;
            case INFORMATION:
                modernType = AlertType.INFORMATION;
                break;
            case CONFIRMATION:
                showModernConfirmation(title, content);
                return;
            default:
                modernType = AlertType.INFORMATION;
        }
        showModernAlert(modernType, title, content);
    }

    public enum AlertType {
        ERROR, WARNING, INFORMATION, SUCCESS
    }

    private static void showModernAlert(AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            StackPane iconContainer = createIconContainer(type);
            String color = getColorForType(type);

            Label titleLabel = createTitleLabel(title);
            Label contentLabel = createContentLabel(content);

            Button okButton = createButton("OK", color, 100, 36);
            okButton.setOnAction(e -> stage.close());

            VBox textBox = new VBox(8, titleLabel, contentLabel);
            textBox.setPadding(new Insets(0, 0, 0, 15));
            HBox.setHgrow(textBox, Priority.ALWAYS);

            HBox headerBox = new HBox(15, iconContainer, textBox);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(20, 20, 20, 20));

            HBox buttonBox = new HBox(okButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 20, 20, 20));

            VBox mainBox = new VBox(10, headerBox, buttonBox);
            mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

            // Apply shadow effect
            mainBox.setEffect(createShadowEffect());

            StackPane root = new StackPane(mainBox);
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setMinWidth(450);
            stage.setMinHeight(200);

            // Animation
            animateDialog(mainBox);

            stage.showAndWait();
        });
    }

    private static boolean showModernConfirmation(String title, String content) {
        final boolean[] result = {false};

        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            StackPane iconContainer = createIconContainer(null); // Confirmation icon

            Label titleLabel = createTitleLabel(title);
            Label contentLabel = createContentLabel(content);

            Button confirmButton = createButton("Confirmer", CONFIRM_COLOR, 130, 36);
            confirmButton.setOnAction(e -> {
                result[0] = true;
                stage.close();
            });

            Button cancelButton = createButton("Annuler", "#E0E0E0", 130, 36);
            cancelButton.setStyle(cancelButton.getStyle() + "; -fx-text-fill: #424242;");
            cancelButton.setOnAction(e -> {
                result[0] = false;
                stage.close();
            });

            VBox textBox = new VBox(8, titleLabel, contentLabel);
            textBox.setPadding(new Insets(0, 0, 0, 15));
            HBox.setHgrow(textBox, Priority.ALWAYS);

            HBox headerBox = new HBox(15, iconContainer, textBox);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(20, 20, 20, 20));

            HBox buttonBox = new HBox(15, cancelButton, confirmButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 20, 20, 20));

            VBox mainBox = new VBox(10, headerBox, buttonBox);
            mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

            // Apply shadow effect
            mainBox.setEffect(createShadowEffect());

            StackPane root = new StackPane(mainBox);
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setMinWidth(500);
            stage.setMinHeight(220);

            // Animation
            animateDialog(mainBox);

            stage.showAndWait();
        });

        return result[0];
    }

    // Helper methods for cleaner code

    private static String getColorForType(AlertType type) {
        if (type == null) {
            return CONFIRM_COLOR;
        }

        switch (type) {
            case ERROR: return ERROR_COLOR;
            case WARNING: return WARNING_COLOR;
            case SUCCESS: return SUCCESS_COLOR;
            case INFORMATION: default: return INFO_COLOR;
        }
    }

    private static StackPane createIconContainer(AlertType type) {
        String color = getColorForType(type);
        String iconChar = getIconCharacter(type);

        // Create a circular background
        Circle circle = new Circle(ICON_SIZE / 2);
        circle.setFill(Color.web(color));

        // Create the icon text
        Text icon = new Text(iconChar);
        if (fontAwesome != null) {
            icon.setFont(fontAwesome);
        } else {
            // Fallback icon characters if FontAwesome is not available
            switch (iconChar) {
                case "\uf071": icon.setText("⚠"); break; // Error
                case "\uf06a": icon.setText("⚠"); break; // Warning
                case "\uf058": icon.setText("✓"); break; // Success
                case "\uf05a": icon.setText("ℹ"); break; // Info
                case "\uf059": icon.setText("?"); break; // Question
                default: icon.setText("ℹ");
            }
            icon.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        }
        icon.setFill(Color.WHITE);

        // Try to load from resources as backup
        try {
            String iconName = type == null ? "question" : type.name().toLowerCase();
            String path = "/icons/" + iconName + ".png";
            InputStream stream = AlertUtil.class.getResourceAsStream(path);

            if (stream != null) {
                ImageView imageView = new ImageView(new Image(stream));
                imageView.setFitWidth(ICON_SIZE - 16);
                imageView.setFitHeight(ICON_SIZE - 16);
                return new StackPane(circle, imageView);
            }
        } catch (Exception ignored) {
            // Continue with text fallback
        }

        return new StackPane(circle, icon);
    }

    private static String getIconCharacter(AlertType type) {
        if (type == null) {
            return "\uf059"; // Question mark for confirmation
        }

        switch (type) {
            case ERROR: return "\uf071"; // Exclamation triangle
            case WARNING: return "\uf06a"; // Exclamation circle
            case SUCCESS: return "\uf058"; // Check circle
            case INFORMATION: default: return "\uf05a"; // Info circle
        }
    }

    private static Label createTitleLabel(String title) {
        Label label = new Label(title);
        label.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, TITLE_FONT_SIZE));
        label.setTextFill(Color.web("#2d3748"));
        return label;
    }

    private static Label createContentLabel(String content) {
        Label label = new Label(content);
        label.setFont(Font.font(FONT_FAMILY, CONTENT_FONT_SIZE));
        label.setTextFill(Color.web("#4a5568"));
        label.setWrapText(true);
        label.setTextAlignment(TextAlignment.LEFT);
        return label;
    }

    private static Button createButton(String text, String color, double width, double height) {
        Button button = new Button(text);
        String style = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18;",
                color
        );
        button.setStyle(style);
        button.setPrefWidth(width);
        button.setPrefHeight(height);

        // Hover effects
        button.setOnMouseEntered(e ->
                button.setStyle(String.format(
                        "-fx-background-color: derive(%s, 20%%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18;",
                        color
                ))
        );
        button.setOnMouseExited(e ->
                button.setStyle(String.format(
                        "-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 18;",
                        color
                ))
        );

        return button;
    }

    private static DropShadow createShadowEffect() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(15.0);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setColor(Color.color(0, 0, 0, 0.15));
        return shadow;
    }

    private static void animateDialog(VBox dialog) {
        dialog.setScaleX(0.9);
        dialog.setScaleY(0.9);
        dialog.setOpacity(0);

        Timeline timeline = new Timeline();
        KeyValue kvScaleX = new KeyValue(dialog.scaleXProperty(), 1);
        KeyValue kvScaleY = new KeyValue(dialog.scaleYProperty(), 1);
        KeyValue kvOpacity = new KeyValue(dialog.opacityProperty(), 1);

        KeyFrame kf = new KeyFrame(Duration.millis(180), kvOpacity, kvScaleX, kvScaleY);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }
    public static int showOptionsDialog(String title, String content, String... options) {
        final int[] result = {-1};

        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);

            StackPane iconContainer = createIconContainer(null); // Confirmation icon

            Label titleLabel = createTitleLabel(title);
            Label contentLabel = createContentLabel(content);

            HBox buttonBox = new HBox(15);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 20, 20, 20));

            // Create buttons for each option
            for (int i = 0; i < options.length; i++) {
                final int index = i;
                Button button = createButton(options[i], i == 0 ? CONFIRM_COLOR : "#E0E0E0", 150, 36);
                if (i > 0) {
                    button.setStyle(button.getStyle() + "; -fx-text-fill: #424242;");
                }
                button.setOnAction(e -> {
                    result[0] = index;
                    stage.close();
                });
                buttonBox.getChildren().add(button);
            }

            VBox textBox = new VBox(8, titleLabel, contentLabel);
            textBox.setPadding(new Insets(0, 0, 0, 15));
            HBox.setHgrow(textBox, Priority.ALWAYS);

            HBox headerBox = new HBox(15, iconContainer, textBox);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(20, 20, 20, 20));

            VBox mainBox = new VBox(10, headerBox, buttonBox);
            mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

            // Apply shadow effect
            mainBox.setEffect(createShadowEffect());

            StackPane root = new StackPane(mainBox);
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: transparent;");

            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);

            stage.setScene(scene);
            stage.setMinWidth(500);
            stage.setMinHeight(220);

            // Animation
            animateDialog(mainBox);

            stage.showAndWait();
        });

        return result[0];
    }
}