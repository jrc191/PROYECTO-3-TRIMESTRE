package controllers.admin;

import dao.ReservasDaoI;
import dao.UsuarioDaoI;
import dao.impl.ReservaDaoImpl;
import dao.impl.UsuarioDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
    @FXML private ImageView addBtn;
    @FXML private VBox usuariosVBox;
    @FXML private ScrollPane scrollUsuarios;
    @FXML private ImageView editarBtn;
    @FXML private ImageView eliminarBtn;
    @FXML private Button guardarBtn;
    @FXML private Button cancelarBtn;

    private List<Usuario> usuariosOriginal;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private UsuarioDaoI usuarioDao;
    private ReservasDaoI reservasDao;
    private HBox nuevaFila;
    private Usuario usuarioEditando; // Variable para almacenar el usuario que se está editando
    private boolean modoEdicion = false; // Flag para saber si estamos en modo edición

    @FXML
    public void initialize() {
        addBtn.setOnMouseClicked(e -> agregarNuevoUsuario());
        guardarBtn.setOnAction(e -> guardarNuevoUsuario());
        cancelarBtn.setOnAction(e->cancelarNuevoUsuario());
    }

    private void agregarNuevoUsuario() {
        // Ocultar botón de añadir y mostrar botón de guardar
        addBtn.setVisible(false);
        guardarBtn.setVisible(true);
        cancelarBtn.setVisible(true);

        // Deshabilitar otros botones de acción
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);

        // Crear campos de texto para la nueva fila
        TextField dniField = new TextField();
        TextField nombreField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        // Configurar los campos
        dniField.setPromptText("DNI");
        nombreField.setPromptText("Nombre");
        emailField.setPromptText("Email");
        passwordField.setPromptText("Contraseña");

        // Establecer anchos
        dniField.setPrefWidth(120);
        nombreField.setPrefWidth(200);
        emailField.setPrefWidth(250);
        passwordField.setPrefWidth(150);

        // Crear la nueva fila
        nuevaFila = new HBox();
        nuevaFila.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Añadir los campos a la fila
        nuevaFila.getChildren().addAll(
                new Label("Nuevo:"), // Espacio para el checkbox (no aplicable)
                dniField,
                nombreField,
                emailField,
                passwordField
        );

        // Insertar la nueva fila al principio de la lista
        usuariosVBox.getChildren().add(1, nuevaFila);

        // Desplazar la vista para mostrar la nueva fila
        scrollUsuarios.setVvalue(0);
    }

    private void guardarNuevoUsuario() {
        // Obtener los campos de la nueva fila
        HBox fila = nuevaFila;

        String dni;

        if (modoEdicion){
            dni =((Label)fila.getChildren().get(1)).getText();
        }
        else{
            dni =((TextField)fila.getChildren().get(1)).getText();
        }

        TextField nombreField = (TextField) fila.getChildren().get(2);
        TextField emailField = (TextField) fila.getChildren().get(3);
        PasswordField passwordField = (PasswordField) fila.getChildren().get(4);

        // Validar campos
        if ((!modoEdicion && dni.isEmpty()) || nombreField.getText().isEmpty() || emailField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("DNI, Nombre y Email son obligatorios");
            alert.show();
            return;
        }

        // Validar formato de email
        if (!emailField.getText().contains("@")) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("El email debe ser válido");
            alert.show();
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);

            if (modoEdicion) {
                // Modo edición - Actualizar usuario existente
                usuarioEditando.setNombre(nombreField.getText());
                usuarioEditando.setEmail(emailField.getText());

                // Solo actualizar contraseña si se proporcionó una nueva
                if (!passwordField.getText().isEmpty()) {
                    usuarioEditando.setPassword(passwordField.getText());
                }

                boolean exito = usuarioDao.actualizarUsuario(usuarioEditando);

                if (exito) {
                    // Actualizar la lista de usuarios
                    List<Usuario> usuariosActualizados = usuarioDao.listUsuariosAdmin();
                    mostrarUsuarios(usuariosActualizados);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Éxito");
                    alert.setContentText("Usuario actualizado correctamente");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("No se pudo actualizar el usuario");
                    alert.show();
                }
            } else {
                // Modo añadir - Crear nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setDni(dni);
                nuevoUsuario.setNombre(nombreField.getText());
                nuevoUsuario.setEmail(emailField.getText());
                nuevoUsuario.setPassword(passwordField.getText());

                boolean exito = usuarioDao.registrarUsuario(nuevoUsuario);

                if (exito) {
                    // Actualizar la lista de usuarios
                    List<Usuario> usuariosActualizados = usuarioDao.listUsuariosAdmin();
                    mostrarUsuarios(usuariosActualizados);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Éxito");
                    alert.setContentText("Usuario añadido correctamente");
                    alert.show();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("No se pudo añadir el usuario");
                    alert.show();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("Error al conectar con la base de datos: " + e.getMessage());
            alert.show();
        }

        // Restaurar estado
        resetearModoEdicion();
    }

    private void cancelarNuevoUsuario() {
        // Eliminar la fila de edición si existe
        if (nuevaFila != null && usuariosVBox.getChildren().contains(nuevaFila)) {
            usuariosVBox.getChildren().remove(nuevaFila);
        }

        // Restaurar estado
        resetearModoEdicion();
    }

    private void resetearModoEdicion() {
        // Restaurar botones
        addBtn.setVisible(true);
        guardarBtn.setVisible(false);
        cancelarBtn.setVisible(false);
        editarBtn.setDisable(false);
        eliminarBtn.setDisable(false);

        // Resetear variables de edición
        modoEdicion = false;
        usuarioEditando = null;
        nuevaFila = null;
    }

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
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No se puede editar el usuario administrador");
            alert.show();
            return;
        }

        modoEdicion = true;
        usuarioEditando = usuario;

        // Ocultar botones no necesarios
        addBtn.setVisible(false);
        guardarBtn.setVisible(true);
        cancelarBtn.setVisible(true);
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);

        // Campo DNI como texto no editable (Label o TextField deshabilitado)
        Label dniLabel = new Label(usuario.getDni());
        dniLabel.setPrefWidth(120);

        TextField nombreField = new TextField(usuario.getNombre());
        TextField emailField = new TextField(usuario.getEmail());
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nueva contraseña (dejar vacío para no cambiar)");

        // Configurar los campos
        nombreField.setPromptText("Nombre");
        emailField.setPromptText("Email");

        // Establecer anchos
        nombreField.setPrefWidth(200);
        emailField.setPrefWidth(250);
        passwordField.setPrefWidth(150);

        // Crear la fila de edición
        nuevaFila = new HBox();
        nuevaFila.setStyle("-fx-background-color: #fffde7; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        // Añadir los campos a la fila (DNI como Label ahora)
        nuevaFila.getChildren().addAll(
                new Label("Editando:"),
                dniLabel,  // Cambiado de TextField a Label
                nombreField,
                emailField,
                passwordField
        );

        // Insertar la fila de edición al principio
        usuariosVBox.getChildren().add(1, nuevaFila);

        // Desplazar la vista para mostrar la fila de edición
        scrollUsuarios.setVvalue(0);
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
                int reservasEliminadas = reservasDao.cancelarReservasByUsuarioID(usuario.getDni());
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