package org.cpi2.controllers;


public class MainWindowNavigator {

    private static MainWindow mainWindowController;

    
    public static void setMainWindowController(MainWindow controller) {
        mainWindowController = controller;
    }

    
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

