package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import org.cpi2.entities.Candidat;
// Using entity Document with full qualification to avoid conflict with inner Document class

import java.util.Map;
import java.util.Optional;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.service.CandidatService;
import org.cpi2.service.DocumentService;
import org.cpi2.service.DossierService;
import org.cpi2.service.TypeDocumentService;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Documents {

    @FXML private ComboBox<Candidat> candidatComboBox;
    @FXML private TextField nomDocumentField;
    @FXML private ComboBox<String> typeDocumentComboBox;
    @FXML private DatePicker dateAjoutPicker;
    @FXML private Label selectedFileLabel;
    @FXML private Button browseButton;
    @FXML private Button saveButton;
    @FXML private Button loadButton;
    @FXML private TableView<Document> documentsTableView;
    @FXML private TableColumn<Document, String> nomColumn;
    @FXML private TableColumn<Document, String> typeColumn;
    @FXML private TableColumn<Document, LocalDate> dateColumn;

    private File selectedFile;
    private final TypeDocumentService typeDocumentService = new TypeDocumentService();
    private final DossierService dossierService = new DossierService();
    private final CandidatService candidatService = new CandidatService();
    private final DocumentService documentService = new DocumentService();
    private ObservableList<Document> documentsList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Initialize the ComboBox with document types
        typeDocumentComboBox.getItems().addAll(
                typeDocumentService.getAllTypeDocuments().stream().sorted().toList()
        );
        
        // Set current date as default for date picker
        dateAjoutPicker.setValue(LocalDate.now());
        
        // Initialize table columns
        setupTableColumns();
        
        // Load candidates into ComboBox
        loadCandidates();
    }
    
    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
    }
    
    private void loadCandidates() {
        List<Candidat> candidats = candidatService.getAllCandidats();
        candidatComboBox.setItems(FXCollections.observableArrayList(candidats));
        
        // Set a custom string converter to display candidate name
        candidatComboBox.setConverter(new StringConverter<Candidat>() {
            @Override
            public String toString(Candidat candidat) {
                if (candidat == null) return "";
                return candidat.getNom() + " " + candidat.getPrenom();
            }

            @Override
            public Candidat fromString(String string) {
                return null; // Not needed for ComboBox
            }
        });
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
            Candidat selectedCandidat = candidatComboBox.getValue();
            if (selectedCandidat == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un candidat");
                return;
            }
            
            try {
                // First, check if the candidate already has a dossier in the database
                Optional<Dossier> existingDossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
                Dossier dossier;
                
                if (existingDossierOpt.isPresent()) {
                    // Use the existing dossier
                    dossier = existingDossierOpt.get();
                    System.out.println("Using existing dossier with ID: " + dossier.getId());
                } else {
                    // Create a new dossier
                    dossier = new Dossier();
                    dossier.setCandidatId(selectedCandidat.getId());
                    boolean created = dossierService.creerDossier(dossier, selectedCandidat.getId());
                    if (!created) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de créer le dossier pour ce candidat");
                        return;
                    }
                    
                    // Re-fetch the dossier to get its ID
                    existingDossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
                    if (existingDossierOpt.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Erreur", "Le dossier a été créé mais impossible de le récupérer");
                        return;
                    }
                    dossier = existingDossierOpt.get();
                    System.out.println("Created new dossier with ID: " + dossier.getId());
                }
                
                // Update the candidate's dossier reference
                selectedCandidat.setDossier(dossier);
                
                // Create document from form - use fully qualified name to avoid confusion with inner Document class
                TypeDocument typeDocument = TypeDocument.valueOf(typeDocumentComboBox.getValue());
                org.cpi2.entities.Document document = new org.cpi2.entities.Document(
                    typeDocument, 
                    nomDocumentField.getText(), 
                    selectedFile.getAbsolutePath()
                );
                
                // Confirm the dossier ID exists
                Long dossierId = dossier.getId();
                if (dossierId == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "ID du dossier est null");
                    return;
                }
                System.out.println("Adding document to dossier with ID: " + dossierId);
                
                // Add the document to the dossier
                boolean success = dossierService.ajouterDocument(dossierId, document, selectedFile);
                
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Document enregistré avec succès!");
                    loadDocuments(); // Refresh the documents list
                    clearForm();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'enregistrer le document");
                }
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void loadDocuments() {
        Candidat selectedCandidat = candidatComboBox.getValue();
        if (selectedCandidat == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner un candidat pour charger ses documents");
            return;
        }
        
        documentsList.clear();
        
        // Fetch the dossier directly from the database
        Optional<Dossier> dossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
        
        if (dossierOpt.isEmpty()) {
            System.out.println("No dossier found for candidate: " + selectedCandidat.getNom() + " " + selectedCandidat.getPrenom());
            showAlert(Alert.AlertType.INFORMATION, "Information", 
                    "Aucun dossier n'a été trouvé pour ce candidat");
            return;
        }
        
        Dossier dossier = dossierOpt.get();
        // Update the candidate's dossier reference
        selectedCandidat.setDossier(dossier);
        
        System.out.println("Found dossier with ID: " + dossier.getId() + " for candidate: " + 
                selectedCandidat.getNom() + " " + selectedCandidat.getPrenom());
        
        // Make sure to use the fully qualified name for the entity Document to avoid naming conflicts
        Map<TypeDocument, TreeSet<org.cpi2.entities.Document>> documents = dossier.getDocuments();
        
        if (documents == null || documents.isEmpty()) {
            System.out.println("No documents found in dossier ID: " + dossier.getId());
            showAlert(Alert.AlertType.INFORMATION, "Information", 
                    "Aucun document n'a été trouvé pour ce candidat");
            return;
        }
        
        System.out.println("Found " + documents.size() + " document types in dossier");
        
        // Convert documents from the dossier to our Document model for the TableView
        documents.forEach((type, docs) -> {
            System.out.println("Document type: " + type + " has " + docs.size() + " documents");
            
            for (org.cpi2.entities.Document entityDoc : docs) {
                // Extract the properties from the entity Document
                String nomFichier = entityDoc.getNomFichier();
                String typeStr = type.toString();
                LocalDate dateUpload = null;
                if (entityDoc.getDateUpload() != null) {
                    dateUpload = entityDoc.getDateUpload().toLocalDate();
                } else {
                    dateUpload = LocalDate.now();
                }
                
                System.out.println("Adding to table: " + nomFichier);
                // Create our UI Document model using the inner Document class
                Document uiDocument = new Document(nomFichier, typeStr, dateUpload);
                documentsList.add(uiDocument);
            }
        });
        
        // Update the TableView
        documentsTableView.setItems(documentsList);
        System.out.println("Added " + documentsList.size() + " documents to table view");
    }

    private boolean validateForm() {
        if (candidatComboBox.getValue() == null || 
            nomDocumentField.getText().isEmpty() || 
            typeDocumentComboBox.getValue() == null || 
            dateAjoutPicker.getValue() == null || 
            selectedFile == null) {
            
            showAlert(Alert.AlertType.ERROR, "Erreur de validation", 
                    "Veuillez remplir tous les champs et sélectionner un fichier.");
            return false;
        }
        return true;
    }
    
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void clearForm() {
        candidatComboBox.getSelectionModel().clearSelection();
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
