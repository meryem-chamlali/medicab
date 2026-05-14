package example.Controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import example.DAO.LoginDAO; // Nom mis à jour
import example.Model.Utilisateur;
import example.Util.NavigationManager;
import example.Util.SessionManager;
import javafx.concurrent.Task;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Label errorLabel;

    // Utilisation du nouveau LoginDAO
    private final LoginDAO loginDAO = new LoginDAO();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        // Permet de valider en appuyant sur "Entrée" directement dans le champ mot de passe
        passwordField.setOnAction(e -> handleLogin());
        usernameField.setOnAction(e -> handleLogin());
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            afficherErreur("Champs vides !");
            return;
        }

        loginButton.setDisable(true);
        errorLabel.setVisible(false);

        Task<Utilisateur> task = new Task<>() {
            @Override
            protected Utilisateur call() throws SQLException {
                return loginDAO.authentifier(username, password);
            }
        };

        task.setOnSucceeded(ev -> {
            loginButton.setDisable(false);
            Utilisateur utilisateur = task.getValue();
            if (utilisateur != null) {
                SessionManager.setUtilisateur(utilisateur);
                ouvrirPage("/view/dashboard.fxml", "Accueil");
            } else {
                afficherErreur("Identifiants incorrects.");
            }
        });

        task.setOnFailed(ev -> {
            loginButton.setDisable(false);
            afficherErreur("Erreur de connexion DB.");
        });

        new Thread(task, "login-task").start();
    }
    /**
     * Méthode générique pour changer de page proprement et appliquer le style
     */
    private void ouvrirPage(String fxmlPath, String titre) {
        try {
            Stage stage = (Stage) loginButton.getScene().getWindow();
            NavigationManager.navigate(stage, fxmlPath, titre, 1200, 750);
            stage.setResizable(true);
            stage.centerOnScreen();
            
        } catch (IOException e) {
            afficherErreur("Erreur de chargement de l'interface : " + fxmlPath);
            System.err.println(" Erreur FXML : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void afficherErreur(String message) {
        errorLabel.setText(" " + message);
        errorLabel.setVisible(true);
    }
}