package controllers.admin;

import dao.UsuarioDaoI;
import dao.impl.UsuarioDaoImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.CerrarSesion;
import utils.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static controllers.LoginController.getUsuarioLogueadoEmail;

/**
 * Controlador principal del panel de administración.
 * Gestiona la navegación entre las diferentes secciones del panel.
 */
public class NuevoAdminController {
    // Elementos de la interfaz
    @FXML public ScrollPane scrollContenido;
    @FXML public VBox contenidoArea;
    @FXML public Label cerrarSesion;
    @FXML public Label usuariosLabel;
    @FXML public Label espectaculosLabel;
    @FXML public Label reservasLabel;
    @FXML public Button volverBtn;
    @FXML public Label mensajesLabel;
    @FXML private Label usuarioLabel;
    @FXML public Label rutaLabel;
    @FXML private Label arribaBtn, abajoBtn;

    // Datos y estado
    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    private String idUsuario;
    private UsuarioDaoI usuarioDao;
    private String rutaPrevia;
    private ListarReservasController reservasController;
    private ListarMensajesController mensajesController;


    //Inicializa el controlador, cargando los datos del usuario logueado y mostrando la vista de usuarios por defecto.
    @FXML
    public void initialize() {
        try {
            inicializarDatosUsuario();
            listarUsuarios(null); // Cargar vista de usuarios por defecto
        } catch (SQLException e) {
            manejarErrorInicializacion(e);
        }
    }


    //Cierra la sesión actual y regresa a la pantalla de login
    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado = null;
        Stage stage = (Stage) usuarioLabel.getScene().getWindow();
        CerrarSesion.cerrarSesion(stage, "/resources/styles/styles.css", "/resources/images/logo.png");
    }

    //Carga y muestra la vista de listado de usuarios.
    public void listarUsuarios(MouseEvent mouseEvent) throws SQLException {
        try {
            cargarVista("/views/admin/listarUsuarios.fxml", "USUARIOS");
            actualizarEstadoMenu(0.5, 1, 1, 1); // Usuarios seleccionado
        } catch (IOException e) {
            manejarErrorCargaVista("usuarios", e);
        }
    }

    //Carga y muestra la vista de listado de espectáculos.

    public void listarEspectaculos(MouseEvent mouseEvent) throws SQLException {
        try {
            FXMLLoader loader = cargarVista("/views/admin/listarEspectaculos.fxml", "ESPECTÁCULOS");
            ListarEspectaculosController controller = loader.getController();
            controller.cargarEspectaculos();
            actualizarEstadoMenu(1, 0.5, 1, 1); // Espectáculos seleccionado
        } catch (IOException e) {
            manejarErrorCargaVista("espectáculos", e);
        }
    }

    //Carga y muestra la vista de listado de reservas.

    public void listarReservas(MouseEvent mouseEvent) throws SQLException {
        try {
            FXMLLoader loader = cargarVista("/views/admin/listarReservas.fxml", "RESERVAS");
            this.reservasController = loader.getController();
            reservasController.cargarReservas();
            actualizarEstadoMenu(1, 1, 0.5, 1); // Reservas seleccionado
        } catch (IOException e) {
            manejarErrorCargaVista("reservas", e);
        }
    }

    //Carga y muestra la vista de mensajes.
    public void mostrarMensajes(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = cargarVista("/views/admin/listarMensajes.fxml", "MENSAJES");
            this.mensajesController = loader.getController();

            if (this.reservasController != null) {
                this.mensajesController.setReservasController(this.reservasController);
            }

            actualizarEstadoMenu(1, 1, 1, 0.5); // Mensajes seleccionado
        } catch (IOException e) {
            manejarErrorCargaVista("mensajes", e);
        }
    }


    //Inicializa los datos del usuario logueado.
    private void inicializarDatosUsuario() throws SQLException {
        if (emailUsuarioLogueado != null) {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
            idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);
            usuarioLabel.setText(idUsuario);
            System.out.println("ID Usuario obtenido: " + idUsuario);
            rutaLabel.setText("PANEL DE ADMINISTRACIÓN");
        }
    }

    //Carga una vista FXML en el área de contenido principal.
    private FXMLLoader cargarVista(String fxmlPath, String titulo) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Parent vista = loader.load();
        contenidoArea.getChildren().clear();
        contenidoArea.getChildren().add(vista);
        rutaLabel.setText(titulo);
        return loader;
    }

    //Actualiza el estado visual de los elementos del menú.

    private void actualizarEstadoMenu(double usuarios, double espectaculos, double reservas, double mensajes) {
        usuariosLabel.setOpacity(usuarios);
        espectaculosLabel.setOpacity(espectaculos);
        reservasLabel.setOpacity(reservas);
        mensajesLabel.setOpacity(mensajes);
    }

    //Maneja errores durante la inicialización del controlador.
    private void manejarErrorInicializacion(SQLException e) {
        e.printStackTrace();
        mostrarError("Error al inicializar", "No se pudieron cargar los datos del usuario");
    }

    //Maneja errores al cargar las vistas.
    private void manejarErrorCargaVista(String vista, Exception e) {
        e.printStackTrace();
        Label errorLabel = new Label("Error al cargar el listado de " + vista);
        errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        contenidoArea.getChildren().clear();
        contenidoArea.getChildren().add(errorLabel);
    }

    //Muestra un mensaje de error en una ventana emergente.
    private void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}