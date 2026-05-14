package example.Controller;

import example.DAO.MaladieDAO;
import example.Model.Maladie;
import example.Model.Utilisateur;
import example.Util.NavigationManager;
import example.Util.Refreshable;
import example.Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class MaladieController implements Initializable, Refreshable {

    @FXML private TextField codeField, nomField, categorieField, rechercheField;
    @FXML private TextArea descriptionArea;
    @FXML private Label validationLabel, compteurLabel, initialesLabel, nomUtilisateurLabel, roleLabel;
    @FXML private TableView<Maladie> maladieTable;
    @FXML private TableColumn<Maladie, String> colCode, colNom, colCategorie, colDescription;

    private final MaladieDAO maladieDAO = new MaladieDAO();
    private final ObservableList<Maladie> maladiesData = FXCollections.observableArrayList();
    private int idMaladieSelectionnee = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerColonnes();
        if (validationLabel != null) validationLabel.setVisible(false);
        maladieTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, maladie) -> {
            if (maladie != null) remplirFormulaire(maladie);
        });
    }

    // Appelé par NavigationManager à chaque fois qu'on arrive sur cette page
    @Override
    public void onShow() {
        afficherInfosUtilisateur();
        chargerToutesLesMaladies();
        effacerFormulaire();
    }

    private void configurerColonnes() {
        colCode.setCellValueFactory(new PropertyValueFactory<>("code"));
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colCategorie.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("description"));
        maladieTable.setItems(maladiesData);
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

    private void chargerToutesLesMaladies() {
        try {
            List<Maladie> liste = maladieDAO.getToutesLesMaladies();
            maladiesData.setAll(liste);
            if (compteurLabel != null)
                compteurLabel.setText(liste.size() + " maladie(s) au total");
        } catch (SQLException e) {
            afficherErreur("Erreur de chargement : " + e.getMessage());
        }
    }

    @FXML
    private void ajouterMaladie() {
        if (!validerFormulaire()) return;
        try {
            if (maladieDAO.ajouterMaladie(construireMaladie())) {
                afficherSucces("Maladie ajoutée avec succès.");
                effacerFormulaire();
                chargerToutesLesMaladies();
            }
        } catch (SQLException e) { afficherErreur("Erreur d'ajout : " + e.getMessage()); }
    }

    @FXML
    private void modifierMaladie() {
        if (idMaladieSelectionnee == -1) { afficherErreur("Sélectionnez une maladie à modifier."); return; }
        if (!validerFormulaire()) return;
        try {
            Maladie maladie = construireMaladie();
            maladie.setId(idMaladieSelectionnee);
            if (maladieDAO.modifierMaladie(maladie)) {
                afficherSucces("Maladie modifiée avec succès.");
                effacerFormulaire();
                chargerToutesLesMaladies();
            }
        } catch (SQLException e) { afficherErreur("Erreur de modification : " + e.getMessage()); }
    }

    @FXML
    private void supprimerMaladie() {
        Maladie selectionnee = maladieTable.getSelectionModel().getSelectedItem();
        if (selectionnee == null) { afficherErreur("Sélectionnez une maladie à supprimer."); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la maladie « " + selectionnee.getNom() + " » ?",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText(null);
        if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (maladieDAO.supprimerMaladie(selectionnee.getId())) {
                    afficherSucces("Maladie supprimée.");
                    effacerFormulaire();
                    chargerToutesLesMaladies();
                }
            } catch (SQLException e) { afficherErreur("Erreur de suppression : " + e.getMessage()); }
        }
    }

    @FXML
    private void rechercherMaladie() {
        try {
            String critere = rechercheField.getText().trim();
            List<Maladie> res = maladieDAO.rechercherMaladies(critere);
            maladiesData.setAll(res);
            if (compteurLabel != null) compteurLabel.setText(res.size() + " résultat(s)");
        } catch (SQLException e) { afficherErreur("Erreur de recherche."); }
    }

    @FXML
    private void afficherToutesMaladies() {
        rechercheField.clear();
        chargerToutesLesMaladies();
    }

    @FXML
    private void effacerFormulaire() {
        if (codeField != null) codeField.clear();
        if (nomField != null) nomField.clear();
        if (categorieField != null) categorieField.clear();
        if (descriptionArea != null) descriptionArea.clear();
        idMaladieSelectionnee = -1;
        maladieTable.getSelectionModel().clearSelection();
        if (validationLabel != null) validationLabel.setVisible(false);
    }

    private void remplirFormulaire(Maladie maladie) {
        codeField.setText(maladie.getCode());
        nomField.setText(maladie.getNom());
        categorieField.setText(maladie.getCategorie());
        descriptionArea.setText(maladie.getDescription());
        idMaladieSelectionnee = maladie.getId();
    }

    private boolean validerFormulaire() {
        if (!MaladieDAO.validerCode(codeField.getText()))     { afficherErreur("Code invalide."); return false; }
        if (!MaladieDAO.validerNom(nomField.getText()))       { afficherErreur("Nom invalide."); return false; }
        if (!MaladieDAO.validerCategorie(categorieField.getText())) { afficherErreur("Catégorie invalide."); return false; }
        return true;
    }

    private Maladie construireMaladie() {
        return new Maladie(0,
                codeField.getText().trim(),
                nomField.getText().trim(),
                categorieField.getText().trim(),
                descriptionArea.getText() == null ? "" : descriptionArea.getText().trim());
    }

    // ════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ════════════════════════════════════════════════════════════════
    @FXML private void allerDashboard(ActionEvent e)   { naviguer("/view/dashboard.fxml",   "Tableau de Bord",        1200, 750, e); }
    @FXML private void allerPatients(ActionEvent e)    { naviguer("/view/patient.fxml",     "Gestion des Patients",   1200, 750, e); }
    @FXML private void allerMaladies(ActionEvent e)    { naviguer("/view/maladie.fxml",     "Gestion des Maladies",   1200, 750, e); }
    @FXML private void allerRendezVous(ActionEvent e)  { naviguer("/view/rendezvous.fxml",  "Gestion des Rendez-vous",1200, 750, e); }

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
}