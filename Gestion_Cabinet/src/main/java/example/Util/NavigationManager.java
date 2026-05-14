package example.Util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class NavigationManager {

    private static final Map<String, CachedView> VIEW_CACHE = new HashMap<>();

    private NavigationManager() {}

    private static final class CachedView {
        final Parent root;
        final Object controller;

        CachedView(Parent root, Object controller) {
            this.root = root;
            this.controller = controller;
        }
    }

    public static void clearCache() {
        VIEW_CACHE.clear();
    }

    public static void navigate(Stage stage, String fxmlPath, String title, double width, double height) throws IOException {
        CachedView cached = VIEW_CACHE.get(fxmlPath);
        if (cached == null) {
            URL url = NavigationManager.class.getResource(fxmlPath);
            if (url == null) {
                throw new IOException("FXML introuvable : " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            cached = new CachedView(root, loader.getController());
            VIEW_CACHE.put(fxmlPath, cached);
        }

        Scene scene = stage.getScene();
        if (scene == null) {
            scene = new Scene(cached.root, width, height);
            URL css = NavigationManager.class.getResource("/style.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            stage.setScene(scene);
        } else {
            scene.setRoot(cached.root);
            stage.setWidth(width);
            stage.setHeight(height);
        }

        if (cached.controller instanceof Refreshable r) {
            r.onShow();
        }

        stage.setTitle("MediCab — " + title);
        stage.centerOnScreen();
    }
}
