package controllers;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import dao.ButacaDaoI;
import utils.CerrarSesion;
import utils.DatabaseConnection;
import dao.ReservasDaoI;
import dao.UsuarioDaoI;
import dao.impl.ButacaDaoImpl;
import dao.impl.ReservaDaoImpl;
import dao.impl.UsuarioDaoImpl;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;
import models.*;
import utils.Transitions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservasController {

    @FXML public Label espectaculoLabel;
    @FXML private Label usuarioLabel;
    @FXML private GridPane gridPane;
    @FXML private ChoiceBox<String> eleccionBox;

    private static final String ASIENTOS_OCUPADOS = "/resources/images/BUTACA-ROJA.png";
    private static final String ASIENTOS_VIP = "/resources/images/BUTACA-AMARILLA.png";
    private static final String ASIENTOS_ESTANDAR = "/resources/images/BUTACA-VERDE.png";

    private List<Butaca> todosLosAsientos;
    private List<Butaca> butacasOcupadas;
    private List<Butaca> butacasVIP;
    private List<EntradaCesta> cestaList;
    private ButacaDaoI butacaDao;
    private UsuarioDaoI usuarioDao;
    private ReservasDaoI reservasDao;
    private Espectaculo espectaculo;

    private String emailUsuarioLogueado;
    private String idUsuario;
    private String espectaculoSeleccionado;
    private String idEspectaculoSeleccionado;
    private CestaController cestaController;

    public ReservasController() {
        // Constructor vacío
    }

    public ReservasController(String emailUsuario, String nombreEspectaculo, String idEspectaculo, CestaController cestaController, Espectaculo espectaculo) {
        this.emailUsuarioLogueado = emailUsuario;
        this.espectaculoSeleccionado = nombreEspectaculo;
        this.idEspectaculoSeleccionado = idEspectaculo;
        this.cestaController = cestaController;
        this.espectaculo = espectaculo;
    }

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.butacaDao = new ButacaDaoImpl(conn);
            this.reservasDao = new ReservaDaoImpl(conn);
            this.usuarioDao = new UsuarioDaoImpl(conn);

            // Inicialización de las listas
            butacasOcupadas = new ArrayList<>();
            butacasVIP = new ArrayList<>();
            cestaList = new ArrayList<>();

            idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);

            // Obtener las butacas ocupadas y VIP
            butacasOcupadas = butacaDao.obtenerButacasOcupadas(idEspectaculoSeleccionado);
            butacasVIP = butacaDao.obtenerButacasVIP();
            cestaList = utils.CestaStorage.cargarCesta(emailUsuarioLogueado);

        } catch (SQLException e) {
            throw new RuntimeException("Error al conectar con la base de datos", e);
        }

        // Inicializamos opciones del ChoiceBox
        eleccionBox.getItems().addAll( "VIP", "Estandar");
        eleccionBox.setValue("-");

        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
        }

        if (espectaculoSeleccionado != null) {
            espectaculoLabel.setText(espectaculoSeleccionado);
        }

        if (idEspectaculoSeleccionado == null || idEspectaculoSeleccionado.isEmpty()) {
            throw new IllegalStateException("No se ha seleccionado un espectáculo válido");
        }

        mostrarTodasButacas();
    }

    public void fadeInScene(Node rootNode) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    public void configureStage(Stage stage) {
        stage.setMinWidth(750);
        stage.setMinHeight(550);
        stage.setMaxWidth(800);
        stage.setMaxHeight(700);
    }

    // Método para oscurecer la imagen del asiento
    private void oscurecerAsiento(Button button) {
        ImageView imageView = (ImageView) button.getGraphic();

        // Aplicar el filtro para oscurecer la imagen
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.20);  // Hace que la imagen sea más oscura

        imageView.setEffect(colorAdjust);
    }


    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado = null;
        Stage stage = (Stage) usuarioLabel.getScene().getWindow();
        CerrarSesion.cerrarSesion(stage, "/resources/styles/styles.css", "/resources/images/logo.png");
    }

    @FXML
    private void filtrarPorAsiento() {
        String tipoSeleccionado = eleccionBox.getValue();
        gridPane.getChildren().clear();

        // Cargar butacas en la cesta
        List<EntradaCesta> butacasEnCesta = utils.CestaStorage.cargarCesta(emailUsuarioLogueado);

        for (Butaca asiento : todosLosAsientos) {
            Button boton = crearAsiento(asiento);
            boolean ocupada = butacasOcupadas.stream()
                    .anyMatch(b -> b.getFila() == asiento.getFila() && b.getColumna() == asiento.getColumna());

            // Verificar si la butaca está en la cesta
            boolean enCesta = butacasEnCesta.stream()
                    .anyMatch(e -> e.getFila() == asiento.getFila()
                            && e.getCol() == asiento.getColumna()
                            && e.getNombreEspectaculo().equals(espectaculoSeleccionado));

            // Aplicar filtro por tipo y oscurecer si está en cesta o no coincide con el filtro
            if ((tipoSeleccionado.equals("VIP") && asiento.getTipo() == 'V') ||
                    (tipoSeleccionado.equals("Estandar") && asiento.getTipo() == 'E')) {

                if (enCesta || ocupada) {
                    oscurecerAsiento(boton);
                    boton.setDisable(true);
                }
            } else {
                oscurecerAsiento(boton);
                boton.setDisable(true);
            }

            gridPane.add(boton, asiento.getColumna(), asiento.getFila());
        }
    }

    //Para mostrar las butacas. Usa método auxiliar para mostrar butacas según ocupadas, vip o estándar
    private void mostrarTodasButacas() {
        gridPane.getChildren().clear();
        butacasOcupadas = butacaDao.obtenerButacasOcupadas(idEspectaculoSeleccionado);
        todosLosAsientos = butacaDao.obtenerTodasButacas(idEspectaculoSeleccionado);
        cestaList = utils.CestaStorage.cargarCesta(emailUsuarioLogueado);

        for (Butaca butaca : todosLosAsientos) {
            Button boton = crearAsiento(butaca);

            // Verificar si la butaca está en la cesta
            boolean enCesta = cestaList.stream()
                    .anyMatch(e -> e.getFila() == butaca.getFila()
                            && e.getCol() == butaca.getColumna()
                            && e.getNombreEspectaculo().equals(espectaculoSeleccionado));

            if (enCesta) {
                oscurecerAsiento(boton);
                boton.setDisable(true);
            }

            gridPane.add(boton, butaca.getColumna(), butaca.getFila());
        }
        eleccionBox.setValue("-");
    }

    // Duro con cojones. Para crear un asiento de un color u otro según si está OCUPADO (ROJO), VIP (AMARILLO) o ESTÁNDAR (VERDE)
    private Button crearAsiento(Butaca butaca) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);

        // Verificar si está ocupada
        boolean ocupada = butacasOcupadas.stream()
                .anyMatch(b -> b.getFila() == butaca.getFila() && b.getColumna() == butaca.getColumna());

        // Verificar si está en la cesta
        boolean enCesta = cestaList.stream()
                .anyMatch(e -> e.getFila() == butaca.getFila()
                        && e.getCol() == butaca.getColumna()
                        && e.getNombreEspectaculo().equals(espectaculoSeleccionado));

        // Verificar si hay reserva temporal
        boolean reservaTemp = false;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);
            String idReservaTemp = idEspectaculoSeleccionado + "_" + idUsuario + "_F" + butaca.getFila() + "-C" + butaca.getColumna();
            reservaTemp = reservaDao.existeReservaTemporal(idReservaTemp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (ocupada || enCesta || reservaTemp) {
            setImagenAsientos(imageView, "occupied");
            button.setDisable(true);
        } else if (butaca.getTipo() == 'V') {
            setImagenAsientos(imageView, "vip");
        } else {
            setImagenAsientos(imageView, "standard");
        }

        button.setGraphic(imageView);
        button.setId("F" + butaca.getFila() + "_C" + butaca.getColumna());

        // Solo permitir acción si no está ocupada, en cesta o con reserva temporal
        if (!ocupada && !enCesta && !reservaTemp) {
            button.setOnAction(event ->
                    handleSeleccionAsientos(button, butaca.getFila(), butaca.getColumna())
            );
        }

        return button;
    }

    // Método para setear la imagen según el tipo de asiento. La ruta siempre es la misma, así que usamos un final y lo creamos al principio del controlador.
    private void setImagenAsientos(ImageView imageView, String tipoAsiento) {
        String imagePath = switch (tipoAsiento.toLowerCase()) {
            case "vip" -> ASIENTOS_VIP;
            case "occupied" -> ASIENTOS_OCUPADOS;
            default -> ASIENTOS_ESTANDAR;
        };

        //Seteamos la imagen según la ruta.
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Método para manejar la selección del asiento.
    private void handleSeleccionAsientos(Button button, int fila, int columna) {
        // Verificar reservas temporales
        boolean reservaTemp = false;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);
            String idReservaTemp = idEspectaculoSeleccionado + "_" + idUsuario + "_F" + fila + "-C" + columna;
            reservaTemp = reservaDao.existeReservaTemporal(idReservaTemp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (reservaTemp) {
            button.setDisable(true);
            return;
        }

        // Verifica si las listas están inicializadas
        if (butacasOcupadas == null || cestaList == null) {
            System.out.println("Error: Las listas de butacas o la cesta no están inicializadas.");
            return;
        }

        // Verifica si el asiento está ocupado o ya en la cesta
        boolean ocupada = butacasOcupadas.stream()
                .anyMatch(b -> b.getFila() == fila && b.getColumna() == columna);
        boolean yaEnCesta = cestaList.stream()
                .anyMatch(ec -> ec.getFila() == fila && ec.getCol() == columna &&
                        ec.getNombreEspectaculo().equals(espectaculoSeleccionado));

        if (ocupada || yaEnCesta) {
            System.out.println("Asiento ocupado o ya en la cesta - no se puede seleccionar");
            button.setDisable(true);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);

            // 1. Contar reservas activas para este usuario y espectáculo
            int reservasActivas = reservaDao.contarReservasActivasPorUsuarioYEspectaculo(idUsuario, idEspectaculoSeleccionado);

            // 2. Contar entradas en la cesta para este espectáculo
            long enCesta = cestaList.stream()
                    .filter(e -> e.getIdEspectaculo().equals(idEspectaculoSeleccionado))
                    .count();

            // 3. Verificar el límite total (reservas activas + en cesta)
            if (reservasActivas + enCesta >= 4) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Límite alcanzado");
                alert.setContentText(String.format(
                        "Ya tienes %d entradas reservadas y %d en la cesta para este espectáculo.\n" +
                                "El límite es de 4 entradas por espectáculo.",
                        reservasActivas, enCesta));
                alert.show();
                return;
            }

            // Determinar si es VIP y establecer precio
            boolean isVip = butacasVIP.stream()
                    .anyMatch(b -> b.getFila() == fila && b.getColumna() == columna);
            double precio = isVip ? espectaculo.getPrecioVip() : espectaculo.getPrecioBase();

            if (cestaController != null) {
                // Crear reserva temporal
                Reservas reservaTempo = new Reservas();
                reservaTempo.setId_butaca("F" + fila + "-C" + columna);
                reservaTempo.setId_espectaculo(idEspectaculoSeleccionado);
                reservaTempo.setId_usuario(idUsuario);
                reservaTempo.setId_reserva(idEspectaculoSeleccionado + "_" + idUsuario + "_F" + fila + "-C" + columna);

                reservasDao.registrarReservasTEMP(reservaTempo);
                cestaController.agregarEntrada(espectaculoSeleccionado, idEspectaculoSeleccionado,
                        fila, columna, precio, isVip);
                System.out.println("Asiento añadido a la cesta y registrado temporalmente.");

                // Actualizar vista según filtro del eleccionBox
                if ("VIP".equals(eleccionBox.getValue()) || "Estandar".equals(eleccionBox.getValue())) {
                    filtrarPorAsiento();
                } else {
                    mostrarTodasButacas();
                }
            } else {
                System.err.println("Error: La cesta no está inicializada.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setContentText("No se pudo verificar las reservas existentes.");
            alert.show();
        }
    }

    @FXML
    private void mostrarTodas(ActionEvent event) {
        mostrarTodasButacas();
    }


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


    public void volverCartelera(ActionEvent actionEvent) {
        cambioEscena("../views/cartelera.fxml");
    }



    private void cambioEscena(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
            Parent root = loader.load();

            Stage stage = (Stage) usuarioLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("../resources/styles/styles.css").toExternalForm());
            stage.setTitle("CINES JRC");

            Image icon = new Image(getClass().getResourceAsStream("../resources/images/logo.png"));
            stage.getIcons().add(icon);

            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cesta(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cesta.fxml"));
            Parent root = loader.load();
            CestaController cestaController = loader.getController();
            cestaController.setEmailUsuarioLogueado(emailUsuarioLogueado);


            Stage stage = (Stage) usuarioLabel.getScene().getWindow();
            Scene scene = new Scene(root);

            scene.getStylesheets().add(getClass().getResource("../resources/styles/styles.css").toExternalForm());
            stage.setTitle("CINES JRC");

            Image icon = new Image(getClass().getResourceAsStream("../resources/images/logo.png"));
            stage.getIcons().add(icon);

            Transitions transitions = new Transitions();
            transitions.fadeInScene(root);

            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}