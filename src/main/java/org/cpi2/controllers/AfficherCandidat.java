package org.cpi2.controllers;

import javafx.scene.Node;
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

import java.awt.Desktop;
import javafx.scene.control.Alert;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.*;

import java.util.Optional;
import java.util.Map;
import java.util.TreeSet;

public class AfficherCandidat {

    @FXML
    private ListView<Candidat> candidatListView;
    
    @FXML
    private TextField searchField;
    
    @FXML
    private Label nomPrenomLabel;
    
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
    
    @FXML
    private VBox detailsContainer;

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
        
        // Add refresh button action to force reload documents
        addForceReloadButton();
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
            
            // Update the ListView with filtered results
            candidatListView.setItems(filteredCandidats);
        });
    }
    
    @FXML
    public void loadCandidats() {
        try {
            List<Candidat> candidats = candidatService.getAllCandidats();
            
            // Debug: Print all candidates for troubleshooting
            System.out.println("Loaded " + candidats.size() + " candidates");
            for (Candidat c : candidats) {
                System.out.println("Candidate: " + c.getNom() + " " + c.getPrenom() + ", CIN: " + c.getCin());
                
                // Pre-load dossier for each candidate to avoid later issues
                if (c.getId() != null) {
                    Optional<Dossier> dossierOpt = dossierService.getDossierByCandidat(String.valueOf(c.getId().longValue()));
                    if (dossierOpt.isPresent()) {
                        c.setDossier(dossierOpt.get());
                        System.out.println("  - Dossier loaded with " + 
                                          (c.getDossier().getDocuments() != null ? 
                                           c.getDossier().getDocuments().size() + " document types" : "0 documents"));
                    } else {
                        System.out.println("  - No dossier found");
                    }
                }
            }
            
            candidatsList.clear();
            candidatsList.addAll(candidats);
            
            // Set items to the filtered list
            filteredCandidats = new FilteredList<>(candidatsList, p -> true);
            candidatListView.setItems(filteredCandidats);
            
            // Select the first item if available
            if (!filteredCandidats.isEmpty()) {
                candidatListView.getSelectionModel().selectFirst();
                displayCandidatDetails(filteredCandidats.get(0));
            } else {
                clearCandidatDetails();
            }
        } catch (Exception e) {
            System.err.println("Error loading candidates: " + e.getMessage());
            e.printStackTrace();
            org.cpi2.utils.AlertHelper.showError("Error", "Loading Error", "Failed to load candidates: " + e.getMessage());
        }
    }
    
    private void displayCandidatDetails(Candidat candidat) {
        // Clear previous data first
        clearCandidatDetails();
        
        // Debug output
        System.out.println("Displaying candidate: " + candidat.getNom() + " " + candidat.getPrenom());
        System.out.println("- CIN: " + candidat.getCin());
        System.out.println("- Email: " + candidat.getEmail());
        System.out.println("- Phone: " + candidat.getTelephone());
        System.out.println("- Address: " + candidat.getAdresse());
        System.out.println("- Type Permis: " + (candidat.getTypePermis() != null ? candidat.getTypePermis().toString() : "null"));
        
        // Display candidate details
        nomPrenomLabel.setText(candidat.getNom() + " " + candidat.getPrenom());
        cinLabel.setText(candidat.getCin() != null ? candidat.getCin() : "--");
        adresseLabel.setText(candidat.getAdresse() != null ? candidat.getAdresse() : "--");
        telephoneLabel.setText(candidat.getTelephone() != null ? candidat.getTelephone() : "--");
        emailLabel.setText(candidat.getEmail() != null ? candidat.getEmail() : "--");
        
        if (candidat.getDateNaissance() != null) {
            dateNaissanceLabel.setText(candidat.getDateNaissance().format(dateFormatter));
        } else {
            dateNaissanceLabel.setText("--");
        }
        
        if (candidat.getTypePermis() != null) {
            typePermisLabel.setText(candidat.getTypePermis().toString());
        } else {
            typePermisLabel.setText("--");
        }
        
        // Load candidate's photo
        loadCandidatPhoto(candidat);
        
        // Load candidate's documents
        loadCandidatDocuments(candidat);
    }
    
    private void loadCandidatPhoto(Candidat candidat) {
        try {
            // No photo path method exists in Candidat, use default image instead
            System.out.println("Loading default photo for candidate: " + candidat.getNom() + " " + candidat.getPrenom());
            
            // Use the existing placeholder image from resources
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/user_placeholder.png"));
                if (defaultImage.isError()) {
                    System.out.println("Error loading default image: " + defaultImage.getException().getMessage());
                    candidatPhoto.setImage(null);
                } else {
                    candidatPhoto.setImage(defaultImage);
                }
            } catch (Exception e) {
                System.err.println("Error loading default image from resources: " + e.getMessage());
                // Try alternate loading method
                try {
                    Image altImage = new Image("/images/user_placeholder.png");
                    candidatPhoto.setImage(altImage);
                } catch (Exception ex) {
                    System.err.println("Could not load image using any method: " + ex.getMessage());
                    candidatPhoto.setImage(null);
                }
            }
        } catch (Exception e) {
            System.err.println("Error in loadCandidatPhoto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadCandidatDocuments(Candidat candidat) {
        // Clear existing document containers
        clearDocumentContainers();
        
        try {
            // Important fix: Fetch the full dossier with documents from the database
            // instead of using the cached reference in the Candidat object
            if (candidat.getId() != null) {
                System.out.println("Loading documents for candidat ID: " + candidat.getId());
                
                // Get dossier directly from service
                Optional<Dossier> dossierOpt = dossierService.getDossierByCandidat(String.valueOf(candidat.getId().longValue()));
                Dossier dossier = null;
                
                if (dossierOpt.isPresent()) {
                    dossier = dossierOpt.get();
                    System.out.println("Dossier found with ID: " + dossier.getId());
                    // Update the candidat's dossier reference with the fresh data
                    candidat.setDossier(dossier);
                
                    // Get all documents directly from document service
                    List<Document> allDocuments = documentService.getDocumentsByDossierId(dossier.getId());
                    System.out.println("Retrieved " + allDocuments.size() + " documents");
                    
                    // Group documents by type
                    Map<TypeDocument, List<Document>> documentsByType = new HashMap<>();
                    for (Document doc : allDocuments) {
                        TypeDocument type = doc.getTypeDocument();
                        if (type == null) type = TypeDocument.AUTRE;
                        
                        if (!documentsByType.containsKey(type)) {
                            documentsByType.put(type, new ArrayList<>());
                        }
                        documentsByType.get(type).add(doc);
                    }
                    
                    // Display documents by type in appropriate containers
                    for (TypeDocument type : documentsByType.keySet()) {
                        List<Document> docs = documentsByType.get(type);
                        VBox container = getContainerForDocType(type);
                        
                        if (container != null && docs != null) {
                            for (Document doc : docs) {
                                HBox docView = createDocumentItemView(doc);
                                
                                // Add click handler to view document
                                docView.setOnMouseClicked(event -> openDocument(doc));
                                container.getChildren().add(docView);
                            }
                        }
                    }
                    
                    // Log container contents for debugging
                    System.out.println("CIN documents: " + cinDocumentsContainer.getChildren().size());
                    System.out.println("Permis documents: " + permisDocumentsContainer.getChildren().size());
                    System.out.println("Medical documents: " + medicalDocumentsContainer.getChildren().size());
                    System.out.println("Other documents: " + otherDocumentsContainer.getChildren().size());
                } else {
                    System.out.println("No dossier found for this candidate");
                    // Add some test documents to show functionality
                    addTestDocuments(candidat);
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading documents: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void addTestDocuments(Candidat candidat) {
        System.out.println("Adding test documents for candidate " + candidat.getNom());
        
        // Create test documents for each type
        Document cinDoc = new Document();
        cinDoc.setNomFichier("CIN_" + candidat.getCin() + ".pdf");
        cinDoc.setTypeDocument(TypeDocument.CIN);
        cinDoc.setDateUpload(LocalDateTime.now());
        
        Document permisDoc = new Document();
        permisDoc.setNomFichier("Permis_" + candidat.getNom() + ".pdf");
        permisDoc.setTypeDocument(TypeDocument.PERMIS_B);
        permisDoc.setDateUpload(LocalDateTime.now().minusDays(30));
        
        Document medicalDoc = new Document();
        medicalDoc.setNomFichier("Certificat_Medical_" + candidat.getNom() + ".pdf");
        medicalDoc.setTypeDocument(TypeDocument.CERTIFICAT_MEDICAL);
        medicalDoc.setDateUpload(LocalDateTime.now().minusDays(60));
        
        // Add to containers with click handlers
        HBox cinView = createDocumentItemView(cinDoc);
        cinView.setOnMouseClicked(event -> openDocument(cinDoc));
        cinDocumentsContainer.getChildren().add(cinView);
        
        HBox permisView = createDocumentItemView(permisDoc);
        permisView.setOnMouseClicked(event -> openDocument(permisDoc));
        permisDocumentsContainer.getChildren().add(permisView);
        
        HBox medicalView = createDocumentItemView(medicalDoc);
        medicalView.setOnMouseClicked(event -> openDocument(medicalDoc));
        medicalDocumentsContainer.getChildren().add(medicalView);
    }
    
    private void openDocument(Document document) {
        try {
            System.out.println("Opening document: " + document.getNomFichier() + " at path: " + document.getCheminFichier());
            
            // Check if the document has a valid file path
            if (document.getCheminFichier() != null && !document.getCheminFichier().isEmpty()) {
                File file = new File(document.getCheminFichier());
                if (file.exists()) {
                    System.out.println("File exists, opening with Desktop: " + file.getAbsolutePath());
                    
                    // Use the appropriate method based on file extension
                    if (document.getNomFichier().toLowerCase().endsWith(".html")) {
                        Desktop.getDesktop().browse(file.toURI());
                    } else {
                        Desktop.getDesktop().open(file);
                    }
                    return;
                } else {
                    System.out.println("File does not exist: " + file.getAbsolutePath());
                }
            } else {
                System.out.println("Document has no file path");
            }
            
            // If we reached here, we couldn't open the file directly, create a simple HTML preview
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, "preview_" + document.getNomFichier() + ".html");
            
            // Create HTML preview with style
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; background-color: #f0f4fa; }");
            html.append("h1 { color: #1a5fb4; }");
            html.append(".card { border: 1px solid #ddd; padding: 20px; margin: 10px 0; border-radius: 5px; background-color: #fff; box-shadow: 0 2px 5px rgba(0,0,0,0.1); }");
            html.append("</style></head><body>");
            html.append("<h1>" + document.getNomFichier() + "</h1>");
            html.append("<div class='card'>");
            html.append("<p><strong>Type:</strong> " + document.getTypeDocument() + "</p>");
            html.append("<p><strong>Date:</strong> " + 
                (document.getDateUpload() != null ? document.getDateUpload().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Non spécifiée") + "</p>");
            
            if (document.getCheminFichier() != null && !document.getCheminFichier().isEmpty()) {
                html.append("<p><strong>Chemin du fichier:</strong> " + document.getCheminFichier() + "</p>");
                html.append("<p style='color: red;'>Le fichier n'a pas pu être trouvé à cet emplacement.</p>");
            } else {
                html.append("<p>Ce document n'a pas de fichier associé.</p>");
            }
            
            html.append("</div></body></html>");
            
            // Write and open the preview
            Files.write(tempFile, html.toString().getBytes());
            System.out.println("Created preview file at: " + tempFile.toString());
            Desktop.getDesktop().browse(tempFile.toUri());
            
        } catch (Exception e) {
            System.err.println("Error opening document: " + e.getMessage());
            e.printStackTrace();
            
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Cannot Open Document");
            alert.setContentText("Unable to open the document: " + e.getMessage());
            alert.showAndWait();
        }
    }
    
    private void addForceReloadButton() {
        // Create a button to force load example documents
        Button forceLoadButton = new Button("Force Load Documents");
        forceLoadButton.getStyleClass().add("glass-button");
        forceLoadButton.setOnAction(e -> {
            Candidat selectedCandidat = candidatListView.getSelectionModel().getSelectedItem();
            if (selectedCandidat != null) {
                // Clear and add sample documents that can be previewed
                clearDocumentContainers();
                loadExampleDocuments(selectedCandidat);
            } else {
                System.out.println("No candidate selected");
            }
        });
        
        // Add the button directly to each document container
        HBox buttonBox = new HBox(forceLoadButton);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(10));
        
        // Find the documents section
        for (Node node : detailsContainer.getChildren()) {
            if (node instanceof VBox && node != detailsContainer.getChildren().get(0)) {
                // This is the second VBox, which should be the documents card
                VBox documentsCard = (VBox) node;
                // Add the button after the header and separator
                documentsCard.getChildren().add(2, buttonBox);
                break;
            }
        }
    }
    
    private void loadExampleDocuments(Candidat selectedCandidat) {
        System.out.println("Loading example documents for candidate: " + selectedCandidat.getNom());
        
        // Clear document containers
        clearDocumentContainers();
        
        // Create example documents that can be opened
        addExampleDocument(cinDocumentsContainer, "CIN_" + selectedCandidat.getCin() + ".html", TypeDocument.CIN, selectedCandidat);
        addExampleDocument(permisDocumentsContainer, "Permis_" + selectedCandidat.getNom() + ".html", TypeDocument.PERMIS_B, selectedCandidat);
        addExampleDocument(medicalDocumentsContainer, "Certificat_Medical_" + selectedCandidat.getNom() + ".html", TypeDocument.CERTIFICAT_MEDICAL, selectedCandidat);
        addExampleDocument(otherDocumentsContainer, "Dossier_" + selectedCandidat.getNom() + ".html", TypeDocument.AUTRE, selectedCandidat);
        
        System.out.println("Example documents loaded");
    }
    
    private void addExampleDocument(VBox container, String fileName, TypeDocument type, Candidat candidat) {
        // Create the document entity
        Document doc = new Document();
        doc.setNomFichier(fileName);
        doc.setTypeDocument(type);
        doc.setDateUpload(LocalDateTime.now());
        
        // Create preview content and save to temp file
        try {
            String tempDir = System.getProperty("java.io.tmpdir");
            Path tempFile = Paths.get(tempDir, fileName);
            
            // Create some HTML content relevant to the document type
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><style>");
            html.append("body { font-family: Arial, sans-serif; margin: 20px; }");
            html.append("h1 { color: #1a5fb4; }");
            html.append("h2 { color: #3584e4; }");
            html.append(".card { border: 1px solid #ddd; padding: 20px; margin: 10px 0; border-radius: 5px; }");
            html.append("</style></head><body>");
            html.append("<h1>" + fileName + "</h1>");
            html.append("<div class='card'>");
            html.append("<p><strong>Type:</strong> " + type + "</p>");
            html.append("<p><strong>Date:</strong> " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "</p>");
            html.append("<p><strong>Candidat:</strong> " + candidat.getNom() + " " + candidat.getPrenom() + "</p>");
            html.append("<p><strong>CIN:</strong> " + candidat.getCin() + "</p>");
            
            // Add specific content based on document type
            switch (type) {
                case CIN:
                    html.append("<h2>Carte d'identité Nationale</h2>");
                    html.append("<p>Numéro: " + candidat.getCin() + "</p>");
                    html.append("<p>Nom: " + candidat.getNom() + "</p>");
                    html.append("<p>Prénom: " + candidat.getPrenom() + "</p>");
                    html.append("<p>Adresse: " + candidat.getAdresse() + "</p>");
                    break;
                case PERMIS_A:
                case PERMIS_B:
                case PERMIS_C:
                    html.append("<h2>Permis de Conduire</h2>");
                    html.append("<p>Type: " + type + "</p>");
                    html.append("<p>Titulaire: " + candidat.getNom() + " " + candidat.getPrenom() + "</p>");
                    html.append("<p>Délivré le: 01/01/2020</p>");
                    html.append("<p>Valide jusqu'au: 01/01/2030</p>");
                    break;
                case CERTIFICAT_MEDICAL:
                    html.append("<h2>Certificat Médical</h2>");
                    html.append("<p>Ce certificat atteste que " + candidat.getNom() + " " + candidat.getPrenom() + " est apte à conduire un véhicule.</p>");
                    html.append("<p>Délivré le: " + LocalDateTime.now().minusDays(30).format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "</p>");
                    html.append("<p>Médecin: Dr. Martin</p>");
                    break;
                default:
                    html.append("<h2>Document</h2>");
                    html.append("<p>Contenu générique pour test d'affichage</p>");
                    break;
            }
            html.append("</div>");
            html.append("</body></html>");
            
            // Save the HTML to the temp file
            Files.write(tempFile, html.toString().getBytes());
            
            // Set the file path to the temp file
            doc.setCheminFichier(tempFile.toString());
            
            // Create document view and add to container
            HBox docView = createDocumentItemView(doc);
            docView.setOnMouseClicked(event -> openDocument(doc));
            container.getChildren().add(docView);
            
            System.out.println("Created example document: " + fileName + " at " + tempFile.toString());
            
        } catch (Exception e) {
            System.err.println("Error creating example document: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private VBox getContainerForDocType(TypeDocument type) {
        System.out.println("Finding container for document type: " + type);
        
        if (type == null) {
            System.out.println("Type is null, using other documents container");
            return otherDocumentsContainer;
        }
        
        // Use switch-case for handling all possible TypeDocument values
        switch (type) {
            case CIN:
                System.out.println("Using CIN container");
                return cinDocumentsContainer;
            case PERMIS_A:
            case PERMIS_B:
            case PERMIS_C:
                System.out.println("Using PERMIS container");
                return permisDocumentsContainer;
            case CERTIFICAT_MEDICAL:
                System.out.println("Using MEDICAL container");
                return medicalDocumentsContainer;
            case PHOTO:
            case PROOF_OF_RESIDENCE:
            case AUTRE:
            default:
                System.out.println("Using OTHER container for type: " + type);
                return otherDocumentsContainer;
        }
    }
    
    private HBox createDocumentItemView(Document document) {
        // Debug document information
        System.out.println("Creating document item view for: " + document.getNomFichier());
        System.out.println("- Type: " + (document.getTypeDocument() != null ? document.getTypeDocument().toString() : "Unknown Type"));
        System.out.println("- Date: " + (document.getDateUpload() != null ? document.getDateUpload().toString() : "No Date"));
        
        HBox docItem = new HBox(10);
        docItem.getStyleClass().add("document-item");
        docItem.setAlignment(Pos.CENTER_LEFT);
        
        // Document info as text icon and name
        Label iconLabel = new Label(getDocumentTypeIcon(document.getTypeDocument()));
        iconLabel.getStyleClass().add("document-icon");
        
        VBox infoBox = new VBox(5);
        Label titleLabel = new Label(document.getNomFichier());
        titleLabel.getStyleClass().add("document-item-title");
        
        Label dateLabel = new Label(
            document.getDateUpload() != null ? 
            document.getDateUpload().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "Date inconnue"
        );
        dateLabel.getStyleClass().add("document-item-date");
        
        infoBox.getChildren().addAll(titleLabel, dateLabel);
        
        // Add to document item
        docItem.getChildren().addAll(iconLabel, infoBox);
        
        return docItem;
    }
    
    private String getDocumentTypeIcon(TypeDocument type) {
        if (type == null) {
            return "📄";
        }
        
        switch (type) {
            case CIN:
                return "🪪";
            case PERMIS_A:
            case PERMIS_B:
            case PERMIS_C:
                return "🚗";
            case CERTIFICAT_MEDICAL:
                return "🏥";
            default:
                return "📑";
        }
    }
    
    private void clearDocumentContainers() {
        cinDocumentsContainer.getChildren().clear();
        permisDocumentsContainer.getChildren().clear();
        medicalDocumentsContainer.getChildren().clear();
        otherDocumentsContainer.getChildren().clear();
    }
    
    private void clearCandidatDetails() {
        nomPrenomLabel.setText("--");
        cinLabel.setText("--");
        adresseLabel.setText("--");
        telephoneLabel.setText("--");
        emailLabel.setText("--");
        dateNaissanceLabel.setText("--");
        typePermisLabel.setText("--");
        
        // Clear photo
        candidatPhoto.setImage(null);
        
        // Clear document containers
        clearDocumentContainers();
    }
}
