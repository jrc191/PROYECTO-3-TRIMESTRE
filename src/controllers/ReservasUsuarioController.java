package controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import utils.DatabaseConnection;
import dao.impl.ReservaDaoImpl;
import dao.impl.UsuarioDaoImpl;
import dao.impl.EspectaculoDaoImpl;
import models.Reservas;
import utils.Transitions;
import utils.CerrarSesion;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static controllers.LoginController.getUsuarioLogueadoEmail;

public class ReservasUsuarioController {

    @FXML private VBox contenedorReservas;
    @FXML private Label usuarioLabel;
    @FXML private ScrollPane scrollReservas;
    @FXML private Label arribaBtn, abajoBtn;

    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    private String idUsuario;

    @FXML
    public void initialize() {
        scrollReservas.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
        agregarListenersScroll();
        cargarReservas();
    }

    private void agregarListenersScroll() {
        Transitions.configurarListenersScroll(scrollReservas, arribaBtn, abajoBtn);
    }

    private void cargarReservas() {
        contenedorReservas.getChildren().clear();

        try {
            Connection conn = DatabaseConnection.getConnection();
            ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);
            UsuarioDaoImpl usuarioDao = new UsuarioDaoImpl(conn);


            this.idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);
            System.out.println("ID -> "+idUsuario);

            //para mostrar el email del usuario logueado al lado del botón de cerrar sesión
            if (emailUsuarioLogueado != null) {
                usuarioLabel.setText("Email: " + emailUsuarioLogueado);
            }

            List<Reservas> reservas = reservaDao.consultarReservasByUsuario(idUsuario);

            for (Reservas reserva1: reservas){
                System.out.println(reserva1.getId_reserva()+"\n");
            }

            if (reservas.isEmpty()) {
                Label mensaje = new Label("No tienes reservas.");
                mensaje.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
                contenedorReservas.getChildren().add(mensaje);
                return;
            }

            for (Reservas reserva : reservas) {
                contenedorReservas.getChildren().add(crearTarjetaReserva(reserva));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar las reservas");
        }
    }

    private VBox crearTarjetaReserva(Reservas reserva) {

        String estadoReserva = ""; //PARA USAR CON ESTADOLABEL

        if (reserva.getEstado()=='C'){
            estadoReserva="CANCELADA";
        } else if (reserva.getEstado()=='O') {
            estadoReserva="CONFIRMADA";
        }
        else{
            estadoReserva="Error. Contacte con un administrador";
        }

        VBox tarjeta = new VBox(10);
        tarjeta.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        tarjeta.setPrefWidth(680);

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);


        String nombreEsp;

        try {
            Connection conn = DatabaseConnection.getConnection();
            EspectaculoDaoImpl espectaculoDao = new EspectaculoDaoImpl(conn);
            nombreEsp=espectaculoDao.obtenerNombrePorId(reserva.getId_espectaculo());
        } catch (SQLException e) {
            nombreEsp="Se produjo un error";
            e.printStackTrace();
        }
        Label espectaculoLabel = new Label("Espectáculo: " + nombreEsp);
        Label butacaLabel = new Label("Butaca: " + reserva.getId_butaca());
        Label precioLabel = new Label(String.format("Precio: %.2f €", reserva.getPrecio()));
        Label estadoLabel = new Label("Estado: "+estadoReserva);

        espectaculoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white;");
        butacaLabel.setStyle("-fx-text-fill: #e0e0e0;");
        precioLabel.setStyle("-fx-text-fill: #e0e0e0;");
        estadoLabel.setStyle("-fx-text-fill: #e0e0e0;");

        infoBox.getChildren().addAll(espectaculoLabel, butacaLabel, precioLabel, estadoLabel);

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelarBtn.setOnAction(e -> cancelarReserva(reserva));

        Button solicitarBtn = new Button("Solicitar re-reserva");
        solicitarBtn.setStyle("-fx-background-color: #4e3a74; -fx-text-fill: white; -fx-font-weight: bold;");
        solicitarBtn.setDisable(true); // Deshabilitado hasta implementar la funcionalidad
        solicitarBtn.setOnAction(e->{
            solicitarReserva(reserva);
        });

        HBox botonesBox = new HBox(10, cancelarBtn, solicitarBtn);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (reserva.getEstado()=='C'){
            cancelarBtn.setDisable(true);
            solicitarBtn.setDisable(false);
        }

        contentBox.getChildren().addAll(infoBox, spacer, botonesBox);
        tarjeta.getChildren().add(contentBox);

        return tarjeta;
    }

    private void solicitarReserva(Reservas reserva) {

    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Solicitar Re-Reserva");
        confirmAlert.setHeaderText("Solicitar Re-Reserva");
        confirmAlert.setContentText("¿Estás seguro de que quieres volver a solicitar esta reserva?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {

                try {
                    Connection conn = DatabaseConnection.getConnection();
                    ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);

                    int canceladas= reservaDao.cancelarReserva(reserva.getId_reserva());

                    if (canceladas>0){
                        mostrarInfo("Reserva cancelada con éxito");
                        cargarReservas();
                    }else{
                        mostrarError("Error al cancelar las reservas");
                    }
                } catch (SQLException e){
                    e.printStackTrace();
                }

                mostrarInfo("Se envió un mensaje al administrador con su solcitud de reserva.");
            }
        });

    }

    private void cancelarReserva(Reservas reserva) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar cancelación");
        confirmAlert.setHeaderText("Cancelar reserva");
        confirmAlert.setContentText("¿Estás seguro de que quieres cancelar esta reserva?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    Connection conn = DatabaseConnection.getConnection();
                    ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);

                    int canceladas= reservaDao.cancelarReserva(reserva.getId_reserva());

                    if (canceladas>0){
                        mostrarInfo("Reserva cancelada con éxito");
                        cargarReservas();
                    }else{
                        mostrarError("Error al cancelar las reservas");
                    }


                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al cancelar la reserva");
                }
            }
        });
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.show();
    }

    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setContentText(mensaje);
        alert.show();
    }

    @FXML
    private void volverCartelera() {
        Stage stage = (Stage) contenedorReservas.getScene().getWindow();
        Transitions.cambioEscena(stage, "/views/cartelera.fxml",
                "/resources/styles/styles.css", "CINES JRC", "/resources/images/logo.png", null);
    }

    @FXML
    private void cerrarSesion() {
        emailUsuarioLogueado = null;
        Stage stage = (Stage) usuarioLabel.getScene().getWindow();
        CerrarSesion.cerrarSesion(stage, "/resources/styles/styles.css", "/resources/images/logo.png");
    }
}