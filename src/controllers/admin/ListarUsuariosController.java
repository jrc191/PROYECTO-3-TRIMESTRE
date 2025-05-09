package controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Usuario;
import java.util.List;

public class ListarUsuariosController {
    @FXML
    private VBox usuariosVBox;
    @FXML
    private ScrollPane scrollUsuarios;
    @FXML
    private Label flechaArriba; //Flechas movimiento del scroll
    @FXML
    private Label flechaAbajo;  //Flechas movimiento del scroll

    private List<Usuario> usuariosOriginal;
    private boolean scrollConfigDone = false;

    public void mostrarUsuarios(List<Usuario> usuarios) {
        usuariosVBox.getChildren().clear();
        // TABLA USUARIOS (estática). Quizás a futuro hacerla dinámica?. AÑADIR EDITAR Y BORRAR USERS
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #232a4d; -fx-padding: 8px;");
        Label dniHeader = new Label("DNI");
        Label nombreHeader = new Label("Nombre");
        Label emailHeader = new Label("Email");
        dniHeader.setPrefWidth(150);
        nombreHeader.setPrefWidth(250);
        emailHeader.setPrefWidth(250);
        dniHeader.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
        nombreHeader.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
        emailHeader.setStyle("-fx-text-fill: #FFC107; -fx-font-weight: bold;");
        header.getChildren().addAll(dniHeader, nombreHeader, emailHeader);
        usuariosVBox.getChildren().add(header);
        // Filas
        for (Usuario usuario : usuarios) {
            HBox row = new HBox();
            row.setStyle("-fx-background-color: transparent; -fx-padding: 6px;");
            Label dni = new Label(usuario.getDni());
            Label nombre = new Label(usuario.getNombre());
            Label email = new Label(usuario.getEmail());
            dni.setPrefWidth(150);
            nombre.setPrefWidth(250);
            email.setPrefWidth(250);
            dni.setStyle("-fx-text-fill: white;");
            nombre.setStyle("-fx-text-fill: white;");
            email.setStyle("-fx-text-fill: white;");
            row.getChildren().addAll(dni, nombre, email);
            usuariosVBox.getChildren().add(row);
        }

        // Configuración de scroll
        if (!scrollConfigDone && scrollUsuarios != null && flechaArriba != null && flechaAbajo != null) {
            utils.Transitions.configurarListenersScroll(scrollUsuarios, flechaArriba, flechaAbajo);
            scrollUsuarios.setPannable(true); //Habilitamos el arrastre del ratón
            scrollConfigDone = true; //configuración del scroll hecha.
        }
    }



}
