package utils;

import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.effect.ColorAdjust;
import java.io.IOException;

public class Transitions {

    // Efecto de fade-in para cualquier nodo
    public static void fadeInScene(Node rootNode) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    // Cambio de escena genérico
    public static void cambioEscena(Stage stage, String fxmlPath, String stylesheetPath, String windowTitle, String iconPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(Transitions.class.getResource(fxmlPath));
            if (controller != null) {
                loader.setController(controller);
            }
            Parent root = loader.load();
            Scene scene = new Scene(root);
            if (stylesheetPath != null) {
                scene.getStylesheets().add(Transitions.class.getResource(stylesheetPath).toExternalForm());
            }
            stage.setTitle(windowTitle);
            if (iconPath != null) {
                Image icon = new Image(Transitions.class.getResourceAsStream(iconPath));
                stage.getIcons().add(icon);
            }
            fadeInScene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Configura listeners de scroll y flechas para cualquier ScrollPane y dos Labels
    public static void configurarListenersScroll(ScrollPane scrollPane, Label arribaBtn, Label abajoBtn) {
        scrollPane.setOnMouseEntered(e -> {
            arribaBtn.setOpacity(0);
            abajoBtn.setOpacity(0);
        });
        scrollPane.setOnMouseExited(e -> {
            arribaBtn.setOpacity(1);
            abajoBtn.setOpacity(1);
        });
        arribaBtn.setOnMouseClicked(e ->
                scrollPane.setVvalue(scrollPane.getVvalue() - 0.2));
        abajoBtn.setOnMouseClicked(e ->
                scrollPane.setVvalue(scrollPane.getVvalue() + 0.2));
        arribaBtn.setOnKeyPressed(e ->
                scrollPane.setVvalue(scrollPane.getVvalue() - 0.2));
        arribaBtn.setOpacity(0);
        abajoBtn.setOpacity(0);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    // Oscurece cualquier VBox (entrada)
    public static void oscurecerEntrada(VBox entradaCesta) {
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.5);  // Hace que la imagen sea más oscura
        entradaCesta.setEffect(colorAdjust);
    }
}
