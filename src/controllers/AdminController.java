package controllers;

import utils.DatabaseConnection;
import dao.UsuarioDaoI;
import dao.impl.UsuarioDaoImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.Usuario;
import utils.Transitions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static controllers.LoginController.getUsuarioLogueadoEmail;

public class AdminController {

    public Tab reservasTab;
    public Tab usuariosTab;
    public Button verReservasBtn;
    public Button cancelarReservaBtn;
    public Button estadisticasBtn;
    public Button agregarPeliculaBtn;
    public Button eliminarPeliculaBtn;
    public Button modificarPeliculaBtn;
    public Button listarPeliculasBtn;
    public Button agregarUsuarioBtn;
    public Button eliminarUsuarioBtn;
    public Button modificarUsuarioBtn;
    public Button listarUsuariosBtn;
    public VBox menuLateral;

    private String idUsuario;
    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    private List<Usuario> usuariosList = new ArrayList<>();

    private UsuarioDaoI usuarioDao;

    @FXML
    public StackPane contenidoArea;
    @FXML
    private Label usuarioLabel;
    @FXML
    private VBox contenedorUsuarios;
    @FXML
    private ScrollPane scrollUsuarios;
    @FXML
    private Label arribaBtn, abajoBtn;

    // Acordeón de menú
    @FXML
    private VBox usuariosAccordion, reservasAccordion, peliculasAccordion;
    @FXML
    private VBox usuariosOpciones, reservasOpciones, peliculasOpciones;
    @FXML
    private Button usuariosBtn, reservasBtn, peliculasBtn;

    @FXML
    public void initialize() {
        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
            try {
                Connection conn = DatabaseConnection.getConnection();
                this.usuarioDao = new UsuarioDaoImpl(conn);
                idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);
                System.out.println("ID Usuario obtenido: " + idUsuario);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (scrollUsuarios != null) {
            scrollUsuarios.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
            scrollUsuarios.setFitToWidth(true);
            agregarListenersScroll();
        }
    }

    // Métodos para mostrar/ocultar opciones
    @FXML
    private void toggleOpcionesUsuarios(ActionEvent event) {
        boolean isVisible = usuariosOpciones.isVisible();
        usuariosOpciones.setVisible(!isVisible);
        usuariosBtn.setText(isVisible ? "▶ Usuarios" : "▼ Usuarios");
    }

    @FXML
    private void toggleOpcionesReservas(ActionEvent event) {
        boolean isVisible = reservasOpciones.isVisible();
        reservasOpciones.setVisible(!isVisible);
        reservasBtn.setText(isVisible ? "▶ Reservas" : "▼ Reservas");
    }

    @FXML
    private void toggleOpcionesPeliculas(ActionEvent event) {
        boolean isVisible = peliculasOpciones.isVisible();
        peliculasOpciones.setVisible(!isVisible);
        peliculasBtn.setText(isVisible ? "▶ Películas" : "▼ Películas");
    }

    // Métodos para cargar vistas de usuarios
    @FXML
    private void cargarAgregarUsuario(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/regComponent.fxml"));
            Parent registrationView = loader.load();
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(registrationView);
        } catch (IOException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar el formulario de registro");
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(errorLabel);
        }
    }

    @FXML
    private void cargarEliminarUsuario(ActionEvent event) {
        cargarVista("eliminarUsuario");
    }

    @FXML
    private void cargarModificarUsuario(ActionEvent event) {
        cargarVista("modificarUsuario");
    }

    @FXML
    private void cargarListarUsuarios(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/listarUsuarios.fxml"));
            Parent listarUsuariosView = loader.load();
            controllers.ListarUsuariosController controller = loader.getController();
            // Obtener usuarios vía DAO
            Connection conn = utils.DatabaseConnection.getConnection();
            dao.UsuarioDaoI usuarioDao = new dao.impl.UsuarioDaoImpl(conn);
            java.util.List<models.Usuario> usuarios = usuarioDao.listUsuariosAdmin();
            controller.mostrarUsuarios(usuarios);
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(listarUsuariosView);
        } catch (Exception e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar la lista de usuarios");
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(errorLabel);
        }
    }

    // Métodos para cargar vistas de reservas
    @FXML
    private void cargarVerReservas(ActionEvent event) {
        cargarVista("verReservas");
    }

    @FXML
    private void cargarCancelarReserva(ActionEvent event) {
        cargarVista("cancelarReserva");
    }

    @FXML
    private void cargarEstadisticas(ActionEvent event) {
        cargarVista("estadisticas");
    }

    // Métodos para cargar vistas de películas
    @FXML
    private void cargarAgregarPelicula(ActionEvent event) {
        cargarVista("agregarPelicula");
    }

    @FXML
    private void cargarEliminarPelicula(ActionEvent event) {
        cargarVista("eliminarPelicula");
    }

    @FXML
    private void cargarModificarPelicula(ActionEvent event) {
        cargarVista("modificarPelicula");
    }

    @FXML
    private void cargarListarPeliculas(ActionEvent event) {
        cargarVista("listarPeliculas");
    }

    private void cargarVista(String vista) {
        contenidoArea.getChildren().clear();
        Label label = new Label("Cargando vista: " + vista);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
        contenidoArea.getChildren().add(label);


    }

    // Resto de métodos existentes...
    private void agregarListenersScroll() {
        scrollUsuarios.setOnMouseEntered(e -> {
            arribaBtn.setOpacity(0);
            abajoBtn.setOpacity(0);
        });

        scrollUsuarios.setOnMouseExited(e -> {
            arribaBtn.setOpacity(1);
            abajoBtn.setOpacity(1);
        });

        arribaBtn.setOnMouseClicked(e ->
                scrollUsuarios.setVvalue(scrollUsuarios.getVvalue() - 0.2));

        abajoBtn.setOnMouseClicked(e ->
                scrollUsuarios.setVvalue(scrollUsuarios.getVvalue() + 0.2));

        arribaBtn.setOnKeyPressed(e->
                scrollUsuarios.setVvalue(scrollUsuarios.getVvalue() - 0.2));

        arribaBtn.setOpacity(0);
        abajoBtn.setOpacity(0);

        scrollUsuarios.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }

    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado=null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/registro.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usuarioLabel.getScene().getWindow();

            Scene scene = new Scene(root);

            Transitions transitions = new Transitions();
            transitions.fadeInScene(root);

            scene.getStylesheets().add(getClass().getResource("../Resources/styles.css").toExternalForm());
            stage.setTitle("CINES JRC");

            Image icon = new Image(getClass().getResourceAsStream("../Resources/logo.png"));
            stage.getIcons().add(icon);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}