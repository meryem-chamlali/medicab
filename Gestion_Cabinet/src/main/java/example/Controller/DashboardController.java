package example.Controller;

import example.DAO.DashboardDAO;
import example.Model.Utilisateur;
import example.Util.NavigationManager;
import example.Util.Refreshable;
import example.Util.SessionManager;
import example.Util.UiAsync;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DashboardController implements Initializable, Refreshable {

    public static class StatsData {
        public final int totalP, pCeMois, totalR, rAVenir, m;
        public final List<DashboardDAO.RdvJour> rdvs;
        public StatsData(int totalP, int pCeMois, int totalR, int rAVenir, int m, List<DashboardDAO.RdvJour> rdvs) {
            this.totalP = totalP; this.pCeMois = pCeMois;
            this.totalR = totalR; this.rAVenir = rAVenir;
            this.m = m; this.rdvs = rdvs;
        }
    }

    @FXML private Label initialesLabel;
    @FXML private Label nomUtilisateurLabel;
    @FXML private Label roleLabel;
    @FXML private Label totalPatientsLabel;
    @FXML private Label totalMaladiesLabel;
    @FXML private Label totalRdvLabel;

    @FXML private TableView<DashboardDAO.RdvJour>           rdvTable;
    @FXML private TableColumn<DashboardDAO.RdvJour, String> colHeure;
    @FXML private TableColumn<DashboardDAO.RdvJour, String> colPatient;
    @FXML private TableColumn<DashboardDAO.RdvJour, String> colMaladie;  // ← nouveau

    private final DashboardDAO dashboardDAO = new DashboardDAO();
    private final ObservableList<DashboardDAO.RdvJour> rdvData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurerTableau();
        if (totalPatientsLabel != null) totalPatientsLabel.setText("…");
        if (totalMaladiesLabel != null) totalMaladiesLabel.setText("…");
    }

    @Override
    public void onShow() {
        afficherInfosUtilisateur();
        chargerStatistiquesAsync();
    }

    private void configurerTableau() {
        colHeure.setCellValueFactory(new PropertyValueFactory<>("heure"));
        colPatient.setCellValueFactory(new PropertyValueFactory<>("nomPatient"));
        colMaladie.setCellValueFactory(new PropertyValueFactory<>("maladie")); // ← nouveau
        rdvTable.setItems(rdvData);
        rdvTable.setPlaceholder(new Label("Aucun rendez-vous aujourd'hui."));
    }

    private void afficherInfosUtilisateur() {
        Utilisateur u = SessionManager.getUtilisateur();
        if (u != null) {
            if (initialesLabel != null)      initialesLabel.setText(u.getInitiales());
            if (nomUtilisateurLabel != null)  nomUtilisateurLabel.setText(u.getNomComplet());
            if (roleLabel != null)
                roleLabel.setText("Admin".equalsIgnoreCase(u.getRole()) ? "Administrateur" : "Secrétaire");
        }
    }

    private void chargerStatistiquesAsync() {
        UiAsync.run(() -> {
            int totalP   = dashboardDAO.compterPatients();
            int pCeMois  = dashboardDAO.compterPatientsCeMois();
            int totalR   = dashboardDAO.compterRendezVousAujourdhui();
            int rAVenir  = dashboardDAO.compterRendezVousAujourdhuiAVenir();
            int m        = dashboardDAO.compterMaladies();
            List<DashboardDAO.RdvJour> rdvs = dashboardDAO.getRdvAujourdhui();
            return new StatsData(totalP, pCeMois, totalR, rAVenir, m, rdvs);
        }, data -> {
            if (totalPatientsLabel != null)
                totalPatientsLabel.setText(data.totalP + " (+" + data.pCeMois + " ce mois)");
            if (totalMaladiesLabel != null)
                totalMaladiesLabel.setText(String.valueOf(data.m));
            if (totalRdvLabel != null)
                totalRdvLabel.setText(data.totalR + " (" + data.rAVenir + " à venir)");
            rdvData.setAll(data.rdvs);
            rdvTable.refresh();
        }, e -> {
            if (totalPatientsLabel != null) totalPatientsLabel.setText("--");
            if (totalMaladiesLabel != null) totalMaladiesLabel.setText("--");
            if (totalRdvLabel != null)      totalRdvLabel.setText("--");
        });
    }

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
}