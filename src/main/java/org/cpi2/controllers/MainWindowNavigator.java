package org.cpi2.controllers;

/**
 * Helper class for navigating between views in the main window
 * This is a utility class for the AccueilPageController to avoid code duplication with MainWindow class
 */
public class MainWindowNavigator {

    // Reference to the main window controller
    private static MainWindow mainWindowController;

    /**
     * Set the main window controller for navigation
     * @param controller The MainWindow controller
     */
    public static void setMainWindowController(MainWindow controller) {
        mainWindowController = controller;
    }

    // Navigation methods that delegate to the MainWindow controller
    
    public static void loadAfficherCandidat() {
        if (mainWindowController != null) {
            mainWindowController.loadAfficherCandidat();
        }
    }

    public static void loadAfficherSeance() {
        if (mainWindowController != null) {
            mainWindowController.loadAfficherSeance();
        }
    }

    public static void loadAfficherMoniteur() {
        if (mainWindowController != null) {
            mainWindowController.loadAfficherMoniteur();
        }
    }

    public static void loadAjouterCandidat() {
        if (mainWindowController != null) {
            mainWindowController.loadAjouterCandidat();
        }
    }

    public static void loadSessionConduite() {
        if (mainWindowController != null) {
            mainWindowController.loadSessionConduite();
        }
    }

    public static void loadPayment() {
        if (mainWindowController != null) {
            mainWindowController.loadPayment();
        }
    }

    public static void loadGestionVehicules() {
        if (mainWindowController != null) {
            mainWindowController.loadGestionVehicules();
        }
    }

    public static void loadExamRegistration() {
        if (mainWindowController != null) {
            mainWindowController.loadExamRegistration();
        }
    }

    public static void loadDashboardFinance() {
        if (mainWindowController != null) {
            mainWindowController.loadDashboardFinance();
        }
    }

    public static void loadNotifications() {
        if (mainWindowController != null) {
            mainWindowController.loadNotifications();
        }
    }

    public static void loadAutoEcoleManagement() {
        if (mainWindowController != null) {
            mainWindowController.loadAutoEcoleManagement();
        }
    }
} 