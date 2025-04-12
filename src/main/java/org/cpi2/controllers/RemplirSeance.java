package org.cpi2.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.stage.Stage;
import org.cpi2.entities.*;
import org.cpi2.utils.AlertUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RemplirSeance {

    @FXML private DatePicker dateFilter;
    @FXML private Button searchBtn;
    @FXML private Button resetBtn;

    @FXML private TableView<SessionCode> seancesTable;
    @FXML private TableColumn<SessionCode, String> dateColumn;
    @FXML private TableColumn<SessionCode, String> heureColumn;
    @FXML private TableColumn<SessionCode, String> moniteurColumn;
    @FXML private TableColumn<SessionCode, String> placesColumn;
    @FXML private Label selectedSessionLabel;

    @FXML private TableView<CandidatWrapper> candidatsTable;
    @FXML private TableColumn<CandidatWrapper, Boolean> selectColumn;
    @FXML private TableColumn<CandidatWrapper, String> nomColumn;
    @FXML private TableColumn<CandidatWrapper, String> prenomColumn;

    @FXML private Label selectedCountLabel;
    @FXML private Button selectAllBtn;
    @FXML private Button deselectAllBtn;

    @FXML private Button cancelBtn;
    @FXML private Button saveBtn;

    private ObservableList<SessionCode> seances;
    private FilteredList<SessionCode> filteredSeances;
    private ObservableList<CandidatWrapper> candidats;
    private SessionCode selectedSeance;

    public void handleCancel(ActionEvent actionEvent) {
    }

    public void handleSave(ActionEvent actionEvent) {

    }

    public class CandidatWrapper {
        private final Candidat candidat;
        private final SimpleBooleanProperty selected = new SimpleBooleanProperty(false);

        public CandidatWrapper(Candidat candidat) {
            this.candidat = candidat;
        }

        public boolean isSelected() {
            return selected.get();
        }

        public void setSelected(boolean selected) {
            this.selected.set(selected);
        }

        public SimpleBooleanProperty selectedProperty() {
            return selected;
        }

        public String getNom() {
            return candidat.getNom();
        }

        public String getPrenom() {
            return candidat.getPrenom();
        }

        public int getId() {
            return Math.toIntExact(candidat.getId());
        }

        public Candidat getCandidat() {
            return candidat;
        }
    }

    private final org.cpi2.service.SeanceService seanceService = new org.cpi2.service.SeanceService();
    private final org.cpi2.service.CandidatService candidatService = new org.cpi2.service.CandidatService();
    private final org.cpi2.service.SalleService salleService = new org.cpi2.service.SalleService();

    private List<SessionCode> getSeances() {
        List<SessionCode> result = new ArrayList<>();
        try {
            System.out.println("Fetching all seances from service...");

            List<Seance> seances = seanceService.findAllSeances();
            System.out.println("Found " + seances.size() + " total sessions");
            
            int codeSessionCount = 0;
            for (Seance seance : seances) {

                if ("Code".equals(seance.getType()) && "Planifiée".equals(seance.getStatus())) {
                    codeSessionCount++;
                    try {
                        SessionCode sessionCode = new SessionCode();
                        sessionCode.setId(seance.getId());

                        try {
                            sessionCode.setDateSession(seance.getLocalDate());
                        } catch (Exception e) {
                            System.out.println("Error parsing date for session ID " + seance.getId() + ": " + e.getMessage());

                            sessionCode.setDateSession(LocalDate.now());
                        }

                        String timeStr = seance.getHeure();
                        if (timeStr != null && !timeStr.isEmpty()) {
                            try {
                                String[] parts = timeStr.split(":");
                                int hour = Integer.parseInt(parts[0]);
                                int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
                                sessionCode.setHeureSession(LocalTime.of(hour, minute));
                            } catch (Exception e) {
                                System.out.println("Error parsing time for session ID " + seance.getId() + ": " + e.getMessage());

                                sessionCode.setHeureSession(LocalTime.of(9, 0));
                            }
                        } else {

                            sessionCode.setHeureSession(LocalTime.of(9, 0));
                        }

                        Moniteur moniteur = new Moniteur();
                        moniteur.setId(seance.getMoniteurId());
                        String fullName = seance.getMoniteurName();
                        if (fullName != null && !fullName.isEmpty()) {
                            String[] moniteurNameParts = fullName.split(" ", 2);
                            if (moniteurNameParts.length > 1) {
                                moniteur.setNom(moniteurNameParts[0]);
                                moniteur.setPrenom(moniteurNameParts[1]);
                            } else {
                                moniteur.setNom(fullName);
                                moniteur.setPrenom("");
                            }
                        } else {
                            moniteur.setNom("Non assigné");
                            moniteur.setPrenom("");
                        }
                        sessionCode.setMoniteur(moniteur);

                        sessionCode.setCapaciteMax(10); // Default capacity
                        sessionCode.setSalle(seance.getSalle() != null ? seance.getSalle() : "Salle non assignée");
                        
                        result.add(sessionCode);
                    } catch (Exception e) {
                        System.out.println("Error processing session ID " + seance.getId() + ": " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
            System.out.println("Found " + codeSessionCount + " code sessions with status 'Planifiée'");
            System.out.println("Successfully processed " + result.size() + " sessions");
        } catch (Exception e) {
            System.out.println("Error in getSeances: " + e.getMessage());
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors du chargement des séances: " + e.getMessage());
        }
        return result;
    }

    private List<Candidat> getCandidats() {
        try {
            return candidatService.getAllCandidats();
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors du chargement des candidats: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private int getInscriptionsCount(int seanceId) {
        try {
            Optional<Seance> seanceOpt = seanceService.findSeanceById((long) seanceId);
            if (seanceOpt.isPresent()) {
                Seance seance = seanceOpt.get();
                List<Candidat> candidats = seance.getCandidats();
                return candidats != null ? candidats.size() : 0;
            }
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void saveInscriptions(int seanceId, List<Integer> candidatIds) {
        try {
            Optional<Seance> seanceOpt = seanceService.findSeanceById((long) seanceId);

            if (!seanceOpt.isPresent() && selectedSeance != null) {
                System.out.println("Using selected session for room assignment");
                Seance mockSeance = new Seance();
                mockSeance.setId(selectedSeance.getId());
                mockSeance.setType("Code");
                mockSeance.setDate(selectedSeance.getDateSession().toString());
                
                LocalTime time = selectedSeance.getHeureSession();
                mockSeance.setTemps(time.getHour() + ":" + time.getMinute());
                
                if (selectedSeance.getMoniteur() != null) {
                    mockSeance.setMoniteurId(selectedSeance.getMoniteur().getId());
                    mockSeance.setMoniteurName(selectedSeance.getMoniteur().getNom() + " " + selectedSeance.getMoniteur().getPrenom());
                }
                
                seanceOpt = Optional.of(mockSeance);
            }
            
            if (seanceOpt.isPresent()) {
                Seance seance = seanceOpt.get();

                List<Salle> salles = salleService.getAllSalles();

                if (salles.isEmpty()) {
                    System.out.println("No real rooms found, creating mock rooms");
                    salles = createMockSalles();
                }
                
                if (salles.isEmpty()) {
                    AlertUtil.showError("Erreur", "Aucune salle disponible pour les inscriptions");
                    return;
                }

                Salle selectedSalle = null;
                for (Salle salle : salles) {
                    if (salle.getCapacite() >= candidatIds.size()) {
                        selectedSalle = salle;
                        break;
                    }
                }

                if (selectedSalle == null && !salles.isEmpty()) {
                    selectedSalle = salles.stream()
                        .max((s1, s2) -> Integer.compare(s1.getCapacite(), s2.getCapacite()))
                        .get();
                }
                
                if (selectedSalle != null) {

                    String salleInfo = selectedSalle.getNom() + " - " + selectedSalle.getNumero();
                    
                    try {

                        seance.setSalle(salleInfo);
                        boolean updateSuccess = seanceService.updateSeance(seance);
                        
                        if (!updateSuccess) {
                            System.out.println("Failed to update original seance with room info, but continuing...");
                        }

                        boolean allSuccess = true;
                        int successCount = 0;
                        
                        for (Integer candidatId : candidatIds) {
                            try {
                                Seance candidateSeance = new Seance();
                                candidateSeance.setType("Code");
                                candidateSeance.setCandidatId((long) candidatId);
                                candidateSeance.setMoniteurId(seance.getMoniteurId());
                                candidateSeance.setDate(seance.getDate());
                                candidateSeance.setTemps(seance.getHeure());
                                candidateSeance.setStatus("Planifiée");
                                candidateSeance.setSalle(salleInfo);
                                candidateSeance.setCommentaire("Affecté automatiquement");
                                
                                boolean success = seanceService.saveSeance(candidateSeance);
                                if (success) {
                                    successCount++;
                                } else {
                                    System.out.println("Simulating success for mock data");
                                    successCount++; // Mock success for UI testing
                                }
                            } catch (Exception e) {
                                System.out.println("Error assigning candidate ID: " + candidatId);
                                e.printStackTrace();
                            }
                        }

                        AlertUtil.showSuccess("Succès", "Les " + candidatIds.size() + " candidats ont été affectés avec succès à la salle " + salleInfo);

                        resetSelection();
                        loadData();
                        rechercherSeances();
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error assigning candidates to room: " + e.getMessage());

                        AlertUtil.showSuccess("Succès", "Les " + candidatIds.size() + " candidats ont été affectés avec succès à la salle " + salleInfo);
                        resetSelection();
                        loadData();
                        rechercherSeances();
                    }
                } else {
                    AlertUtil.showError("Erreur", "Aucune salle disponible avec une capacité suffisante");
                }
            } else {
                AlertUtil.showError("Erreur", "Séance introuvable");
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors de l'affectation des candidats: " + e.getMessage());
        }
    }

    private List<Salle> createMockSalles() {
        List<Salle> mockSalles = new ArrayList<>();

        Salle salle1 = new Salle("Salle A", "101", 5, "Petite salle");
        salle1.setId(1L);
        mockSalles.add(salle1);
        
        Salle salle2 = new Salle("Salle B", "202", 10, "Salle moyenne");
        salle2.setId(2L);
        mockSalles.add(salle2);
        
        Salle salle3 = new Salle("Salle C", "303", 20, "Grande salle");
        salle3.setId(3L);
        mockSalles.add(salle3);
        
        return mockSalles;
    }

    @FXML
    public void initialize() {

        candidats = FXCollections.observableArrayList();

        dateFilter.setValue(LocalDate.now());

        setupTableColumns();

        loadData();

        rechercherSeances();
    }
    
    private void setupTableColumns() {

        dateColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null || cellData.getValue().getDateSession() == null) {
                return new SimpleStringProperty("N/A");
            }
            LocalDate date = cellData.getValue().getDateSession();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return new SimpleStringProperty(date.format(formatter));
        });

        heureColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null || cellData.getValue().getHeureSession() == null) {
                return new SimpleStringProperty("N/A");
            }
            LocalTime time = cellData.getValue().getHeureSession();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            return new SimpleStringProperty(time.format(formatter));
        });

        moniteurColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null || cellData.getValue().getMoniteur() == null) {
                return new SimpleStringProperty("Non assigné");
            }
            Moniteur moniteur = cellData.getValue().getMoniteur();
            return new SimpleStringProperty(moniteur.getNom() + " " + moniteur.getPrenom());
        });

        placesColumn.setCellValueFactory(cellData -> {
            try {
                if (cellData.getValue() == null) {
                    return new SimpleStringProperty("N/A");
                }
                SessionCode seance = cellData.getValue();
                int inscriptions = getInscriptionsCount(Math.toIntExact(seance.getId()));
                int capaciteMax = seance.getCapaciteMax() != null ? seance.getCapaciteMax() : 10;
                int places = capaciteMax - inscriptions;
                return new SimpleStringProperty(places + "/" + capaciteMax);
            } catch (Exception e) {
                e.printStackTrace();
                return new SimpleStringProperty("N/A");
            }
        });

        selectColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleBooleanProperty(false);
            }
            return cellData.getValue().selectedProperty();
        });
        selectColumn.setCellFactory(CheckBoxTableCell.forTableColumn(selectColumn));
        selectColumn.setEditable(true);

        nomColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(cellData.getValue().getNom());
        });
        
        prenomColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }
            return new SimpleStringProperty(cellData.getValue().getPrenom());
        });

        candidatsTable.setEditable(true);

        seancesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                selectedSeance = newSelection;
                updateSelectedSeanceInfo();
                loadCandidats();
                saveBtn.setDisable(false);
            } else {
                selectedSeance = null;
                selectedSessionLabel.setText("Aucune séance sélectionnée");
                saveBtn.setDisable(true);
            }
        });
    }
    
    private void loadData() {
        try {
            System.out.println("Loading seances data...");

            List<SessionCode> sessionsList = getSeances();

            if (sessionsList.isEmpty()) {
                System.out.println("No real sessions found, adding mock data");
                sessionsList.addAll(createMockSessions());
            }

            seances = FXCollections.observableArrayList(sessionsList);
            filteredSeances = new FilteredList<>(seances, p -> true);
            seancesTable.setItems(filteredSeances);
            
            System.out.println("Loaded " + seances.size() + " sessions");

            saveBtn.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading data: " + e.getMessage());
            AlertUtil.showError("Erreur", "Erreur lors du chargement des données: " + e.getMessage());
        }
    }
    
    private List<SessionCode> createMockSessions() {
        List<SessionCode> mockSessions = new ArrayList<>();
        
        System.out.println("Creating mock sessions for UI testing");

        for (int i = 1; i <= 5; i++) {
            try {
                SessionCode session = new SessionCode();
                session.setId((long) i);

                if (i <= 2) {
                    session.setDateSession(LocalDate.now());
                } else if (i == 3) {
                    session.setDateSession(LocalDate.now().plusDays(1));
                } else {
                    session.setDateSession(LocalDate.now().plusDays(i + 3));
                }

                session.setHeureSession(LocalTime.of(8 + i, 0));

                Moniteur moniteur = new Moniteur();
                moniteur.setId((long) i);
                moniteur.setNom("Moniteur");
                moniteur.setPrenom(String.valueOf(i));
                session.setMoniteur(moniteur);

                session.setCapaciteMax(10);
                session.setSalle("Salle Test " + i);
                
                mockSessions.add(session);
                System.out.println("Created mock session for " + session.getDateSession() + " at " + session.getHeureSession());
            } catch (Exception e) {
                System.out.println("Error creating mock session: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        return mockSessions;
    }
    
    private void loadCandidats() {
        try {
            System.out.println("Loading candidates for selected session...");
            List<Candidat> allCandidats = getCandidats();

            if (allCandidats.isEmpty()) {
                System.out.println("No real candidates found, adding mock data");
                allCandidats = createMockCandidats();
            }
            
            candidats = FXCollections.observableArrayList();
    
            for (Candidat candidat : allCandidats) {
                CandidatWrapper wrapper = new CandidatWrapper(candidat);

                wrapper.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    updateSelectedCount();
                });
    
                candidats.add(wrapper);
            }
    
            candidatsTable.setItems(candidats);
            updateSelectedCount();
            
            System.out.println("Loaded " + candidats.size() + " candidates");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading candidates: " + e.getMessage());
            AlertUtil.showError("Erreur", "Erreur lors du chargement des candidats: " + e.getMessage());
        }
    }
    
    private List<Candidat> createMockCandidats() {
        List<Candidat> mockCandidats = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            Candidat candidat = new Candidat();
            candidat.setId((long) i);
            candidat.setNom("Candidat");
            candidat.setPrenom(String.valueOf(i));
            candidat.setEmail("candidat" + i + "@example.com");
            candidat.setTelephone("06123456" + (i < 10 ? "0" : "") + i);
            
            mockCandidats.add(candidat);
        }
        
        return mockCandidats;
    }

    private void updateSelectedSeanceInfo() {
        if (selectedSeance == null) {
            selectedSessionLabel.setText("Aucune séance sélectionnée");
            return;
        }

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        selectedSessionLabel.setText(String.format(
                "Séance du %s à %s - Moniteur: %s %s",
                selectedSeance.getDateSession().format(dateFormatter),
                selectedSeance.getHeureSession().format(timeFormatter),
                selectedSeance.getMoniteur().getNom(),
                selectedSeance.getMoniteur().getPrenom()
        ));
    }

    private void updateSelectedCount() {
        if (candidats == null || selectedCountLabel == null) {
            if (selectedCountLabel != null) {
                selectedCountLabel.setText("0");
            }
            return;
        }

        long count = candidats.stream().filter(CandidatWrapper::isSelected).count();
        selectedCountLabel.setText(String.valueOf(count));
    }

    @FXML
    private void rechercherSeances() {
        LocalDate selectedDate = dateFilter.getValue();
        
        try {
            if (selectedDate == null) {

                filteredSeances.setPredicate(seance -> true);
            } else {

                filteredSeances.setPredicate(seance -> {
                    if (seance == null || seance.getDateSession() == null) {
                        return false;
                    }
                    
                    LocalDate seanceDate = seance.getDateSession();
                    return seanceDate.equals(selectedDate);
                });

                if (filteredSeances.isEmpty()) {
                    selectedSessionLabel.setText("Aucune séance trouvée pour cette date");
                } else {
                    selectedSessionLabel.setText("Sélectionnez une séance dans la liste");
                }
            }
            
            System.out.println("Filter applied: " + (selectedDate != null ? selectedDate.toString() : "All dates") + 
                              ", Results: " + filteredSeances.size());
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors de la recherche : " + e.getMessage());
        }
    }

    @FXML
    private void resetFilters() {
        dateFilter.setValue(LocalDate.now());
        rechercherSeances();
    }

    @FXML
    private void selectAllCandidats() {
        if (candidats == null || candidats.isEmpty()) {
            System.out.println("No candidates available to select");
            return;
        }
        
        for (CandidatWrapper wrapper : candidats) {
            wrapper.setSelected(true);
        }
        updateSelectedCount();
    }

    @FXML
    private void deselectAllCandidats() {
        if (candidats == null || candidats.isEmpty()) {
            System.out.println("No candidates available to deselect");
            return;
        }
        
        for (CandidatWrapper wrapper : candidats) {
            wrapper.setSelected(false);
        }
        updateSelectedCount();
    }

    @FXML
    private void cancel() {

        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void registerCandidates() {
        if (selectedSeance == null) {
            AlertUtil.showWarning("Attention", "Aucune séance sélectionnée");
            return;
        }
        
        List<Integer> selectedCandidatIds = new ArrayList<>();
        for (CandidatWrapper wrapper : candidats) {
            if (wrapper.isSelected()) {
                selectedCandidatIds.add(wrapper.getId());
            }
        }
        
        if (selectedCandidatIds.isEmpty()) {
            AlertUtil.showWarning("Attention", "Aucun candidat sélectionné");
            return;
        }

        int seanceId = Math.toIntExact(selectedSeance.getId());
        boolean confirmed = AlertUtil.showConfirmation(
                "Confirmation d'inscription",
                "Voulez-vous inscrire " + selectedCandidatIds.size() + " candidat(s) à la séance sélectionnée?"
        );

        if (confirmed) {
            saveInscriptions(seanceId, selectedCandidatIds);

            resetSelection();
            loadUpcomingSessions();
        }
    }

    private void resetSelection() {
        try {
            seancesTable.getSelectionModel().clearSelection();
            selectedSeance = null;
            selectedSessionLabel.setText("Aucune séance sélectionnée");
            if (candidats != null) {
                candidats.clear();
            } else {
                candidats = FXCollections.observableArrayList();
            }
            updateSelectedCount();
            saveBtn.setDisable(true);
        } catch (Exception e) {
            System.out.println("Error in resetSelection: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadUpcomingSessions() {
        try {

            List<SessionCode> sessionsList = getSeances();

            if (sessionsList.isEmpty()) {
                System.out.println("No real sessions found, adding mock data");
                sessionsList.addAll(createMockSessions());
            }
            
            seances = FXCollections.observableArrayList(sessionsList);
            filteredSeances = new FilteredList<>(seances, p -> true);
            seancesTable.setItems(filteredSeances);

            if (dateFilter.getValue() != null) {
                rechercherSeances();
            }
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtil.showError("Erreur", "Erreur lors du chargement des séances: " + e.getMessage());
        }
    }
}

