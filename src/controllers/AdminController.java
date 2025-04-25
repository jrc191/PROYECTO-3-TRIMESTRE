package controllers;

import dao.DatabaseConnection;
import dao.UsuarioDaoI;
import dao.impl.UsuarioDaoImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import models.EntradaCesta;
import models.Usuario;
import utils.CestaStorage;
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
    private String idUsuario;
    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    private List<Usuario> usuariosList = new ArrayList<>();

    private UsuarioDaoI usuarioDao;
    @FXML
    private Label usuarioLabel;
    @FXML
    private VBox contenedorUsuarios;
    @FXML
    private ScrollPane scrollUsuarios;
    @FXML
    private Label arribaBtn, abajoBtn;


    @FXML
    public void initialize(){

        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
            try {
                Connection conn = DatabaseConnection.getConnection();
                this.usuarioDao = new UsuarioDaoImpl(conn);
                idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);
                System.out.println("ID Usuario obtenido: " + idUsuario); // Debug
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Configurar el scroll
        scrollUsuarios.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
        scrollUsuarios.setFitToWidth(true);

        agregarListenersScroll();

    }

    // Agregar este método para manejar el scroll
    private void agregarListenersScroll() {
        // Mostrar/ocultar flechas al entrar/salir del scroll
        scrollUsuarios.setOnMouseEntered(e -> {
            arribaBtn.setOpacity(0);
            abajoBtn.setOpacity(0);
        });

        scrollUsuarios.setOnMouseExited(e -> {
            arribaBtn.setOpacity(1);
            abajoBtn.setOpacity(1);
        });

        // Controlar el scroll con las flechas
        arribaBtn.setOnMouseClicked(e ->
                scrollUsuarios.setVvalue(scrollUsuarios.getVvalue() - 0.2));

        abajoBtn.setOnMouseClicked(e ->
                scrollUsuarios.setVvalue(scrollUsuarios.getVvalue() + 0.2));


        arribaBtn.setOnKeyPressed(e->
                scrollUsuarios.setVvalue(scrollUsuarios.getVvalue() - 0.2));

        // Ajustar opacidad inicial
        arribaBtn.setOpacity(0);
        abajoBtn.setOpacity(0);

        scrollUsuarios.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

    }

    public void agregaraUsuario(String idUsuario, String emailUsuarioLogueado) {
        if (usuariosList == null) {
            usuariosList = new ArrayList<>();
        }

        /*
        EntradaCesta entrada = new EntradaCesta(idUsuario, emailUsuarioLogueado); //TODO: CREAR USUARIO CON PLANTILLA
        entradas.add(entrada);
        total += precio;
        actualizarCesta();

        CestaStorage.guardarCesta(emailUsuarioLogueado, entradas); //Sencillamente espectacular. Para guardar la cesta en ficheros según el mail
        */
    }


    @FXML
    public void handleUsuarios(){
        //MOSTRAR USUARIOS EN LISTA
    }

    @FXML
    public void handleReservas(){

    }

    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado=null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/registro.fxml"));
            Parent root = loader.load();

            // Obtener el Stage actual, con utilizar cualquier atributo fxml o nodo sirve.
            Stage stage = (Stage) usuarioLabel.getScene().getWindow();

            // Crear una nueva escena
            Scene scene = new Scene(root);

            Transitions transitions = new Transitions();
            transitions.fadeInScene(root);

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
