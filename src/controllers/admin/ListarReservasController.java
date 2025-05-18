package controllers.admin;

import dao.ReservasDaoI;
import dao.impl.ReservaDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Reservas;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class ListarReservasController {
    @FXML private ImageView addBtn;
    @FXML private VBox reservasVBox;
    @FXML private ScrollPane scrollReservas;
    @FXML private ImageView editarBtn;
    @FXML private ImageView eliminarBtn;
    @FXML private Button guardarBtn;
    @FXML private Button cancelarBtn;

    private List<Reservas> reservasOriginal;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private ReservasDaoI reservasDao;


    //Inicializa el controlador, junto a la conexión a la base de datos y carga las reservas (además de configurar los eventos de los botones)
    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            reservasDao = new ReservaDaoImpl(conn);
            cargarReservas();
            configurarEventosBotones();
            configurarListenerSeleccion();
        } catch (SQLException e) {
            manejarErrorInicializacion(e);
        }
    }


    //Método para cargar las reservas desde la base de datos (activas y canceladas) y mostrarlas en la interfaz mediante una tabla.
    public void cargarReservas() {
        try {
            limpiarInterfaz();
            cargarDatosReservas();
            crearCabeceraTabla();
            crearFilasReservas();
        } catch (SQLException e) {
            manejarErrorCargaReservas(e);
        }
    }

    //Actualiza el estado de los botones de editar y eliminar según la selección actual y el estado de las reservas (habilita reactivar para canceladas, cancelar para activas).
    private void actualizarEstadoBotones() {
        List<Reservas> seleccionados = obtenerReservasSeleccionadas();

        // Si no hay reservas seleccionadas, deshabilitar los botones
        if (seleccionados.isEmpty()) {
            deshabilitarBotonesAccion();
            return;
        }
        // Si hay reservas seleccionadas, habilitar los botones según el estado de las reservas
        boolean todasCanceladas = seleccionados.stream().allMatch(r -> r.getEstado() == 'C');
        boolean todasActivas = seleccionados.stream().allMatch(r -> r.getEstado() == 'O');

        configurarBotonEditar(todasCanceladas);
        configurarBotonEliminar(todasActivas);
    }


    // Reactiva una reserva después de la confirmación del administrador.
    private void reactivarReserva(Reservas reserva) {
        if (reserva.getEstado() == 'O') {
            mostrarError("Esta reserva ya está activa");
            return;
        }

        Alert confirmacion = crearAlertaConfirmacion("Confirmar reactivación",
                "Reactivar reserva", "¿Está seguro que desea reactivar esta reserva cancelada?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                procesarReactivacionReserva(reserva);
            }
        });
    }

    //Cancela una reserva activa después de la confirmación del administrador.
    private void cancelarReserva(Reservas reserva) {
        if (reserva.getEstado() == 'C') {
            mostrarError("Esta reserva ya está cancelada");
            return;
        }

        Alert confirmacion = crearAlertaConfirmacion("Confirmar cancelación",
                "Cancelar reserva", "¿Está seguro que desea cancelar esta reserva?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                procesarCancelacionReserva(reserva);
            }
        });
    }

    //Reactiva múltiples reservas seleccionadas después de la confirmación del administrador.
    //Muestra un mensaje de error si no se puede reactivar alguna reserva (p.ej. si la butaca ya está ocupada).
    private void reactivarSeleccionados() {
        List<Reservas> seleccionados = obtenerReservasSeleccionadas();

        if (!validarSeleccionReactivacion(seleccionados)) {
            return;
        }

        Alert confirmacion = crearAlertaConfirmacion("Confirmar reactivación múltiple",
                "Reactivar " + seleccionados.size() + " reservas",
                "¿Está seguro que desea reactivar las " + seleccionados.size() + " reservas seleccionadas?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                procesarReactivacionMultiple(seleccionados);
            }
        });
    }


    // Cancela múltiples reservas seleccionadas después de la confirmación del administrador.
    private void cancelarSeleccionados() {
        List<Reservas> seleccionados = obtenerReservasSeleccionadas();

        if (!validarSeleccionCancelacion(seleccionados)) {
            return;
        }

        Alert confirmacion = crearAlertaConfirmacion("Confirmar cancelación múltiple",
                "Cancelar " + seleccionados.size() + " reservas",
                "¿Está seguro que desea cancelar las " + seleccionados.size() + " reservas seleccionadas?");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                procesarCancelacionMultiple(seleccionados);
            }
        });
    }


    private void configurarEventosBotones() {
        editarBtn.setOnMouseClicked(e -> reactivarSeleccionados());
        eliminarBtn.setOnMouseClicked(e -> cancelarSeleccionados());
        deshabilitarBotonesAccion();
    }

    private void configurarListenerSeleccion() {
        reservasVBox.getChildren().addListener((javafx.collections.ListChangeListener<javafx.scene.Node>) c -> {
            while (c.next()) {
                if (c.wasAdded() || c.wasRemoved()) {
                    actualizarEstadoBotones();
                }
            }
        });
    }

    private void manejarErrorInicializacion(SQLException e) {
        e.printStackTrace();
        mostrarError("Error al conectar con la base de datos");
    }

    private void limpiarInterfaz() {
        reservasVBox.getChildren().clear();
        checkBoxes.clear();
    }

    private void cargarDatosReservas() throws SQLException {
        List<Reservas> reservasActivas = reservasDao.listarTodasReservas();
        List<Reservas> reservasCanceladas = reservasDao.listarHistorialReservas();
        reservasOriginal = new ArrayList<>();
        reservasOriginal.addAll(reservasActivas);
        reservasOriginal.addAll(reservasCanceladas);
    }

    private void crearCabeceraTabla() {
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

        Label[] headers = {
                crearLabelCabecera(" V", 50),
                crearLabelCabecera("Espectáculo", 150),
                crearLabelCabecera("Butaca", 100),
                crearLabelCabecera("Usuario", 150),
                crearLabelCabecera("Precio", 80),
                crearLabelCabecera("Estado", 80),
                crearLabelCabecera("Acciones", 100)
        };

        header.getChildren().addAll(headers);
        reservasVBox.getChildren().add(header);
    }

    private Label crearLabelCabecera(String texto, double ancho) {
        Label label = new Label(texto);
        label.setPrefWidth(ancho);
        label.setStyle("-fx-text-fill: #6c757d; -fx-font-weight: bold;");
        return label;
    }

    private void crearFilasReservas() {
        for (Reservas reserva : reservasOriginal) {
            HBox row = crearFilaReserva(reserva);
            reservasVBox.getChildren().add(row);
        }
    }

    private HBox crearFilaReserva(Reservas reserva) {
        HBox row = new HBox();
        aplicarEstiloFila(row, reserva.getEstado());

        CheckBox checkBox = new CheckBox();
        checkBox.setPrefWidth(50);
        checkBoxes.add(checkBox);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> actualizarEstadoBotones());

        Label espectaculo = crearLabelDato(reserva.getId_espectaculo(), 150);
        Label butaca = crearLabelDato(reserva.getId_butaca(), 100);
        Label usuario = crearLabelDato(reserva.getId_usuario(), 150);
        Label precio = crearLabelDato(String.format("%.2f €", reserva.getPrecio()), 80);
        Label estado = crearLabelEstado(reserva.getEstado());

        ImageView reactivarIcon = crearIconoAccion("/resources/images/tick.png", "Reactivar reserva");
        ImageView cancelarIcon = crearIconoAccion("/resources/images/cancel.png", "Cancelar reserva");
        configurarIconosReserva(reactivarIcon, cancelarIcon, reserva);

        HBox accionesBox = new HBox(5, reactivarIcon, cancelarIcon);
        accionesBox.setPrefWidth(100);

        row.getChildren().addAll(checkBox, espectaculo, butaca, usuario, precio, estado, accionesBox);
        return row;
    }

    private void aplicarEstiloFila(HBox fila, char estado) {
        String estiloBase = "-fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
        fila.setStyle(estado == 'C' ? estiloBase + "-fx-background-color: #fff5f5;" : estiloBase + "-fx-background-color: white;");
    }

    private Label crearLabelDato(String texto, double ancho) {
        Label label = new Label(texto);
        label.setPrefWidth(ancho);
        label.setStyle("-fx-text-fill: #495057;");
        return label;
    }

    private Label crearLabelEstado(char estado) {
        Label label = new Label(estado == 'O' ? "ACTIVA" : "CANCELADA");
        label.setPrefWidth(80);
        label.setStyle(estado == 'O' ?
                "-fx-text-fill: #4CAF50; -fx-font-weight: bold;" :
                "-fx-text-fill: #f44336; -fx-font-weight: bold;");
        return label;
    }

    private ImageView crearIconoAccion(String recurso, String tooltip) {
        ImageView icon = new ImageView(new Image(getClass().getResourceAsStream(recurso)));
        icon.setFitHeight(16);
        icon.setFitWidth(16);
        Tooltip.install(icon, new Tooltip(tooltip));
        return icon;
    }

    private void configurarIconosReserva(ImageView reactivarIcon, ImageView cancelarIcon, Reservas reserva) {
        if (reserva.getEstado() == 'C') {
            configurarIconoReactivar(reactivarIcon, reserva);
            deshabilitarIcono(cancelarIcon);
        } else {
            configurarIconoCancelar(cancelarIcon, reserva);
            deshabilitarIcono(reactivarIcon);
        }
    }

    private void configurarIconoReactivar(ImageView icon, Reservas reserva) {
        icon.setStyle("-fx-cursor: hand;");
        icon.setOnMouseClicked(e -> reactivarReserva(reserva));
        icon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0.8, 0, 0));
    }

    private void configurarIconoCancelar(ImageView icon, Reservas reserva) {
        icon.setStyle("-fx-cursor: hand;");
        icon.setOnMouseClicked(e -> cancelarReserva(reserva));
        icon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0.8, 0, 0));
    }

    private void deshabilitarIcono(ImageView icon) {
        icon.setOpacity(0.2);
        icon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0, -0.3, 0));
    }

    private void deshabilitarBotonesAccion() {
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);
        editarBtn.setOpacity(0.5);
        eliminarBtn.setOpacity(0.5);
    }

    private void configurarBotonEditar(boolean habilitar) {
        editarBtn.setDisable(!habilitar);
        editarBtn.setOpacity(habilitar ? 1.0 : 0.5);
    }

    private void configurarBotonEliminar(boolean habilitar) {
        eliminarBtn.setDisable(!habilitar);
        eliminarBtn.setOpacity(habilitar ? 1.0 : 0.5);
    }

    private List<Reservas> obtenerReservasSeleccionadas() {
        List<Reservas> seleccionados = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                seleccionados.add(reservasOriginal.get(i));
            }
        }
        return seleccionados;
    }

    private Alert crearAlertaConfirmacion(String titulo, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }



    private void procesarCancelacionReserva(Reservas reserva) {
        try {
            int resultado = reservasDao.cancelarReserva(reserva.getId_reserva());
            if (resultado > 0) {
                mostrarMensaje("Reserva cancelada", "La reserva ha sido cancelada exitosamente");
                cargarReservas();
            } else {
                mostrarError("No se pudo cancelar la reserva");
            }
        } catch (SQLException e) {
            mostrarError("Error al cancelar la reserva");
        }
    }

    private boolean validarSeleccionReactivacion(List<Reservas> seleccionados) {
        if (seleccionados.isEmpty()) {
            mostrarError("Seleccione al menos una reserva para reactivar");
            return false;
        }

        if (seleccionados.stream().anyMatch(r -> r.getEstado() != 'C')) {
            mostrarError("Solo se pueden reactivar reservas canceladas");
            return false;
        }

        return true;
    }

    private boolean validarSeleccionCancelacion(List<Reservas> seleccionados) {
        if (seleccionados.isEmpty()) {
            mostrarError("Seleccione al menos una reserva para cancelar");
            return false;
        }

        if (seleccionados.stream().anyMatch(r -> r.getEstado() != 'O')) {
            mostrarError("Solo se pueden cancelar reservas activas");
            return false;
        }

        return true;
    }


    private void procesarReactivacionReserva(Reservas reserva) {
        try {
            // Verificar si el usuario ya tiene 4 reservas activas para este espectáculo
            int reservasActivas = reservasDao.contarReservasActivasPorUsuarioYEspectaculo(
                    reserva.getId_usuario(),
                    reserva.getId_espectaculo()
            );

            if (reservasActivas >= 4) {
                mostrarError("No se puede reactivar la reserva. El usuario ya tiene 4 reservas activas para este espectáculo.");
                return;
            }

            int resultado = reservasDao.reactivarReserva(reserva.getId_reserva());
            if (resultado > 0) {
                mostrarMensaje("Reserva reactivada", "La reserva ha sido reactivada exitosamente");
                cargarReservas();
            } else if (resultado == -1) {
                mostrarError("No se puede reactivar la reserva porque la butaca ya está ocupada");
            } else {
                mostrarError("No se pudo reactivar la reserva");
            }
        } catch (SQLException e) {
            mostrarError("Error al reactivar la reserva: " + e.getMessage());
        }
    }

    private void procesarReactivacionMultiple(List<Reservas> seleccionados) {
        try {
            int reactivadas = 0;
            int errores = 0;
            List<String> erroresButacas = new ArrayList<>();
            List<String> erroresLimite = new ArrayList<>();

            for (Reservas reserva : seleccionados) {
                // Verificar límite de reservas por espectáculo
                int reservasActivas = reservasDao.contarReservasActivasPorUsuarioYEspectaculo(
                        reserva.getId_usuario(),
                        reserva.getId_espectaculo()
                );

                if (reservasActivas >= 4) {
                    erroresLimite.add("Usuario " + reserva.getId_usuario() + " en espectáculo " + reserva.getId_espectaculo());
                    errores++;
                    continue;
                }

                int resultado = reservasDao.reactivarReserva(reserva.getId_reserva());
                if (resultado > 0) {
                    reactivadas++;
                } else if (resultado == -1) {
                    erroresButacas.add(reserva.getId_butaca());
                    errores++;
                } else {
                    errores++;
                }
            }

            mostrarResultadoReactivacion(reactivadas, errores, erroresButacas, erroresLimite);
            if (reactivadas > 0) {
                cargarReservas();
            }
        } catch (SQLException e) {
            mostrarError("Error al reactivar las reservas: " + e.getMessage());
        }
    }

    private void mostrarResultadoReactivacion(int exitosas, int errores, List<String> butacasOcupadas, List<String> limitesExcedidos) {
        if (exitosas > 0) {
            String mensaje = "Se reactivaron " + exitosas + " reservas";
            if (errores > 0) {
                mensaje += "\nNo se pudieron reactivar " + errores + " reservas";
                if (!butacasOcupadas.isEmpty()) {
                    mensaje += "\nButacas ocupadas: " + String.join(", ", butacasOcupadas);
                }
                if (!limitesExcedidos.isEmpty()) {
                    mensaje += "\nLímite de reservas excedido para: " + String.join(", ", limitesExcedidos);
                }
            }
            mostrarMensaje("Reservas reactivadas", mensaje);
        } else {
            String mensajeError = "No se pudo reactivar ninguna reserva";
            if (!limitesExcedidos.isEmpty()) {
                mensajeError += "\nMotivo: Límite de reservas excedido para algunos usuarios";
            }
            if (!butacasOcupadas.isEmpty()) {
                mensajeError += "\nMotivo: Butacas ya ocupadas";
            }
            mostrarError(mensajeError);
        }
    }


    private void procesarCancelacionMultiple(List<Reservas> seleccionados) {
        try {
            int canceladas = 0;
            for (Reservas reserva : seleccionados) {
                int resultado = reservasDao.cancelarReserva(reserva.getId_reserva());
                if (resultado > 0) {
                    canceladas++;
                }
            }

            if (canceladas > 0) {
                mostrarMensaje("Reservas canceladas", "Se cancelaron " + canceladas + " reservas");
                cargarReservas();
            } else {
                mostrarError("No se pudo cancelar ninguna reserva");
            }
        } catch (SQLException e) {
            mostrarError("Error al cancelar las reservas");
        }
    }

    private void manejarErrorCargaReservas(SQLException e) {
        e.printStackTrace();
        mostrarError("Error al cargar las reservas");
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.show();
    }

    private void mostrarMensaje(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}