package controllers;

import dao.DatabaseConnection;
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
import javafx.stage.Stage;
import models.Espectaculo;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


import static controllers.LoginController.*;

public class CarteleraController {

    //Parámetro email usuario logueado del LoginController
    private static String emailUsuarioLogueado = getUsuarioLogueadoEmail();

    //Implementamos Interfaz Dao
    private EspectaculoDaoI espectaculoDao;

    public void setEspectaculoDAO(EspectaculoDaoI espectaculoDAO) {
        this.espectaculoDao = espectaculoDAO;
    }

    @FXML
    private Label usuarioLabel;
    @FXML
    private HBox contenedorEspectaculos;
    @FXML
    private TextField filtroNombreField;

    @FXML
    private DatePicker filtroFechaField;
    @FXML
    private Label mensajeLabel; // para mostrar mensajes de error, correctos y otra información
    @FXML
    private Label izquierdaBtn, derechaBtn;
    @FXML
    private ScrollPane scrollEspectaculos;
    @FXML
    private Label messageLabelReserva;


    @FXML
    public void initialize() {

        try {
            Connection conn = DatabaseConnection.getConnection(); // método estático para obtener la conexión
            setEspectaculoDAO(new EspectaculoDaoImpl(conn));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (espectaculoDao == null) {
            mensajeLabel.setText("Error al conectar con la base de datos");
            return;
        }

        //POR FIN, para quitar la puñetera línea blanca de la derecha (2 horas con esto, no es broma)
        scrollEspectaculos.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242; ");
        scrollEspectaculos.setFitToWidth(true);

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


    }

    //Para habilitar el carrusel cuando es necesario (cuando hay resultados). Cuando no los hay, deshabilitarlo
    private void habilitarScroll(boolean habilitar) {

        izquierdaBtn.setVisible(habilitar);
        derechaBtn.setVisible(habilitar);

        // true : habilita el arrastrar con ratón, false : deshabilita el arrastrar con ratón. False cuando no hay resultados, true cuando los hay.
        scrollEspectaculos.setPannable(habilitar);
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
        if ( (nombreFiltro == null || nombreFiltro.isEmpty() ) && date == null) {
            espectaculos = espectaculoDao.obtenerTodos();
        } else {
            //filtrar por fecha
            if (nombreFiltro == null || nombreFiltro.isEmpty()){
                espectaculos = espectaculoDao.obtenerPorFecha(date);
            }
            //filtrar por nombre
            else{
                espectaculos = espectaculoDao.obtenerPorNombre(nombreFiltro);
            }


        }
        habilitarScroll(false); //deshabilitamos el carrusel cuando está vacío

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
            } else if (date !=null) {
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

            if (contenedorEspectaculos.getChildren().size()>2){
                habilitarScroll(true); //habilitamos el carrusel cuando hay más de 2 resultados
            }
        }
    }


    //método para ir añadiendo espectáculos en forma de tarjeta a partir de un objeto espectáculo creado
    // con los resultados de la BBDD. Yo no sé ni cuanto tiempo me ha llevado esto ya, pero funciona :)
    //Para mi yo del futuro: no te metas en más fregaos por mejorar la estética, que mejoras una cosa
    // y te acabas cargando 10.

    private Node crearTarjetaEspectaculo(Espectaculo esp) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        tarjeta.setPrefSize(300, 200);

        // Guardar el tamaño original para restaurarlo después
        final double originalWidth = tarjeta.getPrefWidth();
        final double originalHeight = tarjeta.getPrefHeight();

        Label nombre = new Label(esp.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16;");

        Label fecha = new Label("Fecha: " + esp.getFecha().getDayOfMonth()+"-"+esp.getFecha().getMonthValue()+"-"+esp.getFecha().getYear());
        Label precioBase = new Label("Precio base: " + esp.getPrecioBase()+" €");
        Label precioVip = new Label("Precio VIP: " + esp.getPrecioVip()+" €");
        fecha.setStyle("-fx-text-fill: #a0a0a0;");
        precioBase.setStyle("-fx-text-fill: #e0e0e0;");
        precioVip.setStyle("-fx-text-fill: #e0e0e0;");

        // Botón de reserva. Solo visible cuando el cursor está encima
        Button reservarBtn = new Button("Reservar entradas");
        reservarBtn.setStyle("-fx-background-color: #4e3a74; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 5 10;");
        reservarBtn.setVisible(false);
        reservarBtn.setCursor(Cursor.HAND);
        VBox.setMargin(reservarBtn, new Insets(10, 0, 0, 0)); // Margen superior para el botón

        reservarBtn.setOnAction(event -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/reserva.fxml"));
                FXMLLoader cestaLoader = new FXMLLoader(getClass().getResource("../views/cesta.fxml"));
                Parent cestaRoot = cestaLoader.load();
                CestaController cestaController = cestaLoader.getController();
                cestaController.setEmailUsuarioLogueado(emailUsuarioLogueado);
                cestaController.setEspectaculoSeleccionado(esp.getNombre());
                cestaController.setIdEspectaculoSeleccionado(esp.getId());

                loader.setControllerFactory(clazz -> {
                    return new ReservasController(
                            getUsuarioLogueadoEmail(),
                            esp.getNombre(),
                            esp.getId(),
                            cestaController
                    );
                });

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

        tarjeta.getChildren().addAll(nombre, fecha, precioBase, precioVip, reservarBtn);

        // Evento para cuando el ratón entra en la tarjeta
        tarjeta.setOnMouseEntered(event -> {
            tarjeta.setPrefWidth(originalWidth * 1.05); // Aumentar tamaño un 5% para dar sensación de focus
            tarjeta.setPrefHeight(originalHeight * 1.05);
            tarjeta.setStyle("-fx-background-color: #3a427c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 15, 0, 0, 7);");
            reservarBtn.setVisible(true); //hacemos visible el botón cuando el ratón pasa por encima
        });

        // Evento para cuando el ratón sale de la tarjeta
        tarjeta.setOnMouseExited(event -> {
            tarjeta.setPrefWidth(originalWidth);
            tarjeta.setPrefHeight(originalHeight);
            tarjeta.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
            reservarBtn.setVisible(false); //hacemos invisible de nuevo el botón de reserva
        });

        return tarjeta;
    }

    //método para cerrar sesión y volver al login
    //bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login

    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado=null;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/login.fxml"));
            Parent root = loader.load();

            // Obtener el Stage actual, con utilizar cualquier atributo fxml o nodo sirve.
            Stage stage = (Stage) contenedorEspectaculos.getScene().getWindow();

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



    //A IMPLEMENTAR
    public void filtrarPorFecha(ActionEvent actionEvent) {
        LocalDate fecha = filtroFechaField.getValue();
        cargarEspectaculos(null, fecha);

    }

    //método para filtrar por nombre. usa el método auxiliar sobrecargado de cargarEspectaculos con parámetro de filtro para filtrar
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

    public TextField getFiltroNombreField(){
        return filtroNombreField;
    }

    public void setFiltroNombre(TextField filtroNombre) {
        this.filtroNombreField = filtroNombre;
    }
}
