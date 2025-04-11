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
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Optional;

/**
 * Classe utilitaire pour afficher des alertes modernes et stylisées dans l'application.
 * Cette classe remplace les alertes standard de JavaFX par des alertes personnalisées
 * avec des animations et un style moderne.
 */
public class ModernAlertUtils {
    
    // Couleurs pour les différents types d'alertes
    private static final String SUCCESS_COLOR = "#4CAF50";
    private static final String ERROR_COLOR = "#F44336";
    private static final String WARNING_COLOR = "#FF9800";
    private static final String INFO_COLOR = "#2196F3";
    private static final String CONFIRM_COLOR = "#673AB7";
    
    /**
     * Affiche une alerte d'erreur moderne.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showError(String title, String content) {
        showModernAlert(AlertType.ERROR, title, content);
    }
    
    /**
     * Affiche une alerte d'information moderne.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showInfo(String title, String content) {
        showModernAlert(AlertType.INFORMATION, title, content);
    }
    
    /**
     * Affiche une alerte d'avertissement moderne.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showWarning(String title, String content) {
        showModernAlert(AlertType.WARNING, title, content);
    }
    
    /**
     * Affiche une alerte de succès moderne.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showSuccess(String title, String content) {
        showModernAlert(AlertType.SUCCESS, title, content);
    }
    
    /**
     * Affiche une alerte de confirmation moderne et retourne le résultat.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmation(String title, String content) {
        return showModernConfirmation(title, content);
    }
    
    /**
     * Méthode pour afficher une alerte standard (pour compatibilité avec l'ancien code).
     * Cette méthode utilise les alertes modernes mais conserve la signature de l'ancienne méthode.
     * 
     * @param type Le type d'alerte JavaFX standard
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
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
    
    /**
     * Types d'alertes modernes disponibles.
     */
    public enum AlertType {
        ERROR, WARNING, INFORMATION, SUCCESS
    }
    
    /**
     * Affiche une alerte moderne avec animation.
     * 
     * @param type Le type d'alerte moderne
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    private static void showModernAlert(AlertType type, String title, String content) {
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            
            // Création de l'icône en fonction du type d'alerte
            ImageView icon = new ImageView();
            String color;
            
            switch (type) {
                case ERROR:
                    icon.setImage(createIcon("\uf071", ERROR_COLOR)); // Icône d'erreur (FontAwesome)
                    color = ERROR_COLOR;
                    break;
                case WARNING:
                    icon.setImage(createIcon("\uf06a", WARNING_COLOR)); // Icône d'avertissement
                    color = WARNING_COLOR;
                    break;
                case SUCCESS:
                    icon.setImage(createIcon("\uf058", SUCCESS_COLOR)); // Icône de succès
                    color = SUCCESS_COLOR;
                    break;
                case INFORMATION:
                default:
                    icon.setImage(createIcon("\uf05a", INFO_COLOR)); // Icône d'information
                    color = INFO_COLOR;
                    break;
            }
            
            icon.setFitHeight(48);
            icon.setFitWidth(48);
            
            // Création du titre
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            titleLabel.setTextFill(Color.web("#2d3748"));
            
            // Création du contenu
            Label contentLabel = new Label(content);
            contentLabel.setFont(Font.font("Segoe UI", 14));
            contentLabel.setTextFill(Color.web("#4a5568"));
            contentLabel.setWrapText(true);
            
            // Bouton OK
            Button okButton = new Button("OK");
            okButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
            okButton.setPrefWidth(100);
            okButton.setPrefHeight(30);
            okButton.setOnAction(e -> stage.close());
            
            // Effet de survol pour le bouton
            okButton.setOnMouseEntered(e -> okButton.setStyle("-fx-background-color: derive(" + color + ", 20%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
            okButton.setOnMouseExited(e -> okButton.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
            
            // Layout pour le texte
            VBox textBox = new VBox(5, titleLabel, contentLabel);
            textBox.setPadding(new Insets(0, 0, 0, 10));
            HBox.setHgrow(textBox, Priority.ALWAYS);
            
            // Layout pour l'icône et le texte
            HBox headerBox = new HBox(10, icon, textBox);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(15, 15, 15, 15));
            
            // Layout pour le bouton
            HBox buttonBox = new HBox(okButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 15, 15, 15));
            
            // Layout principal
            VBox mainBox = new VBox(10, headerBox, buttonBox);
            mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            
            // Effet d'ombre
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(10.0);
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(0);
            dropShadow.setColor(Color.color(0, 0, 0, 0.2));
            mainBox.setEffect(dropShadow);
            
            // Conteneur principal avec marge
            StackPane root = new StackPane(mainBox);
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: transparent;");
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            
            stage.setScene(scene);
            stage.setMinWidth(400);
            stage.setMinHeight(200);
            
            // Animation d'entrée
            mainBox.setScaleX(0.7);
            mainBox.setScaleY(0.7);
            mainBox.setOpacity(0);
            
            Timeline timeline = new Timeline();
            KeyValue kvScaleX = new KeyValue(mainBox.scaleXProperty(), 1);
            KeyValue kvScaleY = new KeyValue(mainBox.scaleYProperty(), 1);
            KeyValue kvOpacity = new KeyValue(mainBox.opacityProperty(), 1);
            
            KeyFrame kf = new KeyFrame(Duration.millis(150), kvOpacity, kvScaleX, kvScaleY);
            timeline.getKeyFrames().add(kf);
            timeline.play();
            
            stage.showAndWait();
        });
    }
    
    /**
     * Affiche une alerte de confirmation moderne et retourne le résultat.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     * @return true si l'utilisateur a confirmé, false sinon
     */
    private static boolean showModernConfirmation(String title, String content) {
        final boolean[] result = {false};
        
        Platform.runLater(() -> {
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initStyle(StageStyle.TRANSPARENT);
            
            // Création de l'icône
            ImageView icon = new ImageView();
            icon.setImage(createIcon("\uf059", CONFIRM_COLOR)); // Icône de question
            icon.setFitHeight(48);
            icon.setFitWidth(48);
            
            // Création du titre
            Label titleLabel = new Label(title);
            titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
            titleLabel.setTextFill(Color.web("#2d3748"));
            
            // Création du contenu
            Label contentLabel = new Label(content);
            contentLabel.setFont(Font.font("Segoe UI", 14));
            contentLabel.setTextFill(Color.web("#4a5568"));
            contentLabel.setWrapText(true);
            
            // Boutons
            Button confirmButton = new Button("Confirmer");
            confirmButton.setStyle("-fx-background-color: " + CONFIRM_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;");
            confirmButton.setPrefWidth(120);
            confirmButton.setPrefHeight(30);
            confirmButton.setOnAction(e -> {
                result[0] = true;
                stage.close();
            });
            
            Button cancelButton = new Button("Annuler");
            cancelButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-background-radius: 20;");
            cancelButton.setPrefWidth(120);
            cancelButton.setPrefHeight(30);
            cancelButton.setOnAction(e -> {
                result[0] = false;
                stage.close();
            });
            
            // Effet de survol pour les boutons
            confirmButton.setOnMouseEntered(e -> confirmButton.setStyle("-fx-background-color: derive(" + CONFIRM_COLOR + ", 20%); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
            confirmButton.setOnMouseExited(e -> confirmButton.setStyle("-fx-background-color: " + CONFIRM_COLOR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 20;"));
            
            cancelButton.setOnMouseEntered(e -> cancelButton.setStyle("-fx-background-color: #EEEEEE; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-background-radius: 20;"));
            cancelButton.setOnMouseExited(e -> cancelButton.setStyle("-fx-background-color: #E0E0E0; -fx-text-fill: #424242; -fx-font-weight: bold; -fx-background-radius: 20;"));
            
            // Layout pour le texte
            VBox textBox = new VBox(5, titleLabel, contentLabel);
            textBox.setPadding(new Insets(0, 0, 0, 10));
            HBox.setHgrow(textBox, Priority.ALWAYS);
            
            // Layout pour l'icône et le texte
            HBox headerBox = new HBox(10, icon, textBox);
            headerBox.setAlignment(Pos.CENTER_LEFT);
            headerBox.setPadding(new Insets(15, 15, 15, 15));
            
            // Layout pour les boutons
            HBox buttonBox = new HBox(10, cancelButton, confirmButton);
            buttonBox.setAlignment(Pos.CENTER_RIGHT);
            buttonBox.setPadding(new Insets(0, 15, 15, 15));
            
            // Layout principal
            VBox mainBox = new VBox(10, headerBox, buttonBox);
            mainBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
            
            // Effet d'ombre
            DropShadow dropShadow = new DropShadow();
            dropShadow.setRadius(10.0);
            dropShadow.setOffsetX(0);
            dropShadow.setOffsetY(0);
            dropShadow.setColor(Color.color(0, 0, 0, 0.2));
            mainBox.setEffect(dropShadow);
            
            // Conteneur principal avec marge
            StackPane root = new StackPane(mainBox);
            root.setPadding(new Insets(10));
            root.setStyle("-fx-background-color: transparent;");
            
            Scene scene = new Scene(root);
            scene.setFill(Color.TRANSPARENT);
            
            stage.setScene(scene);
            stage.setMinWidth(450);
            stage.setMinHeight(200);
            
            // Animation d'entrée
            mainBox.setScaleX(0.7);
            mainBox.setScaleY(0.7);
            mainBox.setOpacity(0);
            
            Timeline timeline = new Timeline();
            KeyValue kvScaleX = new KeyValue(mainBox.scaleXProperty(), 1);
            KeyValue kvScaleY = new KeyValue(mainBox.scaleYProperty(), 1);
            KeyValue kvOpacity = new KeyValue(mainBox.opacityProperty(), 1);
            
            KeyFrame kf = new KeyFrame(Duration.millis(150), kvOpacity, kvScaleX, kvScaleY);
            timeline.getKeyFrames().add(kf);
            timeline.play();
            
            stage.showAndWait();
        });
        
        return result[0];
    }
    
    /**
     * Crée une icône pour l'alerte en utilisant un caractère Unicode (pour simuler FontAwesome).
     * 
     * @param unicode Le caractère Unicode à utiliser
     * @param color La couleur de l'icône
     * @return Une image contenant l'icône
     */
    private static Image createIcon(String unicode, String color) {
        // Création d'un label avec le caractère Unicode
        Label iconLabel = new Label(unicode);
        iconLabel.setFont(Font.font("FontAwesome", 48)); // Utilise FontAwesome si disponible, sinon utilise la police par défaut
        iconLabel.setTextFill(Color.web(color));
        
        // Création d'une région pour contenir l'icône
        Region icon = new Region();
        icon.setStyle("-fx-background-color: " + color + "; -fx-shape: 'M256 8C119.043 8 8 119.083 8 256c0 136.997 111.043 248 248 248s248-111.003 248-248C504 119.083 392.957 8 256 8zm0 448c-110.532 0-200-89.431-200-200 0-110.495 89.472-200 200-200 110.491 0 200 89.471 200 200 0 110.53-89.431 200-200 200zm0-338c23.196 0 42 18.804 42 42s-18.804 42-42 42-42-18.804-42-42 18.804-42 42-42zm56 254c0 6.627-5.373 12-12 12h-88c-6.627 0-12-5.373-12-12v-24c0-6.627 5.373-12 12-12h12v-64h-12c-6.627 0-12-5.373-12-12v-24c0-6.627 5.373-12 12-12h64c6.627 0 12 5.373 12 12v100h12c6.627 0 12 5.373 12 12v24z';");
        icon.setPrefSize(48, 48);
        
        // Utilisation de l'icône prédéfinie si disponible, sinon utilisation du label
        try {
            // Essayer de charger une icône depuis les ressources
            String iconPath = "/icons/" + getIconName(unicode) + ".png";
            Image image = new Image(Objects.requireNonNull(ModernAlertUtils.class.getResourceAsStream(iconPath)));
            return image;
        } catch (Exception e) {
            // Fallback: utiliser l'icône générée par le code
            return icon.snapshot(null, null);
        }
    }
    
    /**
     * Obtient le nom de l'icône en fonction du caractère Unicode.
     * 
     * @param unicode Le caractère Unicode
     * @return Le nom de l'icône correspondante
     */
    private static String getIconName(String unicode) {
        switch (unicode) {
            case "\uf071": return "error";
            case "\uf06a": return "warning";
            case "\uf058": return "success";
            case "\uf05a": return "info";
            case "\uf059": return "question";
            default: return "info";
        }
    }
}