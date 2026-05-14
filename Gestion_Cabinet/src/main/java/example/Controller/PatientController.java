package example.Controller;

import example.DAO.PatientDAO;
import example.Model.Patient;
import example.Model.Utilisateur;
import example.Util.NavigationManager;
import example.Util.Refreshable;
import example.Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.input.MouseEvent;

public class PatientController implements Initializable, Refreshable {
    private int selectedPatientId = -1;

    @FXML private TextField nomField, prenomField, ageField, telephoneField, rechercheField;
    @FXML private Label validationLabel, compteurLabel, initialesLabel, nomUtilisateurLabel, roleLabel;
    @FXML private TableView<Patient> patientTable;
    @FXML private TableColumn<Patient, String> colNom, colPrenom, colTelephone;
    @FXML private TableColumn<Patient, Integer> colAge;

    private final PatientDAO patientDAO = new PatientDAO();
    private final ObservableList<Patient> patientsData = FXCollections.observableArrayList();
    private int idPatientSelectionne = -1;
    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        if (validationLabel != null) validationLabel.setVisible(false);
        patientTable.getSelectionModel().selectedItemProperty().addListener((obs, ancien, patient) -> {
            if (patient != null) remplirFormulaire(patient);
        });
    }

    // Appelé par NavigationManager à chaque arrivée sur cette page
    @Override
    public void onShow() {
        afficherInfosUtilisateur();
        chargerTousLesPatients();
        effacerFormulaire();
    }

    private void configurerColonnes() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colPrenom.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        colAge.setCellValueFactory(new PropertyValueFactory<>("age"));
        colTelephone.setCellValueFactory(new PropertyValueFactory<>("telephone"));
        patientTable.setItems(patientsData);
    }

    private void afficherInfosUtilisateur() {
        Utilisateur u = SessionManager.getUtilisateur();
        if (u != null) {
            if (initialesLabel != null) initialesLabel.setText(u.getInitiales());
            if (nomUtilisateurLabel != null) nomUtilisateurLabel.setText(u.getNomComplet());
            if (roleLabel != null)
                roleLabel.setText("Admin".equalsIgnoreCase(u.getRole()) ? "Administrateur" : "Secrétaire");
        }
    }

    private void chargerTousLesPatients() {
        try {
            List<Patient> liste = patientDAO.getTousLesPatients();
            patientsData.setAll(liste);
            if (compteurLabel != null)
                compteurLabel.setText(liste.size() + " patient(s) au total");
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterPatient() {
        if (!validerFormulaire()) return;
        try {
            if (patientDAO.ajouterPatient(construirePatient())) {
                afficherSucces("Patient ajouté avec succès !");
                effacerFormulaire();
                chargerTousLesPatients();
            }
        } catch (SQLException e) { afficherErreur("Erreur d'ajout : " + e.getMessage()); }
    }

    @FXML
    private void modifierPatient() {
        if (idPatientSelectionne == -1) { afficherErreur("Sélectionnez un patient à modifier."); return; }
        if (!validerFormulaire()) return;
        try {
            Patient p = construirePatient();
            p.setId(idPatientSelectionne);
            if (patientDAO.modifierPatient(p)) {
                afficherSucces("Patient modifié avec succès !");
                effacerFormulaire();
                chargerTousLesPatients();
            }
        } catch (SQLException e) { afficherErreur("Erreur de modification : " + e.getMessage()); }
    }

    @FXML
    private void supprimerPatient() {
        Patient selectionne = patientTable.getSelectionModel().getSelectedItem();
        if (selectionne == null) { afficherErreur("Sélectionnez un patient à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer le patient « " + selectionne.getNom() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (patientDAO.supprimerPatient(selectionne.getId())) {
                    afficherSucces("Patient supprimé.");
                    effacerFormulaire();
                    chargerTousLesPatients();
                }
            } catch (SQLException e) { afficherErreur("Erreur de suppression : " + e.getMessage()); }
        }
    }

    @FXML
    private void rechercherPatient() {
        try {
            String critere = rechercheField.getText().trim();
            List<Patient> res = patientDAO.rechercherPatients(critere);
            patientsData.setAll(res);
            if (compteurLabel != null) compteurLabel.setText(res.size() + " résultat(s)");
        } catch (SQLException e) { afficherErreur("Erreur de recherche."); }
    }

    @FXML
    private void afficherTousPatients() {
        if (rechercheField != null) rechercheField.clear();
        chargerTousLesPatients();
    }

    @FXML
    public void effacerFormulaire() {
        if (nomField != null) nomField.clear();
        if (prenomField != null) prenomField.clear();
        if (ageField != null) ageField.clear();
        if (telephoneField != null) telephoneField.clear();
        idPatientSelectionne = -1;
        patientTable.getSelectionModel().clearSelection();
        if (validationLabel != null) validationLabel.setVisible(false);
    }

    private void remplirFormulaire(Patient p) {
        nomField.setText(p.getNom());
        prenomField.setText(p.getPrenom());
        ageField.setText(String.valueOf(p.getAge()));
        telephoneField.setText(p.getTelephone());
        idPatientSelectionne = p.getId();
    }

    private boolean validerFormulaire() {
        if (!PatientDAO.validerNom(nomField.getText()))           { afficherErreur("Nom invalide."); return false; }
        if (!PatientDAO.validerAge(ageField.getText()))           { afficherErreur("Âge invalide (1-150)."); return false; }
        if (!PatientDAO.validerTelephone(telephoneField.getText())){ afficherErreur("Téléphone invalide."); return false; }
        return true;
    }

    private Patient construirePatient() {
        return new Patient(0,
                nomField.getText().trim(),
                prenomField.getText().trim(),
                Integer.parseInt(ageField.getText().trim()),
                telephoneField.getText().trim());
    }

    // ════════════════════════════════════════════════════════════════
    // NAVIGATION — NavigationManager (même système que Dashboard)
    // ════════════════════════════════════════════════════════════════
    @FXML private void allerDashboard(ActionEvent e)  { naviguer("/view/dashboard.fxml",  "Tableau de Bord",        1200, 750, e); }
    @FXML private void allerPatients(ActionEvent e)   { naviguer("/view/patient.fxml",    "Gestion des Patients",   1200, 750, e); }
    @FXML private void allerMaladies(ActionEvent e)   { naviguer("/view/maladie.fxml",    "Gestion des Maladies",   1200, 750, e); }
    @FXML private void allerRendezVous(ActionEvent e) { naviguer("/view/rendezvous.fxml", "Gestion des Rendez-vous",1200, 750, e); }

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

    // ════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ════════════════════════════════════════════════════════════════
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
    @FXML
    private void ligneClicker(MouseEvent event) {
        Patient selectedPatient = patientTable.getSelectionModel().getSelectedItem();

        if (selectedPatient != null) {
            System.out.println("ID : " + selectedPatient.getId());
            selectedPatientId = selectedPatient.getId();
        }
    }

    @FXML
    private void reserverRdv(ActionEvent event) {
        System.out.println("ici");
        if (selectedPatientId == -1) {
            System.out.println("Veuillez sélectionner un patient !");
            return;
        }

        // Stocker l'ID du patient dans PatientSession
        PatientSession.setSelectedPatientId(selectedPatientId);

        // Naviguer vers la page rendezvous.fxml
        naviguer("/view/rendezvous.fxml", "Réserver RDV", 600, 400, event);
    }
}