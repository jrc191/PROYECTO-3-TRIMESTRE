package controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CestaController {
    @FXML
    private VBox contenedorEntradas;
    @FXML
    private Label totalLabel;
    @FXML
    public Label espectaculoLabel;
    @FXML
    private Label usuarioLabel;

    // Parámetros usados para cerrar sesión, reservar ... entre otros
    private String emailUsuarioLogueado;
    private String espectaculoSeleccionado;
    private String idEspectaculoSeleccionado;
    public DatePicker filtroFechaField;
    public TextField filtroNombreField;

    private List<EntradaCesta> entradas = new ArrayList<>();
    private double total = 0.0; //precio total de las entradas de la cesta

    public void initialize() {
        // Mostrar el email del usuario logueado

        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
        }


        actualizarCesta();
    }

    public void agregarEntrada(String nombreEspectaculo, int fila, int col, double precio, boolean esVip) {
        EntradaCesta entrada = new EntradaCesta(nombreEspectaculo, fila, col, precio, esVip);
        entradas.add(entrada);
        total += precio;
        actualizarCesta(); // Refresh the cart UI
    }

    //Para devolver la cesta en otros controladores
    private CestaController getOrCreateCestaController() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/cesta.fxml"));
            Parent root = loader.load();
            CestaController cestaController = loader.getController();
            cestaController.setEmailUsuarioLogueado(emailUsuarioLogueado);
            cestaController.setEspectaculoSeleccionado(espectaculoSeleccionado);
            cestaController.setIdEspectaculoSeleccionado(idEspectaculoSeleccionado);
            return cestaController;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void actualizarCesta() {
        contenedorEntradas.getChildren().clear();

        for (EntradaCesta entrada : entradas) {
            HBox entradaBox = new HBox(10);
            entradaBox.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 10;");

            VBox infoBox = new VBox(5);
            Label nombreLabel = new Label(entrada.getNombreEspectaculo());
            nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 14px;");

            Label detalleLabel = new Label("Butaca: " + entrada.getFila() +", "+entrada.getCol()+
                    (entrada.isVip() ? " (VIP)" : " (Estándar)"));
            detalleLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px;");

            Label precioLabel = new Label(String.format("Precio: %.2f €", entrada.getPrecio())); //mostrar precio con 2 decimales
            precioLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 12px;");

            infoBox.getChildren().addAll(nombreLabel, detalleLabel, precioLabel);

            Button eliminarBtn = new Button("Eliminar"); //para borrar la entrada
            eliminarBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
            eliminarBtn.setCursor(javafx.scene.Cursor.HAND);
            eliminarBtn.setOnAction(e -> {
                entradas.remove(entrada);
                total -= entrada.getPrecio();
                actualizarCesta();
            });

            entradaBox.getChildren().addAll(infoBox, eliminarBtn);
            contenedorEntradas.getChildren().add(entradaBox);
        }

        totalLabel.setText(String.format("Total: %.2f €", total));
    }

    //método para cerrar sesión y volver al login
    //bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login
    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado=null;

        cambioEscena("../views/login.fxml");
    }

    //A IMPLEMENTAR
    public void volverReservas(ActionEvent actionEvent) {
        cambioEscena("../views/reserva.fxml");
    }

    //método para cambiar de escena
    private void cambioEscena(String name) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(name));
            Parent root = loader.load();

            // Obtener el Stage actual, con utilizar cualquier atributo fxml o nodo sirve.
            Stage stage = (Stage) contenedorEntradas.getScene().getWindow();

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

    public void filtrarPorFecha(ActionEvent actionEvent) {
    }

    public void mostrarTodas(ActionEvent actionEvent) {
    }

    public void filtrarPorNombre(ActionEvent actionEvent) {
    }

    // En CestaController.java, agregar estos métodos:
    public void setEmailUsuarioLogueado(String email) {
        this.emailUsuarioLogueado = email;
        if (usuarioLabel != null) {
            usuarioLabel.setText("Email: " + email);
        }
    }

    public void setEspectaculoSeleccionado(String nombreEspectaculo) {
        this.espectaculoSeleccionado = nombreEspectaculo;
    }

    public void setIdEspectaculoSeleccionado(String idEspectaculo) {
        this.idEspectaculoSeleccionado = idEspectaculo;
    }

    //clase de entradas de la cesta
    static class EntradaCesta {
        private String nombreEspectaculo;
        private int fila;
        private int col;
        private double precio;
        private boolean vip; //true si es vip

        public EntradaCesta(String nombreEspectaculo, int fila, int col, double precio, boolean vip) {
            this.nombreEspectaculo = nombreEspectaculo;
            this.fila = fila;
            this.col=col;
            this.precio = precio;
            this.vip = vip;
        }

        public String getNombreEspectaculo() { return nombreEspectaculo; }
        public int getFila() { return fila; }
        public int getCol() { return col; }
        public double getPrecio() { return precio; }
        public boolean isVip() { return vip; }
    }
}