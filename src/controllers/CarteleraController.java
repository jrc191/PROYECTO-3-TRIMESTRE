package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static controllers.LoginController.*;

public class CarteleraController {

    private static String emailUsuarioLogueado = getUsuarioLogueadoEmail();

    @FXML
    private Label usuarioLabel;
    @FXML
    private HBox contenedorEspectaculos;
    @FXML
    private TextField filtroNombreField;
    @FXML
    private Label mensajeLabel; // para mostrar mensajes de error, correctos y otra información
    @FXML
    private Label izquierdaBtn, derechaBtn;
    @FXML
    private ScrollPane scrollEspectaculos;



    @FXML
    public void initialize() {

        //POR FIN, para quitar la puñetera línea blanca de la derecha (2 horas con esto, no es broma)
        scrollEspectaculos.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");

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



    private void cargarEspectaculos() {
        cargarEspectaculos(null); // Cargamos todo por defecto
    }

    //Bendita sobrecarga de métodos
    private void cargarEspectaculos(String nombreFiltro) {
        contenedorEspectaculos.getChildren().clear(); // Borramos el contenido para filtrar en caso de que haya filtros previos

        List<Espectaculo> espectaculos;
        //si no se introduce filtro, llamamos al método sin filtro
        if (nombreFiltro == null || nombreFiltro.isEmpty()) {
            espectaculos = obtenerDatosDesdeOracle();
        } else {
            espectaculos = obtenerDatosDesdeOracle(nombreFiltro);
        }

        //si después de cargar los espectáculos no hay resultados, mostramos el mensaje de error que no se han encontrado
        if (espectaculos.isEmpty()) {
            if (nombreFiltro != null && !nombreFiltro.isEmpty()) {
                mensajeLabel.setVisible(true);
                mensajeLabel.setText("No se encontraron espectáculos con el nombre: '" + nombreFiltro + "'");
            }
        } else {
            //si encuentra resultados, los muestra
            for (Espectaculo esp : espectaculos) {
                contenedorEspectaculos.getChildren().add(crearTarjetaEspectaculo(esp));
            }
        }
    }

    //método para cargar TODOS los datos de espectáculos
    private List<Espectaculo> obtenerDatosDesdeOracle() {
        List<Espectaculo> espectaculosList = new ArrayList<>();

        //comprobamos conexion
        if (checkConexion()) {

            //query para devolver toda la información
            String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip FROM ESPECTACULOS";

            try (PreparedStatement pstmt = conexion().prepareStatement(query);
                 ResultSet rs = pstmt.executeQuery()) {

                //creamos un espectaculo por cada resultado de la query
                while (rs.next()) {
                    Espectaculo espectaculo = new Espectaculo(
                            rs.getString("id_espectaculo"),
                            rs.getString("nombre"),
                            rs.getDate("fecha").toLocalDate(), // Conversión a LocalDate
                            rs.getDouble("precio_base"),
                            rs.getDouble("precio_vip")
                    );
                    //y lo añadimos
                    espectaculosList.add(espectaculo);
                }

            } catch (SQLException e) {
                // Manejo de errores
                System.err.println("Error al cargar espectáculos: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error en la base de datos", e);
            }
        } else {
            throw new IllegalStateException("No hay conexión a la base de datos");
        }

        return espectaculosList;
    }

    //método para cargar los datos según el nombre
    private List<Espectaculo> obtenerDatosDesdeOracle(String nombre) {
        List<Espectaculo> espectaculosList = new ArrayList<>();

        //comprobamos conexion
        if (checkConexion()) {

            //query para devolver toda la información según el nombre del espectaculo
            String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip " +
                    "FROM ESPECTACULOS WHERE LOWER(nombre) LIKE LOWER(?)";

            try (PreparedStatement pstmt = conexion().prepareStatement(query)) {
                // Set the parameter for the query
                pstmt.setString(1, "%" + nombre + "%");

                try (ResultSet rs = pstmt.executeQuery()) {
                    //creamos un espectaculo por cada resultado de la query
                    while (rs.next()) {
                        Espectaculo espectaculo = new Espectaculo(
                                rs.getString("id_espectaculo"),
                                rs.getString("nombre"),
                                rs.getDate("fecha").toLocalDate(), // Conversión a LocalDate
                                rs.getDouble("precio_base"),
                                rs.getDouble("precio_vip")
                        );
                        //y lo añadimos
                        espectaculosList.add(espectaculo);
                    }
                }

            } catch (SQLException e) {
                // Manejo de errores
                System.err.println("Error al cargar espectáculos: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Error en la base de datos", e);
            }
        } else {
            throw new IllegalStateException("No hay conexión a la base de datos");
        }

        return espectaculosList;
    }


    //método para ir añadiendo espectáculos a partir de un objeto espectáculo creado con los resultados de la BBDD
    //yo no sé ni cuanto tiempo me ha llevado esto ya, pero funciona :)

    private Node crearTarjetaEspectaculo(Espectaculo esp) {
        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        tarjeta.setPrefSize(300, 200);

        Label nombre = new Label(esp.getNombre());
        nombre.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16;");

        Label fecha = new Label("Fecha: " + esp.getFecha());
        Label precioBase = new Label("Precio base: $" + esp.getPrecioBase()+" €");
        Label precioVip = new Label("Precio VIP: " + esp.getPrecioVip()+" €");
        fecha.setStyle("-fx-text-fill: #a0a0a0;");
        precioBase.setStyle("-fx-text-fill: #e0e0e0;");
        precioVip.setStyle("-fx-text-fill: #e0e0e0;");

        tarjeta.getChildren().addAll(nombre, fecha, precioBase, precioVip);
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
    }

    //método para filtrar por nombre. usa el método auxiliar sobrecargado de cargarEspectaculos con parámetro de filtro para filtrar
    public void filtrarPorNombre(ActionEvent actionEvent) {
        String nombreFiltro = filtroNombreField.getText().trim();
        cargarEspectaculos(nombreFiltro);
    }

    //método para mostrar todos los espectáculos. usa el método auxiliar de cargarEspectaculos sin parámetro.
    public void mostrarTodas(ActionEvent actionEvent) {
        filtroNombreField.clear();
        mensajeLabel.setVisible(false);
        cargarEspectaculos();
    }

    public TextField getFiltroNombreField(){
        return filtroNombreField;
    }

    public void setFiltroNombre(TextField filtroNombre) {
        this.filtroNombreField = filtroNombre;
    }
}

class Espectaculo{
    private String id;
    private String nombre;
    private LocalDate fecha;
    private double pbase;
    private double pvip;

    public Espectaculo(String id, String nombre, LocalDate fecha, double pbase, double pvip){
        this.id=id;
        this.nombre=nombre;
        this.fecha=fecha;
        this.pbase=pbase;
        this.pvip=pvip;
    }

    public String getNombre() {
        return nombre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getPrecioBase() {
        return pbase;
    }

    public double getPrecioVip() {
        return pvip;
    }
}