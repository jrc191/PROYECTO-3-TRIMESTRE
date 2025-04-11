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
            //inicio del programa en registro. cambiar a login.fxml si queremos que abra desde login.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/registro.fxml"));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("../Resources/styles.css").toExternalForm());
            primaryStage.setTitle("CINES JRC");
            
            // Establecer el icono de la ventana
            Image icon = new Image(getClass().getResourceAsStream("../Resources/logo.png"));  
            primaryStage.getIcons().add(icon);  

            primaryStage.setResizable(false);

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
