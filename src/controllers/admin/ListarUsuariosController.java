package controllers.admin;

import dao.ReservasDaoI;
import dao.UsuarioDaoI;
import dao.impl.ReservaDaoImpl;
import dao.impl.UsuarioDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Usuario;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class ListarUsuariosController {
    @FXML private VBox usuariosVBox;
    @FXML private ScrollPane scrollUsuarios;
    @FXML private ImageView editarBtn;
    @FXML private ImageView eliminarBtn;

    private List<Usuario> usuariosOriginal;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private UsuarioDaoI usuarioDao;
    private ReservasDaoI reservasDao;

    public void mostrarUsuarios(List<Usuario> usuarios) {
        usuariosVBox.getChildren().clear();
        checkBoxes.clear();
        usuariosOriginal = usuarios;

        // Encabezado de la tabla
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Checkbox para selección múltiple
        Label seleccionHeader = new Label(" V");
        Label dniHeader = new Label("DNI");
        Label nombreHeader = new Label("Nombre");
        Label emailHeader = new Label("Email");
        Label accionesHeader = new Label("Acciones");

        // Ajustar ancho de columnas
        seleccionHeader.setPrefWidth(80);
        dniHeader.setPrefWidth(120);
        nombreHeader.setPrefWidth(200);
        emailHeader.setPrefWidth(250);
        accionesHeader.setPrefWidth(100);

        // Estilo del encabezado
        String headerStyle = "-fx-text-fill: #6c757d; -fx-font-weight: bold;";
        seleccionHeader.setStyle(headerStyle);
        dniHeader.setStyle(headerStyle);
        nombreHeader.setStyle(headerStyle);
        emailHeader.setStyle(headerStyle);
        accionesHeader.setStyle(headerStyle);

        header.getChildren().addAll(seleccionHeader, dniHeader, nombreHeader, emailHeader, accionesHeader);
        usuariosVBox.getChildren().add(header);

        // Filas de datos
        for (Usuario usuario : usuarios) {
            HBox row = new HBox();
            row.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            // Checkbox para selección
            CheckBox checkBox = new CheckBox();
            checkBox.setPrefWidth(80);
            checkBoxes.add(checkBox);

            Label dni = new Label(usuario.getDni());
            Label nombre = new Label(usuario.getNombre());
            Label email = new Label(usuario.getEmail());

            // Botones de acción
            ImageView editarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/editar.png")));
            ImageView eliminarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/eliminar.png")));

            // Configurar botones según si es admin o no
            if ("admin@admin.com".equals(usuario.getEmail())) {
                // Deshabilitar acciones para admin
                checkBox.setDisable(true);
                checkBox.setOpacity(0.5);

                editarIcon.setImage(new Image(getClass().getResourceAsStream("/resources/images/editar.png")));
                editarIcon.setDisable(true);
                editarIcon.setOpacity(0.5);

                eliminarIcon.setImage(new Image(getClass().getResourceAsStream("/resources/images/eliminar.png")));
                eliminarIcon.setDisable(true);
                eliminarIcon.setOpacity(0.5);

                editarIcon.setFitHeight(16);
                editarIcon.setFitWidth(16);
                eliminarIcon.setFitHeight(16);
                eliminarIcon.setFitWidth(16);

            } else {
                // Habilitar acciones para usuarios normales
                editarIcon.setFitHeight(16);
                editarIcon.setFitWidth(16);
                editarIcon.setStyle("-fx-cursor: hand;");
                editarIcon.setOnMouseClicked(e -> editarUsuario(usuario));

                eliminarIcon.setFitHeight(16);
                eliminarIcon.setFitWidth(16);
                eliminarIcon.setStyle("-fx-cursor: hand;");
                eliminarIcon.setOnMouseClicked(e -> eliminarUsuario(usuario));
            }

            HBox accionesBox = new HBox(5, editarIcon, eliminarIcon);
            accionesBox.setPrefWidth(100);

            dni.setPrefWidth(120);
            nombre.setPrefWidth(200);
            email.setPrefWidth(250);
            accionesBox.setPrefWidth(100);

            String cellStyle = "-fx-text-fill: #495057;";
            dni.setStyle(cellStyle);
            nombre.setStyle(cellStyle);
            email.setStyle(cellStyle);

            row.getChildren().addAll(checkBox, dni, nombre, email, accionesBox);
            usuariosVBox.getChildren().add(row);
        }

        // Configurar botones de acciones globales
        editarBtn.setOnMouseClicked(e -> editarSeleccionados());
        eliminarBtn.setOnMouseClicked(e -> eliminarSeleccionados());
    }

    //editar individualmente
    private void editarUsuario(Usuario usuario) {
        if ("admin@admin.com".equals(usuario.getEmail())) {
            System.out.println("No se puede editar el usuario administrador");
            return;
        }
        System.out.println("Editar usuario: " + usuario.getDni());

    }

    //eliminar individualmente
    private void eliminarUsuario(Usuario usuario) {
        List<Usuario> usuarios = new ArrayList<>();
        usuarios.add(usuario);

        if ("admin@admin.com".equals(usuario.getEmail())) {
            System.out.println("No se puede eliminar el usuario administrador");
            return;
        }
        System.out.println("Eliminar usuario: " + usuario.getDni());

        eliminarUsuarioYActualizarVista(usuarios);

    }

    private void editarSeleccionados() {
        List<Usuario> seleccionados = getUsuariosSeleccionados();

        if (seleccionados.isEmpty()) {
            System.out.println("Seleccione un usuario para editar");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR SELECCIÓN");
            alert.setContentText("Seleccione, al menos, un usuario.");
            alert.show();
            return;
        }
        if (seleccionados.size() > 1) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error. Límite alcanzado");
            alert.setContentText("Seleccione, como máximo, un usuario a la vez.");
            return;
        }
        if ("admin@admin.com".equals(seleccionados.get(0).getEmail())) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error. Permiso denegado.");
            alert.setContentText("El usuario administrador no puede editarse.");
            return;
        }
        editarUsuario(seleccionados.get(0));
    }

    private void eliminarSeleccionados() {
        List<Usuario> seleccionados = getUsuariosSeleccionados();
        // Filtrar para no incluir al admin
        seleccionados.removeIf(u -> "admin@admin.com".equals(u.getEmail()));

        if (seleccionados.isEmpty()) {
            System.out.println("Seleccione al menos un usuario para eliminar (excepto administrador)");
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("ERROR SELECCIÓN");
            alert.setContentText("Seleccione, al menos, un usuario.");
            alert.show();

            return;
        }
        System.out.println("Eliminar usuarios seleccionados: " + seleccionados.size());

        eliminarUsuarioYActualizarVista(seleccionados);
        // IMPLEMENTAR ELIMINAR SELECCIONADOS
    }

    //MÉTODO PARA ELIMINAR USUARIO INDIVIDUAL O LISTA DE USUARIOS
    private void eliminarUsuarioYActualizarVista(List<Usuario> seleccionados) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
            this.reservasDao = new ReservaDaoImpl(conn);

            for (Usuario usuario : seleccionados) {
                System.out.println(usuario.getDni()); //LOGS

                // Eliminar reservas
                int reservasEliminadas = reservasDao.eliminarReservasByUsuario(usuario.getDni());
                System.out.println("Reservas eliminadas: " + reservasEliminadas);

                // Eliminar reservas temporales
                int reservasTempEliminadas = reservasDao.eliminarReservasTemporalesByUsuario(usuario.getDni());
                System.out.println("Reservas temporales eliminadas: " + reservasTempEliminadas);

                // Eliminar el usuario
                int eliminados = usuarioDao.eliminarUsuarioByID(usuario.getDni());
                if (eliminados > 0) {
                    System.out.println("SE BORRARON LOS USUARIOS");
                    usuariosOriginal.remove(usuario);
                    mostrarUsuarios(usuariosOriginal);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Confirmado");
                    alert.setContentText("Se borró el usuario: " + usuario.getDni() +
                            "\nReservas eliminadas: " + (reservasEliminadas + reservasTempEliminadas));
                    alert.show();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Ocurrió un error al eliminar el usuario o sus reservas.");
            alert.show();
        }
    }

    private List<Usuario> getUsuariosSeleccionados() {
        List<Usuario> seleccionados = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected() && !"admin@admin.com".equals(usuariosOriginal.get(i).getEmail())) {
                seleccionados.add(usuariosOriginal.get(i));
            }
        }
        return seleccionados;
    }
}