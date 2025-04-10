package org.cpi2.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.cpi2.entities.*;
import org.cpi2.service.CandidatService;
import org.cpi2.service.ExamenService;
import org.cpi2.service.InscriptionService;
import org.cpi2.service.PaiementService;

import java.net.URL;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;

public class Paiement implements Initializable {
    private final PaiementService paiementService = new PaiementService();
    private final InscriptionService inscriptionService = new InscriptionService();
    private final CandidatService candidatService = new CandidatService();
    private final ExamenService examenService = new ExamenService();

    // Fields from FXML
    @FXML private ComboBox<String> typeComboBox;
    @FXML private ComboBox<String> candidatComboBox;
    @FXML private TextField montantField;
    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> modeComboBox;
    @FXML private TextArea descriptionArea;

    // Search fields
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> searchCandidatComboBox;

    // Table
    @FXML private TableView<PaiementData> paiementsTable;
    @FXML private TableColumn<PaiementData, LocalDate> dateColumn;
    @FXML private TableColumn<PaiementData, String> candidatColumn;
    @FXML private TableColumn<PaiementData, String> typeColumn;
    @FXML private TableColumn<PaiementData, Double> montantColumn;
    @FXML private TableColumn<PaiementData, String> methodeColumn;
    @FXML private TableColumn<PaiementData, String> descriptionColumn;
    @FXML private TableColumn<PaiementData, Void> actionsColumn;

    // Labels and buttons
    @FXML private Label totalLabel;
    @FXML private Button printButton;
    @FXML private Button exportButton;

    // Mock data class for table
    public static class PaiementData {
        private long id;
        private LocalDate date;
        private String candidat;
        private String cin;
        private String type;
        private Double montant;
        private String methode;
        private String description;

        public PaiementData(LocalDate date, String candidat, String type, Double montant, String methode, String description) {
            this.date = date;
            this.cin=cin;
            this.candidat = candidat;
            this.type = type;
            this.montant = montant;
            this.methode = methode;
            this.description = description;
        }
        public PaiementData(long id, LocalDate date,String cin, String candidat, String type, Double montant, String methode, String description) {
            this.id = id;
            this.date = date;
            this.cin=cin;
            this.candidat = candidat;
            this.type = type;
            this.montant = montant;
            this.methode = methode;
            this.description = description;
        }

        public long getId() {
            return id;
        }
        public void setId(long id) {
            this.id = id;
        }
        public String getCin() { return cin; }
        public LocalDate getDate() { return date; }
        public String getCandidat() { return candidat; }
        public String getType() { return type; }
        public Double getMontant() { return montant; }
        public String getMethode() { return methode; }
        public String getDescription() { return description; }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupFormControls();
        setupTable();
        setupListeners();
        loadMockData();
    }

    private void setupFormControls() {
        // Initialize payment type options
        typeComboBox.setItems(FXCollections.observableArrayList(
                "Inscription", "Examen", "Tranche"
        ));

        // Initialize mock candidates
        candidatComboBox.setItems(FXCollections.observableArrayList(
                candidatService.getAllCandidats().stream()
                        .map(c -> "("+c.getCin()+") : "+c.getNom()+" "+c.getPrenom())
                        .toList()
        ));

        searchCandidatComboBox.setItems(candidatComboBox.getItems());

        // Initialize payment modes
        modeComboBox.setItems(FXCollections.observableArrayList(
                Arrays.stream(ModePaiement.values())
                        .map(Enum::name)
                        .toList()
        ));

        // Set today's date as default
        datePicker.setValue(LocalDate.now());

        // Add listener for amount field validation
        montantField.textProperty().addListener((obs, old, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                montantField.setText(old);
            }
        });
    }

    private void setupTable() {
        // Configure table columns
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        candidatColumn.setCellValueFactory(new PropertyValueFactory<>("candidat"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        montantColumn.setCellValueFactory(new PropertyValueFactory<>("montant"));
        methodeColumn.setCellValueFactory(new PropertyValueFactory<>("methode"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Add action buttons to the actions column
        actionsColumn.setCellFactory(param -> new TableCell<>() {
            private final Button editButton = new Button("Modifier");
            private final Button deleteButton = new Button("Supprimer");

            {
                editButton.getStyleClass().add("edit-button");
                deleteButton.getStyleClass().add("delete-button");

                editButton.setOnAction(event -> {
                    PaiementData data = getTableView().getItems().get(getIndex());
                    if(datePicker.getValue() == null && montantField.getText().isEmpty() && descriptionArea.getText().isEmpty()){
                        showErrorDialog("Veuillez remplir au moine un champ pour la modification");
                        return;
                    }
                    if(candidatComboBox.getValue() != null && !candidatComboBox.getValue().substring(1, candidatComboBox.getValue().indexOf(")")).equals(data.candidat.substring(1, data.candidat.indexOf(")")))){
                        showErrorDialog("Vous ne pouvez pas modifier le candidat");
                        return;
                    }
                    if(typeComboBox.getValue() != null && !typeComboBox.getValue().equals(data.type)){
                        showErrorDialog("Vous ne pouvez pas modifier le type de paiement");
                        return;
                    }
                    if(data.type.equals("Examen") && !montantField.getText().equals(data.montant.toString())){
                        showErrorDialog("Vous ne pouvez pas modifier montant du paiement d'examen");
                        return;
                    }
                    try{
                    if(data.type.equals("Examen")){
                        paiementService.update(new PaiementExamen(
                                StatutPaiement.COMPLETE,
                                data.getId(),
                                candidatService.getCandidatByCin(data.getCin()),
                                montantField.getText().isEmpty() ? data.getMontant() : Double.parseDouble(montantField.getText()),
                                datePicker.getValue()==null ? data.date : datePicker.getValue(),
                                modeComboBox.getValue() == null ? ModePaiement.valueOf(data.methode) : ModePaiement.valueOf(modeComboBox.getValue()),
                                examenService.getPendingExamen(data.getCin()),
                                descriptionArea.getText().isEmpty() ? data.description : descriptionArea.getText()
                        ));
                    }
                    else{
                        Inscription inscription = inscriptionService.getActifInscirptionBycin(data.getCin()).get(0);
                        double reste = paiementService.calculerMontantRestant(inscription.getId());
                        if(!montantField.getText().isEmpty()) {
                            if (Double.parseDouble(montantField.getText()) > reste) {
                                showErrorDialog("Montant supérieur au montant restant (" + reste + ")DT");
                                return;
                            }
                            if (Double.parseDouble(montantField.getText()) < reste && typeComboBox.getValue().equals("Totale")) {
                                showErrorDialog("Montant inférieur au montant de l'inscription Totale (" + inscription.getPlan().getPrice() + ")DT");
                                return;
                            }
                        }
                        paiementService.update(new PaiementInscription(
                                StatutPaiement.COMPLETE,
                                data.getId(),
                                candidatService.getCandidatByCin(data.getCin()),
                                montantField.getText().isEmpty() ? data.getMontant() : Double.parseDouble(montantField.getText()),
                                datePicker.getValue()==null ? data.date : datePicker.getValue(),
                                modeComboBox.getValue() == null ? ModePaiement.valueOf(data.methode) : ModePaiement.valueOf(modeComboBox.getValue()),
                                inscription,
                                typeComboBox.getValue() == null ? data.type : typeComboBox.getValue(),
                                descriptionArea.getText().isEmpty() ? data.description : descriptionArea.getText()
                        ));
                    }
                    }catch (Exception e){
                        showErrorDialog("Erreur lors de la modification du paiement");
                        e.printStackTrace();
                        return;
                    }
                    showSuccessDialog("Paiement modifié avec succès");
                    loadMockData();
                    updateTotalLabel();
                });

                deleteButton.setOnAction(event -> {
                    //need to check the dates updates in the inscription :'(
                    PaiementData data = getTableView().getItems().get(getIndex());
                    if(!data.type.equals("Examen")) {
                        PaiementInscription paiement = (PaiementInscription) paiementService.getPaiementById(data.getId()).orElseThrow();
                        paiement.setStatut(StatutPaiement.ANNULEE);
                        if (paiement.getInscription().isActive()) {
                            inscriptionService.updatePaymentStatus(paiement.getInscription().getId(), false);
                            paiementService.cancelPiament(data.getId());
                        }
                        else{
                            showErrorDialog("Impossible de supprimer le paiement d'inscription car l'inscription n'est pas active");
                            return;
                        }

                    }
                    showSuccessDialog("Paiement supprimé avec succès");
                    getTableView().getItems().remove(getIndex());
                    updateTotalLabel();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupListeners() {
        // Setup button handlers
        printButton.setOnAction(event -> showSuccessDialog("Impression en cours..."));
        exportButton.setOnAction(event -> showSuccessDialog("Exportation en cours..."));
    }

    private void loadMockData() {

        ObservableList<PaiementData> paiements = FXCollections.observableArrayList(
                paiementService.getAllPaiements().stream()
                        .map(p -> new PaiementData(
                                p.getId(),
                                p.getDatePaiement(),
                                p.getCandidat().getCin(),
                                p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                                p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",//TODO: need to determine if its a monthly payment or not
                                p.getMontant(),
                                p.getModePaiement().toString(),
                                p.getDescription()
                        ))
                        .toList()

        );
        paiementsTable.setItems(paiements);
        updateTotalLabel();
    }

    @FXML
    private void handleSavePayment() {
        if (!validatePaymentForm()) {
            return;
        }

        try {
            double montant = Double.parseDouble(montantField.getText());
            String type = typeComboBox.getValue();
            String candidat = candidatComboBox.getValue();
            String mode = modeComboBox.getValue();
            LocalDate date = datePicker.getValue();
            String description = descriptionArea.getText();

            String cin = candidat.substring(1, candidat.indexOf(")"));
            // Save the payment to the database
            try{
            if (!type.equals("Examen")) {
                if(inscriptionService.haveActifInscription(cin)){
                    Inscription inscription = inscriptionService.getActifInscirptionBycin(cin).get(0);
                    double reste = paiementService.calculerMontantRestant(inscription.getId());

                    if (type.equals("Inscription"))
                        type = "Totale";
                    else
                        type = inscription.getPaymentCycle();
                    if(!Objects.equals(type, inscription.getPaymentCycle())){
                        showErrorDialog("Type de paiement incorrect vous avez choisit "+inscription.getPaymentCycle()+" lors de l'inscription");
                        return;
                    }
                    if(montant>reste){
                        if(reste==0){
                            showErrorDialog("Le candidat a déjà payé la totalité de son inscription");//TODO: add the rest of the message
                        }
                        else
                            showErrorDialog("Montant supérieur au montant restant ("+reste+")DT");//TODO: add the rest of the message
                        return;
                    }
                    if(montant < reste && Objects.equals(type, "Totale")){
                        showErrorDialog("Montant inférieur au montant de l'inscription ("+inscription.getPlan().getPrice()+")DT");//TODO: add the rest of the message
                        return;
                    }


                    paiementService.enregistrerPaiement(new PaiementInscription(StatutPaiement.COMPLETE,null,
                            candidatService.getCandidatByCin(cin),
                            montant, date, ModePaiement.valueOf(mode), inscription, type, description
                    ));
                    if(montant==reste){
                        inscriptionService.updatePaymentStatus(inscription.getId(),true);
                    }

                    showSuccessDialog("Paiement enregistré avec succès il vous reste "+(reste - montant)+" DT à payer");
                }
                else {
                    showErrorDialog("Le candidat n'a pas d'inscription active");
                    return;
                }

            } else {
                if(!examenService.hasPendingExamens(cin)){
                    showErrorDialog("Le candidat n'a pas d'examen actif");
                    return;
                }
                Examen examen = examenService.getPendingExamen(cin);
                if(montant > examen.getFrais()){
                    showErrorDialog("Montant supérieur au montant de l'examen ("+examen.getFrais()+")DT");
                    return;
                }
                if(montant < examen.getFrais() ){
                    showErrorDialog("Montant inférieur au montant de l'examen ("+examen.getFrais()+")DT");
                    return;
                }
                paiementService.enregistrerPaiement(new PaiementExamen(StatutPaiement.COMPLETE  ,null,
                        candidatService.getCandidatByCin(cin),
                        montant, date, ModePaiement.valueOf(mode), examen, "Payement d'examen du "+examen.getType()
                ));
                return;
            }


            loadMockData();

            }catch(Exception e){
                showErrorDialog("Erreur lors de l'enregistrement du paiement");
                e.printStackTrace();//for debugging
                return;
            }

            updateTotalLabel();
            showSuccessDialog("Paiement enregistré avec succès");
            clearPaymentForm();
        } catch (NumberFormatException e) {
            showErrorDialog("Montant invalide");
        }
    }


    @FXML
    private void handleSearch() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        String candidat = searchCandidatComboBox.getValue();
        if (startDate == null && (endDate == null || candidat == null)) {
            paiementsTable.getItems().clear();
            paiementService.getAllPaiements()
                    .forEach(p -> paiementsTable.getItems().add(new PaiementData(
                            p.getDatePaiement(),
                            p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                            p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                            p.getMontant(),
                            p.getModePaiement().toString(),
                            p.getDescription()
                    )));
            return;
        }
        if (startDate.isAfter(endDate)) {
            showErrorDialog("La date de début ne peut pas être après la date de fin");
            return;
        }
        System.out.println(searchCandidatComboBox.getValue());
        if(searchCandidatComboBox.getValue() == null || searchCandidatComboBox.getValue().isEmpty()){
            paiementsTable.getItems().clear();
            paiementService.getAllPaiements().stream().filter(p -> !p.getDatePaiement().isBefore(startDate) && !p.getDatePaiement().isAfter(endDate))
                    .forEach(p -> paiementsTable.getItems().add(new PaiementData(
                            p.getDatePaiement(),
                            p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                            p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                            p.getMontant(),
                            p.getModePaiement().toString(),
                            p.getDescription()
                    )));
        }
        else {
            paiementsTable.getItems().clear();
            String cin = candidat.substring(1, candidat.indexOf(")"));
            paiementService.getAllPaiements().stream().filter(p -> p.getCandidat().getCin().equals(cin) && !p.getDatePaiement().isBefore(startDate) && !p.getDatePaiement().isAfter(endDate))
                    .forEach(p -> paiementsTable.getItems().add(new PaiementData(
                            p.getDatePaiement(),
                            p.getCandidat().getNom()+" "+p.getCandidat().getPrenom(),
                            p instanceof PaiementInscription ? ((p.getTypePaiement() == null) ? "Inscription" : p.getTypePaiement()): "Examen",
                            p.getMontant(),
                            p.getModePaiement().toString(),
                            p.getDescription()
                    )));
        }
    }

    private boolean validatePaymentForm() {
        if (typeComboBox.getValue() == null) {
            showErrorDialog("Veuillez sélectionner le type de paiement");
            return false;
        }
        if (candidatComboBox.getValue() == null) {
            showErrorDialog("Veuillez sélectionner un candidat");
            return false;
        }
        if (montantField.getText().isEmpty()) {
            showErrorDialog("Veuillez saisir le montant");
            return false;
        }
        if (datePicker.getValue() == null) {
            showErrorDialog("Veuillez sélectionner une date");
            return false;
        }
        if (modeComboBox.getValue() == null) {
            showErrorDialog("Veuillez sélectionner le mode de paiement");
            return false;
        }
        return true;
    }

    private void clearPaymentForm() {
        typeComboBox.setValue(null);
        candidatComboBox.setValue(null);
        montantField.clear();
        datePicker.setValue(LocalDate.now());
        modeComboBox.setValue(null);
        descriptionArea.clear();
    }

    private void updateTotalLabel() {
        double total = paiementsTable.getItems().stream()
                .mapToDouble(PaiementData::getMontant)
                .sum();
        totalLabel.setText(String.format("Total: %.2f DT", total));
    }

    // Dialog methods
    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succès");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}