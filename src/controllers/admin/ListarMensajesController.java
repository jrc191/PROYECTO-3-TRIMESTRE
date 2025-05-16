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

    @FXML
    public void initialize() {
        guardarBtn.setOnAction(e -> actualizarEstadoMensaje());
        cancelarBtn.setOnAction(e -> borrarNuevoMensaje());
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
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar los mensajes");
        }
    }

    public void mostrarMensajes(List<Mensajes> mensajes) {
        scrollVBox.getChildren().clear();
        checkBoxes.clear();
        mensajesOriginal = mensajes;

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
            accionesHeader.setPrefWidth(100);

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

            // ComboBox para estado
            ComboBox<String> estadoCombo = new ComboBox<>();
            estadoCombo.getItems().addAll("P - Pendiente", "A - Aprobada", "R - Rechazada");

            // Establecer valor actual
            switch (mensaje.getEstado_solicitud()) {
                case 'P': estadoCombo.setValue("P - Pendiente"); break;
                case 'A': estadoCombo.setValue("A - Aprobada"); break;
                case 'R': estadoCombo.setValue("R - Rechazada"); break;
                default: estadoCombo.setValue("P - Pendiente");
            }

            estadoCombo.setPrefWidth(80);

            // Botones de acción
            ImageView editarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/editar.png")));
            ImageView eliminarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/eliminar.png")));

            editarIcon.setFitHeight(16);
            editarIcon.setFitWidth(16);
            editarIcon.setStyle("-fx-cursor: hand;");
            editarIcon.setOnMouseClicked(e -> editarMensaje(mensaje));

            eliminarIcon.setFitHeight(16);
            eliminarIcon.setFitWidth(16);
            eliminarIcon.setStyle("-fx-cursor: hand;");
            eliminarIcon.setOnMouseClicked(e -> eliminarMensaje(mensaje));

            HBox accionesBox = new HBox(5, editarIcon, eliminarIcon);
            accionesBox.setPrefWidth(100);

            id.setPrefWidth(50);
            usuario.setPrefWidth(100);
            reserva.setPrefWidth(120);
            fecha.setPrefWidth(150);
            tipo.setPrefWidth(100);
            accionesBox.setPrefWidth(100);

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

    private void actualizarEstadoMensaje() {
        try {
            for (int i = 0; i < scrollVBox.getChildren().size(); i++) {
                if (i == 0) continue; // Saltar el encabezado

                HBox row = (HBox) scrollVBox.getChildren().get(i);
                ComboBox<String> estadoCombo = (ComboBox<String>) row.getChildren().get(6);
                String estadoSeleccionado = estadoCombo.getValue();
                char nuevoEstado = estadoSeleccionado.charAt(0);

                Mensajes mensaje = mensajesOriginal.get(i-1);
                if (mensaje.getEstado_solicitud() != nuevoEstado) {
                    mensaje.setEstado_solicitud(nuevoEstado);
                    mensajeDao.actualizarEstadoMensaje(mensaje.getId_solicitud(), nuevoEstado);
                }
            }
            mostrarAlerta("Éxito", "Estados actualizados correctamente", Alert.AlertType.INFORMATION);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al actualizar los estados");
        }
    }

    private void borrarNuevoMensaje() {
        // Implementar lógica de cancelación si es necesario
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