package example;

import example.Util.DBConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            URL loginUrl = getClass().getResource("/view/login.fxml");
            if (loginUrl == null) {
                throw new IllegalStateException("Ressource introuvable: /view/login.fxml. Verifiez resources/view.");
            }
            Parent root = FXMLLoader.load(loginUrl);
            // Taille fixe pour le login
            Scene scene = new Scene(root, 480, 560);
            
            if (getClass().getResource("/style.css") != null) {
                scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
            }

            primaryStage.setTitle("MediCab — Connexion");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        DBConnection.closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}