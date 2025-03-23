package org.cpi2.utils;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Region;
import javafx.beans.property.Property;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;


public class ValidationUtils {
    
    // Map to store field-error label associations
    private static final Map<Region, Label> fieldErrorMap = new HashMap<>();
    
    // Map to store validation priorities (lower number = higher priority)
    private static final Map<Label, Integer> errorPriorities = new HashMap<>();
    

    public static void addValidation(TextField field, Predicate<String> validator, 
                                    String errorMessage, int priority) {
        setupValidation(field, field.textProperty(), validator, errorMessage, priority);
    }
    

    public static <T> void addValidation(ComboBox<T> field, Predicate<T> validator, 
                                    String errorMessage, int priority) {
        setupValidation(field, field.valueProperty(), validator, errorMessage, priority);
    }
    

    public static void addValidation(DatePicker field, Predicate<LocalDate> validator, 
                                    String errorMessage, int priority) {
        setupValidation(field, field.valueProperty(), validator, errorMessage, priority);
    }
    

    private static <T> void setupValidation(Region field, Property<T> observable, 
                                          Predicate<T> validator, String errorMessage, int priority) {
        // Check if this field already has an error label
        Label errorLabel = fieldErrorMap.get(field);
        
        // If not, create a new one and add it to the parent container
        if (errorLabel == null) {
            errorLabel = new Label();
            errorLabel.getStyleClass().add("error-label");
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            
            if (field.getParent() != null) {
                if (field.getParent() instanceof VBox) {
                    VBox parent = (VBox) field.getParent();
                    int index = parent.getChildren().indexOf(field);
                    if (index >= 0) {
                        parent.getChildren().add(index + 1, errorLabel);
                    }
                } 
                else if (field.getParent().getParent() instanceof VBox) {
                    VBox grandParent = (VBox) field.getParent().getParent();
                    int index = grandParent.getChildren().indexOf(field.getParent());
                    if (index >= 0) {
                        grandParent.getChildren().add(index + 1, errorLabel);
                    }
                }
            }
            
            fieldErrorMap.put(field, errorLabel);
        }
        
        errorPriorities.put(errorLabel, priority);
        
        Label finalErrorLabel = errorLabel;
        observable.addListener((obs, oldVal, newVal) -> {
            boolean isValid = validator.test(newVal);

            if (!isValid) {
                Integer currentPriority = errorPriorities.getOrDefault(finalErrorLabel, Integer.MAX_VALUE);
                if (priority <= currentPriority) {
                    finalErrorLabel.setText(errorMessage);
                    finalErrorLabel.setVisible(true);
                    finalErrorLabel.setManaged(true);
                    field.getStyleClass().remove("valid-field");
                    field.getStyleClass().add("error-field");
                    errorPriorities.put(finalErrorLabel, priority);
                }
            } else {

                if (priority == errorPriorities.getOrDefault(finalErrorLabel, Integer.MAX_VALUE)) {
                    finalErrorLabel.setVisible(false);
                    finalErrorLabel.setManaged(false);
                    field.getStyleClass().remove("error-field");
                    
                    if (!hasErrors(field)) {
                        field.getStyleClass().add("valid-field");
                    }
                    
                    errorPriorities.put(finalErrorLabel, Integer.MAX_VALUE);
                }
            }
        });
        
        boolean isInitiallyValid = validator.test(observable.getValue());
        if (isInitiallyValid) {
            field.getStyleClass().add("valid-field");
        }
    }
    
    public static void clearValidation(Region field) {
        Label errorLabel = fieldErrorMap.get(field);
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            field.getStyleClass().remove("error-field");
            field.getStyleClass().remove("valid-field");
        }
    }

    public static boolean hasErrors(Region field) {
        Label errorLabel = fieldErrorMap.get(field);
        return errorLabel != null && errorLabel.isVisible();
    }
    

    public static boolean hasAnyErrors() {
        return fieldErrorMap.values().stream().anyMatch(Label::isVisible);
    }
}
