package org.cpi2.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Callback;
import org.cpi2.entities.Notification;
import org.cpi2.service.NotificationService;
import org.cpi2.utils.AlertHelper;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class Notifications implements Initializable {
    @FXML private ListView<Notification> recentNotificationsListView;
    @FXML private ListView<Notification> upcomingNotificationsListView;
    @FXML private ListView<Notification> processedNotificationsListView;
    @FXML private StackPane emptyPlaceholder;
    @FXML private Label totalNotificationsLabel;
    @FXML private Label notificationStats;
    
    // Filter controls
    @FXML private ComboBox<String> filterTypeComboBox;
    @FXML private ComboBox<String> filterStatusComboBox;
    @FXML private TextField searchField;
    
    // Details section
    @FXML private VBox detailsPane;
    @FXML private VBox detailsContent;
    @FXML private Circle typeIndicator;
    @FXML private Label typeIconText;
    @FXML private Label typeLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label priorityLabel;
    @FXML private Label titleLabel;
    @FXML private TextArea descriptionArea;
    @FXML private Label vehicleLabel;
    @FXML private Label statusLabel;
    @FXML private Label createdDateLabel;
    @FXML private Button markAsHandledBtn;
    @FXML private Button viewVehicleBtn;
    
    private NotificationService notificationService;
    private Notification selectedNotification;
    
    // Active tab tracking
    private ListView<Notification> activeListView;
    
    // Tab buttons for styling
    @FXML private Button recentTabButton;
    @FXML private Button upcomingTabButton;
    @FXML private Button processedTabButton;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        notificationService = new NotificationService();
        
        // Set default active list view
        activeListView = recentNotificationsListView;
        
        // Initialize filter combo boxes
        initializeFilters();
        
        // Set up cell factories for list views
        setupListViews();
        
        // Hide details panel initially
        detailsPane.setVisible(false);
        
        // Load data
        refreshNotifications();
    }
    
    private void initializeFilters() {
        // Type filter
        filterTypeComboBox.getItems().addAll(
            "Tous", "ASSURANCE", "VIGNETTE", "ENTRETIEN"
        );
        filterTypeComboBox.setValue("Tous");
        
        // Status filter
        filterStatusComboBox.getItems().addAll(
            "Tous", "ACTIVE", "A_VENIR", "TRAITEE"
        );
        filterStatusComboBox.setValue("Tous");
    }
    
    private void setupListViews() {
        // Configure cell factory for all list views
        Callback<ListView<Notification>, ListCell<Notification>> cellFactory = param -> new ListCell<Notification>() {
            @Override
            protected void updateItem(Notification notification, boolean empty) {
                super.updateItem(notification, empty);
                
                if (empty || notification == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // Create custom card-like cell
                    VBox container = new VBox(8);
                    container.getStyleClass().add("notification-cell-card");
                    container.setPrefWidth(param.getWidth() - 20);
                    
                    // Header row with type indicator and date
                    HBox header = new HBox(10);
                    header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    // Type indicator circle
                    StackPane typeIndicatorStack = new StackPane();
                    typeIndicatorStack.getStyleClass().add("cell-type-indicator");
                    
                    Circle statusCircle = new Circle(8);
                    statusCircle.getStyleClass().add("status-circle");
                    
                    // Set color based on notification type
                    switch (notification.getType()) {
                        case "ASSURANCE":
                            statusCircle.setFill(Color.web("#3498db"));
                            break;
                        case "VIGNETTE":
                            statusCircle.setFill(Color.web("#2ecc71"));
                            break;
                        case "ENTRETIEN":
                            statusCircle.setFill(Color.web("#f39c12"));
                            break;
                        default:
                            statusCircle.setFill(Color.GRAY);
                    }
                    
                    // Add type icon text
                    Label iconText = new Label(getTypeIcon(notification.getType()));
                    iconText.getStyleClass().add("cell-type-icon");
                    
                    typeIndicatorStack.getChildren().addAll(statusCircle, iconText);
                    
                    // Type label
                    Label typeLabel = new Label(getTypeDisplayName(notification.getType()));
                    typeLabel.getStyleClass().add("notification-type");
                    
                    // Spacer
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                    
                    // Date label
                    Label dateLabel = new Label();
                    if (notification.getDateEcheance() != null) {
                        dateLabel.setText(notification.getDateEcheance().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    } else {
                        dateLabel.setText("--");
                    }
                    dateLabel.getStyleClass().add("date-label");
                    
                    // Priority indicator
                    Label priorityLabel = new Label(getPriorityDisplayName(notification.getPriorite()));
                    priorityLabel.getStyleClass().addAll("priority-label");
                    
                    switch (notification.getPriorite()) {
                        case "HAUTE":
                            priorityLabel.getStyleClass().add("priority-high");
                            break;
                        case "MOYENNE":
                            priorityLabel.getStyleClass().add("priority-medium");
                            break;
                        case "BASSE":
                            priorityLabel.getStyleClass().add("priority-low");
                            break;
                    }
                    
                    header.getChildren().addAll(typeIndicatorStack, typeLabel, spacer, priorityLabel, dateLabel);
                    
                    // Title row
                    Label titleLabel = new Label(notification.getTitre());
                    titleLabel.getStyleClass().add("notification-title");
                    titleLabel.setWrapText(true);
                    
                    // Vehicle row
                    Label vehicleLabel = new Label(notification.getVehiculeInfo());
                    vehicleLabel.getStyleClass().add("notification-vehicle");
                    
                    // Build final container
                    container.getChildren().addAll(header, titleLabel, vehicleLabel);
                    setGraphic(container);
                }
            }
        };
        
        // Apply the cell factory to all list views
        recentNotificationsListView.setCellFactory(cellFactory);
        upcomingNotificationsListView.setCellFactory(cellFactory);
        processedNotificationsListView.setCellFactory(cellFactory);
        
        // Add selection listeners
        recentNotificationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showNotificationDetails(newVal);
                // Clear selection in other lists
                upcomingNotificationsListView.getSelectionModel().clearSelection();
                processedNotificationsListView.getSelectionModel().clearSelection();
            }
        });
        
        upcomingNotificationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showNotificationDetails(newVal);
                // Clear selection in other lists
                recentNotificationsListView.getSelectionModel().clearSelection();
                processedNotificationsListView.getSelectionModel().clearSelection();
            }
        });
        
        processedNotificationsListView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                showNotificationDetails(newVal);
                // Clear selection in other lists
                recentNotificationsListView.getSelectionModel().clearSelection();
                upcomingNotificationsListView.getSelectionModel().clearSelection();
            }
        });
    }
    
    private void showNotificationDetails(Notification notification) {
        selectedNotification = notification;
        detailsPane.setVisible(true);
        
        // Set notification details
        typeLabel.setText(getTypeDisplayName(notification.getType()));
        dueDateLabel.setText(notification.getDateEcheance().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        priorityLabel.setText(getPriorityDisplayName(notification.getPriorite()));
        titleLabel.setText(notification.getTitre());
        descriptionArea.setText(notification.getDescription());
        vehicleLabel.setText(notification.getVehiculeInfo());
        statusLabel.setText(getStatusDisplayName(notification.getStatut()));
        createdDateLabel.setText(notification.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
        
        // Set type icon
        typeIconText.setText(getTypeIcon(notification.getType()));
        
        // Set status color
        switch (notification.getType()) {
            case "ASSURANCE":
                typeIndicator.setFill(Color.web("#3498db"));
                break;
            case "VIGNETTE":
                typeIndicator.setFill(Color.web("#2ecc71"));
                break;
            case "ENTRETIEN":
                typeIndicator.setFill(Color.web("#f39c12"));
                break;
            default:
                typeIndicator.setFill(Color.GRAY);
        }
        
        // Set priority style
        priorityLabel.getStyleClass().removeAll("priority-high", "priority-medium", "priority-low");
        switch (notification.getPriorite()) {
            case "HAUTE":
                priorityLabel.getStyleClass().add("priority-high");
                break;
            case "MOYENNE":
                priorityLabel.getStyleClass().add("priority-medium");
                break;
            case "BASSE":
                priorityLabel.getStyleClass().add("priority-low");
                break;
        }
        
        // Enable/disable mark as handled button based on status
        markAsHandledBtn.setDisable("TRAITEE".equals(notification.getStatut()));
    }
    
    /**
     * Get the appropriate icon for a notification type
     * @param type The notification type
     * @return Unicode icon character
     */
    private String getTypeIcon(String type) {
        switch (type) {
            case "ASSURANCE": return "\uD83D\uDCB6"; // 💶 Money
            case "VIGNETTE": return "\uD83D\uDCCB"; // 📋 Clipboard
            case "ENTRETIEN": return "\uD83D\uDD27"; // 🔧 Wrench
            default: return "\uD83D\uDCE8"; // 📨 Letter
        }
    }
    
    private String getTypeDisplayName(String type) {
        switch (type) {
            case "ASSURANCE": return "Assurance";
            case "VIGNETTE": return "Vignette";
            case "ENTRETIEN": return "Entretien";
            default: return type;
        }
    }
    
    private String getStatusDisplayName(String status) {
        switch (status) {
            case "ACTIVE": return "En cours";
            case "A_VENIR": return "À venir";
            case "TRAITEE": return "Traitée";
            default: return status;
        }
    }
    
    private String getPriorityDisplayName(String priority) {
        switch (priority) {
            case "HAUTE": return "Haute";
            case "MOYENNE": return "Moyenne";
            case "BASSE": return "Basse";
            default: return priority;
        }
    }
    
    private void refreshNotifications() {
        // Clear existing data
        recentNotificationsListView.getItems().clear();
        upcomingNotificationsListView.getItems().clear();
        processedNotificationsListView.getItems().clear();
        
        // Load notifications by category
        List<Notification> recentNotifications = notificationService.getRecentNotifications();
        List<Notification> upcomingNotifications = notificationService.getUpcomingNotifications();
        List<Notification> processedNotifications = notificationService.getProcessedNotifications();
        
        // Add items to lists
        recentNotificationsListView.getItems().addAll(recentNotifications);
        upcomingNotificationsListView.getItems().addAll(upcomingNotifications);
        processedNotificationsListView.getItems().addAll(processedNotifications);
        
        // Update total count and stats
        int totalCount = recentNotifications.size() + upcomingNotifications.size() + processedNotifications.size();
        totalNotificationsLabel.setText(String.valueOf(totalCount));
        notificationStats.setText(notificationService.getStatisticsText());
        
        // Show/hide empty placeholder
        updateEmptyPlaceholderVisibility();
        
        // Hide details if no notification is selected
        if (selectedNotification == null) {
            detailsPane.setVisible(false);
        }
    }
    
    private void updateEmptyPlaceholderVisibility() {
        boolean activeListEmpty = false;
        
        if (activeListView == recentNotificationsListView) {
            activeListEmpty = recentNotificationsListView.getItems().isEmpty();
        } else if (activeListView == upcomingNotificationsListView) {
            activeListEmpty = upcomingNotificationsListView.getItems().isEmpty();
        } else if (activeListView == processedNotificationsListView) {
            activeListEmpty = processedNotificationsListView.getItems().isEmpty();
        }
        
        emptyPlaceholder.setVisible(activeListEmpty);
    }
    
    @FXML
    public void showRecentTab(ActionEvent event) {
        // Show only recent notifications list
        recentNotificationsListView.setVisible(true);
        upcomingNotificationsListView.setVisible(false);
        processedNotificationsListView.setVisible(false);
        
        // Set the active list view
        activeListView = recentNotificationsListView;
        
        // Update empty placeholder visibility
        updateEmptyPlaceholderVisibility();
        
        // Update tab button styles
        Button sourceButton = (Button) event.getSource();
        sourceButton.getStyleClass().add("tab-selected");
        
        // Find and remove from other buttons
        sourceButton.getParent().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Button && node != sourceButton) {
                ((Button) node).getStyleClass().remove("tab-selected");
            }
        });
    }
    
    @FXML
    public void showUpcomingTab(ActionEvent event) {
        // Show only upcoming notifications list
        recentNotificationsListView.setVisible(false);
        upcomingNotificationsListView.setVisible(true);
        processedNotificationsListView.setVisible(false);
        
        // Set the active list view
        activeListView = upcomingNotificationsListView;
        
        // Update empty placeholder visibility
        updateEmptyPlaceholderVisibility();
        
        // Update tab button styles
        Button sourceButton = (Button) event.getSource();
        sourceButton.getStyleClass().add("tab-selected");
        
        // Find and remove from other buttons
        sourceButton.getParent().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Button && node != sourceButton) {
                ((Button) node).getStyleClass().remove("tab-selected");
            }
        });
    }
    
    @FXML
    public void showProcessedTab(ActionEvent event) {
        // Show only processed notifications list
        recentNotificationsListView.setVisible(false);
        upcomingNotificationsListView.setVisible(false);
        processedNotificationsListView.setVisible(true);
        
        // Set the active list view
        activeListView = processedNotificationsListView;
        
        // Update empty placeholder visibility
        updateEmptyPlaceholderVisibility();
        
        // Update tab button styles
        Button sourceButton = (Button) event.getSource();
        sourceButton.getStyleClass().add("tab-selected");
        
        // Find and remove from other buttons
        sourceButton.getParent().getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof Button && node != sourceButton) {
                ((Button) node).getStyleClass().remove("tab-selected");
            }
        });
    }
    
    @FXML
    public void handleRefresh(ActionEvent event) {
        refreshNotifications();
    }
    
    @FXML
    public void handleFilterChange(ActionEvent event) {
        applyFilters();
    }
    
    @FXML
    public void handleSearch(ActionEvent event) {
        applyFilters();
    }
    
    private void applyFilters() {
        String searchTerm = searchField.getText();
        String typeFilter = filterTypeComboBox.getValue().equals("Tous") ? "" : filterTypeComboBox.getValue();
        String statusFilter = filterStatusComboBox.getValue().equals("Tous") ? "" : filterStatusComboBox.getValue();
        
        // Apply filters and update lists
        List<Notification> filteredNotifications = notificationService.searchNotifications(searchTerm, typeFilter, statusFilter);
        
        // Clear existing data
        recentNotificationsListView.getItems().clear();
        upcomingNotificationsListView.getItems().clear();
        processedNotificationsListView.getItems().clear();
        
        // Add filtered items to appropriate lists
        for (Notification notification : filteredNotifications) {
            switch (notification.getStatut()) {
                case "ACTIVE":
                    recentNotificationsListView.getItems().add(notification);
                    break;
                case "A_VENIR":
                    upcomingNotificationsListView.getItems().add(notification);
                    break;
                case "TRAITEE":
                    processedNotificationsListView.getItems().add(notification);
                    break;
            }
        }
        
        // Update total count
        int totalCount = filteredNotifications.size();
        totalNotificationsLabel.setText(String.valueOf(totalCount));
        
        // Update stats text
        notificationStats.setText(
            recentNotificationsListView.getItems().size() + " récentes | " +
            upcomingNotificationsListView.getItems().size() + " à venir | " +
            processedNotificationsListView.getItems().size() + " traitées"
        );
        
        // Show/hide empty placeholder
        updateEmptyPlaceholderVisibility();
    }
    
    @FXML
    public void handleMarkAsHandled(ActionEvent event) {
        if (selectedNotification != null) {
            notificationService.markAsHandled(selectedNotification.getVehiculeId(), selectedNotification.getType());
            refreshNotifications();
            AlertHelper.showInformation("Succès", "La notification a été marquée comme traitée.");
        }
    }
    
    @FXML
    public void handleViewVehicle(ActionEvent event) {
        if (selectedNotification != null) {
            // Navigate to vehicle details page with the selected vehicule id
            AlertHelper.showInformation("Information", "Fonctionnalité à implémenter: Afficher les détails du véhicule " + 
                                       selectedNotification.getVehiculeInfo());
        }
    }
    
    @FXML
    public void handleCloseDetails(ActionEvent event) {
        detailsPane.setVisible(false);
        selectedNotification = null;
        recentNotificationsListView.getSelectionModel().clearSelection();
        upcomingNotificationsListView.getSelectionModel().clearSelection();
        processedNotificationsListView.getSelectionModel().clearSelection();
    }
    
    @FXML
    public void handleSettings(ActionEvent event) {
        AlertHelper.showInformation("Paramètres", "Paramètres de notification - Fonctionnalité à venir");
    }
}
