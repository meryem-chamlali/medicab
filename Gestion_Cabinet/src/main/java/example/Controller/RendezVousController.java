package example.Controller;

import example.DAO.MaladieDAO;
import example.DAO.PatientDAO;
import example.DAO.RendezVousDAO;
import example.Model.*;
import example.Util.DateUtil;
import example.Util.NavigationManager;
import example.Util.Refreshable;
import example.Util.SessionManager;

import fr.opensagres.xdocreport.document.IXDocReport;
import fr.opensagres.xdocreport.document.registry.XDocReportRegistry;
import fr.opensagres.xdocreport.template.IContext;
import fr.opensagres.xdocreport.template.TemplateEngineKind;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

public class RendezVousController implements Initializable, Refreshable {

    @FXML private Label initialesLabel, nomUtilisateurLabel, roleLabel;
    @FXML private Label validationLabel, compteurLabel;

    @FXML private ComboBox<Patient>  patientComboBox;
    @FXML private ComboBox<Maladie>  maladieComboBox;   // ← nouveau
    @FXML private DatePicker         datePicker;
    @FXML private TextField          heureField, motifField, rechercheField;
    @FXML private ComboBox<String>   statutComboBox;

    @FXML private TableView<RendezVous>             rdvTable;
    @FXML private TableColumn<RendezVous, String>   colPatient, colMaladie, colDate, colHeure, colStatut;

    @FXML
    private Button ajouterBtn;
    @FXML
    private Button modifierBtn;
    @FXML
    private Button supprimerBtn;
    @FXML
    private Button viderBtn;

    private final RendezVousDAO rdvDAO     = new RendezVousDAO();
    private final PatientDAO    patientDAO = new PatientDAO();
    private final MaladieDAO    maladieDAO = new MaladieDAO();

    private final ObservableList<RendezVous> rdvsData     = FXCollections.observableArrayList();
    private final ObservableList<Patient>    patientsData = FXCollections.observableArrayList();
    private final ObservableList<Maladie>    maladiesData = FXCollections.observableArrayList();



    private int rdvSelectionneId = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();

        if (statutComboBox != null) {
            statutComboBox.getItems().addAll("Planifie", "Confirme", "Annule", "Termine");
            statutComboBox.setValue("Planifie");
        }

        rdvTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, rdv) -> {
            if (rdv != null) remplirFormulaire(rdv);
        });

        if (validationLabel != null) validationLabel.setVisible(false);

        cacherMedecinBoutons();
    }

    @Override
    public void onShow() {
        afficherInfosUtilisateur();
        chargerPatients();
        chargerMaladies();
        chargerTousLesRendezVous();
        effacerFormulaire();
    }

    private void cacherMedecinBoutons(){
        Utilisateur u = SessionManager.getUtilisateur();
        if(!u.getRole().equals("Admin")){
            ajouterBtn.setVisible(false);
            supprimerBtn.setVisible(false);
            modifierBtn.setVisible(false);
            viderBtn.setVisible(false);
        }
    }

    private void configurerColonnes() {
        colPatient.setCellValueFactory(new PropertyValueFactory<>("nomPatient"));
        colMaladie.setCellValueFactory(new PropertyValueFactory<>("maladieNom"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateRdv"));
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heureRdv"));
        colStatut.setCellValueFactory(new PropertyValueFactory<>("statut"));
        rdvTable.setItems(rdvsData);
    }

    private void afficherInfosUtilisateur() {
        Utilisateur u = SessionManager.getUtilisateur();
        if (u != null) {
            if (initialesLabel != null)     initialesLabel.setText(u.getInitiales());
            if (nomUtilisateurLabel != null) nomUtilisateurLabel.setText(u.getNomComplet());
            if (roleLabel != null)
                roleLabel.setText("Admin".equalsIgnoreCase(u.getRole()) ? "Administrateur" : "Secrétaire");
        }
    }

    private void chargerPatients() {
        try {
            List<Patient> liste = patientDAO.getTousLesPatients();
            patientsData.setAll(liste);
            if (patientComboBox != null) {
                patientComboBox.setItems(patientsData);
                patientComboBox.setConverter(new javafx.util.StringConverter<Patient>() {
                    @Override public String toString(Patient p) {
                        return p == null ? "" : p.getNom() + " " + p.getPrenom();
                    }
                    @Override public Patient fromString(String s) { return null; }
                });
            }
        } catch (SQLException e) { afficherErreur("Erreur chargement patients."); }
    }

    private void chargerMaladies() {
        try {
            List<Maladie> liste = maladieDAO.getToutesLesMaladies();
            maladiesData.setAll(liste);
            if (maladieComboBox != null) {
                maladieComboBox.setItems(maladiesData);
                maladieComboBox.setConverter(new javafx.util.StringConverter<Maladie>() {
                    @Override public String toString(Maladie m) {
                        return m == null ? "" : m.getNom();
                    }
                    @Override public Maladie fromString(String s) { return null; }
                });
            }
        } catch (SQLException e) { afficherErreur("Erreur chargement maladies."); }
    }

    private void chargerTousLesRendezVous() {
        try {
            List<RendezVous> liste = rdvDAO.getTousLesRendezVous();
            rdvsData.setAll(liste);
            if (compteurLabel != null)
                compteurLabel.setText(liste.size() + " rendez-vous au total");
        } catch (SQLException e) { afficherErreur("Erreur chargement RDV."); }
    }

    private void chargerTousLesRendezVousParDate() {
        try {
            List<RendezVous> liste = rdvDAO.getTousLesRendezVousParDate(rechercheField.getText());
            rdvsData.setAll(liste);
            if (compteurLabel != null)
                compteurLabel.setText(liste.size() + " rendez-vous au total");
        } catch (SQLException e) { afficherErreur("Erreur chargement RDV."); }
    }

    @FXML
    private void ajouterRendezVous() {
        if (!validerFormulaire()) return;
        try {
            RendezVous rdv = recupererRDVObjet();
            if (rdvDAO.ajouterRendezVous(rdv)) {
                afficherSucces("Rendez-vous ajouté avec succès !");
                effacerFormulaire();
                chargerTousLesRendezVous();
            }
        } catch (SQLException e) { afficherErreur("Erreur ajout : " + e.getMessage()); }
    }

    @FXML
    private void modifierRendezVous() {
        if (!validerFormulaire()) return;
        try {
            int idRDV = rdvTable.getSelectionModel().getSelectedItem().getId();
            RendezVous rdvModifie = recupererRDVObjet();
            rdvModifie.setPatientId(
                    rdvTable.getSelectionModel().getSelectedItem().getPatientId()
            );
            if (rdvDAO.modifierRendezVous(rdvModifie, idRDV)) {
                afficherSucces("Rendez-vous modifié !");
                effacerFormulaire();
                chargerTousLesRendezVous();
            }
        } catch (SQLException e) { afficherErreur("Erreur modification : " + e.getMessage()); }
    }

    @FXML
    private void supprimerRendezVous() {
        //donne l'objet
        RendezVous ligneSelectionee = rdvTable.getSelectionModel().getSelectedItem();
        try {
            rdvDAO.supprimerRendezVous(ligneSelectionee.getId());
            chargerTousLesRendezVous();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void telechargerConsultation() {

        try {
            // 1. Vérifier sélection
            RendezVous selected = rdvTable.getSelectionModel().getSelectedItem();
            if (selected == null) {
                System.out.println("Veuillez sélectionner un RDV !");
                return;
            }

            RendezVous rdv = rdvDAO.recupererRDV(selected.getId());

            // 2. Charger le template
            InputStream templateStream = getClass()
                    .getResourceAsStream("/template/consultation.docx");

            IXDocReport report = XDocReportRegistry.getRegistry()
                    .loadReport(templateStream, TemplateEngineKind.Velocity);

            // 3. Créer le contexte
            IContext context = report.createContext();
            context.put("nom", rdv.getNomPatient());
            context.put("maladie", rdv.getMaladieNom());
            context.put("consultation", rdv.getConsultation());

            // 4. Choisir où sauvegarder le fichier
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Enregistrer la consultation");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Fichier Word", "*.docx")
            );

            File file = fileChooser.showSaveDialog(rdvTable.getScene().getWindow());

            if (file != null) {
                try (OutputStream out = new FileOutputStream(file)) {
                    // 5. Générer le document
                    report.process(context, out);
                }
                System.out.println("Fichier généré avec succès !");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void afficherTousRendezVousParDate() {
        if (rechercheField != null) {
            if(rechercheField.getText() == null || rechercheField.getText() == ""){
                chargerTousLesRendezVous();
                rechercheField.clear();
                return;
            }
            chargerTousLesRendezVousParDate();
            rechercheField.clear();
        }
    }

    @FXML
    private void effacerFormulaire() {
        if (patientComboBox != null)  patientComboBox.setValue(null);
        if (maladieComboBox != null)  maladieComboBox.setValue(null);
        if (datePicker != null)       datePicker.setValue(null);
        if (heureField != null)       heureField.clear();
        if (motifField != null)       motifField.clear();
        if (statutComboBox != null)   statutComboBox.setValue("Planifie");
        rdvSelectionneId = -1;
        rdvTable.getSelectionModel().clearSelection();
        if (validationLabel != null) validationLabel.setVisible(false);
    }

    private void remplirFormulaire(RendezVous rdv) {
        rdvSelectionneId = rdv.getId();
        if (maladieComboBox != null) {
            if (rdv.getMaladieId() > 0) {
                for (Maladie m : maladiesData) {
                    if (m.getId() == rdv.getMaladieId()) {
                        maladieComboBox.setValue(m); break;
                    }
                }
            } else {
                maladieComboBox.setValue(null);
            }
        }
        if (datePicker != null) {
            try { datePicker.setValue(java.time.LocalDate.parse(DateUtil.transformerDateEnString(rdv.getDateRdv()))); }
            catch (Exception ignored) {}
        }
        if (heureField != null)     heureField.setText(DateUtil.transformerTimeEnString(rdv.getHeureRdv()));
        if (statutComboBox != null) statutComboBox.setValue(rdv.getStatut().name());
    }

    private boolean validerFormulaire() {
        if (datePicker.getValue() == null) {
            afficherErreur("Sélectionnez une date."); return false; }
        if (statutComboBox.getValue() == null) {
            afficherErreur("Statut invalide."); return false; }
        if (heureField.getText() == null) {
            afficherErreur("heure invalide."); return false; }
        return true;
    }

    private RendezVous recupererRDVObjet() {
        Maladie m = maladieComboBox != null ? maladieComboBox.getValue() : null;
        int maladieId = (m != null) ? m.getId() : 0;
        Date dateRDV = DateUtil.transformerStringEnDate(datePicker.getValue().toString());
        Time heureRDV = DateUtil.transformerStringEnTime(heureField.getText().trim());
        Statut statut = Statut.valueOf(statutComboBox.getValue());

        return new RendezVous(
                PatientSession.getSelectedPatientId(),
                maladieId,
                dateRDV,
                heureRDV,
                statut,
                null);
    }

    // ── NAVIGATION ──────────────────────────────────────────────────
    @FXML private void allerDashboard(ActionEvent e)  { naviguer("/view/dashboard.fxml",  "Tableau de Bord",         1200, 750, e); }
    @FXML private void allerPatients(ActionEvent e)   { naviguer("/view/patient.fxml",    "Gestion des Patients",    1200, 750, e); }
    @FXML private void allerMaladies(ActionEvent e)   { naviguer("/view/maladie.fxml",    "Gestion des Maladies",    1200, 750, e); }
    @FXML private void allerRendezVous(ActionEvent e) { naviguer("/view/rendezvous.fxml", "Gestion des Rendez-vous", 1200, 750, e); }

    @FXML
    private void deconnexion(ActionEvent event) {
        SessionManager.setUtilisateur(null);
        NavigationManager.clearCache();
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setResizable(false);
            NavigationManager.navigate(stage, "/view/login.fxml", "Connexion", 480, 560);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void naviguer(String fxml, String titre, double w, double h, ActionEvent event) {
        try {
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            NavigationManager.navigate(stage, fxml, titre, w, h);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── UTILITAIRES ─────────────────────────────────────────────────
    private void afficherErreur(String msg) {
        if (validationLabel == null) return;
        validationLabel.setText("⚠ " + msg);
        validationLabel.setStyle("-fx-text-fill: #e53935; -fx-font-weight: bold;");
        validationLabel.setVisible(true);
    }

    private void afficherSucces(String msg) {
        if (validationLabel == null) return;
        validationLabel.setText("✔ " + msg);
        validationLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        validationLabel.setVisible(true);
    }
}