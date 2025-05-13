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
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");
        }
    }

    public void cargarReservas() {
        try {
            reservasOriginal = reservasDao.listarTodasReservas();
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

            // DAtos
            for (Reservas reserva : reservasOriginal) {
                HBox row = new HBox();
                row.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

                CheckBox checkBox = new CheckBox();
                checkBox.setPrefWidth(50);
                checkBoxes.add(checkBox);

                Label espectaculo = new Label(reserva.getId_espectaculo());
                Label butaca = new Label(reserva.getId_butaca());
                Label usuario = new Label(reserva.getId_usuario());
                Label precio = new Label(String.format("%.2f €", reserva.getPrecio()));
                Label estado = new Label(String.valueOf(reserva.getEstado()));

                // Botones de acción
                ImageView editarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/edit.png")));
                ImageView cancelarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/cancel.png")));

                if (reserva.getEstado() == 'C') { // Reserva cancelada
                    checkBox.setDisable(true);
                    checkBox.setOpacity(0.5);

                    editarIcon.setStyle("-fx-cursor: hand;");
                    editarIcon.setOnMouseClicked(e -> editarReserva(reserva));
                    editarIcon.setFitHeight(16);
                    editarIcon.setFitWidth(16);

                    cancelarIcon.setDisable(true); //No se puede cancelar una reserva cancelada
                    cancelarIcon.setOpacity(0.5);
                    cancelarIcon.setFitHeight(16);
                    cancelarIcon.setFitWidth(16);

                } else { // Reserva activa
                    editarIcon.setFitHeight(16);
                    editarIcon.setFitWidth(16);
                    editarIcon.setDisable(true);
                    editarIcon.setOpacity(0.5);

                    cancelarIcon.setFitHeight(16);
                    cancelarIcon.setFitWidth(16);
                    cancelarIcon.setStyle("-fx-cursor: hand;");
                    cancelarIcon.setOnMouseClicked(e -> cancelarReserva(reserva));
                }

                HBox accionesBox = new HBox(5, editarIcon, cancelarIcon);
                accionesBox.setPrefWidth(100);

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
                estado.setStyle(cellStyle);

                if (reserva.getEstado() == 'C') {
                    estado.setStyle("-fx-text-fill: #f44336; -fx-font-weight: bold;");
                    row.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
                } else {
                    estado.setStyle("-fx-text-fill: #4CAF50; -fx-font-weight: bold;");
                }

                row.getChildren().addAll(checkBox, espectaculo, butaca,
                        usuario, precio, estado, accionesBox);
                reservasVBox.getChildren().add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar las reservas");
        }
    }

    private void editarReserva(Reservas reserva) {
        // Implementar lógica de edición
        System.out.println("Editar reserva: " + reserva.getId_reserva());
        if (reserva.getEstado()=='C'){
            try {
                reservasDao.reactivarReserva(reserva.getId_reserva());
                mostrarMensaje("INFORMACIÓN", "Se reactivó la reserva correctamente.");
                cargarReservas();
            } catch (SQLException e) {
                mostrarError("Hubo un error al reactivar la reserva. Inténtelo de nuevo.");
                throw new RuntimeException(e);
            }
        }
        else if (reserva.getEstado()=='O'){
            try {
                reservasDao.cancelarReserva(reserva.getId_reserva());
                mostrarMensaje("INFORMACIÓN", "Se canceló la reserva correctamente.");
                cargarReservas();
            } catch (SQLException e) {
                mostrarError("Hubo un error al cancelar la reserva. Inténtelo de nuevo.");
                throw new RuntimeException(e);
            }

        }
        else{
           System.err.println("Se esperaba un valor de estado diferente. Compruebe la base de datos.");
           mostrarError("Compruebe la BBDD.");
        }
    }

    private void cancelarReserva(Reservas reserva) {
        if (reserva.getEstado() == 'C') {
            mostrarError("Esta reserva ya está cancelada");
            return;
        }

        try {
            int resultado = reservasDao.cancelarReserva(reserva.getId_reserva());
            if (resultado > 0) {
                cargarReservas();
                mostrarMensaje("Reserva cancelada", "La reserva ha sido cancelada exitosamente");
            } else {
                mostrarError("No se pudo cancelar la reserva");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cancelar la reserva");
        }
    }

    private void editarSeleccionados() {
        List<Reservas> seleccionados = getReservasSeleccionadas();
        if (seleccionados.isEmpty()) {
            mostrarError("Seleccione al menos una reserva para editar");
            return;
        }
        if (seleccionados.size() > 1) {
            mostrarError("Solo puede editar una reserva a la vez");
            return;
        }
        editarReserva(seleccionados.get(0));
    }

    private void cancelarSeleccionados() {
        List<Reservas> seleccionados = getReservasSeleccionadas();
        if (seleccionados.isEmpty()) {
            mostrarError("Seleccione al menos una reserva para cancelar");
            return;
        }

        try {
            int canceladas = 0;
            for (Reservas reserva : seleccionados) {
                if (reserva.getEstado() != 'C') {
                    int resultado = reservasDao.cancelarReserva(reserva.getId_reserva());
                    if (resultado > 0) {
                        canceladas++;
                    }
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