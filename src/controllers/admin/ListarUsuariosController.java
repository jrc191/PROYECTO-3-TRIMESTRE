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

/**
 * Controlador para la gestión de usuarios en el panel de administración.
 */
public class ListarUsuariosController {
    // Elementos de la interfaz
    @FXML private ImageView addBtn;
    @FXML private VBox usuariosVBox;
    @FXML private ScrollPane scrollUsuarios;
    @FXML private ImageView editarBtn;
    @FXML private ImageView eliminarBtn;
    @FXML private Button guardarBtn;
    @FXML private Button cancelarBtn;

    // Datos y estado
    private List<Usuario> usuariosOriginal;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private UsuarioDaoI usuarioDao;
    private ReservasDaoI reservasDao;
    private HBox nuevaFila;
    private Usuario usuarioEditando;
    private boolean modoEdicion = false;


    // Inicialización del controlador con la interfaz, configuración de eventos y carga de datos
    @FXML
    public void initialize() {
        configurarEventosBotones();

        try {
            Connection conn = DatabaseConnection.getConnection();
            usuarioDao = new UsuarioDaoImpl(conn);
            reservasDao = new ReservaDaoImpl(conn);
            cargarUsuarios();
        } catch (SQLException e) {
            manejarErrorInicializacion(e);
        }
    }

    private void configurarEventosBotones() {
        addBtn.setOnMouseClicked(e -> agregarNuevoUsuario());
        guardarBtn.setOnAction(e -> guardarNuevoUsuario());
        cancelarBtn.setOnAction(e -> cancelarNuevoUsuario());
        editarBtn.setOnMouseClicked(e -> editarSeleccionados());
        eliminarBtn.setOnMouseClicked(e -> eliminarSeleccionados());
        deshabilitarBotonesAccion();
    }

    // Método para cargar los usuarios desde la base de datos (se actualiza la vista)
    private void cargarUsuarios() {
        try {
            limpiarInterfaz();
            usuariosOriginal = usuarioDao.listUsuariosAdmin();
            crearCabeceraTabla();
            crearFilasUsuarios();
        } catch (SQLException e) {
            manejarErrorCargaUsuarios(e);
        }
    }


    private void limpiarInterfaz() {
        usuariosVBox.getChildren().clear();
        checkBoxes.clear();
    }

    private void crearCabeceraTabla() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Label seleccionHeader = crearLabelCabecera(" V", 80);
        Label dniHeader = crearLabelCabecera("DNI", 120);
        Label nombreHeader = crearLabelCabecera("Nombre", 200);
        Label emailHeader = crearLabelCabecera("Email", 250);
        Label accionesHeader = crearLabelCabecera("Acciones", 100);

        header.getChildren().addAll(seleccionHeader, dniHeader, nombreHeader, emailHeader, accionesHeader);
        usuariosVBox.getChildren().add(header);
    }

    private Label crearLabelCabecera(String texto, double ancho) {
        Label label = new Label(texto);
        label.setPrefWidth(ancho);
        label.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
        return label;
    }

    // Método para crear las filas de usuarios en la tabla
    private void crearFilasUsuarios() {
        for (Usuario usuario : usuariosOriginal) {
            HBox row = crearFilaUsuario(usuario);
            usuariosVBox.getChildren().add(row);
        }
    }

    // Método para crear una fila de usuario
    private HBox crearFilaUsuario(Usuario usuario) {
        HBox row = new HBox();
        row.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        CheckBox checkBox = new CheckBox();
        checkBox.setPrefWidth(80);
        checkBoxes.add(checkBox);

        Label dni = crearLabelDato(usuario.getDni(), 120);
        Label nombre = crearLabelDato(usuario.getNombre(), 200);
        Label email = crearLabelDato(usuario.getEmail(), 250);

        ImageView editarIcon = crearIconoAccion("/resources/images/editar.png", "Editar usuario");
        ImageView eliminarIcon = crearIconoAccion("/resources/images/eliminar.png", "Eliminar usuario");
        configurarIconosUsuario(editarIcon, eliminarIcon, usuario);

        HBox accionesBox = new HBox(5, editarIcon, eliminarIcon);
        accionesBox.setPrefWidth(100);

        row.getChildren().addAll(checkBox, dni, nombre, email, accionesBox);
        return row;
    }

    private Label crearLabelDato(String texto, double ancho) {
        Label label = new Label(texto);
        label.setPrefWidth(ancho);
        label.setStyle("-fx-text-fill: #495057;");
        return label;
    }

    private ImageView crearIconoAccion(String recurso, String tooltip) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(recurso)));
        icon.setFitHeight(16);
        icon.setFitWidth(16);
        Tooltip.install(icon, new Tooltip(tooltip));
        return icon;
    }

    private void configurarIconosUsuario(ImageView editarIcon, ImageView eliminarIcon, Usuario usuario) {
        if ("admin@admin.com".equals(usuario.getEmail())) {
            deshabilitarIcono(editarIcon);
            deshabilitarIcono(eliminarIcon);
        } else {
            configurarIconoEditar(editarIcon, usuario);
            configurarIconoEliminar(eliminarIcon, usuario);
        }
    }

    private void configurarIconoEditar(ImageView icon, Usuario usuario) {
        icon.setStyle("-fx-cursor: hand;");
        icon.setOnMouseClicked(e -> editarUsuario(usuario));
    }

    private void configurarIconoEliminar(ImageView icon, Usuario usuario) {
        icon.setStyle("-fx-cursor: hand;");
        icon.setOnMouseClicked(e -> eliminarUsuario(usuario));
    }

    private void deshabilitarIcono(ImageView icon) {
        icon.setDisable(true);
        icon.setOpacity(0.5);
    }

    private void deshabilitarBotonesAccion() {
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);
        editarBtn.setOpacity(0.5);
        eliminarBtn.setOpacity(0.5);
    }

    private void agregarNuevoUsuario() {
        prepararInterfazParaNuevoUsuario();
        crearFormularioNuevoUsuario();
    }

    private void prepararInterfazParaNuevoUsuario() {
        addBtn.setVisible(false);
        guardarBtn.setVisible(true);
        cancelarBtn.setVisible(true);
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);
    }

    // Método para crear el formulario de nuevo usuario en una fila
    private void crearFormularioNuevoUsuario() {
        TextField dniField = new TextField();
        TextField nombreField = new TextField();
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();

        configurarCamposFormulario(dniField, nombreField, emailField, passwordField);

        nuevaFila = new HBox();
        nuevaFila.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        nuevaFila.getChildren().addAll(
                new Label("Nuevo:"),
                dniField,
                nombreField,
                emailField,
                passwordField
        );

        usuariosVBox.getChildren().add(1, nuevaFila);
        scrollUsuarios.setVvalue(0);
    }

    private void configurarCamposFormulario(TextField dniField, TextField nombreField, TextField emailField, PasswordField passwordField) {
        dniField.setPromptText("DNI");
        nombreField.setPromptText("Nombre");
        emailField.setPromptText("Email");
        passwordField.setPromptText("Contraseña");

        dniField.setPrefWidth(120);
        nombreField.setPrefWidth(200);
        emailField.setPrefWidth(250);
        passwordField.setPrefWidth(150);
    }

    // Método para guardar el nuevo usuario o actualizar el existente. Con validación de datos
    private void guardarNuevoUsuario() {
        if (!validarDatosUsuario()) {
            return;
        }

        try {
            if (modoEdicion) {
                actualizarUsuarioExistente();
            } else {
                crearNuevoUsuario();
            }
        } catch (SQLException e) {
            manejarErrorGuardado(e);
        }
    }

    // Método para validar los datos del usuario
    private boolean validarDatosUsuario() {
        HBox fila = nuevaFila;
        String dni = modoEdicion ? ((Label)fila.getChildren().get(1)).getText() : ((TextField)fila.getChildren().get(1)).getText();
        TextField nombreField = (TextField) fila.getChildren().get(2);
        TextField emailField = (TextField) fila.getChildren().get(3);
        PasswordField passwordField = (PasswordField) fila.getChildren().get(4);

        if ((!modoEdicion && dni.isEmpty()) || nombreField.getText().isEmpty() || emailField.getText().isEmpty()) {
            mostrarError("Error", "DNI, Nombre y Email son obligatorios");
            return false;
        }

        if (!emailField.getText().contains("@")) {
            mostrarError("Error", "El email debe ser válido");
            return false;
        }

        return true;
    }

    // Método para actualizar el usuario existente o crear uno nuevo
    private void actualizarUsuarioExistente() throws SQLException {
        HBox fila = nuevaFila;
        TextField nombreField = (TextField) fila.getChildren().get(2);
        TextField emailField = (TextField) fila.getChildren().get(3);
        PasswordField passwordField = (PasswordField) fila.getChildren().get(4);

        usuarioEditando.setNombre(nombreField.getText());
        usuarioEditando.setEmail(emailField.getText());

        if (!passwordField.getText().isEmpty()) {
            usuarioEditando.setPassword(passwordField.getText());
        }

        boolean exito = usuarioDao.actualizarUsuario(usuarioEditando);

        if (exito) {
            mostrarMensaje("Éxito", "Usuario actualizado correctamente");
            cargarUsuarios();
        } else {
            mostrarError("Error", "No se pudo actualizar el usuario");
        }
    }

    // Método para crear un nuevo usuario
    private void crearNuevoUsuario() throws SQLException {
        HBox fila = nuevaFila;
        String dni = ((TextField)fila.getChildren().get(1)).getText();
        TextField nombreField = (TextField) fila.getChildren().get(2);
        TextField emailField = (TextField) fila.getChildren().get(3);
        PasswordField passwordField = (PasswordField) fila.getChildren().get(4);

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setDni(dni);
        nuevoUsuario.setNombre(nombreField.getText());
        nuevoUsuario.setEmail(emailField.getText());
        nuevoUsuario.setPassword(passwordField.getText());

        boolean exito = usuarioDao.registrarUsuario(nuevoUsuario);

        // Si el usuario es creado correctamente, se añade a la lista de usuarios
        if (exito) {
            mostrarMensaje("Éxito", "Usuario añadido correctamente");
            cargarUsuarios();
        } else {
            mostrarError("Error", "No se pudo añadir el usuario");
        }
    }

    // Método para cancelar la creación o edición de un usuario. Se resetea la interfaz
    private void cancelarNuevoUsuario() {
        eliminarFilaEdicionSiExiste();
        resetearModoEdicion();
    }

    // Método para eliminar la fila de edición si existe
    private void eliminarFilaEdicionSiExiste() {
        if (nuevaFila != null && usuariosVBox.getChildren().contains(nuevaFila)) {
            usuariosVBox.getChildren().remove(nuevaFila);
        }
    }

    private void resetearModoEdicion() {
        addBtn.setVisible(true);
        guardarBtn.setVisible(false);
        cancelarBtn.setVisible(false);
        editarBtn.setDisable(false);
        eliminarBtn.setDisable(false);

        modoEdicion = false;
        usuarioEditando = null;
        nuevaFila = null;
    }

    private void editarUsuario(Usuario usuario) {
        if (validarUsuarioAdministrador(usuario)) {
            return;
        }

        prepararInterfazParaEdicion();
        crearFormularioEdicionUsuario(usuario);
    }

    private boolean validarUsuarioAdministrador(Usuario usuario) {
        if ("admin@admin.com".equals(usuario.getEmail())) {
            mostrarError("Error", "No se puede modificar el usuario administrador");
            return true;
        }
        return false;
    }

    private void prepararInterfazParaEdicion() {
        modoEdicion = true;
        addBtn.setVisible(false);
        guardarBtn.setVisible(true);
        cancelarBtn.setVisible(true);
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);
    }

    // Método para crear el formulario de edición de usuario. DNI no editable
    private void crearFormularioEdicionUsuario(Usuario usuario) {
        usuarioEditando = usuario;

        Label dniLabel = new Label(usuario.getDni());
        TextField nombreField = new TextField(usuario.getNombre());
        TextField emailField = new TextField(usuario.getEmail());
        PasswordField passwordField = new PasswordField();

        configurarCamposEdicion(dniLabel, nombreField, emailField, passwordField);

        nuevaFila = new HBox();
        nuevaFila.setStyle("-fx-background-color: #fffde7; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        nuevaFila.getChildren().addAll(
                new Label("Editando:"),
                dniLabel,
                nombreField,
                emailField,
                passwordField
        );

        usuariosVBox.getChildren().add(1, nuevaFila);
        scrollUsuarios.setVvalue(0);
    }

    private void configurarCamposEdicion(Label dniLabel, TextField nombreField, TextField emailField, PasswordField passwordField) {
        nombreField.setPromptText("Nombre");
        emailField.setPromptText("Email");
        passwordField.setPromptText("Nueva contraseña (dejar vacío para no cambiar)");

        dniLabel.setPrefWidth(120);
        nombreField.setPrefWidth(200);
        emailField.setPrefWidth(250);
        passwordField.setPrefWidth(150);
    }

    private void editarSeleccionados() {
        List<Usuario> seleccionados = obtenerUsuariosSeleccionados();

        if (!validarSeleccionEdicion(seleccionados)) {
            return;
        }

        editarUsuario(seleccionados.get(0));
    }

    private List<Usuario> obtenerUsuariosSeleccionados() {
        List<Usuario> seleccionados = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                seleccionados.add(usuariosOriginal.get(i));
            }
        }
        return seleccionados;
    }

    private boolean validarSeleccionEdicion(List<Usuario> seleccionados) {
        if (seleccionados.isEmpty()) {
            mostrarError("Error", "Seleccione al menos un usuario para editar");
            return false;
        }

        if (seleccionados.size() > 1) {
            mostrarError("Error", "Solo puede editar un usuario a la vez");
            return false;
        }

        if ("admin@admin.com".equals(seleccionados.get(0).getEmail())) {
            mostrarError("Error", "No se puede editar el usuario administrador");
            return false;
        }

        return true;
    }

    // Método para eliminar los usuarios seleccionados. También elimina las reservas asociadas y actualiza la vista
    private void eliminarSeleccionados() {
        List<Usuario> seleccionados = obtenerUsuariosSeleccionados();
        seleccionados.removeIf(u -> "admin@admin.com".equals(u.getEmail()));

        if (!validarSeleccionEliminacion(seleccionados)) {
            return;
        }

        eliminarUsuariosYActualizarVista(seleccionados);
    }

    private boolean validarSeleccionEliminacion(List<Usuario> seleccionados) {
        if (seleccionados.isEmpty()) {
            mostrarError("Error", "Seleccione al menos un usuario para eliminar");
            return false;
        }
        return true;
    }

    // Método para eliminar los usuarios seleccionados y actualizar la vista
    private void eliminarUsuariosYActualizarVista(List<Usuario> seleccionados) {
        try {
            for (Usuario usuario : seleccionados) {
                // Primero eliminar reservas asociadas
                int reservasEliminadas = reservasDao.cancelarReservasByUsuarioID(usuario.getDni());
                int reservasTempEliminadas = reservasDao.eliminarReservasTemporalesByUsuario(usuario.getDni());

                // Luego eliminar el usuario
                int eliminados = usuarioDao.eliminarUsuarioByID(usuario.getDni());
                if (eliminados > 0) {
                    usuariosOriginal.remove(usuario);
                    mostrarMensaje("Confirmado", "Se borró el usuario: " + usuario.getDni() +
                            "\nReservas eliminadas: " + (reservasEliminadas + reservasTempEliminadas));
                }
            }

            cargarUsuarios(); // Actualizar la lista de usuarios
        } catch (SQLException e) {
            manejarErrorEliminacion(e);
        }
    }

    // Método para eliminar un usuario específico y sus reservas asociadas
    private void eliminarUsuario(Usuario usuario) {
        try {
            // Primero eliminar reservas asociadas
            int reservasEliminadas = reservasDao.cancelarReservasByUsuarioID(usuario.getDni());
            int reservasTempEliminadas = reservasDao.eliminarReservasTemporalesByUsuario(usuario.getDni());

            // Luego eliminar el usuario
            int eliminados = usuarioDao.eliminarUsuarioByID(usuario.getDni());

            if (eliminados > 0) {
                usuariosOriginal.remove(usuario);
                mostrarMensaje("Usuario eliminado",
                        "Se eliminó el usuario: " + usuario.getDni() +
                                "\nReservas eliminadas: " + (reservasEliminadas + reservasTempEliminadas));
                cargarUsuarios();
            }
        } catch (SQLException e) {
            manejarErrorEliminacion(e);
        }
    }

    private void manejarErrorInicializacion(SQLException e) {
        e.printStackTrace();
        mostrarError("Error", "Error al conectar con la base de datos");
    }

    private void manejarErrorCargaUsuarios(SQLException e) {
        e.printStackTrace();
        mostrarError("Error", "Error al cargar los usuarios");
    }

    private void manejarErrorGuardado(SQLException e) {
        e.printStackTrace();
        mostrarError("Error", "Error al guardar los datos del usuario");
    }

    private void manejarErrorEliminacion(SQLException e) {
        e.printStackTrace();
        mostrarError("Error", "Error al eliminar el usuario o sus reservas");
    }

    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}