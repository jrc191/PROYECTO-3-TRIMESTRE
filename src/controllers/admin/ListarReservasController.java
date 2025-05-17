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

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            reservasDao = new ReservaDaoImpl(conn);
            cargarReservas();

            // Configurar eventos de los botones
            editarBtn.setOnMouseClicked(e -> editarSeleccionados());
            eliminarBtn.setOnMouseClicked(e -> cancelarSeleccionados());

            // Inicialmente deshabilitar ambos botones
            editarBtn.setDisable(true);
            eliminarBtn.setDisable(true);
            editarBtn.setOpacity(0.5);
            eliminarBtn.setOpacity(0.5);

            // Listener para cambios en la selección
            reservasVBox.getChildren().addListener((javafx.collections.ListChangeListener<javafx.scene.Node>) c -> {
                while (c.next()) {
                    if (c.wasAdded() || c.wasRemoved()) {
                        actualizarEstadoBotones();
                    }
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");
        }
    }

    public void cargarReservas() {
        try {

            reservasVBox.getChildren().clear();
            checkBoxes.clear();

            List<Reservas> reservasActivas = reservasDao.listarTodasReservas();
            List<Reservas> reservasCanceladas = reservasDao.listarHistorialReservas();

            reservasOriginal = new ArrayList<>();
            reservasOriginal.addAll(reservasActivas);
            reservasOriginal.addAll(reservasCanceladas);

            reservasVBox.getChildren().clear();
            checkBoxes.clear();

            // Encabezado de la tabla
            HBox header = new HBox();
            header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            // Checkbox para selección múltiple
            Label seleccionHeader = new Label(" V");
            Label espectaculoHeader = new Label("Espectáculo");
            Label butacaHeader = new Label("Butaca");
            Label usuarioHeader = new Label("Usuario");
            Label precioHeader = new Label("Precio");
            Label estadoHeader = new Label("Estado");
            Label accionesHeader = new Label("Acciones");

            // Estilos y tamaños
            seleccionHeader.setPrefWidth(50);
            espectaculoHeader.setPrefWidth(150);
            butacaHeader.setPrefWidth(100);
            usuarioHeader.setPrefWidth(150);
            precioHeader.setPrefWidth(80);
            estadoHeader.setPrefWidth(80);
            accionesHeader.setPrefWidth(100);

            String headerStyle = "-fx-text-fill: #6c757d; -fx-font-weight: bold;";
            seleccionHeader.setStyle(headerStyle);
            espectaculoHeader.setStyle(headerStyle);
            butacaHeader.setStyle(headerStyle);
            usuarioHeader.setStyle(headerStyle);
            precioHeader.setStyle(headerStyle);
            estadoHeader.setStyle(headerStyle);
            accionesHeader.setStyle(headerStyle);

            header.getChildren().addAll(seleccionHeader, espectaculoHeader, butacaHeader,
                    usuarioHeader, precioHeader, estadoHeader, accionesHeader);
            reservasVBox.getChildren().add(header);

            // Filas de datos
            for (Reservas reserva : reservasOriginal) {
                HBox row = new HBox();
                String rowStyle = "-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";

                if (reserva.getEstado() == 'C') {
                    rowStyle = "-fx-background-color: #fff5f5; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;";
                }
                row.setStyle(rowStyle);

                CheckBox checkBox = new CheckBox();
                checkBox.setPrefWidth(50);
                checkBoxes.add(checkBox);

                // Listener para cambios en la selección
                checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
                    actualizarEstadoBotones();
                });

                Label espectaculo = new Label(reserva.getId_espectaculo());
                Label butaca = new Label(reserva.getId_butaca());
                Label usuario = new Label(reserva.getId_usuario());
                Label precio = new Label(String.format("%.2f €", reserva.getPrecio()));

                // Mostrar estado con texto descriptivo
                String estadoText = "";
                if (reserva.getEstado() == 'O') {
                    estadoText = "ACTIVA";
                } else if (reserva.getEstado() == 'C') {
                    estadoText = "CANCELADA";
                }
                Label estado = new Label(estadoText);

                // Botones de acción
                ImageView reactivarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/tick.png")));
                ImageView cancelarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/cancel.png")));

                // Configurar tooltips
                Tooltip.install(reactivarIcon, new Tooltip("Reactivar reserva"));
                Tooltip.install(cancelarIcon, new Tooltip("Cancelar reserva"));

                // Configurar estilos y acciones
                if (reserva.getEstado() == 'C') {
                    // Para reservas canceladas: habilitar reactivación
                    reactivarIcon.setFitHeight(16);
                    reactivarIcon.setFitWidth(16);
                    reactivarIcon.setStyle("-fx-cursor: hand;");
                    reactivarIcon.setOnMouseClicked(e -> editarReserva(reserva));
                    reactivarIcon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0.8, 0, 0));

                    // Deshabilitar cancelación
                    cancelarIcon.setFitHeight(16);
                    cancelarIcon.setFitWidth(16);
                    cancelarIcon.setOpacity(0.2);
                    cancelarIcon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0, -0.3, 0));
                } else {
                    // Para reservas activas: habilitar cancelación
                    cancelarIcon.setFitHeight(16);
                    cancelarIcon.setFitWidth(16);
                    cancelarIcon.setStyle("-fx-cursor: hand;");
                    cancelarIcon.setOnMouseClicked(e -> cancelarReserva(reserva));
                    cancelarIcon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0.8, 0, 0));

                    // Deshabilitar reactivación
                    reactivarIcon.setFitHeight(16);
                    reactivarIcon.setFitWidth(16);
                    reactivarIcon.setOpacity(0.2);
                    reactivarIcon.setEffect(new javafx.scene.effect.ColorAdjust(0, 0, -0.3, 0));
                }

                // Añadir los botones a la fila
                HBox accionesBox = new HBox(5, reactivarIcon, cancelarIcon);
                accionesBox.setPrefWidth(100);

                // Establecer anchos y estilos
                espectaculo.setPrefWidth(150);
                butaca.setPrefWidth(100);
                usuario.setPrefWidth(150);
                precio.setPrefWidth(80);
                estado.setPrefWidth(80);
                accionesBox.setPrefWidth(100);

                String cellStyle = "-fx-text-fill: #495057;";
                espectaculo.setStyle(cellStyle);
                butaca.setStyle(cellStyle);
                usuario.setStyle(cellStyle);
                precio.setStyle(cellStyle);

                if (reserva.getEstado() == 'C') {
                    estado.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                } else {
                    estado.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }

                row.getChildren().addAll(checkBox, espectaculo, butaca, usuario, precio, estado, accionesBox);
                reservasVBox.getChildren().add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar las reservas");
        }
    }

    private void actualizarEstadoBotones() {
        List<Reservas> seleccionados = getReservasSeleccionadas();

        if (seleccionados.isEmpty()) {
            editarBtn.setDisable(true);
            eliminarBtn.setDisable(true);
            editarBtn.setOpacity(0.5);
            eliminarBtn.setOpacity(0.5);
            return;
        }

        boolean todasCanceladas = true;
        boolean todasActivas = true;

        for (Reservas reserva : seleccionados) {
            if (reserva.getEstado() == 'O') {
                todasCanceladas = false;
            } else if (reserva.getEstado() == 'C') {
                todasActivas = false;
            }
        }

        // Habilitar reactivar solo si todas las seleccionadas están canceladas
        editarBtn.setDisable(!todasCanceladas);
        editarBtn.setOpacity(todasCanceladas ? 1.0 : 0.5);

        // Habilitar cancelar solo si todas las seleccionadas están activas
        eliminarBtn.setDisable(!todasActivas);
        eliminarBtn.setOpacity(todasActivas ? 1.0 : 0.5);
    }

    private void editarReserva(Reservas reserva) {
        if (reserva.getEstado() == 'O') {
            mostrarError("Esta reserva ya está activa");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar reactivación");
        confirmAlert.setHeaderText("Reactivar reserva");
        confirmAlert.setContentText("¿Está seguro que desea reactivar esta reserva cancelada?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
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
                    e.printStackTrace();
                    mostrarError("Error al reactivar la reserva: " + e.getMessage());
                }
            }
        });
    }

    private void cancelarReserva(Reservas reserva) {
        if (reserva.getEstado() == 'C') {
            mostrarError("Esta reserva ya está cancelada");
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar cancelación");
        confirmAlert.setHeaderText("Cancelar reserva");
        confirmAlert.setContentText("¿Está seguro que desea cancelar esta reserva?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int resultado = reservasDao.cancelarReserva(reserva.getId_reserva());
                    if (resultado > 0) {
                        mostrarMensaje("Reserva cancelada", "La reserva ha sido cancelada exitosamente");
                        cargarReservas();
                    } else {
                        mostrarError("No se pudo cancelar la reserva");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al cancelar la reserva");
                }
            }
        });
    }

    private void editarSeleccionados() {
        List<Reservas> seleccionados = getReservasSeleccionadas();
        if (seleccionados.isEmpty()) {
            mostrarError("Seleccione al menos una reserva para reactivar");
            return;
        }

        // Verificar que todas las reservas seleccionadas estén canceladas
        for (Reservas reserva : seleccionados) {
            if (reserva.getEstado() != 'C') {
                mostrarError("Solo se pueden reactivar reservas canceladas");
                return;
            }
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar reactivación múltiple");
        confirmAlert.setHeaderText("Reactivar " + seleccionados.size() + " reservas");
        confirmAlert.setContentText("¿Está seguro que desea reactivar las " + seleccionados.size() + " reservas seleccionadas?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int reactivadas = 0;
                    int errores = 0;
                    List<String> erroresButacas = new ArrayList<>();

                    for (Reservas reserva : seleccionados) {
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

                    if (reactivadas > 0) {
                        cargarReservas();
                        String mensaje = "Se reactivaron " + reactivadas + " reservas";
                        if (errores > 0) {
                            mensaje += "\nNo se pudieron reactivar " + errores + " reservas";
                            if (!erroresButacas.isEmpty()) {
                                mensaje += "\nButacas ocupadas: " + String.join(", ", erroresButacas);
                            }
                        }
                        mostrarMensaje("Reservas reactivadas", mensaje);
                    } else {
                        mostrarError("No se pudo reactivar ninguna reserva");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al reactivar las reservas: " + e.getMessage());
                }
            }
        });
    }

    private void cancelarSeleccionados() {
        List<Reservas> seleccionados = getReservasSeleccionadas();
        if (seleccionados.isEmpty()) {
            mostrarError("Seleccione al menos una reserva para cancelar");
            return;
        }

        // Verificar que todas las reservas seleccionadas estén activas
        for (Reservas reserva : seleccionados) {
            if (reserva.getEstado() != 'O') {
                mostrarError("Solo se pueden cancelar reservas activas");
                return;
            }
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirmar cancelación múltiple");
        confirmAlert.setHeaderText("Cancelar " + seleccionados.size() + " reservas");
        confirmAlert.setContentText("¿Está seguro que desea cancelar las " + seleccionados.size() + " reservas seleccionadas?");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    int canceladas = 0;
                    for (Reservas reserva : seleccionados) {
                        int resultado = reservasDao.cancelarReserva(reserva.getId_reserva());
                        if (resultado > 0) {
                            canceladas++;
                        }
                    }

                    if (canceladas > 0) {
                        cargarReservas();
                        mostrarMensaje("Reservas canceladas", "Se cancelaron " + canceladas + " reservas");
                    } else {
                        mostrarError("No se pudo cancelar ninguna reserva");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al cancelar las reservas");
                }
            }
        });
    }

    private List<Reservas> getReservasSeleccionadas() {
        List<Reservas> seleccionados = new ArrayList<>();
        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                seleccionados.add(reservasOriginal.get(i));
            }
        }
        return seleccionados;
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