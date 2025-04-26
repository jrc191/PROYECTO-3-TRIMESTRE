package utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;

import java.io.IOException;

public class CerrarSesionUtil {
    /**
     * Cierra la sesión y redirige a la vista de registro/login.
     */
    public static void cerrarSesion(Stage stage, String stylesheetPath, String iconPath) {
        try {
            FXMLLoader loader = new FXMLLoader(CerrarSesionUtil.class.getResource("/views/registro.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            if (stylesheetPath != null) {
                scene.getStylesheets().add(CerrarSesionUtil.class.getResource(stylesheetPath).toExternalForm());
            }
            stage.setTitle("CINES JRC");
            if (iconPath != null) {
                Image icon = new Image(CerrarSesionUtil.class.getResourceAsStream(iconPath));
                stage.getIcons().add(icon);
            }
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
