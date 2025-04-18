package controllers;

import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static controllers.LoginController.*;

public class ReservasController {

    @FXML
    public Label espectaculoLabel;
    @FXML
    private Label usuarioLabel;
    @FXML
    private GridPane gridPane;

    private List<Butacas> butacasOcupadas;
    private List<Butacas> butacasVIP;

    // Image paths
    private static final String ASIENTOS_OCUPADOS = "/resources/images/BUTACA-ROJA.png";
    private static final String ASIENTOS_VIP = "/resources/images/BUTACA-AMARILLA.png";
    private static final String ASIENTOS_ESTANDAR = "/resources/images/BUTACA-VERDE.png";

    // Cambiado a campos de instancia en lugar de estáticos
    private String emailUsuarioLogueado;
    private String espectaculoSeleccionado;
    private String idEspectaculoSeleccionado;

    public ReservasController(String emailUsuario, String nombreEspectaculo, String idEspectaculo) {
        this.emailUsuarioLogueado = emailUsuario;
        this.espectaculoSeleccionado = nombreEspectaculo;
        this.idEspectaculoSeleccionado = idEspectaculo;
    }

    // Quitar la inicialización de estos campos del método initialize()
    @FXML
    public void initialize() {
        // Mostrar el email del usuario logueado
        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
        }

        // Mostrar el título del espectáculo seleccionado
        if (espectaculoSeleccionado != null) {
            espectaculoLabel.setText(espectaculoSeleccionado);
        }

        // Validar ID del espectáculo
        if (idEspectaculoSeleccionado == null || idEspectaculoSeleccionado.isEmpty()) {
            throw new IllegalStateException("No se ha seleccionado un espectáculo válido");
        }

        initializeGrid();
    }

    private void initializeGrid() {
        // Borramos si hay algún botón previo
        gridPane.getChildren().clear();

        butacasOcupadas = obtenerDatosButacasOcupadas();
        butacasVIP = obtenerDatosButacasVIP();

        for (int fila = 0; fila < 10; fila++) {
            for (int columna = 0; columna < 10; columna++) {
                Button asiento = crearAsiento(fila, columna);
                gridPane.add(asiento, columna, fila);
            }
        }


        /* Creamos el grid
        for (int fila = 0; fila < 10; fila++) {
            for (int columna = 0; columna < 10; columna++) {
                Button asiento = crearAsiento(fila, columna, butacas, idEspectaculoSeleccionado);
                gridPane.add(asiento, columna, fila);
            }
        } */
    }

    private Button crearAsiento(int fila, int columna) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);

        boolean ocupada = false;
        boolean vip=false;

        for (Butacas butaca : butacasOcupadas) {
            if (butaca.getFila() == fila && butaca.getCol() == columna) {
                ocupada = true;
                setImagenAsientos(imageView, "occupied");
                break;
            }
        }

        for (Butacas butaca: butacasVIP){
            if (butaca.getFila() == fila && butaca.getCol() ==columna){
                vip=true;
                setImagenAsientos(imageView, "vip");
                break;
            }
        }

        if (!ocupada && !vip){
            setImagenAsientos(imageView, "standard");
        }

        button.setGraphic(imageView);
        button.setId("F" + fila + "_C" + columna);

        // Configurar acción del botón
        button.setOnAction(event -> handleSeleccionAsientos(button, fila, columna));

        return button;
    }



    private void setImagenAsientos(ImageView imageView, String tipoAsiento) {
        try {
            String imagePath = switch (tipoAsiento.toLowerCase()) {
                case "vip" -> ASIENTOS_VIP;
                case "occupied" -> ASIENTOS_OCUPADOS;
                default -> ASIENTOS_ESTANDAR;
            };

            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private  List<Butacas> obtenerDatosButacas(){
        List<Butacas> butacasList = new ArrayList<>();

        if (!checkConexion()){
            conexion();
        }

        if (!checkConexion()){
            throw new IllegalStateException("ERROR DE CONEXION");
        }

        String query = "SELECT id_butaca, fila, columna, tipo " +
                "FROM BUTACAS ORDER BY id_butaca ASC";

        try {
            PreparedStatement stmt= conexion().prepareStatement(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()){
                Butacas butaca = new Butacas(
                        rs.getString("id_butaca"),
                        rs.getInt("fila"),
                        rs.getInt("columna"),
                        rs.getString("tipo").charAt(0)
                );

                butacasList.add(butaca);
            }


        } catch (SQLException e){
            e.printStackTrace();
        }




        return butacasList;
    }

    //Obtener información de las butacas
    private List<Butacas> obtenerDatosButacasOcupadas() {
        List<Butacas> butacasList = new ArrayList<>();

        if (!checkConexion()){
            conexion();
        }

        if (!checkConexion()){
            throw new IllegalStateException("ERROR AL CONECTAR CON LA BBDD");
        }

        // Consulta para obtener butacas ocupadas para este espectáculo
        String queryOcupadas = "SELECT b.id_butaca, b.fila, b.columna, b.tipo " +
                "FROM BUTACAS b, RESERVAS r " +
                "WHERE b.id_butaca = r.id_butaca " +
                "AND r.ID_ESPECTACULO = ?";

        try {
            // Obtener butacas ocupadas
            PreparedStatement pstmtOcupadas = conexion().prepareStatement(queryOcupadas);
            pstmtOcupadas.setString(1, idEspectaculoSeleccionado);
            ResultSet rsOcupadas = pstmtOcupadas.executeQuery();

            while (rsOcupadas.next()) {
                Butacas butaca = new Butacas(
                        rsOcupadas.getString("id_butaca"),
                        rsOcupadas.getInt("fila"),
                        rsOcupadas.getInt("columna"),
                        rsOcupadas.getString("tipo").charAt(0)
                );
                butacasList.add(butaca);
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar butacas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error en la base de datos", e);
        }


        return butacasList;
    }

    private List<Butacas> obtenerDatosButacasVIP(){
        List<Butacas> butacasList = new ArrayList<>();

        if (!checkConexion()){
            conexion();
        }

        if (!checkConexion()){
            throw new IllegalStateException("ERROR AL CONECTAR LA BBDD");
        }

        // Consulta para obtener butacas ocupadas para este espectáculo
        String queryVIP = "SELECT b.id_butaca, b.fila, b.columna, b.tipo " +
                "FROM BUTACAS b " +
                "WHERE b.tipo='V' ";

        try {
            // Obtener butacas vip
            PreparedStatement pstmtVIP = conexion().prepareStatement(queryVIP);
            ResultSet rsVIP = pstmtVIP.executeQuery();

            while (rsVIP.next()) {
                Butacas butaca = new Butacas(
                        rsVIP.getString("id_butaca"),
                        rsVIP.getInt("fila"),
                        rsVIP.getInt("columna"),
                        rsVIP.getString("tipo").charAt(0)
                );
                butacasList.add(butaca);
            }

            butacasList.removeAll(obtenerDatosButacasOcupadas());

        } catch (SQLException e) {
            System.err.println("Error al cargar butacas: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error en la base de datos", e);
        }


        return butacasList;
    }

    private void handleSeleccionAsientos(Button button, int fila, int columna) {
        ImageView imageView = (ImageView) button.getGraphic();

        // Verificar si el asiento está ocupado
        List<Butacas> butacasOcupadas = obtenerDatosButacasOcupadas();
        boolean ocupada = false;

        for (Butacas butaca : butacasOcupadas) {
            if (butaca.getFila() == fila && butaca.getCol() == columna) {
                ocupada = true;
                break;
            }
        }

        if (ocupada) {
            System.out.println("Asiento ocupado - no se puede seleccionar");
            System.out.println(idEspectaculoSeleccionado);
            //  mostrar un mensaje al usuario aquí, a implementar
        } else {
            System.out.println("Asiento seleccionado: Fila " + fila + ", Columna " + columna);
            // Lógica para seleccionar el asiento
        }
    }

    // Eliminar los métodos estáticos get/set y cambiarlos por métodos de instancia
    public void setEspectaculoSeleccionado(String nombreEspectaculo) {
        this.espectaculoSeleccionado = nombreEspectaculo;
        if (espectaculoLabel != null) {
            espectaculoLabel.setText(nombreEspectaculo);
        }
    }

    public String getEspectaculoSeleccionado() {
        return espectaculoSeleccionado;
    }

    public void setIDEspectaculoSeleccionado(String idEspectaculo) {
        this.idEspectaculoSeleccionado = idEspectaculo;
    }

    public String getIDEspectaculoSeleccionado() {
        return idEspectaculoSeleccionado;
    }

    //método para cerrar sesión y volver al login
    //bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login
    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado=null;

        cambioEscena("../views/login.fxml");
    }

    public void filtrarPorAsiento(ActionEvent actionEvent) {
    }

    public void mostrarTodas(ActionEvent actionEvent) {
    }

    //EFECTO FADE al cambiar de escena
    public void fadeInScene(Node rootNode) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    //CONFIG TAMAÑO LOGIN Y REGISTRO FIJOS
    public void configureStage(Stage stage) {
        stage.setMinWidth(750);
        stage.setMinHeight(550);
        stage.setMaxWidth(800);
        stage.setMaxHeight(700);
    }

    public void volverCartelera(ActionEvent actionEvent) {

        cambioEscena("../views/cartelera.fxml");

    }

    //método para cambiar de escena
    private void cambioEscena(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
            Parent root = loader.load();

            // Obtener el Stage actual, con utilizar cualquier atributo fxml o nodo sirve.
            Stage stage = (Stage) usuarioLabel.getScene().getWindow();

            // Crear una nueva escena
            Scene scene = new Scene(root);
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

class Butacas {
    private String id;
    private int fila;
    private int col;
    private char tipo;

    public Butacas(String id, int fila, int col, char tipo) {
        this.id = id;
        this.fila = fila;
        this.col = col;
        this.tipo = tipo;
    }

    public int getFila() {
        return fila;
    }

    public int getCol() {
        return col;
    }

    public char getTipo() {
        return tipo;
    }

    public String getId() {
        return id;
    }
}


