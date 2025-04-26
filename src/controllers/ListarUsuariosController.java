package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Usuario;
import java.util.List;

public class ListarUsuariosController {
    @FXML
    private VBox usuariosVBox;

    private List<Usuario> usuariosOriginal;
    public void mostrarUsuarios(List<Usuario> usuarios) {
        usuariosVBox.getChildren().clear();
        // Cabecera tipo tabla (estática, sin interacción)
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #232a4d; -fx-padding: 8px;");
        Label dniHeader = new Label("DNI");
        Label nombreHeader = new Label("Nombre");
        Label emailHeader = new Label("Email");
        dniHeader.setPrefWidth(150);
        nombreHeader.setPrefWidth(200);
        emailHeader.setPrefWidth(250);
        dniHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        nombreHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        emailHeader.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
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
            nombre.setPrefWidth(200);
            email.setPrefWidth(250);
            dni.setStyle("-fx-text-fill: white;");
            nombre.setStyle("-fx-text-fill: white;");
            email.setStyle("-fx-text-fill: white;");
            row.getChildren().addAll(dni, nombre, email);
            usuariosVBox.getChildren().add(row);
        }
    }

}
