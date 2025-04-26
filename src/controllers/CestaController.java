package controllers;

import javafx.stage.Stage;
import javafx.scene.control.Label;

import utils.DatabaseConnection;
import dao.UsuarioDaoI;
import dao.impl.ReservaDaoImpl;
import dao.impl.UsuarioDaoImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import models.EntradaCesta;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.Reservas;
import utils.CestaStorage;
import utils.Transitions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
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
    @FXML
    private VBox plantillaEntrada;
    @FXML
    private ScrollPane scrollEntradas;
    @FXML
    private Label arribaBtn, abajoBtn;
    @FXML
    private ChoiceBox<String> eleccionBox;

    // Parámetros usados para cerrar sesión, reservar ... entre otros
    private String emailUsuarioLogueado;
    private String idUsuario;
    private UsuarioDaoI usuarioDao;
    private String espectaculoSeleccionado;
    private String idEspectaculoSeleccionado;
    public DatePicker filtroFechaField;
    public TextField filtroNombreField;

    private List<EntradaCesta> entradas = new ArrayList<>();
    private double total = 0.0; //precio total de las entradas de la cesta

    public CestaController() throws SQLException {
    }

    public void initialize() {
        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
            // Inicializamos opciones del ChoiceBox
            eleccionBox.getItems().addAll( "OPCION 1", "OPCION 2");
            eleccionBox.setValue("-");

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
        scrollEntradas.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
        scrollEntradas.setFitToWidth(true);

        // Configurar listeners para las flechas
        agregarListenersScroll();

        actualizarCesta();
    }

    //A IMPLEMENTAR
    @FXML
    private void filtrarPorButaca() {
        String tipoSeleccionado = eleccionBox.getValue();

    }

    // Para manejar el scroll de la cesta
    private void agregarListenersScroll() {
        Transitions.configurarListenersScroll(scrollEntradas, arribaBtn, abajoBtn);
    }

    public void agregarEntrada(String nombreEspectaculo, int fila, int col, double precio, boolean esVip) {
        if (entradas == null) {
            entradas = new ArrayList<>();
        }

        EntradaCesta entrada = new EntradaCesta(nombreEspectaculo, fila, col, precio, esVip);
        entradas.add(entrada);
        total += precio;
        actualizarCesta();

        CestaStorage.guardarCesta(emailUsuarioLogueado, entradas); //Sencillamente espectacular. Para guardar la cesta en ficheros según el mail
    }


    //Para devolver la cesta en otros controladores. YA NO HACE FALTA, DEBIDO A QUE AHORA SE USAN FICHEROS SERIALIZABLES
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

    //para actualizar la cesta. Crea cada entrada según el fichero {email}.ser.
    private void actualizarCesta() {
        contenedorEntradas.getChildren().clear();
        total = 0.0;

        for (EntradaCesta entrada : entradas) {
            VBox entradaCard = new VBox(10);
            entradaCard.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
            entradaCard.setPrefWidth(680);

            HBox contentBox = new HBox(15);
            contentBox.setAlignment(Pos.CENTER_LEFT);

            VBox infoBox = new VBox(5);

            Label nombreLabel = new Label(entrada.getNombreEspectaculo());
            nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;");

            Label detalleLabel = new Label("Butaca: " + entrada.getFila() + ", " + entrada.getCol() +
                    (entrada.isVip() ? " (VIP)" : " (Estándar)"));
            detalleLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

            Label precioLabel = new Label(String.format("Precio: %.2f €", entrada.getPrecio()));
            precioLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

            infoBox.getChildren().addAll(nombreLabel, detalleLabel, precioLabel);

            Button eliminarBtn = new Button("Eliminar");
            eliminarBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");


            eliminarBtn.setOnAction(e -> {
                entradas.remove(entrada);
                total -= entrada.getPrecio();

                try {
                    Connection conn = DatabaseConnection.getConnection();
                    ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);

                    // Usar el mismo formato que en ReservasController
                    String idReservaTemp = idEspectaculoSeleccionado + "_" + idUsuario + "_F" + entrada.getFila() + "-C" + entrada.getCol();
                    System.out.println("Intentando eliminar reserva temporal con ID: " + idReservaTemp); // Debug

                    reservaDao.eliminarReservaTemporal(idReservaTemp);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    Alert alert = new Alert(AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setContentText("No se pudo eliminar la reserva temporal: " + ex.getMessage());
                    alert.show();
                }

                actualizarCesta();
                CestaStorage.guardarCesta(emailUsuarioLogueado, entradas);
            });

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            contentBox.getChildren().addAll(infoBox, spacer, eliminarBtn);
            entradaCard.getChildren().add(contentBox);

            contenedorEntradas.getChildren().add(entradaCard);
            total += entrada.getPrecio();
        }

        totalLabel.setText(String.format("Total: %.2f €", total));
    }
    //CONFIRMAR COMPRA.
    public void confirmarCompra(ActionEvent actionEvent) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("CONFIRMAR COMPRA");
        alert.setContentText("¿Quiere usted confirmar su compra?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);

                    // Eliminar todas las reservas temporales del usuario usando el DAO
                    reservaDao.eliminarReservasTemporalesUsuario(idUsuario);

                    // Guardar las reservas definitivas
                    for (EntradaCesta entrada : entradas) {
                        Reservas reserva = new Reservas();
                        String idReserva = idEspectaculoSeleccionado + "_" + idUsuario + "_F" + entrada.getFila() + "-C" + entrada.getCol();
                        reserva.setId_reserva(idReserva);
                        reserva.setId_espectaculo(idEspectaculoSeleccionado);
                        reserva.setId_butaca("F" + entrada.getFila() + "-C" + entrada.getCol());
                        reserva.setId_usuario(idUsuario);
                        reserva.setPrecio(entrada.getPrecio());
                        reserva.setEstado('O'); // 'O' para ocupado
                        reservaDao.registrarReservas(reserva);
                    }

                    // Limpiar la cesta
                    entradas.clear();
                    CestaStorage.guardarCesta(emailUsuarioLogueado, entradas);
                    actualizarCesta();

                    Alert successAlert = new Alert(AlertType.INFORMATION);
                    successAlert.setTitle("Compra confirmada");
                    successAlert.setContentText("La compra se ha realizado con éxito.");
                    successAlert.show();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert errorAlert = new Alert(AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setContentText("No se pudo confirmar la compra.");
                    errorAlert.show();
                }
            }
        });
    }


    //método para cerrar sesión y volver al login
//bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login
    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado = null;
        Stage stage = (Stage) usuarioLabel.getScene().getWindow();
        utils.CerrarSesionUtil.cerrarSesion(stage, "/Resources/styles.css", "/Resources/logo.png");
    }

    //A IMPLEMENTAR
    public void volverCartelera(ActionEvent actionEvent) {
        cambioEscena("../views/cartelera.fxml");
    }

    //método para cambiar de escena
    // Ahora se usa Transitions.cambioEscena para cambiar de escena
    private void cambioEscena(String name) {
        Stage stage = (Stage) contenedorEntradas.getScene().getWindow();
        // Se asume que el stylesheet y el icon path son los mismos siempre
        Transitions.cambioEscena(stage, name, "../Resources/styles.css", "CINES JRC", "../Resources/logo.png", null);
    }

    public void filtrarPorFecha(ActionEvent actionEvent) {
    }

    public void mostrarTodas(ActionEvent actionEvent) {
    }


    // Método para oscurecer la entrada
    // Ahora se usa Transitions.oscurecerEntrada
    private void oscurecerEntrada(VBox entradaCesta) {
        Transitions.oscurecerEntrada(entradaCesta);
    }

    public void setEmailUsuarioLogueado(String email) {
        this.emailUsuarioLogueado = email;
        if (usuarioLabel != null) {
            usuarioLabel.setText("Email: " + email);
        }

        // Obtener el ID del usuario al establecer el email
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
            this.idUsuario = usuarioDao.getIDUsuarioByEmail(email);
            System.out.println("ID Usuario obtenido: " + idUsuario); // Debug
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar la cesta desde el almacenamiento
        this.entradas = CestaStorage.cargarCesta(email);
        // Recalcular el total
        this.total = entradas.stream().mapToDouble(EntradaCesta::getPrecio).sum();
        actualizarCesta();
    }


    public void setEspectaculoSeleccionado(String nombreEspectaculo) {
        this.espectaculoSeleccionado = nombreEspectaculo;
    }

    public void setIdEspectaculoSeleccionado(String idEspectaculo) {
        this.idEspectaculoSeleccionado = idEspectaculo;
    }

    public void filtrarPorAsiento(ActionEvent actionEvent) {
    }

    public void filtrarPorNombre(ActionEvent actionEvent) {
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario=idUsuario;
    }
}