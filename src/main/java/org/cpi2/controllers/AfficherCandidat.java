package org.cpi2.controllers;

import javafx.scene.Node;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Document;
import org.cpi2.entities.Dossier;
import org.cpi2.entities.TypeDocument;
import org.cpi2.service.CandidatService;
import org.cpi2.service.DocumentService;
import org.cpi2.service.DossierService;
import org.cpi2.utils.AlertUtil;
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

/**
 * Contrôleur pour l'affichage et la gestion des détails d'un candidat
 * Permet de visualiser la liste des candidats, leurs informations personnelles
 * et leurs documents associés
 */
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

    /**
     * Initialise le contrôleur et configure les composants de l'interface
     */
    public void initialize() {
        setupListView();
        setupSearch();
        loadCandidats();
        addForceReloadButton();
    }
    
    /**
     * Configure la ListView pour l'affichage des candidats
     */
    private void setupListView() {
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
        
        candidatListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                displayCandidatDetails(newSelection);
            }
        });
    }
    
    /**
     * Configure le champ de recherche pour filtrer les candidats
     */
    private void setupSearch() {
        filteredCandidats = new FilteredList<>(candidatsList, p -> true);
        
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredCandidats.setPredicate(candidat -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                
                String lowerCaseFilter = newValue.toLowerCase();
                
                if (candidat.getNom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (candidat.getPrenom().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (candidat.getCin().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                } else if (candidat.getTelephone().toLowerCase().contains(lowerCaseFilter)) {
                    return true;
                }
                return false;
            });
            
            candidatListView.setItems(filteredCandidats);
        });
    }
    
    /**
     * Charge la liste des candidats depuis le service
     */
    private void loadCandidats() {
        try {
            List<Candidat> loadedCandidats = candidatService.getAll();
            
            if (loadedCandidats != null && !loadedCandidats.isEmpty()) {
                candidatsList.clear();
                candidatsList.addAll(loadedCandidats);
                candidatListView.setItems(candidatsList);
                
                if (!candidatsList.isEmpty()) {
                    candidatListView.getSelectionModel().select(0);
                    displayCandidatDetails(candidatsList.get(0));
                }
            } else {
                showNoDataAlert();
                clearCandidatDetails();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError( "Loading Error", "Failed to load candidates: " + e.getMessage());
        }
    }
    
    private void displayCandidatDetails(Candidat candidat) {
        if (candidat == null) {
            clearCandidatDetails();
            return;
        }
        
        nomPrenomLabel.setText(candidat.getNom() + " " + candidat.getPrenom());
        cinLabel.setText(candidat.getCin());
        adresseLabel.setText(candidat.getAdresse() != null ? candidat.getAdresse() : "Non spécifiée");
        telephoneLabel.setText(candidat.getTelephone());
        emailLabel.setText(candidat.getEmail() != null ? candidat.getEmail() : "Non spécifié");
        
        if (candidat.getDateNaissance() != null) {
            dateNaissanceLabel.setText(candidat.getDateNaissance().format(dateFormatter));
        } else {
            dateNaissanceLabel.setText("Non spécifiée");
        }
        
        typePermisLabel.setText(candidat.getTypePermis() != null ? candidat.getTypePermis() : "Non spécifié");
        
        loadCandidatPhoto(candidat);
        loadCandidatDocuments(candidat);
    }
    
    /**
     * Charge la photo du candidat si disponible
     */
    private void loadCandidatPhoto(Candidat candidat) {
        try {
            String photoPath = "path/to/default/profile.png";
            
            if (candidat.getPhotoUrl() != null && !candidat.getPhotoUrl().isEmpty()) {
                File photoFile = new File(candidat.getPhotoUrl());
                if (photoFile.exists()) {
                    photoPath = photoFile.toURI().toString();
                }
            }
            
            Image image = new Image(photoPath, true);
            candidatPhoto.setImage(image);
            
            if (image.isError()) {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
                candidatPhoto.setImage(defaultImage);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement de la photo: " + e.getMessage());
            try {
                Image defaultImage = new Image(getClass().getResourceAsStream("/images/default_profile.png"));
                candidatPhoto.setImage(defaultImage);
            } catch (Exception ex) {
                System.err.println("Impossible de charger l'image par défaut: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Charge les documents associés au dossier du candidat
     */
    private void loadCandidatDocuments(Candidat candidat) {
        clearDocumentContainers();
        
        try {
            Dossier dossier = candidat.getDossier();
            
            if (dossier == null) {
                dossier = dossierService.getDossierByCandidat(candidat.getCin());
                candidat.setDossier(dossier);
            }
            
            if (dossier != null && dossier.getDocuments() != null && !dossier.getDocuments().isEmpty()) {
                for (Document document : dossier.getDocuments()) {
                    try {
                        System.out.println("Processing document: " + document.getNomFichier());
                        VBox containerForType = getContainerForDocType(document.getTypeDocument());
                        HBox documentView = createDocumentItemView(document);
                        
                        documentView.setOnMouseClicked(event -> openDocument(document));
                        containerForType.getChildren().add(documentView);
                    } catch (Exception e) {
                        System.err.println("Erreur lors de l'ajout du document: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Aucun document trouvé pour le candidat: " + candidat.getNom());
                
                Label noCinLabel = new Label("Aucun document CIN");
                noCinLabel.getStyleClass().add("no-document-label");
                cinDocumentsContainer.getChildren().add(noCinLabel);
                
                Label noPermisLabel = new Label("Aucun document permis");
                noPermisLabel.getStyleClass().add("no-document-label");
                permisDocumentsContainer.getChildren().add(noPermisLabel);
                
                Label noMedicalLabel = new Label("Aucun document médical");
                noMedicalLabel.getStyleClass().add("no-document-label");
                medicalDocumentsContainer.getChildren().add(noMedicalLabel);
                
                Label noOtherLabel = new Label("Aucun autre document");
                noOtherLabel.getStyleClass().add("no-document-label");
                otherDocumentsContainer.getChildren().add(noOtherLabel);
            }
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des documents: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Ajoute des documents de test pour la démonstration (utilisé seulement en développement)
     */
    private void addTestDocuments(Candidat candidat) {
        // Code supprimé pour la brièveté
    }
    
    /**
     * Ouvre le document sélectionné avec l'application par défaut du système
     */
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
        // Code supprimé pour la brièveté
    }
    
    /**
     * Charge des documents d'exemple pour le candidat sélectionné
     */
    private void loadExampleDocuments(Candidat selectedCandidat) {
        // Code supprimé pour la brièveté
    }
    
    /**
     * Ajoute un document d'exemple au conteneur spécifié
     */
    private void addExampleDocument(VBox container, String fileName, TypeDocument type, Candidat candidat) {
        // Code supprimé pour la brièveté
    }
    
    /**
     * Retourne le conteneur approprié pour le type de document spécifié
     */
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
            case PHOTO:
            case PROOF_OF_RESIDENCE:
            case AUTRE:
            default:
                return otherDocumentsContainer;
        }
    }
    
    /**
     * Crée une vue d'élément de document avec icône et informations
     */
    private HBox createDocumentItemView(Document document) {
        HBox docItem = new HBox(10);
        docItem.getStyleClass().add("document-item");
        docItem.setAlignment(Pos.CENTER_LEFT);
        
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
        docItem.getChildren().addAll(iconLabel, infoBox);
        
        return docItem;
    }
    
    /**
     * Retourne l'icône appropriée pour le type de document spécifié
     */
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
    
    /**
     * Vide tous les conteneurs de documents
     */
    private void clearDocumentContainers() {
        cinDocumentsContainer.getChildren().clear();
        permisDocumentsContainer.getChildren().clear();
        medicalDocumentsContainer.getChildren().clear();
        otherDocumentsContainer.getChildren().clear();
    }
    
    /**
     * Réinitialise tous les champs d'affichage des détails du candidat
     */
    private void clearCandidatDetails() {
        nomPrenomLabel.setText("--");
        cinLabel.setText("--");
        adresseLabel.setText("--");
        telephoneLabel.setText("--");
        emailLabel.setText("--");
        dateNaissanceLabel.setText("--");
        typePermisLabel.setText("--");
        
        candidatPhoto.setImage(null);
        clearDocumentContainers();
    }
}
