package controllers;

import dao.impl.MensajesDaoImpl;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import models.Mensajes;
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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static controllers.LoginController.getUsuarioLogueadoEmail;

public class ReservasUsuarioController {

    @FXML private VBox contenedorReservas;
    @FXML private Label usuarioLabel;
    @FXML private ScrollPane scrollReservas;
    @FXML private Label arribaBtn, abajoBtn;
    @FXML private ChoiceBox<String> filtroEstadoBox;
    @FXML private TextField filtroNombreField;

    private String emailUsuarioLogueado = getUsuarioLogueadoEmail();
    private String idUsuario;
    private List<Reservas> reservasActivas = new ArrayList<>();
    private List<Reservas> reservasCanceladas = new ArrayList<>();

    @FXML
    public void initialize() {
        scrollReservas.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
        agregarListenersScroll();

        // Configurar opciones del filtro de estado
        filtroEstadoBox.getItems().addAll("-", "Confirmadas", "Canceladas", "Mostrar todas");
        filtroEstadoBox.setValue("-");

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

            if (emailUsuarioLogueado != null) {
                usuarioLabel.setText("Email: " + emailUsuarioLogueado);
            }

            // Obtener reservas activas
            reservasActivas = reservaDao.consultarReservasByUsuario(idUsuario);

            // Obtener reservas canceladas del historial
            reservasCanceladas = reservaDao.consultarHistorialByUsuario(idUsuario);

            mostrarReservas(reservasActivas, reservasCanceladas);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar las reservas");
        }
    }

    private void mostrarReservas(List<Reservas> activas, List<Reservas> canceladas) {
        contenedorReservas.getChildren().clear();

        if (activas.isEmpty() && canceladas.isEmpty()) {
            Label mensaje = new Label("No se encontraron reservas");
            mensaje.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenedorReservas.getChildren().add(mensaje);
            return;
        }

        // Mostrar primero las activas
        for (Reservas reserva : activas) {
            contenedorReservas.getChildren().add(crearTarjetaReserva(reserva));
        }

        // Mostrar las canceladas después
        for (Reservas reserva : canceladas) {
            contenedorReservas.getChildren().add(crearTarjetaReserva(reserva));
        }
    }

    @FXML
    private void filtrarPorEstado() {
        String estadoSeleccionado = filtroEstadoBox.getValue();
        if (estadoSeleccionado == null || estadoSeleccionado.equals("-") || estadoSeleccionado.equals("Mostrar todas")) {
            mostrarReservas(reservasActivas, reservasCanceladas);
            return;
        }

        List<Reservas> filtradasActivas = new ArrayList<>();
        List<Reservas> filtradasCanceladas = new ArrayList<>();

        if (estadoSeleccionado.equals("Confirmadas")) {
            filtradasActivas = reservasActivas;
        } else if (estadoSeleccionado.equals("Canceladas")) {
            filtradasCanceladas = reservasCanceladas;
        }

        mostrarReservas(filtradasActivas, filtradasCanceladas);
    }

    @FXML
    private void filtrarPorNombre() {
        String textoBusqueda = filtroNombreField.getText().trim().toLowerCase();
        if (textoBusqueda.isEmpty()) {
            mostrarReservas(reservasActivas, reservasCanceladas);
            return;
        }

        try {
            Connection conn = DatabaseConnection.getConnection();
            EspectaculoDaoImpl espectaculoDao = new EspectaculoDaoImpl(conn);

            List<Reservas> filtradasActivas = reservasActivas.stream()
                    .filter(reserva -> {
                        try {
                            String nombreEsp = espectaculoDao.obtenerNombrePorId(reserva.getId_espectaculo());
                            return nombreEsp.toLowerCase().contains(textoBusqueda);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            List<Reservas> filtradasCanceladas = reservasCanceladas.stream()
                    .filter(reserva -> {
                        try {
                            String nombreEsp = espectaculoDao.obtenerNombrePorId(reserva.getId_espectaculo());
                            return nombreEsp.toLowerCase().contains(textoBusqueda);
                        } catch (SQLException e) {
                            e.printStackTrace();
                            return false;
                        }
                    })
                    .collect(Collectors.toList());

            mostrarReservas(filtradasActivas, filtradasCanceladas);

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al filtrar por nombre");
        }
    }

    @FXML
    private void mostrarTodas() {
        filtroEstadoBox.setValue("-");
        filtroNombreField.clear();
        mostrarReservas(reservasActivas, reservasCanceladas);
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

        if (estadoReserva.equals("CONFIRMADA")){
            estadoLabel.setStyle("-fx-text-fill: #4CAF50;");
        }else{
            estadoLabel.setStyle("-fx-text-fill: #f44336; ");
        }


        infoBox.getChildren().addAll(espectaculoLabel, butacaLabel, precioLabel, estadoLabel);

        Button cancelarBtn = new Button("Cancelar");
        cancelarBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelarBtn.setOnAction(e -> cancelarReserva(reserva));


        HBox botonesBox = new HBox(10, cancelarBtn);
        botonesBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        if (reserva.getEstado()=='C'){
            cancelarBtn.setDisable(true);
        }

        contentBox.getChildren().addAll(infoBox, spacer, botonesBox);
        tarjeta.getChildren().add(contentBox);

        return tarjeta;
    }


    private void cancelarReserva(Reservas reserva) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);

            // Verificar si han pasado menos de 24 horas
            boolean dentroPlazo = reservaDao.estaDentroPlazoCancelacion(reserva.getId_reserva());

            if (dentroPlazo) {
                // Cancelación directa
                int canceladas = reservaDao.cancelarReserva(reserva.getId_reserva());

                if (canceladas > 0) {
                    // Mover a historial
                    reservaDao.moverAHistorial(reserva.getId_reserva(), 'C'); // 'C' para Cancelada
                    mostrarInfo("Reserva cancelada con éxito");
                    cargarReservas();
                } else {
                    mostrarError("Error al cancelar la reserva");
                }
            } else {
                // Crear solicitud de cancelación
                Mensajes solicitud = new Mensajes();
                solicitud.setId_usuario(idUsuario);
                solicitud.setId_reserva(reserva.getId_reserva());
                solicitud.setTipo_solicitud("Cancelacion");
                solicitud.setEstado_solicitud('P'); // Pendiente

                MensajesDaoImpl mensajesDao = new MensajesDaoImpl(conn);
                boolean exito = mensajesDao.crearSolicitud(solicitud);

                if (exito) {
                    mostrarInfo("Solicitud de cancelación enviada al administrador");
                } else {
                    mostrarError("Error al enviar la solicitud de cancelación");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al procesar la cancelación");
        }
    }

// Eliminar el método solicitarReserva() ya que no lo necesitamos más

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