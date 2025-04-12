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

import java.util.Map;
import java.util.Optional;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.service.CandidatService;
import org.cpi2.service.DocumentService;
import org.cpi2.service.DossierService;
import org.cpi2.service.TypeDocumentService;
import org.cpi2.utils.AlertUtil;

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
                AlertUtil.showError( "Erreur", "Veuillez sélectionner un candidat");
                return;
            }
            
            try {
                Optional<Dossier> existingDossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
                Dossier dossier;
                
                if (existingDossierOpt.isPresent()) {
                    dossier = existingDossierOpt.get();
                    System.out.println("Using existing dossier with ID: " + dossier.getId());
                } else {
                    dossier = new Dossier();
                    dossier.setCandidatId(selectedCandidat.getId());
                    boolean created = dossierService.creerDossier(dossier, selectedCandidat.getId());
                    if (!created) {
                        AlertUtil.showError( "Erreur", "Impossible de créer le dossier pour ce candidat");
                        return;
                    }

                    existingDossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
                    if (existingDossierOpt.isEmpty()) {
                        AlertUtil.showError( "Erreur", "Le dossier a été créé mais impossible de le récupérer");
                        return;
                    }
                    dossier = existingDossierOpt.get();
                    System.out.println("Created new dossier with ID: " + dossier.getId());
                }

                selectedCandidat.setDossier(dossier);

                TypeDocument typeDocument = TypeDocument.valueOf(typeDocumentComboBox.getValue());
                org.cpi2.entities.Document document = new org.cpi2.entities.Document(
                    typeDocument, 
                    nomDocumentField.getText(), 
                    selectedFile.getAbsolutePath()
                );

                Long dossierId = dossier.getId();
                if (dossierId == null) {
                    AlertUtil.showError( "Erreur", "ID du dossier est null");
                    return;
                }
                System.out.println("Adding document to dossier with ID: " + dossierId);

                boolean success = dossierService.ajouterDocument(dossierId, document, selectedFile);
                
                if (success) {
                    AlertUtil.showInfo( "Succès", "Document enregistré avec succès!");
                    loadDocuments();
                    clearForm();
                } else {
                    AlertUtil.showError( "Erreur", "Impossible d'enregistrer le document");
                }
            } catch (Exception e) {
                AlertUtil.showError( "Erreur", "Une erreur est survenue: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Loads documents for the selected candidate.
     */
    @FXML
    private void loadDocuments() {
        Candidat selectedCandidat = candidatComboBox.getValue();
        if (selectedCandidat == null) {
            AlertUtil.showWarning( "Attention", "Veuillez sélectionner un candidat pour charger ses documents");
            return;
        }
        
        documentsList.clear();

        Optional<Dossier> dossierOpt = dossierService.getDossierByCandidat(selectedCandidat.getCin());
        
        if (dossierOpt.isEmpty()) {
            System.out.println("No dossier found for candidate: " + selectedCandidat.getNom() + " " + selectedCandidat.getPrenom());
            AlertUtil.showInfo( "Information",
                    "Aucun dossier n'a été trouvé pour ce candidat");
            return;
        }
        
        Dossier dossier = dossierOpt.get();
        selectedCandidat.setDossier(dossier);
        
        System.out.println("Found dossier with ID: " + dossier.getId() + " for candidate: " + 
                selectedCandidat.getNom() + " " + selectedCandidat.getPrenom());

        Map<TypeDocument, TreeSet<org.cpi2.entities.Document>> documents = dossier.getDocuments();
        
        if (documents == null || documents.isEmpty()) {
            System.out.println("No documents found in dossier ID: " + dossier.getId());
            AlertUtil.showInfo( "Information",
                    "Aucun document n'a été trouvé pour ce candidat");
            return;
        }
        
        System.out.println("Found " + documents.size() + " document types in dossier");

        documents.forEach((type, docs) -> {
            System.out.println("Document type: " + type + " has " + docs.size() + " documents");
            
            for (org.cpi2.entities.Document entityDoc : docs) {
                String nomFichier = entityDoc.getNomFichier();
                String typeStr = type.toString();
                LocalDate dateUpload = null;
                if (entityDoc.getDateUpload() != null) {
                    dateUpload = entityDoc.getDateUpload().toLocalDate();
                } else {
                    dateUpload = LocalDate.now();
                }
                
                System.out.println("Adding to table: " + nomFichier);
                Document uiDocument = new Document(nomFichier, typeStr, dateUpload);
                documentsList.add(uiDocument);
            }
        });

        documentsTableView.setItems(documentsList);
        System.out.println("Added " + documentsList.size() + " documents to table view");
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
            
            AlertUtil.showError( "Erreur de validation",
                    "Veuillez remplir tous les champs et sélectionner un fichier.");
            return false;
        }
        return true;
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
