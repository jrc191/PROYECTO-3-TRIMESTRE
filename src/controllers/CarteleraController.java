package controllers;

import javafx.scene.effect.DropShadow;
import javafx.scene.input.DragEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.control.Label;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.util.Duration;
import javafx.scene.image.ImageView;
import utils.DatabaseConnection;
import dao.EspectaculoDaoI;
import dao.impl.EspectaculoDaoImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Espectaculo;
import utils.Transitions;
import utils.CerrarSesion;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class CarteleraController {

    @FXML private Label usuarioLabel;
    @FXML private HBox contenedorEspectaculos;
    @FXML private TextField filtroNombreField;
    @FXML private DatePicker filtroFechaField;
    @FXML private Label mensajeLabel; // para mostrar mensajes de error, correctos y otra información
    @FXML private Label izquierdaBtn, derechaBtn; //Botones del ScrollPane
    @FXML private ScrollPane scrollEspectaculos;
    @FXML private Label messageLabelReserva;

    //Parámetro email usuario logueado del LoginController
    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();

    //Implementamos Interfaz Dao
    private EspectaculoDaoI espectaculoDao;

    public void setEspectaculoDAO(EspectaculoDaoI espectaculoDAO) {
        this.espectaculoDao = espectaculoDAO;
    }

    @FXML
    public void initialize() {

        try {
            Connection conn = DatabaseConnection.getConnection();
            setEspectaculoDAO(new EspectaculoDaoImpl(conn)); //Inicializamos los espectaculos
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (espectaculoDao == null) {
            mensajeLabel.setText("Error al conectar con la base de datos.");
            return;
        }

        //POR FIN, para quitar la puñetera línea blanca de la derecha (2 horas con esto, no es broma)
        scrollEspectaculos.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242; ");


        // Habilitar scroll inicialmente
        habilitarScroll(true);

        //creamos el label para usarlo después en caso de ser necesario
        if (mensajeLabel == null) {
            mensajeLabel = new Label();
            mensajeLabel.setStyle("-fx-text-fill: red;");
            mensajeLabel.setVisible(false);
        }

        //para mostrar el email del usuario logueado al lado del botón de cerrar sesión
        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
        }


        //métodos para crear el carrusel de espectáculos y cargarlos
        agregarListenersCarrusel();
        cargarEspectaculos();
    }



    //Jodido con cojones, lo he entendido por un rayo de inspiración
    private void agregarListenersCarrusel() {

        //esto lo que hace es que en caso de que el mouse esté en el scrollpane, no se muestre (baje la opacidad) de las flechas
        scrollEspectaculos.setOnMouseEntered(e -> {
            izquierdaBtn.setOpacity(0);
            derechaBtn.setOpacity(0);
        });

        //esto lo que hace es que en caso de que el mouse esté encima de las flechas, se muestren (suba la opacidad) de las flechas
        scrollEspectaculos.setOnMouseExited(e -> {
            izquierdaBtn.setOpacity(1);
            derechaBtn.setOpacity(1);
        });

        //más o menos trivial, un mouseEvent para que al pulsar la flecha izquierda, se mueva en 0.2 horizontalmente a la izquierda,
        //y en 0.2 a la derecha si se pulsa a la derecha
        izquierdaBtn.setOnMouseClicked(e -> scrollEspectaculos.setHvalue(scrollEspectaculos.getHvalue() - 0.2));
        derechaBtn.setOnMouseClicked(e -> scrollEspectaculos.setHvalue(scrollEspectaculos.getHvalue() + 0.2));

        scrollEspectaculos.setOnMouseDragged(e->{
            scrollEspectaculos.setVvalue(0);
        });

        scrollEspectaculos.setOnMouseDragOver(e->{
            scrollEspectaculos.setVvalue(0);
        });

        scrollEspectaculos.setOnMouseEntered(e->{
            scrollEspectaculos.setVvalue(0);
        });

        scrollEspectaculos.setOnDragDetected(e->{
            scrollEspectaculos.setVvalue(0);
        });

        scrollEspectaculos.setOnMouseDragEntered(e->{
            scrollEspectaculos.setVvalue(0);
        });

        scrollEspectaculos.setOnMouseDragExited(e->{
            scrollEspectaculos.setVvalue(0);
        });

        scrollEspectaculos.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (event.getDeltaY() != 0) {
                event.consume();  // Consume vertical scroll events
            }
        });
        
    }

    //Para habilitar el carrusel cuando es necesario (cuando hay resultados). Cuando no los hay, deshabilitarlo
    private void habilitarScroll(boolean habilitar) {

        izquierdaBtn.setVisible(habilitar);
        derechaBtn.setVisible(habilitar);

        // true : habilita el arrastrar con ratón, false : deshabilita el arrastrar con ratón.
        // False cuando no hay resultados, true cuando los hay.
        scrollEspectaculos.setPannable(habilitar);


        scrollEspectaculos.setFitToHeight(true);

    }

    // Método de filtro por fecha auxiliar. Usa el método auxiliar sobrecargado de cargarEspectaculos con parámetro de filtro para filtrar
    public void filtrarPorFecha(ActionEvent actionEvent) {
        LocalDate fecha = filtroFechaField.getValue();
        cargarEspectaculos(null, fecha);

    }

    // Método para filtrar por nombre. Usa el método auxiliar sobrecargado de cargarEspectaculos con parámetro de filtro para filtrar
    public void filtrarPorNombre(ActionEvent actionEvent) {
        String nombreFiltro = filtroNombreField.getText().trim();
        cargarEspectaculos(nombreFiltro, null);
    }

    //método para mostrar todos los espectáculos. usa el método auxiliar de cargarEspectaculos sin parámetro.
    public void mostrarTodas(ActionEvent actionEvent) {
        filtroNombreField.clear();
        mensajeLabel.setVisible(false);
        habilitarScroll(true); // habilitamos el scroll cuando se muestren todos los espectaculos.
        cargarEspectaculos();
    }


    private void cargarEspectaculos() {
        cargarEspectaculos(null, null); // Cargamos por defecto
    }

    //Bendita sobrecarga de métodos
    private void cargarEspectaculos(String nombreFiltro, LocalDate date) {
        contenedorEspectaculos.getChildren().clear(); // Borramos el contenido para filtrar en caso de que haya filtros previos

        scrollEspectaculos.setHvalue(0); // Reseteo del scroll a la izquierda

        List<Espectaculo> espectaculos;
        //si no se introduce filtro, llamamos al método sin filtro
        if ((nombreFiltro == null || nombreFiltro.isEmpty()) && date == null) {
            espectaculos = espectaculoDao.obtenerTodos();
        } else {
            //filtrar por fecha
            if (nombreFiltro == null || nombreFiltro.isEmpty()) {
                espectaculos = espectaculoDao.obtenerPorFecha(date);
            }
            //filtrar por nombre
            else {
                espectaculos = espectaculoDao.obtenerPorNombre(nombreFiltro);
            }


        }
        habilitarScroll(false); //deshabilitamos el carrusel del scrollpane cuando está vacío

        //si después de cargar los espectáculos no hay resultados, mostramos el mensaje de error que no se han encontrado
        if (espectaculos.isEmpty()) {

            if (nombreFiltro != null && !nombreFiltro.isEmpty()) {
                //mostramos mensaje de que no se han encontrado resultados
                mensajeLabel.setVisible(true);
                mensajeLabel.setText("No se encontraron espectáculos con el nombre: '" + nombreFiltro + "'");
                mensajeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                mensajeLabel.setAlignment(Pos.CENTER);

                //para centrar el mensaje
                VBox contenedorMensaje = new VBox(mensajeLabel);
                contenedorMensaje.setAlignment(Pos.CENTER);
                contenedorMensaje.setPrefHeight(scrollEspectaculos.getHeight());
                contenedorMensaje.setPrefWidth(scrollEspectaculos.getWidth());

                // Añadir el contenedor del mensaje contenedor de espectaculos
                contenedorEspectaculos.getChildren().add(contenedorMensaje);
            } else if (date != null) {
                //mostramos mensaje de que no se han encontrado resultados
                mensajeLabel.setVisible(true);
                mensajeLabel.setText("No se encontraron espectáculos con la fecha: '" + date.toString() + "'");
                mensajeLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
                mensajeLabel.setAlignment(Pos.CENTER);

                //para centrar el mensaje
                VBox contenedorMensaje = new VBox(mensajeLabel);
                contenedorMensaje.setAlignment(Pos.CENTER);
                contenedorMensaje.setPrefHeight(scrollEspectaculos.getHeight());
                contenedorMensaje.setPrefWidth(scrollEspectaculos.getWidth());

                // Añadir el contenedor del mensaje contenedor de espectaculos
                contenedorEspectaculos.getChildren().add(contenedorMensaje);

            }

        } else {

            //si encuentra resultados, los muestra mediante tarjetas (puntazo lo de las tarjetas)
            for (Espectaculo esp : espectaculos) {
                contenedorEspectaculos.getChildren().add(crearTarjetaEspectaculo(esp));
            }

            if (contenedorEspectaculos.getChildren().size() > 2) {
                habilitarScroll(true); //habilitamos el carrusel cuando hay más de 2 resultados
            }
        }
    }


    // Método para ir añadiendo espectáculos en forma de tarjeta a partir de un objeto espectáculo creado
    // con los resultados de la BBDD. Yo no sé ni cuanto tiempo me ha llevado esto ya, pero funciona :)
    // Para mi yo del futuro: no te metas en más fregaos por mejorar la estética, que mejoras una cosa
    // y te acabas cargando 10. AL FINAL LO HE TOCADO MÁS AÚN PA METER LO DE LAS IMAGENES :(

    private Node crearTarjetaEspectaculo(Espectaculo esp) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-background-radius: 15;");
        tarjeta.setPrefSize(300, 400);
        tarjeta.setMinSize(300, 400);

        StackPane stackPane = new StackPane();
        stackPane.setPrefSize(300, 400);

        //Creamos la imagen
        ImageView imageView = new ImageView();
        try {
            String imagePath = "/resources/images/espectaculos/" + esp.getId()+ ".png";
            Image image = new Image(getClass().getResourceAsStream(imagePath));

            imageView.setImage(image);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);

            // Ajustar tamaño manteniendo relación de aspecto
            if (image.getWidth() / image.getHeight() > 300.0/400.0) {
                imageView.setFitWidth(300);
            } else {
                imageView.setFitHeight(400);
            }

            // Crear un rectángulo con bordes redondeados
            Rectangle clip = new Rectangle(300, 400);
            clip.setArcWidth(15);
            clip.setArcHeight(15);
            imageView.setClip(clip);

            // Fondo para áreas transparentes (opcional)
            Rectangle bg = new Rectangle(300, 400);
            bg.setFill(Color.TRANSPARENT);
            bg.setArcWidth(15);
            bg.setArcHeight(15);

            StackPane imageContainer = new StackPane(bg, imageView);
            imageContainer.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.3)));
            imageContainer.setAlignment(Pos.CENTER);
        } catch (Exception e) {
            // Imagen por defecto
            Image defaultImage = new Image(getClass().getResourceAsStream("/resources/images/espectaculos/default-show.png"));
            imageView.setImage(defaultImage);
            imageView.setFitWidth(300);
            imageView.setFitHeight(400);
            imageView.setPreserveRatio(false);
        }

        //Vbox con info del espectáculo
        VBox infoBox = new VBox(10);
        infoBox.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15;");
        infoBox.setPrefSize(300, 400);
        infoBox.setOpacity(0); // Inicialmente invisible

        Label nombre = new Label(esp.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16;");

        Label fecha = new Label("Fecha: " + esp.getFecha().getDayOfMonth() + "-" + esp.getFecha().getMonthValue() + "-" + esp.getFecha().getYear());
        Label precioBase = new Label("Precio base: " + esp.getPrecioBase() + " €");
        Label precioVip = new Label("Precio VIP: " + esp.getPrecioVip() + " €");
        fecha.setStyle("-fx-text-fill: #a0a0a0;");
        precioBase.setStyle("-fx-text-fill: #e0e0e0;");
        precioVip.setStyle("-fx-text-fill: #e0e0e0;");

        Button reservarBtn = new Button("Reservar entradas");
        reservarBtn.setStyle("-fx-background-color: #4e3a74; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
        reservarBtn.setCursor(Cursor.HAND);
        VBox.setMargin(reservarBtn, new Insets(10, 0, 0, 0));

        reservarBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/reserva.fxml"));
                FXMLLoader cestaLoader = new FXMLLoader(getClass().getResource("../views/cesta.fxml"));

                Parent cestaRoot = cestaLoader.load();
                CestaController cestaController = cestaLoader.getController();
                cestaController.setEmailUsuarioLogueado(emailUsuarioLogueado);

                loader.setControllerFactory(clazz ->
                        new ReservasController(getUsuarioLogueadoEmail(), esp.getNombre(), esp.getId(), cestaController, esp
                        ));

                Parent root = loader.load();
                ReservasController controller = loader.getController();

                controller.fadeInScene(root);
                Stage stage = (Stage) contenedorEspectaculos.getScene().getWindow();
                controller.configureStage(stage);

                stage.setScene(new Scene(root));
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
                if (messageLabelReserva != null) {
                    messageLabelReserva.setText("Error al cargar la vista de reservas");
                }
            }
        });

        infoBox.getChildren().addAll(nombre, fecha, precioBase, precioVip, reservarBtn);
        stackPane.getChildren().addAll(imageView, infoBox);

        // Efectos de pasar el ratón
        stackPane.setOnMouseEntered(event -> {

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), imageView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), infoBox);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeOut, fadeIn);
            parallelTransition.play();
        });

        stackPane.setOnMouseExited(event -> {

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), imageView);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), infoBox);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            ParallelTransition parallelTransition = new ParallelTransition(fadeIn, fadeOut);
            parallelTransition.play();
        });

        tarjeta.getChildren().add(stackPane);
        return tarjeta;
    }

    //método para cerrar sesión y volver al login
    //bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login

    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado = null;
        Stage stage = (Stage) contenedorEspectaculos.getScene().getWindow();
        CerrarSesion.cerrarSesion(stage, "/resources/styles/styles.css", "/resources/images/logo.png");
    }

    //GETTERS Y SETTERS DE FILTROS. Por si acaso hacen falta en otro momento
    public TextField getFiltroNombreField() {
        return filtroNombreField;
    }

    public void setFiltroNombre(TextField filtroNombre) {
        this.filtroNombreField = filtroNombre;
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

    public void reservasUsuario(ActionEvent actionEvent) {
        Transitions transitions = new Transitions();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/reservasUsuarios.fxml"));
        Stage stage = (Stage) usuarioLabel.getScene().getWindow();
        ReservasUsuarioController reservasUsuarioController = loader.getController();
        transitions.cambioEscena(stage, "/views/reservasUsuarios.fxml","../resources/styles/styles.css", "CINES JRC - RESERVAS", "../resources/images/logo.png", reservasUsuarioController);
    }

    public static String getUsuarioLogueadoEmail() {
        String usuarioLogueadoEmail = LoginController.getUsuarioLogueadoEmail();
        return usuarioLogueadoEmail;
    }
}
