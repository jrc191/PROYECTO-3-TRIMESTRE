package controllers.admin;

import dao.MensajesDaoI;
import dao.impl.MensajesDaoImpl;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import models.Mensajes;
import utils.DatabaseConnection;

import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

public class ListarMensajesController {
    @FXML public Button guardarBtn;
    @FXML public Button cancelarBtn;
    @FXML public TableView<Mensajes> tablaMensajes;
    @FXML public TableColumn<Mensajes, Boolean> colSeleccionar;
    @FXML public TableColumn<Mensajes, Integer> colId;
    @FXML public TableColumn<Mensajes, String> colUsuario;
    @FXML public TableColumn<Mensajes, String> colReserva;
    @FXML public TableColumn<Mensajes, String> colFecha;
    @FXML public TableColumn<Mensajes, String> colTipo;
    @FXML public TableColumn<Mensajes, String> colEstado;
    @FXML public TableColumn<Mensajes, Void> colAcciones;


    private MensajesDaoI mensajeDao;
    private final Map<Integer, Character> cambiosPendientes = new HashMap<>();
    private final Map<Integer, Character> estadosOriginales = new HashMap<>();
    private final ObservableList<Mensajes> mensajesData = javafx.collections.FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);

        guardarBtn.setOnAction(e -> confirmarCambios());
        cancelarBtn.setOnAction(e -> cancelarCambios());

        try {
            Connection conn = DatabaseConnection.getConnection();
            this.mensajeDao = new MensajesDaoImpl(conn);
            inicializarTabla();

            cargarMensajes();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");
        }
    }

    private void inicializarTabla() {



        colSeleccionar.setCellValueFactory(param -> param.getValue().seleccionadoProperty());
        colSeleccionar.setCellFactory(CheckBoxTableCell.forTableColumn(colSeleccionar));

        colId.setCellValueFactory(new PropertyValueFactory<>("id_solicitud"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("id_usuario"));
        colReserva.setCellValueFactory(new PropertyValueFactory<>("id_reserva"));
        colFecha.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFecha().toString()));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo_solicitud"));

        colEstado.setCellFactory(ComboBoxTableCell.forTableColumn("P - Pendiente", "A - Aprobada", "R - Rechazada"));
        colEstado.setCellValueFactory(param -> {
            String estado = switch (param.getValue().getEstado_solicitud()) {
                case 'A' -> "A - Aprobada";
                case 'R' -> "R - Rechazada";
                default -> "P - Pendiente";
            };
            return new SimpleStringProperty(estado);
        });

        colEstado.setOnEditCommit(event -> {
            Mensajes mensaje = event.getRowValue();
            char nuevoEstado = event.getNewValue().charAt(0);
            mensaje.setEstado_solicitud(nuevoEstado);
            cambiosPendientes.put(mensaje.getId_solicitud(), nuevoEstado);
            guardarBtn.setDisable(cambiosPendientes.isEmpty());
            cancelarBtn.setDisable(cambiosPendientes.isEmpty());
        });

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button eliminarBtn = new Button("X");

            {
                eliminarBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold; -fx-cursor: hand;");
                eliminarBtn.setOnAction(e -> {
                    Mensajes mensaje = getTableView().getItems().get(getIndex());
                    eliminarMensaje(mensaje);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(eliminarBtn);
                }
            }
        });

        tablaMensajes.setItems(mensajesData);
        tablaMensajes.setEditable(true);
    }

    private void cargarMensajes() {
        try {
            List<Mensajes> mensajes = mensajeDao.mostrarMensajes();
            mostrarMensajes(mensajes);
            cambiosPendientes.clear();
            guardarBtn.setDisable(true);
            cancelarBtn.setDisable(true);
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al cargar los mensajes");
        }
    }

    private void mostrarMensajes(List<Mensajes> mensajes) {
        mensajesData.clear();
        estadosOriginales.clear();

        for (Mensajes m : mensajes) {
            if (m.seleccionadoProperty() == null) {
                m.setSeleccionado(new SimpleBooleanProperty(false));
            }
            estadosOriginales.put(m.getId_solicitud(), m.getEstado_solicitud());
        }

        mensajesData.addAll(mensajes);
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

                        boolean exito = mensajeDao.actualizarEstadoMensaje(id, nuevoEstado);

                        if (exito) {
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
                cargarMensajes();
                guardarBtn.setDisable(true);
                cancelarBtn.setDisable(true);
                mostrarAlerta("Información", "Cambios cancelados", Alert.AlertType.INFORMATION);
            }
        });
    }

    private void eliminarMensaje(Mensajes mensaje) {
        try {
            boolean exito = mensajeDao.eliminarMensaje(mensaje.getId_solicitud());
            if (exito) {
                mensajesData.remove(mensaje);
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
