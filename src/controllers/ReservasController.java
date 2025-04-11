package controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

import static controllers.LoginController.getUsuarioLogueadoEmail;

public class ReservasController {
    @FXML
    public Label espectaculoLabel;
    @FXML
    private Label usuarioLabel;

    // Parámetro email usuario logueado del LoginController
    private static String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    // Parámetro del espectáculo seleccionado
    private static String espectaculoSeleccionado =getEspectaculoSeleccionado();

    @FXML
    public void initialize() {
        // Mostrar el email del usuario logueado
        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
        }

        // Mostrar el título del espectáculo seleccionado si está disponible
        if (espectaculoSeleccionado != null) {
            espectaculoLabel.setText(espectaculoSeleccionado);
        }
    }

    public void setEspectaculoSeleccionado(String nombreEspectaculo) {
        espectaculoSeleccionado = nombreEspectaculo;
        if (espectaculoLabel != null) {
            espectaculoLabel.setText(nombreEspectaculo);
        }
    }

    public static String getEspectaculoSeleccionado() {
        return espectaculoSeleccionado;
    }

    //método para cerrar sesión y volver al login
    //bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login
    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado=null;

        cambioEscena("../views/login.fxml");
    }

    public void filtrarPorAsiento(ActionEvent actionEvent) {
    }

    public void mostrarTodas(ActionEvent actionEvent) {
    }

    //EFECTO FADE al cambiar de escena
    public void fadeInScene(Node rootNode) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    //CONFIG TAMAÑO LOGIN Y REGISTRO FIJOS
    public void configureStage(Stage stage) {
        stage.setMinWidth(750);
        stage.setMinHeight(550);
        stage.setMaxWidth(800);
        stage.setMaxHeight(700);
    }

    public void volverCartelera(ActionEvent actionEvent) {

        cambioEscena("../views/cartelera.fxml");

    }

    //método para cambiar de escena
    private void cambioEscena(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
            Parent root = loader.load();

            // Obtener el Stage actual, con utilizar cualquier atributo fxml o nodo sirve.
            Stage stage = (Stage) usuarioLabel.getScene().getWindow();

            // Crear una nueva escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("../Resources/styles.css").toExternalForm());
            stage.setTitle("CINES JRC");

            // Establecer el icono de la ventana
            Image icon = new Image(getClass().getResourceAsStream("../Resources/logo.png"));
            stage.getIcons().add(icon);

            // Cambiar la escena
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
