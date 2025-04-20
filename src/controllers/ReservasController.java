package controllers;

import dao.ButacaDaoI;
import dao.DatabaseConnection;
import dao.impl.ButacaDaoImpl;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Butaca;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static controllers.LoginController.*;

public class ReservasController {

    @FXML
    public Label espectaculoLabel;
    @FXML
    private Label usuarioLabel;
    @FXML
    private GridPane gridPane;
    @FXML
    private ChoiceBox<String> eleccionBox;

    private static final String ASIENTOS_OCUPADOS = "/resources/images/BUTACA-ROJA.png";
    private static final String ASIENTOS_VIP = "/resources/images/BUTACA-AMARILLA.png";
    private static final String ASIENTOS_ESTANDAR = "/resources/images/BUTACA-VERDE.png";

    private List<Butaca> todosLosAsientos;
    private List<Butaca> butacasOcupadas;
    private List<Butaca> butacasVIP;
    private ButacaDaoI butacaDao;

    private String emailUsuarioLogueado;
    private String espectaculoSeleccionado;
    private String idEspectaculoSeleccionado;
    private CestaController cestaController;

    public ReservasController() {
        // Constructor vacío
    }

    public ReservasController(String emailUsuario, String nombreEspectaculo, String idEspectaculo, CestaController cestaController) {
        this.emailUsuarioLogueado = emailUsuario;
        this.espectaculoSeleccionado = nombreEspectaculo;
        this.idEspectaculoSeleccionado = idEspectaculo;
        this.cestaController = cestaController;
    }

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.butacaDao = new ButacaDaoImpl(conn);

            // Inicialización de las listas
            butacasOcupadas = new ArrayList<>();
            butacasVIP = new ArrayList<>();

            // Obtener las butacas ocupadas y VIP
            butacasOcupadas = butacaDao.obtenerButacasOcupadas(idEspectaculoSeleccionado);
            butacasVIP = butacaDao.obtenerButacasVIP();

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

    private void mostrarTodasButacas() {
        gridPane.getChildren().clear();
        butacasOcupadas = butacaDao.obtenerButacasOcupadas(idEspectaculoSeleccionado);
        todosLosAsientos = butacaDao.obtenerTodasButacas(idEspectaculoSeleccionado);

        for (Butaca butaca : todosLosAsientos) {
            Button boton = crearAsiento(butaca);
            gridPane.add(boton, butaca.getColumna(), butaca.getFila());
        }
    }

    private Button crearAsiento(Butaca butaca) {
        Button button = new Button();
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        ImageView imageView = new ImageView();
        imageView.setFitHeight(40);
        imageView.setFitWidth(40);
        imageView.setPreserveRatio(true);

        boolean ocupada = butacasOcupadas.stream()
                .anyMatch(b -> b.getFila() == butaca.getFila() && b.getColumna() == butaca.getColumna());

        if (ocupada) {
            setImagenAsientos(imageView, "occupied");
        } else if (butaca.getTipo() == 'V') {
            setImagenAsientos(imageView, "vip");
        } else {
            setImagenAsientos(imageView, "standard");
        }

        button.setGraphic(imageView);
        button.setId("F" + butaca.getFila() + "_C" + butaca.getColumna());

        button.setOnAction(event ->
                handleSeleccionAsientos(button, butaca.getFila(), butaca.getColumna())
        );

        return button;
    }

    private void setImagenAsientos(ImageView imageView, String tipoAsiento) {
        String imagePath = switch (tipoAsiento.toLowerCase()) {
            case "vip" -> ASIENTOS_VIP;
            case "occupied" -> ASIENTOS_OCUPADOS;
            default -> ASIENTOS_ESTANDAR;
        };

        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            imageView.setImage(image);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleSeleccionAsientos(Button button, int fila, int columna) {

        // Verifica si las listas están inicializadas
        if (butacasOcupadas == null) {
            System.out.println("Error: Las listas de butacas están vacías o no inicializadas.");
            return;
        }

        boolean ocupada = butacasOcupadas.stream()
                .anyMatch(b -> b.getFila() == fila && b.getColumna() == columna);

        if (ocupada) {
            System.out.println("Asiento ocupado - no se puede seleccionar");
        } else {
            boolean isVip = butacasVIP.stream()
                    .anyMatch(b -> b.getFila() == fila && b.getColumna() == columna);

            double precio = isVip ? 15.0 : 10.0;

            if (cestaController != null) {
                cestaController.agregarEntrada(espectaculoSeleccionado, fila, columna, precio, isVip);
                System.out.println("Asiento añadido a la cesta.");
            } else {
                System.out.println("Error: La cesta no está inicializada.");
            }
        }
    }

    @FXML
    private void filtrarPorAsiento() {
        String tipoSeleccionado = eleccionBox.getValue();
        gridPane.getChildren().clear();

        int filas = 10;
        int columnas = 10;

        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {

                int f = fila;
                int c = columna;

                Butaca asiento = getButacaPosicion(fila, columna);
                if (asiento == null) continue;

                boolean ocupada = butacasOcupadas.stream()
                        .anyMatch(b -> b.getFila() == f && b.getColumna() == c);

                Button boton = crearAsiento(asiento);

                // Aquí aplicamos la lógica correctamente
                if ((tipoSeleccionado.equals("VIP") && asiento.getTipo() == 'V') ||
                        (tipoSeleccionado.equals("Estandar") && asiento.getTipo() == 'E'
                        && !ocupada)) {


                } else {
                    // Si no corresponde al tipo seleccionado, lo oscurecemos
                    oscurecerAsiento(boton);
                    boton.setDisable(true);  // Deshabilitar si no es del tipo seleccionado
                }

                gridPane.add(boton, columna, fila);
            }
        }
    }


    // Método para oscurecer la imagen del asiento
    private void oscurecerAsiento(Button button) {
        ImageView imageView = (ImageView) button.getGraphic();

        // Aplicar el filtro para oscurecer la imagen
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.5);  // Hace que la imagen sea más oscura

        imageView.setEffect(colorAdjust);
    }


    private Butaca getButacaPosicion(int fila, int columna) {
        return todosLosAsientos.stream()
                .filter(a -> a.getFila() == fila && a.getColumna() == columna)
                .findFirst().orElse(null);
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

    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado = null;
        cambioEscena("../views/login.fxml");
    }

    public void volverCartelera(ActionEvent actionEvent) {
        cambioEscena("../views/cartelera.fxml");
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

    private void cambioEscena(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
            Parent root = loader.load();

            Stage stage = (Stage) usuarioLabel.getScene().getWindow();
            Scene scene = new Scene(root);
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

    @FXML
    private void cesta(ActionEvent event) {
        System.out.println("Botón 'Cesta' presionado (placeholder).");
        cambioEscena("/views/cesta.fxml");
    }


}
