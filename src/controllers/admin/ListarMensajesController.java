package controllers.admin;

import dao.MensajesDaoI;
import dao.impl.MensajesDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Mensajes;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ListarMensajesController {
    @FXML public ImageView actualizarBtn;
    @FXML public ImageView eliminarBtn;
    @FXML public Button guardarBtn;
    @FXML public Button cancelarBtn;
    @FXML public ScrollPane scrollMensajes;
    @FXML public VBox scrollVBox;

    private MensajesDaoI mensajeDao;
    private List<Mensajes> mensajesOriginal;
    private List<CheckBox> checkBoxes = new ArrayList<>();
    private Map<Integer, Character> cambiosPendientes = new HashMap<>();
    private Map<Integer, Character> estadosOriginales = new HashMap<>();

    @FXML
    public void initialize() {
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);

        guardarBtn.setOnAction(e -> confirmarCambios());
        cancelarBtn.setOnAction(e -> cancelarCambios());
        actualizarBtn.setOnMouseClicked(e -> cargarMensajes());

        try {
            Connection conn = DatabaseConnection.getConnection();
            this.mensajeDao = new MensajesDaoImpl(conn);
            cargarMensajes();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");
        }
    }

    private void cargarMensajes() {
        try {
            List<Mensajes> mensajes = mensajeDao.mostrarMensajes();
            mostrarMensajes(mensajes);
            cambiosPendientes.clear(); // Limpiar cambios pendientes al recargar
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar los mensajes");
        }
    }

    public void mostrarMensajes(List<Mensajes> mensajes) {
        scrollVBox.getChildren().clear();
        checkBoxes.clear();
        mensajesOriginal = mensajes;
        estadosOriginales.clear(); // Limpiar estados originales

        // Mantener el encabezado
        if (scrollVBox.getChildren().isEmpty()) {
            HBox header = new HBox();
            header.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            Label seleccionHeader = new Label(" V");
            Label idHeader = new Label("ID");
            Label usuarioHeader = new Label("Usuario");
            Label reservaHeader = new Label("Reserva");
            Label fechaHeader = new Label("Fecha");
            Label tipoHeader = new Label("Tipo");
            Label estadoHeader = new Label("Estado");
            Label accionesHeader = new Label("Acciones");

            // Ajustar ancho de columnas
            seleccionHeader.setPrefWidth(80);
            idHeader.setPrefWidth(50);
            usuarioHeader.setPrefWidth(100);
            reservaHeader.setPrefWidth(120);
            fechaHeader.setPrefWidth(150);
            tipoHeader.setPrefWidth(100);
            estadoHeader.setPrefWidth(80);
            accionesHeader.setPrefWidth(90);

            // Estilo del encabezado
            String headerStyle = "-fx-text-fill: #6c757d; -fx-font-weight: bold;";
            seleccionHeader.setStyle(headerStyle);
            idHeader.setStyle(headerStyle);
            usuarioHeader.setStyle(headerStyle);
            reservaHeader.setStyle(headerStyle);
            fechaHeader.setStyle(headerStyle);
            tipoHeader.setStyle(headerStyle);
            estadoHeader.setStyle(headerStyle);
            accionesHeader.setStyle(headerStyle);

            header.getChildren().addAll(seleccionHeader, idHeader, usuarioHeader, reservaHeader,
                    fechaHeader, tipoHeader, estadoHeader, accionesHeader);
            scrollVBox.getChildren().add(header);
        }

        // Filas de datos
        for (Mensajes mensaje : mensajes) {
            HBox row = new HBox();
            row.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");

            // Checkbox para selección
            CheckBox checkBox = new CheckBox();
            checkBox.setPrefWidth(80);
            checkBoxes.add(checkBox);

            Label id = new Label(String.valueOf(mensaje.getId_solicitud()));
            Label usuario = new Label(mensaje.getId_usuario());
            Label reserva = new Label(mensaje.getId_reserva());
            Label fecha = new Label(mensaje.getFecha().toString());
            Label tipo = new Label(mensaje.getTipo_solicitud());

            // Guardar estado original
            estadosOriginales.put(mensaje.getId_solicitud(), mensaje.getEstado_solicitud());

            // ComboBox para estado
            ComboBox<String> estadoCombo = new ComboBox<>();
            estadoCombo.getItems().addAll("P - Pendiente", "A - Aprobada", "R - Rechazada");

            // Establecer valor actual
            char estadoActual = mensaje.getEstado_solicitud();
            switch (estadoActual) {
                case 'P': estadoCombo.setValue("P - Pendiente"); break;
                case 'A': estadoCombo.setValue("A - Aprobada"); break;
                case 'R': estadoCombo.setValue("R - Rechazada"); break;
                default: estadoCombo.setValue("P - Pendiente");
            }

            // Listener para cambios en el ComboBox
            estadoCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    char nuevoEstado = newVal.charAt(0);
                    char estadoOriginal = estadosOriginales.get(mensaje.getId_solicitud());

                    if (nuevoEstado != estadoOriginal) {
                        cambiosPendientes.put(mensaje.getId_solicitud(), nuevoEstado);
                    } else {
                        cambiosPendientes.remove(mensaje.getId_solicitud());
                    }

                    // Actualizar visibilidad de botones
                    guardarBtn.setDisable(cambiosPendientes.isEmpty());
                    cancelarBtn.setDisable(cambiosPendientes.isEmpty());
                }
            });

            estadoCombo.setPrefWidth(80);

            ImageView eliminarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/cancel.png")));

            eliminarIcon.setFitHeight(16);
            eliminarIcon.setFitWidth(16);
            eliminarIcon.setStyle("-fx-cursor: hand;");
            eliminarIcon.setOnMouseClicked(e -> eliminarMensaje(mensaje));

            HBox accionesBox = new HBox(5, eliminarIcon);
            accionesBox.setPrefWidth(90);

            id.setPrefWidth(50);
            usuario.setPrefWidth(100);
            reserva.setPrefWidth(120);
            fecha.setPrefWidth(150);
            tipo.setPrefWidth(90);
            accionesBox.setPrefWidth(90);

            String cellStyle = "-fx-text-fill: #495057;";
            id.setStyle(cellStyle);
            usuario.setStyle(cellStyle);
            reserva.setStyle(cellStyle);
            fecha.setStyle(cellStyle);
            tipo.setStyle(cellStyle);

            row.getChildren().addAll(checkBox, id, usuario, reserva, fecha, tipo, estadoCombo, accionesBox);
            scrollVBox.getChildren().add(row);
        }
    }

    private void confirmarCambios() {
        if (cambiosPendientes.isEmpty()) {
            mostrarAlerta("Información", "No hay cambios pendientes para guardar", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar cambios");
        confirmacion.setHeaderText("¿Está seguro que desea guardar los cambios?");
        confirmacion.setContentText("Se actualizarán " + cambiosPendientes.size() + " mensajes.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    for (Map.Entry<Integer, Character> entry : cambiosPendientes.entrySet()) {
                        int id = entry.getKey();
                        char nuevoEstado = entry.getValue();

                        // Actualizar en base de datos
                        boolean exito = mensajeDao.actualizarEstadoMensaje(id, nuevoEstado);

                        if (exito) {
                            // Actualizar estado original si la operación fue exitosa
                            estadosOriginales.put(id, nuevoEstado);
                        } else {
                            mostrarError("No se pudo actualizar el mensaje con ID: " + id);
                        }
                    }

                    cambiosPendientes.clear();
                    guardarBtn.setDisable(true);
                    cancelarBtn.setDisable(true);

                    mostrarAlerta("Éxito", "Cambios guardados correctamente", Alert.AlertType.INFORMATION);
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al guardar los cambios: " + e.getMessage());
                }
            }
        });
    }

    private void cancelarCambios() {
        if (cambiosPendientes.isEmpty()) {
            mostrarAlerta("Información", "No hay cambios pendientes para cancelar", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar cambios");
        confirmacion.setHeaderText("¿Está seguro que desea descartar los cambios?");
        confirmacion.setContentText("Se perderán " + cambiosPendientes.size() + " cambios pendientes.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                cambiosPendientes.clear();
                cargarMensajes(); // Recargar para restaurar los estados originales
                guardarBtn.setDisable(true);
                cancelarBtn.setDisable(true);
                mostrarAlerta("Información", "Cambios cancelados", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void editarMensaje(Mensajes mensaje) {
        // Implementar lógica de edición si es necesario
    }

    private void eliminarMensaje(Mensajes mensaje) {
        try {
            boolean exito = mensajeDao.eliminarMensaje(mensaje.getId_solicitud());
            if (exito) {
                mensajesOriginal.remove(mensaje);
                mostrarMensajes(mensajesOriginal);
                mostrarAlerta("Éxito", "Mensaje eliminado correctamente", Alert.AlertType.INFORMATION);
            } else {
                mostrarError("No se pudo eliminar el mensaje");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al eliminar el mensaje");
        }
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.show();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}