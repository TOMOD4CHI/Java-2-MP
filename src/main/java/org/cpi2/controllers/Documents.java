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

import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.service.CandidatService;
import org.cpi2.service.DocumentService;
import org.cpi2.service.DossierService;
import org.cpi2.service.TypeDocumentService;

public class Documents {

    @FXML private ComboBox<Candidat> candidatComboBox;
    @FXML private TextField nomDocumentField;
    @FXML private ComboBox<String> typeDocumentComboBox;
    @FXML private DatePicker dateAjoutPicker;
    @FXML private Button browseButton;
    @FXML private Label selectedFileLabel;
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
                
                // Get the type document string from the combo box
                String typeDocumentStr = typeDocumentComboBox.getValue();
                
                // Convert string to TypeDocument enum - handle case mismatch
                TypeDocument typeDocument = null;
                for (TypeDocument type : TypeDocument.values()) {
                    if (type.toString().equalsIgnoreCase(typeDocumentStr)) {
                        typeDocument = type;
                        break;
                    }
                }
                
                if (typeDocument == null) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Type de document invalide: " + typeDocumentStr);
                    return;
                }
                
                // Create entity document (not the UI document)
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
        
        // Clear the current list
        documentsList.clear();
        
        try {
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
            
            // Get the documents from the dossier
            Map<TypeDocument, TreeSet<org.cpi2.entities.Document>> documentsMap = dossier.getDocuments();
            
            if (documentsMap == null || documentsMap.isEmpty()) {
                System.out.println("No documents found in dossier ID: " + dossier.getId());
                showAlert(Alert.AlertType.INFORMATION, "Information", 
                        "Aucun document n'a été trouvé pour ce candidat");
                return;
            }
            
            System.out.println("Found " + documentsMap.size() + " document types in dossier");
            
            // Process each document type manually
            for (TypeDocument type : documentsMap.keySet()) {
                TreeSet<org.cpi2.entities.Document> entityDocs = documentsMap.get(type);
                
                if (entityDocs != null) {
                    System.out.println("Document type: " + type + " has " + entityDocs.size() + " documents");
                    
                    // Process each entity document
                    for (org.cpi2.entities.Document entityDoc : entityDocs) {
                        if (entityDoc != null) {
                            // Get properties from entity document
                            String fileName = entityDoc.getNomFichier();
                            LocalDate uploadDate = null;
                            
                            if (entityDoc.getDateUpload() != null) {
                                uploadDate = entityDoc.getDateUpload().toLocalDate();
                            } else {
                                uploadDate = LocalDate.now();
                            }
                            
                            System.out.println("Adding document to table: " + fileName);
                            
                            // Create a UI Document (inner class) and add to list
                            // We need to make sure we use the correct constructor for our inner class
                            Document uiDoc = new Document(fileName, type.toString(), uploadDate);
                            documentsList.add(uiDoc);
                        }
                    }
                }
            }
            
            // Update the TableView
            documentsTableView.setItems(documentsList);
            System.out.println("Added " + documentsList.size() + " documents to table view");
            
        } catch (Exception e) {
            System.err.println("Error loading documents: " + e.getMessage());
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors du chargement des documents");
        }
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
