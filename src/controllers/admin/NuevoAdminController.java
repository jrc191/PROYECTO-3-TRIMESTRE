package controllers.admin;

import dao.UsuarioDaoI;
import dao.impl.UsuarioDaoImpl;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import static controllers.LoginController.getUsuarioLogueadoEmail;

public class NuevoAdminController {
    @FXML
    public ScrollPane scrollContenido;
    @FXML
    public VBox contenidoArea;
    @FXML
    private Label usuarioLabel;
    @FXML
    public Label rutaLabel;
    @FXML
    private Label arribaBtn, abajoBtn;

    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    private String idUsuario;
    private UsuarioDaoI usuarioDao;

    @FXML
    public void initialize() {
        if (emailUsuarioLogueado != null) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                this.usuarioDao = new UsuarioDaoImpl(conn);
                //CORREGIR ELIPSIS: idUsuario = usuarioDao.getNombreUsuarioByEmail(emailUsuarioLogueado);
                idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);
                usuarioLabel.setText(idUsuario);
                System.out.println("ID Usuario obtenido: " + idUsuario);
                rutaLabel.setText("/views/admin/admin-NUEVO.fxml");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        //A IMPLEMENTAR SCROLL?
        /*if (scrollContenido != null) {
            scrollContenido.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
            scrollContenido.setFitToWidth(true);
            agregarListenersScroll();
        }

        */


    }

    private void agregarListenersScroll() {
        scrollContenido.setOnMouseEntered(e -> {
            arribaBtn.setOpacity(0);
            abajoBtn.setOpacity(0);
        });

        scrollContenido.setOnMouseExited(e -> {
            arribaBtn.setOpacity(1);
            abajoBtn.setOpacity(1);
        });

        arribaBtn.setOnMouseClicked(e ->
                scrollContenido.setVvalue(scrollContenido.getVvalue() - 0.2));

        abajoBtn.setOnMouseClicked(e ->
                scrollContenido.setVvalue(scrollContenido.getVvalue() + 0.2));

        arribaBtn.setOnKeyPressed(e->
                scrollContenido.setVvalue(scrollContenido.getVvalue() - 0.2));

        arribaBtn.setOpacity(0);
        abajoBtn.setOpacity(0);

        scrollContenido.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    }


    public void listarUsuarios(MouseEvent mouseEvent) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/listarUsuarios.fxml"));
            Parent listarUsuariosView = loader.load();
            ListarUsuariosController controller = loader.getController();

            // Obtener usuarios
            Connection conn = utils.DatabaseConnection.getConnection();
            dao.UsuarioDaoI usuarioDao = new dao.impl.UsuarioDaoImpl(conn);
            java.util.List<models.Usuario> usuarios = usuarioDao.listUsuariosAdmin();
            controller.mostrarUsuarios(usuarios);
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(listarUsuariosView);
            rutaLabel.setText("/views/admin/listarUsuarios");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar el formulario de listado de usuarios");
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(errorLabel);
        }

    }

    public void listarEspectaculos(MouseEvent mouseEvent) {

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/listarEspectaculos.fxml"));
            Parent listarEspectaculosView = loader.load();
            ListarEspectaculosController controller = loader.getController();

            // Obtener usuarios
            Connection conn = utils.DatabaseConnection.getConnection();
            dao.EspectaculoDaoI espectaculoDao = new dao.impl.EspectaculoDaoImpl(conn);
            java.util.List<models.Espectaculo> espectaculos = espectaculoDao.obtenerTodos();
            controller.cargarEspectaculos();
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(listarEspectaculosView);
            rutaLabel.setText("/views/admin/listarEspectaculos");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar el formulario de listado de usuarios");
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(errorLabel);
        }

    }

    public void listarReservas(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/listarReservas.fxml"));
            Parent listarReservasView = loader.load();
            ListarReservasController controller = loader.getController();

            // Obtener usuarios
            Connection conn = utils.DatabaseConnection.getConnection();
            dao.ReservasDaoI reservasDao = new dao.impl.ReservaDaoImpl(conn);
            java.util.List<models.Reservas> reservas = reservasDao.listarTodasReservas();
            controller.cargarReservas();
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(listarReservasView);
            rutaLabel.setText("/views/admin/listarReservas.fxml");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            Label errorLabel = new Label("Error al cargar el formulario de listado de reservas.");
            errorLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenidoArea.getChildren().clear();
            contenidoArea.getChildren().add(errorLabel);
        }

    }
}
