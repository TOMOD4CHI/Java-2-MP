package org.cpi2.controllers;

import org.cpi2.entities.Candidat;
import org.cpi2.entities.Document;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.service.CandidatService;
import org.cpi2.service.DocumentService;
import org.cpi2.service.DossierService;
import org.cpi2.utils.ImageViewUtil;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class AfficherCandidat {

    @FXML
    private ListView<Candidat> candidatListView;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label fullNameLabel;
    
    @FXML
    private Label cinLabel;
    
    @FXML
    private Label adresseLabel;
    
    @FXML
    private Label telephoneLabel;
    
    @FXML
    private Label emailLabel;
    
    @FXML
    private Label dateNaissanceLabel;
    
    @FXML
    private Label typePermisLabel;
    
    @FXML
    private ImageView candidatPhoto;
    
    @FXML
    private VBox cinDocumentsContainer;
    
    @FXML
    private VBox permisDocumentsContainer;
    
    @FXML
    private VBox medicalDocumentsContainer;
    
    @FXML
    private VBox otherDocumentsContainer;

    private ObservableList<Candidat> candidatsList = FXCollections.observableArrayList();
    private FilteredList<Candidat> filteredCandidats;
    private final CandidatService candidatService = new CandidatService();
    private final DocumentService documentService = new DocumentService();
    private final DossierService dossierService = new DossierService();
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void initialize() {
        setupListView();
        setupSearch();
        loadCandidats();
    }
    
    private void setupListView() {
        // Configure the cell factory to display candidate names
        candidatListView.setCellFactory(lv -> new ListCell<Candidat>() {
            @Override
            protected void updateItem(Candidat candidat, boolean empty) {
                super.updateItem(candidat, empty);
                if (empty || candidat == null) {
                    setText(null);
                } else {
                    setText(candidat.getNom() + " " + candidat.getPrenom());
                }
            }
        });
        
        // Add selection listener
        candidatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayCandidatDetails(newSelection);
            }
        });
    }
    
    private void setupSearch() {
        // Initialize filtered list
        filteredCandidats = new FilteredList<>(candidatsList, p -> true);
        
        // Add listener to search field
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCandidats.setPredicate(candidat -> {
                // If search text is empty, show all candidates
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                // Match against nom, prenom, or CIN
                if (candidat.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (candidat.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (candidat.getCin() != null && candidat.getCin().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
            
            // Update list view with filtered results
            candidatListView.setItems(filteredCandidats);
        });
    }

    @FXML
    public void loadCandidats() {
        candidatsList.clear();

        // Fetch candidates from service
        List<Candidat> candidats = candidatService.getAllCandidats();
        
        // Debug output
        System.out.println("Loaded " + candidats.size() + " candidates");
        for (Candidat c : candidats) {
            System.out.println("Candidate: " + c.getNom() + " " + c.getPrenom() + ", ID: " + c.getId());
        }
        
        candidatsList.addAll(candidats);
        
        // Debug the size of candidatsList after adding
        System.out.println("candidatsList size after adding: " + candidatsList.size());

        // Update the filtered list
        if (searchField.getText().isEmpty()) {
            filteredCandidats = new FilteredList<>(candidatsList, p -> true);
            candidatListView.setItems(filteredCandidats);
            
            // Debug the items in the ListView
            System.out.println("Setting " + filteredCandidats.size() + " items to ListView");
        } else {
            // Reapply the current filter
            searchField.setText(searchField.getText());
        }
        
        // Force refresh the ListView
        candidatListView.refresh();
        
        // Clear details if no candidates are selected
        if (candidatListView.getSelectionModel().getSelectedItem() == null) {
            clearCandidatDetails();
        }
    }
    
    private void displayCandidatDetails(Candidat candidat) {
        // Update personal info labels
        fullNameLabel.setText(candidat.getNom() + " " + candidat.getPrenom());
        cinLabel.setText("CIN: " + (candidat.getCin() != null ? candidat.getCin() : ""));
        adresseLabel.setText(candidat.getAdresse() != null ? candidat.getAdresse() : "");
        telephoneLabel.setText(candidat.getTelephone() != null ? candidat.getTelephone() : "");
        emailLabel.setText(candidat.getEmail() != null ? candidat.getEmail() : "");
        
        if (candidat.getDateNaissance() != null) {
            dateNaissanceLabel.setText(candidat.getDateNaissance().format(dateFormatter));
        } else {
            dateNaissanceLabel.setText("");
        }
        
        typePermisLabel.setText(candidat.getTypePermis() != null ? candidat.getTypePermis().toString() : "");
        
        // Load candidate photo if available, otherwise use placeholder
        loadCandidatPhoto(candidat);
        
        // Clear document containers
        clearDocumentContainers();
        
        // Load documents
        loadCandidatDocuments(candidat);
    }
    
    private void loadCandidatPhoto(Candidat candidat) {
        // Placeholder logic - replace with actual photo loading if implemented
        // For now using the placeholder image defined in FXML
    }
    
    private void loadCandidatDocuments(Candidat candidat) {
        // Fetch the dossier directly from the database using the CIN
        Optional<Dossier> dossierOpt = dossierService.getDossierByCandidat(candidat.getCin());
        
        if (dossierOpt.isEmpty()) {
            System.out.println("No dossier found for candidate: " + candidat.getNom() + " " + candidat.getPrenom());
            return;
        }
        
        Dossier dossier = dossierOpt.get();
        // Update the candidate's dossier reference
        candidat.setDossier(dossier);
        
        System.out.println("Found dossier with ID: " + dossier.getId() + " for candidate: " + candidat.getNom() + " " + candidat.getPrenom());
        
        Map<TypeDocument, TreeSet<Document>> documents = dossier.getDocuments();
        
        if (documents == null || documents.isEmpty()) {
            System.out.println("No documents found in dossier ID: " + dossier.getId());
            return;
        }
        
        System.out.println("Found " + documents.size() + " document types in dossier");
        
        // Process each document type
        for (Map.Entry<TypeDocument, TreeSet<Document>> entry : documents.entrySet()) {
            TypeDocument type = entry.getKey();
            TreeSet<Document> docSet = entry.getValue();
            
            System.out.println("Document type: " + type + " has " + docSet.size() + " documents");
            
            for (Document doc : docSet) {
                System.out.println("Processing document: " + doc.getNomFichier());
                // Create a document item view and add to appropriate container
                VBox documentContainer = getContainerForDocType(type);
                if (documentContainer != null) {
                    documentContainer.getChildren().add(createDocumentItemView(doc));
                }
            }
        }
    }
    
    private VBox getContainerForDocType(TypeDocument type) {
        if (type == null) {
            return otherDocumentsContainer;
        }
        
        switch (type) {
            case CIN:
                return cinDocumentsContainer;
            case PERMIS_A:
            case PERMIS_B:
            case PERMIS_C:
                return permisDocumentsContainer;
            case CERTIFICAT_MEDICAL:
                return medicalDocumentsContainer;
            default:
                return otherDocumentsContainer;
        }
    }
    
    private HBox createDocumentItemView(Document document) {
        HBox docItem = new HBox(10);
        docItem.getStyleClass().add("document-item");
        docItem.setAlignment(Pos.CENTER_LEFT);
        
        // Document thumbnail
        ImageView thumbnail = createDocumentThumbnail(document);
        
        // Document info
        VBox infoBox = new VBox(5);
        Label titleLabel = new Label(document.getNomFichier());
        titleLabel.getStyleClass().add("document-item-title");
        
        Label dateLabel = new Label(
            document.getDateUpload() != null ? 
            document.getDateUpload().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : ""
        );
        dateLabel.getStyleClass().add("document-item-date");
        
        infoBox.getChildren().addAll(titleLabel, dateLabel);
        
        // Add to document item
        docItem.getChildren().addAll(thumbnail, infoBox);
        
        return docItem;
    }
    
    private ImageView createDocumentThumbnail(Document document) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(80);
        imageView.setFitHeight(80);
        imageView.setPreserveRatio(true);
        imageView.getStyleClass().add("document-thumbnail");
        
        // Try to load image from file path
        if (document.getCheminFichier() != null && !document.getCheminFichier().isEmpty()) {
            File file = new File(document.getCheminFichier());
            if (file.exists()) {
                try {
                    Image image = new Image(file.toURI().toString());
                    imageView.setImage(image);
                } catch (Exception e) {
                    // If image can't be loaded, use a placeholder based on document type
                    setPlaceholderByDocType(imageView, document.getTypeDocument());
                }
            } else {
                setPlaceholderByDocType(imageView, document.getTypeDocument());
            }
        } else {
            setPlaceholderByDocType(imageView, document.getTypeDocument());
        }
        
        return imageView;
    }
    
    private void setPlaceholderByDocType(ImageView imageView, TypeDocument type) {
        String imagePath;
        
        if (type == null) {
            imagePath = "/images/document_placeholder.png";
        } else {
            switch (type) {
                case CIN:
                    imagePath = "/images/id_card_placeholder.png";
                    break;
                case PERMIS_A:
                case PERMIS_B:
                case PERMIS_C:
                    imagePath = "/images/license_placeholder.png";
                    break;
                case CERTIFICAT_MEDICAL:
                    imagePath = "/images/medical_placeholder.png";
                    break;
                default:
                    imagePath = "/images/document_placeholder.png";
            }
        }
        
        // Try to load the placeholder image
        try {
            imageView.setImage(new Image(getClass().getResourceAsStream(imagePath)));
        } catch (Exception e) {
            // If placeholder can't be loaded, set a simple colored background
            imageView.setStyle("-fx-background-color: #e0e0e0;");
        }
    }
    
    private void clearDocumentContainers() {
        cinDocumentsContainer.getChildren().clear();
        permisDocumentsContainer.getChildren().clear();
        medicalDocumentsContainer.getChildren().clear();
        otherDocumentsContainer.getChildren().clear();
    }
    
    private void clearCandidatDetails() {
        fullNameLabel.setText("Sélectionnez un candidat");
        cinLabel.setText("");
        adresseLabel.setText("");
        telephoneLabel.setText("");
        emailLabel.setText("");
        dateNaissanceLabel.setText("");
        typePermisLabel.setText("");
        clearDocumentContainers();
    }
}
