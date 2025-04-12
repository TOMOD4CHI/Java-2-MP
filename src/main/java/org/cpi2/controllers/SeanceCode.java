package org.cpi2.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.cpi2.entities.Candidat;
import org.cpi2.entities.Moniteur;
import org.cpi2.entities.SessionCode;
import org.cpi2.entities.TypePermis;
import org.cpi2.entities.TypeSession;
import org.cpi2.service.CandidatService;
import org.cpi2.service.MoniteurService;
import org.cpi2.service.PresenceService;
import org.cpi2.service.SessionService;
import org.cpi2.utils.AlertUtil;
import org.cpi2.utils.ValidationUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SeanceCode {
    @FXML private TextField capfield;
    @FXML private DatePicker datefield;
    @FXML private TextField tempsfield;
    @FXML private TextField moniteurfield;
    @FXML private ComboBox<String> candidatCombo;
    @FXML private ComboBox<String> salleCombo;
    
    @FXML private Label candidatError;
    @FXML private Label dateError;
    @FXML private Label tempsError;
    @FXML private Label moniteurError;

    @FXML private TableView<Moniteur> moniteurTableView;
    @FXML private TableColumn<Moniteur, Long> idMoniteurColumn;
    @FXML private TableColumn<Moniteur, String> nomMoniteurColumn;
    @FXML private TableColumn<Moniteur, String> prenomMoniteurColumn;
    @FXML private TableColumn<Moniteur, String> specialiteColumn;
    @FXML private TableColumn<Moniteur, String> candidatsColumn;

    @FXML private Button cancelButton;
    @FXML private Button planifierButton;

    private final MoniteurService moniteurService = new MoniteurService();
    private final SessionService sessionService = new SessionService();
    private Moniteur selectedMoniteur;

    @FXML
    public void initialize() {

        idMoniteurColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nomMoniteurColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomMoniteurColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        specialiteColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSpecialites().stream()
                        .map(TypePermis::toString)
                        .collect(Collectors.joining(", "))));  

        capfield.setPromptText("Nombre de places");
        tempsfield.setPromptText("Format: HH:mm");
        moniteurfield.setPromptText("Sélectionnez un moniteur");

        setupValidation();

        datefield.setValue(LocalDate.now());

        moniteurTableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedMoniteur = newSelection;
                moniteurfield.setText(selectedMoniteur.getId() + " - " + selectedMoniteur.getNom() + " " + selectedMoniteur.getPrenom());
            }
        });

        moniteurTableView.setOnMouseClicked(this::handleTableClick);

        planifierButton.setOnAction(this::handlePlanifier);
        cancelButton.setOnAction(e -> handleCancel());

        loadMoniteurs();

        loadCandidats();

        loadSalles();

        capfield.setText("20");
    }

    private void loadMoniteurs() {
        try {
            List<Moniteur> moniteurs = moniteurService.getAllMoniteurs();
            if (moniteurs == null || moniteurs.isEmpty()) {

                moniteurs = createMockMoniteurs();
            }
            moniteurTableView.getItems().clear();
            moniteurTableView.getItems().addAll(moniteurs);
        } catch (Exception e) {
            System.err.println("Erreur lors du chargement des moniteurs: " + e.getMessage());
            e.printStackTrace();

            loadMockMoniteurs();
        }
    }

    private void loadMockMoniteurs() {
        List<Moniteur> mockMoniteurs = createMockMoniteurs();
        moniteurTableView.getItems().clear();
        moniteurTableView.getItems().addAll(mockMoniteurs);
    }

    private List<Moniteur> createMockMoniteurs() {
        List<Moniteur> moniteurs = new ArrayList<>();

        Moniteur moniteur1 = new Moniteur("Dupont", "Jean", "AB1234",
                "123 Rue de Paris", "0601020304",
                LocalDate.of(1985, 5, 15), "jean.dupont@example.com",
                LocalDate.of(2015, 3, 1), 2500.0);
        moniteur1.addSpecialite(TypePermis.B);
        moniteur1.setId(1L);

        Moniteur moniteur2 = new Moniteur("Martin", "Sophie", "CD5678",
                "456 Avenue Lyon", "0607080910",
                LocalDate.of(1990, 8, 20), "sophie.martin@example.com",
                LocalDate.of(2018, 6, 15), 2700.0);
        moniteur2.addSpecialite(TypePermis.A);
        moniteur2.setId(2L);

        moniteurs.add(moniteur1);
        moniteurs.add(moniteur2);

        return moniteurs;
    }

    private void handleTableClick(MouseEvent event) {
        if (event.getClickCount() == 1) {
            Moniteur moniteur = moniteurTableView.getSelectionModel().getSelectedItem();
            if (moniteur != null) {
                selectedMoniteur = moniteur;
                moniteurfield.setText(selectedMoniteur.getId() + " - " + selectedMoniteur.getNom() + " " + selectedMoniteur.getPrenom());
            }
        }
    }

    @FXML
    private void handlePlanifier(ActionEvent event) {

        if (!validateInputs()) {
            return;
        }

        try {

            int capacite = Integer.parseInt(capfield.getText());

            LocalDate date = datefield.getValue();

            LocalTime time = LocalTime.parse(tempsfield.getText(), DateTimeFormatter.ofPattern("HH:mm"));

            SessionCode sessionCode = new SessionCode();
            sessionCode.setDateSession(date);
            sessionCode.setHeureSession(time);
            sessionCode.setMoniteur(selectedMoniteur);
            sessionCode.setCapaciteMax(capacite);
            sessionCode.setSalle(salleCombo.getValue());
            sessionCode.setTypeSession(TypeSession.CODE);

            sessionCode.setPlanId(1);

            if (sessionCode.getDuree() <= 0) {
                sessionCode.setDuree(60);
            }

            boolean saved = sessionService.saveSessionCode(sessionCode);
            
            if (saved) {

                List<SessionCode> sessions = sessionService.viewAllSessionCode();
                SessionCode createdSession = null;

                for (SessionCode session : sessions) {
                    if (session.getDateSession().equals(date) && 
                        session.getHeureSession().equals(time) && 
                        session.getMoniteur().getId() == selectedMoniteur.getId()) {
                        createdSession = session;
                        break;
                    }
                }

                if (createdSession != null) {

                    if (candidatCombo.getValue() != null && !candidatCombo.getValue().isEmpty()) {
                        try {

                            String candidatValue = candidatCombo.getValue();
                            long candidatId = Long.parseLong(candidatValue.split(" - ")[0]);

                            PresenceService presenceService = new PresenceService();
                            presenceService.recordCodePresence(createdSession.getId(), candidatId, false);
                        } catch (Exception e) {
                            System.err.println("Erreur lors de la création de la présence: " + e.getMessage());
                        }
                    }
                }
                
                AlertUtil.showSuccess("Planification", "Séance de code planifiée avec succès");
                clearFields();
            } else {
                AlertUtil.showError("Erreur", "Erreur lors de la planification de la séance");
            }
        } catch (NumberFormatException e) {
            AlertUtil.showConfirmation("Validation", "La capacité doit être un nombre entier");
        } catch (Exception e) {
            System.err.println("Erreur lors de la planification: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors de la planification: " + e.getMessage());
        }
    }

    private void clearFields() {
        capfield.clear();
        datefield.setValue(LocalDate.now());
        tempsfield.clear();
        moniteurfield.clear();
        selectedMoniteur = null;
        moniteurTableView.getSelectionModel().clearSelection();

        candidatError.setVisible(false);
        dateError.setVisible(false);
        tempsError.setVisible(false);
        moniteurError.setVisible(false);

        capfield.getStyleClass().remove("error-field");
        capfield.getStyleClass().remove("valid-field");
        datefield.getStyleClass().remove("error-field");
        datefield.getStyleClass().remove("valid-field");
        tempsfield.getStyleClass().remove("error-field");
        tempsfield.getStyleClass().remove("valid-field");
        moniteurfield.getStyleClass().remove("error-field");
        moniteurfield.getStyleClass().remove("valid-field");
    }

    @FXML
    private void handleCancel() {

        clearFields();
    }

    private void setupValidation() {

        candidatError.setVisible(false);
        dateError.setVisible(false);
        tempsError.setVisible(false);
        moniteurError.setVisible(false);

        capfield.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                candidatError.setText("La capacité est obligatoire");
                candidatError.setVisible(true);
                capfield.getStyleClass().remove("valid-field");
                if (!capfield.getStyleClass().contains("error-field")) {
                    capfield.getStyleClass().add("error-field");
                }
            } else {
                try {
                    int cap = Integer.parseInt(newVal);
                    if (cap <= 0) {
                        candidatError.setText("La capacité doit être un nombre entier positif");
                        candidatError.setVisible(true);
                        capfield.getStyleClass().remove("valid-field");
                        if (!capfield.getStyleClass().contains("error-field")) {
                            capfield.getStyleClass().add("error-field");
                        }
                    } else {
                        candidatError.setVisible(false);
                        capfield.getStyleClass().remove("error-field");
                        if (!capfield.getStyleClass().contains("valid-field")) {
                            capfield.getStyleClass().add("valid-field");
                        }
                    }
                } catch (NumberFormatException e) {
                    candidatError.setText("La capacité doit être un nombre entier positif");
                    candidatError.setVisible(true);
                    capfield.getStyleClass().remove("valid-field");
                    if (!capfield.getStyleClass().contains("error-field")) {
                        capfield.getStyleClass().add("error-field");
                    }
                }
            }
        });

        datefield.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) {
                dateError.setText("La date est obligatoire");
                dateError.setVisible(true);
                datefield.getStyleClass().remove("valid-field");
                if (!datefield.getStyleClass().contains("error-field")) {
                    datefield.getStyleClass().add("error-field");
                }
            } else if (newVal.isBefore(LocalDate.now())) {
                dateError.setText("La date ne peut pas être dans le passé");
                dateError.setVisible(true);
                datefield.getStyleClass().remove("valid-field");
                if (!datefield.getStyleClass().contains("error-field")) {
                    datefield.getStyleClass().add("error-field");
                }
            } else {
                dateError.setVisible(false);
                datefield.getStyleClass().remove("error-field");
                if (!datefield.getStyleClass().contains("valid-field")) {
                    datefield.getStyleClass().add("valid-field");
                }
            }
        });

        tempsfield.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                tempsError.setText("L'heure est obligatoire");
                tempsError.setVisible(true);
                tempsfield.getStyleClass().remove("valid-field");
                if (!tempsfield.getStyleClass().contains("error-field")) {
                    tempsfield.getStyleClass().add("error-field");
                }
            } else {
                try {
                    LocalTime.parse(newVal, DateTimeFormatter.ofPattern("HH:mm"));
                    tempsError.setVisible(false);
                    tempsfield.getStyleClass().remove("error-field");
                    if (!tempsfield.getStyleClass().contains("valid-field")) {
                        tempsfield.getStyleClass().add("valid-field");
                    }
                } catch (Exception e) {
                    tempsError.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
                    tempsError.setVisible(true);
                    tempsfield.getStyleClass().remove("valid-field");
                    if (!tempsfield.getStyleClass().contains("error-field")) {
                        tempsfield.getStyleClass().add("error-field");
                    }
                }
            }
        });

        moniteurfield.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) {
                moniteurError.setText("Veuillez sélectionner un moniteur");
                moniteurError.setVisible(true);
                moniteurfield.getStyleClass().remove("valid-field");
                if (!moniteurfield.getStyleClass().contains("error-field")) {
                    moniteurfield.getStyleClass().add("error-field");
                }
            } else {
                moniteurError.setVisible(false);
                moniteurfield.getStyleClass().remove("error-field");
                if (!moniteurfield.getStyleClass().contains("valid-field")) {
                    moniteurfield.getStyleClass().add("valid-field");
                }
            }
        });
    }
    
    private boolean validateInputs() {

        boolean hasErrors = false;

        if (capfield.getText().trim().isEmpty()) {
            candidatError.setText("La capacité est obligatoire");
            candidatError.setVisible(true);
            hasErrors = true;
        } else {
            try {
                int cap = Integer.parseInt(capfield.getText());
                if (cap <= 0) {
                    candidatError.setText("La capacité doit être un nombre entier positif");
                    candidatError.setVisible(true);
                    hasErrors = true;
                }
            } catch (NumberFormatException e) {
                candidatError.setText("La capacité doit être un nombre entier positif");
                candidatError.setVisible(true);
                hasErrors = true;
            }
        }

        if (datefield.getValue() == null) {
            dateError.setText("La date est obligatoire");
            dateError.setVisible(true);
            hasErrors = true;
        } else if (datefield.getValue().isBefore(LocalDate.now())) {
            dateError.setText("La date ne peut pas être dans le passé");
            dateError.setVisible(true);
            hasErrors = true;
        }

        if (tempsfield.getText().trim().isEmpty()) {
            tempsError.setText("L'heure est obligatoire");
            tempsError.setVisible(true);
            hasErrors = true;
        } else {
            try {
                LocalTime.parse(tempsfield.getText(), DateTimeFormatter.ofPattern("HH:mm"));
            } catch (Exception e) {
                tempsError.setText("Format d'heure invalide. Utilisez le format HH:mm (ex: 14:30)");
                tempsError.setVisible(true);
                hasErrors = true;
            }
        }

        if (moniteurfield.getText().trim().isEmpty() || selectedMoniteur == null) {
            moniteurError.setText("Veuillez sélectionner un moniteur");
            moniteurError.setVisible(true);
            hasErrors = true;
        }
        
        return !hasErrors;
    }

    private void loadCandidats() {
        try {

            CandidatService candidatService = new CandidatService();
            List<Candidat> candidats = candidatService.getAllCandidats();

            ObservableList<String> candidatItems = FXCollections.observableArrayList();

            if (candidats != null && !candidats.isEmpty()) {
                for (Candidat candidat : candidats) {
                    candidatItems.add(candidat.getId() + " - " + candidat.getNom() + " " + candidat.getPrenom());
                }
                candidatCombo.setItems(candidatItems);
            } else {
                System.err.println("No candidates found");
            }
        } catch (Exception e) {
            System.err.println("Error loading candidates: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadSalles() {
        try {

            ObservableList<String> salles = FXCollections.observableArrayList(
                "Salle 1", "Salle 2", "Salle 3", "Salle 4", "Salle 5"
            );
            salleCombo.setItems(salles);

            if (!salles.isEmpty()) {
                salleCombo.setValue(salles.get(0));
            }
        } catch (Exception e) {
            System.err.println("Error loading salles: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


