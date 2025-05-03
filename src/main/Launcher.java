package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/registro.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("../resources/styles/styles.css").toExternalForm());
            primaryStage.setTitle("CINES JRC");

            Image icon = new Image(getClass().getResourceAsStream("../resources/images/logo.png"));
            primaryStage.getIcons().add(icon);

            // Permitir redimensionamiento
            primaryStage.setResizable(false);

            // Establecer tamaño mínimo
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(625);

            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error al cargar el FXML.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
