package controllers.admin;

import dao.MensajesDaoI;
import dao.impl.MensajesDaoImpl;
import dao.impl.ReservaDaoImpl;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import models.Mensajes;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ListarMensajesController {
    @FXML public Button guardarBtn;
    @FXML public Button cancelarBtn;
    @FXML public ImageView eliminarBtn;
    @FXML public ScrollPane scrollMensajes;
    @FXML public VBox mensajesVBox;

    private MensajesDaoI mensajeDao;
    private final Map<Integer, Character> cambiosPendientes = new HashMap<>();
    private final Map<Integer, Character> estadosOriginales = new HashMap<>();
    private List<Mensajes> mensajesList = new ArrayList<>();
    private Map<Integer, ComboBox<String>> estadosComboBoxes = new HashMap<>();
    private Map<Integer, HBox> filasMap = new HashMap<>();
    private ListarReservasController reservasController;

    @FXML
    public void initialize() {
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);

        guardarBtn.setOnAction(e -> confirmarCambios());
        cancelarBtn.setOnAction(e -> cancelarCambios());
        eliminarBtn.setOnMouseClicked(e -> eliminarSeleccionados());

        try {
            Connection conn = DatabaseConnection.getConnection();
            this.mensajeDao = new MensajesDaoImpl(conn);
            cargarMensajes();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");

            // Mostrar mensaje de error en la interfaz
            mostrarMensajesError("Error al conectar con la base de datos");
        }
    }

    private void mostrarMensajesError(String mensaje) {
        mensajesVBox.getChildren().clear();

        Label errorLabel = new Label(mensaje);
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f; -fx-padding: 20px;");

        VBox contenedorError = new VBox(errorLabel);
        contenedorError.setAlignment(Pos.CENTER);
        contenedorError.setPrefHeight(100);
        mensajesVBox.getChildren().add(contenedorError);

        eliminarBtn.setDisable(true);
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);
    }

    private void cargarMensajes() {
        try {
            mensajesList = mensajeDao.mostrarMensajes();
            mostrarMensajes();
            cambiosPendientes.clear();
            guardarBtn.setDisable(true);
            cancelarBtn.setDisable(true);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar los mensajes");
            mostrarMensajesError("Error al cargar los mensajes: " + e.getMessage());
        }
    }

    private void mostrarMensajes() {
        mensajesVBox.getChildren().clear();
        estadosOriginales.clear();
        estadosComboBoxes.clear();
        filasMap.clear();

        guardarBtn.setVisible(false);
        cancelarBtn.setVisible(false);

        // Cabecera
        HBox cabecera = crearCabeceraTabla();
        mensajesVBox.getChildren().add(cabecera);

        if (mensajesList.isEmpty()) {
            // Mostrar mensaje cuando no hay mensajes
            Label noMensajesLabel = new Label("No existen mensajes actualmente");
            noMensajesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-padding: 20px;");

            VBox contenedorMensaje = new VBox(noMensajesLabel);
            contenedorMensaje.setAlignment(Pos.CENTER);
            contenedorMensaje.setPrefHeight(100);
            mensajesVBox.getChildren().add(contenedorMensaje);

            // Deshabilitar el botón de eliminar ya que no hay mensajes
            eliminarBtn.setDisable(true);
        } else {
            // Habilitar el botón de eliminar
            eliminarBtn.setDisable(false);

            // Filas de datos
            for (Mensajes mensaje : mensajesList) {
                HBox fila = crearFilaMensaje(mensaje);
                mensajesVBox.getChildren().add(fila);
                filasMap.put(mensaje.getId_solicitud(), fila);
                estadosOriginales.put(mensaje.getId_solicitud(), mensaje.getEstado_solicitud());
            }
        }
    }

    private HBox crearCabeceraTabla() {
        HBox cabecera = new HBox();
        cabecera.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        cabecera.setPadding(new Insets(10));
        cabecera.setAlignment(Pos.CENTER_LEFT);

        // Definir los encabezados con el mismo ancho que las columnas de datos
        Label seleccionar = crearLabelCabecera("", 50);
        Label id = crearLabelCabecera("ID", 50);
        Label usuario = crearLabelCabecera("Usuario", 120);
        Label reserva = crearLabelCabecera("Reserva", 100);
        Label fecha = crearLabelCabecera("Fecha", 120);
        Label tipo = crearLabelCabecera("Tipo", 100);
        Label estado = crearLabelCabecera("Estado", 100);
        Label acciones = crearLabelCabecera("Acciones", 80);

        cabecera.getChildren().addAll(seleccionar, id, usuario, reserva, fecha, tipo, estado, acciones);
        return cabecera;
    }

    private Label crearLabelCabecera(String texto, double ancho) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(label, Priority.SOMETIMES);
        label.setPrefWidth(ancho);
        return label;
    }

    private HBox crearFilaMensaje(Mensajes mensaje) {
        HBox fila = new HBox();
        fila.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        fila.setPadding(new Insets(8));
        fila.setAlignment(Pos.CENTER_LEFT);

        CheckBox seleccion = new CheckBox();
        seleccion.setSelected(false);
        HBox seleccionBox = new HBox(seleccion);
        seleccionBox.setAlignment(Pos.CENTER);
        seleccionBox.setPrefWidth(50);

        Label id = new Label(String.valueOf(mensaje.getId_solicitud()));
        id.setPrefWidth(50);

        Label usuario = new Label(mensaje.getId_usuario());
        usuario.setPrefWidth(120);

        Label reserva = new Label(mensaje.getId_reserva());
        reserva.setPrefWidth(100);

        Label fecha = new Label(mensaje.getFecha().toString());
        fecha.setPrefWidth(120);

        Label tipo = new Label(mensaje.getTipo_solicitud());
        tipo.setPrefWidth(100);

        ComboBox<String> estadoCombo = new ComboBox<>();
        estadoCombo.getItems().addAll("P - Pendiente", "A - Aprobada", "R - Rechazada");
        estadoCombo.setPrefWidth(100);

        // Establecer valor inicial del combo
        String valorInicial = switch (mensaje.getEstado_solicitud()) {
            case 'A' -> "A - Aprobada";
            case 'R' -> "R - Rechazada";
            default -> "P - Pendiente";
        };
        estadoCombo.setValue(valorInicial);

        estadoCombo.setOnAction(e -> {
            String seleccionado = estadoCombo.getValue();
            char nuevoEstado = seleccionado.charAt(0);

            if (seleccionado==valorInicial){
                guardarBtn.setVisible(false);
                cancelarBtn.setVisible(false);
            }

            if (mensaje.getEstado_solicitud() != nuevoEstado) {
                cambiosPendientes.put(mensaje.getId_solicitud(), nuevoEstado);
                guardarBtn.setDisable(cambiosPendientes.isEmpty());
                cancelarBtn.setDisable(cambiosPendientes.isEmpty());
                guardarBtn.setVisible(true);
                cancelarBtn.setVisible(true);

            } else {
                cambiosPendientes.remove(mensaje.getId_solicitud());
                guardarBtn.setDisable(cambiosPendientes.isEmpty());
                cancelarBtn.setDisable(cambiosPendientes.isEmpty());

            }


        });
        estadosComboBoxes.put(mensaje.getId_solicitud(), estadoCombo);

        Button eliminarBtn = new Button("X");
        eliminarBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold; -fx-cursor: hand;");
        eliminarBtn.setOnAction(e -> eliminarMensaje(mensaje));
        HBox accionesBox = new HBox(eliminarBtn);
        accionesBox.setAlignment(Pos.CENTER);
        accionesBox.setPrefWidth(80);

        fila.getChildren().addAll(seleccionBox, id, usuario, reserva, fecha, tipo, estadoCombo, accionesBox);
        return fila;
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

        Optional<ButtonType> resultadoConfirmacion = confirmacion.showAndWait();

        if (resultadoConfirmacion.isPresent() && resultadoConfirmacion.get() == ButtonType.OK) {
            Connection conn = null;
            try {
                conn = DatabaseConnection.getConnection();
                conn.setAutoCommit(false);

                MensajesDaoImpl mensajeDao = new MensajesDaoImpl(conn);
                List<String> reservasParaCancelar = new ArrayList<>();

                for (Map.Entry<Integer, Character> entry : cambiosPendientes.entrySet()) {
                    int id = entry.getKey();
                    char nuevoEstado = entry.getValue();

                    Mensajes mensaje = mensajesList.stream()
                            .filter(m -> m.getId_solicitud() == id)
                            .findFirst()
                            .orElse(null);

                    if (mensaje != null) {
                        // Si se aprueba un mensaje de cancelación, registrar la reserva
                        if (nuevoEstado == 'A' && "Cancelación".equalsIgnoreCase(mensaje.getTipo_solicitud())) {
                            reservasParaCancelar.add(mensaje.getId_reserva());
                        }

                        // Actualizar el estado del mensaje
                        boolean exito = mensajeDao.actualizarEstadoMensaje(id, nuevoEstado);

                        if (!exito) {
                            conn.rollback();
                            mostrarError("No se pudo actualizar el mensaje con ID: " + id);
                            return;
                        }

                        // Actualizar el objeto en memoria
                        mensaje.setEstado_solicitud(nuevoEstado);
                        estadosOriginales.put(id, nuevoEstado);
                    }
                }

                conn.commit();

                // Mostrar alerta simple con IDs de reservas a cancelar
                if (!reservasParaCancelar.isEmpty()) {
                    StringBuilder mensaje = new StringBuilder("Debe eliminar las siguientes reservas:\n\n");
                    for (String idReserva : reservasParaCancelar) {
                        mensaje.append("ID_RESERVA: ").append(idReserva).append("\n");
                    }

                    Alert alertaReservas = new Alert(Alert.AlertType.WARNING);
                    alertaReservas.setTitle("Reservas para eliminar");
                    alertaReservas.setHeaderText("Atención Administrador");
                    alertaReservas.setContentText(mensaje.toString());
                    alertaReservas.showAndWait();
                }

                mostrarAlerta("Éxito", "Cambios guardados correctamente", Alert.AlertType.INFORMATION);
                cargarMensajes();

                // Actualizar vista de reservas si está disponible
                if (reservasController != null) {
                    reservasController.cargarReservas();
                }

            } catch (SQLException e) {
                try {
                    if (conn != null) conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                e.printStackTrace();
                mostrarError("Error al guardar los cambios: " + e.getMessage());
            } finally {
                try {
                    if (conn != null) conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                cambiosPendientes.clear();
                guardarBtn.setDisable(true);
                cancelarBtn.setDisable(true);
                guardarBtn.setVisible(false);
                cancelarBtn.setVisible(false);
            }
        }
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
                // Restaurar estados originales en los ComboBox
                for (Map.Entry<Integer, Character> entry : estadosOriginales.entrySet()) {
                    int id = entry.getKey();
                    char estadoOriginal = entry.getValue();
                    ComboBox<String> combo = estadosComboBoxes.get(id);
                    if (combo != null) {
                        String valorOriginal = switch (estadoOriginal) {
                            case 'A' -> "A - Aprobada";
                            case 'R' -> "R - Rechazada";
                            default -> "P - Pendiente";
                        };
                        combo.setValue(valorOriginal);
                    }
                }

                cambiosPendientes.clear();
                guardarBtn.setDisable(true);
                cancelarBtn.setDisable(true);
                guardarBtn.setVisible(false);
                cancelarBtn.setVisible(false);
                mostrarAlerta("Información", "Cambios cancelados", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void eliminarMensaje(Mensajes mensaje) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar este mensaje?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    boolean exito = mensajeDao.eliminarMensaje(mensaje.getId_solicitud());
                    if (exito) {
                        HBox fila = filasMap.get(mensaje.getId_solicitud());
                        if (fila != null) {
                            mensajesVBox.getChildren().remove(fila);
                        }
                        mensajesList.remove(mensaje);

                        if (mensajesList.isEmpty()){
                            Label noMensajesLabel = new Label("No existen mensajes actualmente");
                            noMensajesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-padding: 20px;");
                            VBox contenedorMensaje = new VBox(noMensajesLabel);
                            contenedorMensaje.setAlignment(Pos.CENTER);
                            contenedorMensaje.setPrefHeight(100);
                            mensajesVBox.getChildren().add(contenedorMensaje);

                            eliminarBtn.setDisable(true);
                        } else {
                            eliminarBtn.setDisable(false);
                        }

                        filasMap.remove(mensaje.getId_solicitud());
                        estadosComboBoxes.remove(mensaje.getId_solicitud());
                        cambiosPendientes.remove(mensaje.getId_solicitud());

                        guardarBtn.setDisable(cambiosPendientes.isEmpty());
                        cancelarBtn.setDisable(cambiosPendientes.isEmpty());

                        mostrarAlerta("Éxito", "Mensaje eliminado correctamente", Alert.AlertType.INFORMATION);
                    } else {
                        mostrarError("No se pudo eliminar el mensaje");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar el mensaje");
                }
            }
        });
    }

    private void eliminarSeleccionados() {
        List<Mensajes> seleccionados = new ArrayList<>();

        // Recorrer las filas para encontrar los CheckBox seleccionados
        for (HBox fila : filasMap.values()) {
            CheckBox checkBox = (CheckBox) ((HBox) fila.getChildren().get(0)).getChildren().get(0);
            if (checkBox.isSelected()) {
                // Buscar el mensaje correspondiente a esta fila
                int id = Integer.parseInt(((Label) fila.getChildren().get(1)).getText());
                for (Mensajes m : mensajesList) {
                    if (m.getId_solicitud() == id) {
                        seleccionados.add(m);
                        break;
                    }
                }
            }
        }

        if (seleccionados.isEmpty()) {
            mostrarAlerta("Información", "No hay mensajes seleccionados para eliminar", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación múltiple");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar los mensajes seleccionados?");
        confirmacion.setContentText("Se eliminarán " + seleccionados.size() + " mensajes. Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int eliminados = 0;
                for (Mensajes mensaje : seleccionados) {
                    try {
                        boolean exito = mensajeDao.eliminarMensaje(mensaje.getId_solicitud());
                        if (exito) {
                            HBox fila = filasMap.get(mensaje.getId_solicitud());
                            if (fila != null) {
                                mensajesVBox.getChildren().remove(fila);
                            }
                            eliminados++;

                            // Limpiar referencias
                            filasMap.remove(mensaje.getId_solicitud());
                            estadosComboBoxes.remove(mensaje.getId_solicitud());
                            cambiosPendientes.remove(mensaje.getId_solicitud());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                // Actualizar la lista de mensajes
                mensajesList.removeAll(seleccionados);

                // Actualizar estado de botones
                guardarBtn.setDisable(cambiosPendientes.isEmpty());
                cancelarBtn.setDisable(cambiosPendientes.isEmpty());

                if (eliminados > 0) {
                    mostrarAlerta("Éxito", "Se han eliminado " + eliminados + " mensajes", Alert.AlertType.INFORMATION);
                } else {
                    mostrarError("No se pudo eliminar ningún mensaje");
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

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }

    public void setReservasController(ListarReservasController controller) {
        this.reservasController = controller;
        // Verificar que el controlador no sea nulo y tenga conexión
        if (reservasController != null) {
            try {
                Connection conn = DatabaseConnection.getConnection();
                reservasController.initialize();
                reservasController.cargarReservas();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}