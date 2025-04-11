package org.cpi2.utils;

import javafx.scene.control.Alert;

/**
 * Gestionnaire d'alertes qui sert de façade pour les différentes implémentations d'alertes.
 * Cette classe permet de basculer facilement entre les alertes standard et les alertes modernes.
 */
public class AlertManager {
    
    // Définit si les alertes modernes sont activées (true) ou si on utilise les alertes standard (false)
    private static final boolean USE_MODERN_ALERTS = true;
    
    /**
     * Affiche une alerte d'erreur.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showError(String title, String content) {
        if (USE_MODERN_ALERTS) {
            ModernAlertUtils.showError(title, content);
        } else {
            AlertUtil.showError(title, content);
        }
    }
    
    /**
     * Affiche une alerte d'information.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showInfo(String title, String content) {
        if (USE_MODERN_ALERTS) {
            ModernAlertUtils.showInfo(title, content);
        } else {
            AlertUtil.showInfo(title, content);
        }
    }
    
    /**
     * Affiche une alerte d'avertissement.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showWarning(String title, String content) {
        if (USE_MODERN_ALERTS) {
            ModernAlertUtils.showWarning(title, content);
        } else {
            AlertUtil.showWarning(title, content);
        }
    }
    
    /**
     * Affiche une alerte de succès.
     * Cette méthode n'existe que dans la version moderne des alertes.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showSuccess(String title, String content) {
        if (USE_MODERN_ALERTS) {
            ModernAlertUtils.showSuccess(title, content);
        } else {
            // Fallback vers une alerte d'information standard
            AlertUtil.showInfo(title, content);
        }
    }
    
    /**
     * Affiche une alerte de confirmation et retourne le résultat.
     * 
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     * @return true si l'utilisateur a confirmé, false sinon
     */
    public static boolean showConfirmation(String title, String content) {
        if (USE_MODERN_ALERTS) {
            return ModernAlertUtils.showConfirmation(title, content);
        } else {
            return AlertUtil.showConfirmation(title, content);
        }
    }
    
    /**
     * Méthode pour afficher une alerte standard (pour compatibilité avec l'ancien code).
     * 
     * @param type Le type d'alerte JavaFX standard
     * @param title Le titre de l'alerte
     * @param content Le contenu de l'alerte
     */
    public static void showAlert(Alert.AlertType type, String title, String content) {
        if (USE_MODERN_ALERTS) {
            ModernAlertUtils.showAlert(type, title, content);
        } else {
            AlertUtil.showAlert(type, title, content);
        }
    }
}