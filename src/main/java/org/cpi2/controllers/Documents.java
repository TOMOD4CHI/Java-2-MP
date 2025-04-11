package org.cpi2.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.cpi2.entities.Document;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.service.DossierService;
import org.cpi2.service.TypeDocumentService;

import java.io.File;
import java.time.LocalDate;

public class Documents {

    @FXML private TextField candidatIdField;
    @FXML private TextField nomDocumentField;
    @FXML private ComboBox<String> typeDocumentComboBox;
    @FXML private DatePicker dateAjoutPicker;
    @FXML private Label selectedFileLabel;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private TableView<Document> documentsTableView;
    @FXML private TableColumn<Document, String> nomColumn;
    @FXML private TableColumn<Document, String> typeColumn;
    @FXML private TableColumn<Document, LocalDate> dateColumn;

    private File selectedFile;
    private final TypeDocumentService typeDocumentService = new TypeDocumentService();
    private final DossierService dossierService = new DossierService();

    @FXML
    public void initialize() {
        // Initialize the ComboBox with document types
        typeDocumentComboBox.getItems().addAll(
                typeDocumentService.getAllTypeDocuments().stream().sorted().toList()
        );
        
        // Set current date as default for date picker
        dateAjoutPicker.setValue(LocalDate.now());
    }

    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un document");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        Stage stage = (Stage) browseButton.getScene().getWindow();
        selectedFile = fileChooser.showOpenDialog(stage);
        
        if (selectedFile != null) {
            selectedFileLabel.setText(selectedFile.getName());
        }
    }

    @FXML
    private void saveDocument() {
        if (validateForm()) {
            // Here you would save the document to database
            // For this example, we'll just show a success alert

            Dossier dossier = dossierService.getDossierByCandidat(candidatIdField.getText()).orElse(null);
            if(dossier != null) {
                TypeDocument typeDocument = TypeDocument.valueOf(typeDocumentComboBox.getValue());
                org.cpi2.entities.Document document = new org.cpi2.entities.Document(typeDocument, selectedFile.getName(), selectedFile.getAbsolutePath());
                dossierService.ajouterDocument(dossier.getId(), document, selectedFile);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Succès");
                alert.setHeaderText(null);
                alert.setContentText("Document enregistré avec succès!");
                alert.showAndWait();

                clearForm();
            }
            else{
                //Alert that the candidate does not exist
                //RIP l UI hay 3al CLI for now 7ta someone fix it
                System.out.println("Candidat does not exist");
                return;
            }


        }
    }

    @FXML
    private void cancelAction() {
        clearForm();
    }

    private boolean validateForm() {
        if (candidatIdField.getText().isEmpty() || 
            nomDocumentField.getText().isEmpty() || 
            typeDocumentComboBox.getValue() == null || 
            dateAjoutPicker.getValue() == null || 
            selectedFile == null) {
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur de validation");
            alert.setHeaderText(null);
            alert.setContentText("Veuillez remplir tous les champs et sélectionner un fichier.");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    private void clearForm() {
        candidatIdField.clear();
        nomDocumentField.clear();
        typeDocumentComboBox.getSelectionModel().clearSelection();
        dateAjoutPicker.setValue(LocalDate.now());
        selectedFileLabel.setText("Aucun fichier sélectionné");
        selectedFile = null;
    }
    
    // Inner class to represent a document (for TableView)
    public static class Document {
        private String nom;
        private String type;
        private LocalDate dateAjout;
        
        public Document(String nom, String type, LocalDate dateAjout) {
            this.nom = nom;
            this.type = type;
            this.dateAjout = dateAjout;
        }
        
        public String getNom() { return nom; }
        public String getType() { return type; }
        public LocalDate getDateAjout() { return dateAjout; }
    }
}
