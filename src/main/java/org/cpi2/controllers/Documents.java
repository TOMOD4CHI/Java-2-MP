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

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        typeDocumentComboBox.getItems().addAll(
                typeDocumentService.getAllTypeDocuments().stream().sorted().toList()
        );
        
        dateAjoutPicker.setValue(LocalDate.now());
        setupTableColumns();
        loadCandidates();
    }
    
    /**
     * Sets up the table columns.
     */
    private void setupTableColumns() {
        nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateAjout"));
    }
    
    /**
     * Loads candidates into the combo box.
     */
    private void loadCandidates() {
        List<Candidat> candidats = candidatService.getAllCandidats();
        candidatComboBox.setItems(FXCollections.observableArrayList(candidats));
        
        candidatComboBox.setConverter(new StringConverter<Candidat>() {
            @Override
            public String toString(Candidat candidat) {
                if (candidat == null) return "";
                return candidat.getNom() + " " + candidat.getPrenom();
            }

            @Override
            public Candidat fromString(String string) {
                return null;
            }
        });
    }

    /**
     * Opens a file chooser dialog to select a document file.
     */
    @FXML
    private void browseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sélectionner un document");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"),
            new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
            new FileChooser.ExtensionFilter("Tous les fichiers", "*.*")
        );
        
        selectedFile = fileChooser.showOpenDialog(browseButton.getScene().getWindow());
        if (selectedFile != null) {
            selectedFileLabel.setText(selectedFile.getName());
        }
    }

    /**
     * Saves the document to the selected candidate's dossier.
     */
    @FXML
    private void saveDocument() {
        if (!validateForm()) {
            return;
        }
        
        try {
            Candidat selectedCandidat = candidatComboBox.getValue();
            if (selectedCandidat == null) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Veuillez sélectionner un candidat");
                return;
            }
            
            // First, check if the candidate already has a dossier in the database
            Optional<Dossier> existingDossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
            Dossier dossier;
            
            if (existingDossierOpt.isPresent()) {
                // Use the existing dossier
                dossier = existingDossierOpt.get();
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

    /**
     * Loads documents for the selected candidate.
     */
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
                showAlert(Alert.AlertType.INFORMATION, "Information", 
                        "Aucun dossier n'a été trouvé pour ce candidat");
                return;
            }
            
            Dossier dossier = dossierOpt.get();
            // Update the candidate's dossier reference
            selectedCandidat.setDossier(dossier);
            
            // Get the documents from the dossier
            Map<TypeDocument, TreeSet<org.cpi2.entities.Document>> documentsMap = dossier.getDocuments();
            
            if (documentsMap == null || documentsMap.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "Information", 
                        "Aucun document n'a été trouvé pour ce candidat");
                return;
            }
            
            // Process each document type manually
            for (TypeDocument type : documentsMap.keySet()) {
                TreeSet<org.cpi2.entities.Document> entityDocs = documentsMap.get(type);
                
                if (entityDocs != null) {
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
                            
                            // Create a UI Document (inner class) and add to list
                            Document uiDoc = new Document(fileName, type.toString(), uploadDate);
                            documentsList.add(uiDoc);
                        }
                    }
                }
            }
            
            // Update the TableView
            documentsTableView.setItems(documentsList);
            
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors du chargement des documents");
            e.printStackTrace();
        }
    }

    /**
     * Validates the form fields.
     * @return true if all fields are valid, false otherwise
     */
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
    
    /**
     * Shows an alert dialog.
     */
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Clears the form fields.
     */
    private void clearForm() {
        candidatComboBox.getSelectionModel().clearSelection();
        nomDocumentField.clear();
        typeDocumentComboBox.getSelectionModel().clearSelection();
        dateAjoutPicker.setValue(LocalDate.now());
        selectedFileLabel.setText("Aucun fichier sélectionné");
        selectedFile = null;
    }
    
    /**
     * Inner class to represent a document (for TableView)
     */
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
