package controllers.admin;

import dao.MensajesDaoI;
import dao.impl.MensajesDaoImpl;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
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
        configurarBotonesIniciales(); // Configuración de botones
        inicializarConexionYDatos(); // Inicialización de conexión y carga de datos
    }

    // Configuración de botones inicial y eventos
    private void configurarBotonesIniciales() {
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);
        guardarBtn.setOnAction(e -> confirmarCambios());
        cancelarBtn.setOnAction(e -> cancelarCambios());
        eliminarBtn.setOnMouseClicked(e -> eliminarSeleccionados());
    }

    // Inicialización de conexión y carga de datos
    private void inicializarConexionYDatos() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.mensajeDao = new MensajesDaoImpl(conn);
            cargarMensajes();
        } catch (SQLException e) {
            manejarErrorConexion(e);
        }
    }

    // Manejo de errores de conexión
    private void manejarErrorConexion(SQLException e) {
        e.printStackTrace();
        mostrarMensajeErrorEnInterfaz("Error al conectar con la base de datos");
    }

    // Mostrar mensaje de error en la interfaz
    private void mostrarMensajeErrorEnInterfaz(String mensaje) {
        mensajesVBox.getChildren().clear();
        Label errorLabel = crearLabelError(mensaje);
        VBox contenedorError = crearContenedorError(errorLabel);
        mensajesVBox.getChildren().add(contenedorError);
        deshabilitarBotonesAccion();
    }

    // Crear label de error
    private Label crearLabelError(String mensaje) {
        Label label = new Label(mensaje);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f; -fx-padding: 20px;");
        return label;
    }

    // Crear contenedor de error
    private VBox crearContenedorError(Label label) {
        VBox contenedor = new VBox(label);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefHeight(100);
        return contenedor;
    }

    // Deshabilitar botones de acción
    private void deshabilitarBotonesAccion() {
        eliminarBtn.setDisable(true);
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);
    }

    // Cargar mensajes desde la base de datos
    private void cargarMensajes() {
        try {
            //Método para cargar mensajes desde la BBDD
            mensajesList = mensajeDao.mostrarMensajes();

            mostrarMensajesEnInterfaz();

            //reseteamos si hay cambios pendientes
            limpiarCambiosPendientes();
        } catch (SQLException e) {
            manejarErrorCargaMensajes(e);
        }
    }

    // Mostrar mensajes en la interfaz
    private void mostrarMensajesEnInterfaz() {
        mensajesVBox.getChildren().clear(); // Limpiar la vista antes de agregar nuevos mensajes
        reiniciarEstadoInterfaz();
        agregarCabeceraTabla();

        if (mensajesList.isEmpty()) {
            mostrarMensajeNoHayDatos();
        } else {
            habilitarBotonesAccion();
            crearFilasMensajes();
        }
    }

    // Reiniciar el estado de la interfaz
    private void reiniciarEstadoInterfaz() {
        estadosOriginales.clear();
        estadosComboBoxes.clear();
        filasMap.clear();
        ocultarBotonesConfirmacion();
    }

    // Ocultar botones de confirmación
    private void ocultarBotonesConfirmacion() {
        guardarBtn.setVisible(false);
        cancelarBtn.setVisible(false);
    }

    // Método para agregar cabecera a la tabla
    private void agregarCabeceraTabla() {
        HBox cabecera = crearCabeceraTabla();
        mensajesVBox.getChildren().add(cabecera);
    }

    // Método para crear cabecera de la tabla
    private HBox crearCabeceraTabla() {
        HBox cabecera = new HBox();
        cabecera.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        cabecera.setPadding(new Insets(10));
        cabecera.setAlignment(Pos.CENTER_LEFT);

        // Crear etiquetas de cabecera con estilos y tamaños
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

    // Método para crear etiquetas de cabecera
    private Label crearLabelCabecera(String texto, double ancho) {
        Label label = new Label(texto);
        label.setStyle("-fx-font-weight: bold;");
        HBox.setHgrow(label, Priority.SOMETIMES);
        label.setPrefWidth(ancho);
        return label;
    }

    // Método auxiliar para mostrar mensaje cuando no hay datos
    private void mostrarMensajeNoHayDatos() {
        Label noMensajesLabel = new Label("No existen mensajes actualmente");
        noMensajesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-padding: 20px;");

        VBox contenedorMensaje = new VBox(noMensajesLabel);
        contenedorMensaje.setAlignment(Pos.CENTER);
        contenedorMensaje.setPrefHeight(100);
        mensajesVBox.getChildren().add(contenedorMensaje);

        eliminarBtn.setDisable(true);
    }

    // Método para habilitar botones de acción
    private void habilitarBotonesAccion() {
        eliminarBtn.setDisable(false);
    }

    // Método para crear filas de mensajes. Crea y agrega cada fila a la VBox.
    private void crearFilasMensajes() {
        for (Mensajes mensaje : mensajesList) {
            HBox fila = crearFilaMensaje(mensaje);
            mensajesVBox.getChildren().add(fila);
            filasMap.put(mensaje.getId_solicitud(), fila);
            estadosOriginales.put(mensaje.getId_solicitud(), mensaje.getEstado_solicitud());
        }
    }

    // Método para crear una fila de mensaje
    private HBox crearFilaMensaje(Mensajes mensaje) {
        HBox fila = new HBox();
        fila.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        fila.setPadding(new Insets(8));
        fila.setAlignment(Pos.CENTER_LEFT); // Alinear al centro

        // Crear checkbox para seleccionar el mensaje
        CheckBox seleccion = new CheckBox();
        HBox seleccionBox = crearContenedorSeleccion(seleccion);

        Label id = crearLabelDato(String.valueOf(mensaje.getId_solicitud()), 50);
        Label usuario = crearLabelDato(mensaje.getId_usuario(), 120);
        Label reserva = crearLabelDato(mensaje.getId_reserva(), 100);
        Label fecha = crearLabelDato(mensaje.getFecha().toString(), 120);
        Label tipo = crearLabelDato(mensaje.getTipo_solicitud(), 100);

        ComboBox<String> estadoCombo = crearComboBoxEstado(mensaje);
        Button eliminarBtn = crearBotonEliminar(mensaje);

        fila.getChildren().addAll(seleccionBox, id, usuario, reserva, fecha, tipo, estadoCombo, crearContenedorAcciones(eliminarBtn));
        return fila;
    }

    // Método para crear contenedor de selección
    private HBox crearContenedorSeleccion(CheckBox checkBox) {
        HBox contenedor = new HBox(checkBox);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefWidth(50);
        return contenedor;
    }

    // Método para crear label de datos
    private Label crearLabelDato(String texto, double ancho) {
        Label label = new Label(texto);
        label.setPrefWidth(ancho);
        return label;
    }

    // Método para crear ComboBox de estado
    private ComboBox<String> crearComboBoxEstado(Mensajes mensaje) {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll("P - Pendiente", "A - Aprobada", "R - Rechazada");
        comboBox.setPrefWidth(100);
        comboBox.setValue(obtenerValorInicialEstado(mensaje));
        comboBox.setOnAction(e -> manejarCambioEstado(mensaje, comboBox));
        estadosComboBoxes.put(mensaje.getId_solicitud(), comboBox);
        return comboBox;
    }

    // Método auxiliar para obtener el valor inicial del estado (PENDIENTE, APROBADO, RECHAZADO)
    private String obtenerValorInicialEstado(Mensajes mensaje) {
        return switch (mensaje.getEstado_solicitud()) {
            case 'A' -> "A - Aprobada";
            case 'R' -> "R - Rechazada";
            default -> "P - Pendiente";
        };
    }

    // Método auxiliar para manejar el cambio de estado
    private void manejarCambioEstado(Mensajes mensaje, ComboBox<String> comboBox) {
        String seleccionado = comboBox.getValue();
        char nuevoEstado = seleccionado.charAt(0);

        // Actualizar el estado del mensaje si ha cambiado
        if (mensaje.getEstado_solicitud() != nuevoEstado) {
            cambiosPendientes.put(mensaje.getId_solicitud(), nuevoEstado);
        } else {
            cambiosPendientes.remove(mensaje.getId_solicitud());
        }

        // Actualizar el estado de los botones de confirmación
        actualizarEstadoBotonesConfirmacion();
    }

    // Método auxiliar para actualizar el estado de los botones de confirmación
    private void actualizarEstadoBotonesConfirmacion() {
        boolean hayCambios = !cambiosPendientes.isEmpty();
        guardarBtn.setDisable(!hayCambios);
        cancelarBtn.setDisable(!hayCambios);
        guardarBtn.setVisible(hayCambios);
        cancelarBtn.setVisible(hayCambios);
    }

    // Método para crear botón de eliminar mensaje
    private Button crearBotonEliminar(Mensajes mensaje) {
        Button boton = new Button("X");
        boton.setStyle("-fx-background-color: transparent; -fx-text-fill: #f44336; -fx-font-weight: bold; -fx-cursor: hand;");
        boton.setOnAction(e -> eliminarMensaje(mensaje));
        return boton;
    }

    // Método auxiliar para crear contenedor de acciones
    private HBox crearContenedorAcciones(Button boton) {
        HBox contenedor = new HBox(boton);
        contenedor.setAlignment(Pos.CENTER);
        contenedor.setPrefWidth(80);
        return contenedor;
    }

    // Método para manejar error al cargar mensajes
    private void manejarErrorCargaMensajes(SQLException e) {
        e.printStackTrace();
        mostrarError("Error al cargar los mensajes");
        mostrarMensajeErrorEnInterfaz("Error al cargar los mensajes: " + e.getMessage());
    }

    // Método auxiliar para limpiar los cambios pendientes
    private void limpiarCambiosPendientes() {
        cambiosPendientes.clear();
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);
    }


    // Método para confirmar cambios
    private void confirmarCambios() {
        if (cambiosPendientes.isEmpty()) {
            mostrarAlerta("Información", "No hay cambios pendientes para guardar", Alert.AlertType.INFORMATION);
            return; // No hay cambios pendientes
        }

        // Mostrar alerta de confirmación
        Alert confirmacion = crearAlertaConfirmacionCambios();
        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            procesarCambiosEnBaseDeDatos(); //procesamos los cambios en la bbdd
        }
    }

    // Método auxiliar para crear alerta de confirmación de cambios
    private Alert crearAlertaConfirmacionCambios() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar cambios");
        alerta.setHeaderText("¿Está seguro que desea guardar los cambios?");
        alerta.setContentText("Se actualizarán " + cambiosPendientes.size() + " mensajes.");
        return alerta;
    }

    // Método para procesar cambios en la base de datos
    private void procesarCambiosEnBaseDeDatos() {
        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            MensajesDaoImpl mensajeDao = new MensajesDaoImpl(conn);
            List<String> reservasParaCancelar = new ArrayList<>();

            // Procesar cada cambio pendiente
            for (Map.Entry<Integer, Character> entry : cambiosPendientes.entrySet()) {
                int id = entry.getKey();
                char nuevoEstado = entry.getValue();
                Mensajes mensaje = obtenerMensajePorId(id);

                // Verificar si el mensaje existe
                if (mensaje != null) {
                    //si el nuevo estado es Aprobada y el mensaje es de cancelación, agregar a la lista de reservas para cancelar
                    if (esCancelacionAprobada(mensaje, nuevoEstado)) {
                        reservasParaCancelar.add(mensaje.getId_reserva());
                    }

                    // Actualizar el estado del mensaje en la base de datos
                    boolean exito = mensajeDao.actualizarEstadoMensaje(id, nuevoEstado);

                    //Si falla, rollback
                    if (!exito) {
                        conn.rollback();
                        mostrarError("No se pudo actualizar el mensaje con ID: " + id);
                        return;
                    }

                    mensaje.setEstado_solicitud(nuevoEstado);
                    estadosOriginales.put(id, nuevoEstado);
                }
            }
            //si hay reservas para cancelar, procesar la cancelación y actualizar las vistas
            conn.commit();
            mostrarResultadoProcesamiento(reservasParaCancelar);
            cargarMensajes();
            actualizarVistaReservas();

        } catch (SQLException e) {
            manejarErrorTransaccion(conn, e);
        } finally {
            limpiarDespuesDeProcesar(conn);
        }
    }

    // Método auxiliar para obtener mensaje por ID. Es un filtro
    private Mensajes obtenerMensajePorId(int id) {
        return mensajesList.stream()
                .filter(m -> m.getId_solicitud() == id)
                .findFirst()
                .orElse(null);
    }

    // Método auxiliar para verificar si la cancelación fue aprobada
    private boolean esCancelacionAprobada(Mensajes mensaje, char nuevoEstado) {
        return nuevoEstado == 'A' && mensaje.getTipo_solicitud() != null &&
                mensaje.getTipo_solicitud().toLowerCase().contains("cancelac") &&
                mensaje.getId_reserva() != null && !mensaje.getId_reserva().isEmpty();
    }

    // Método auxiliar para mostrar resultado del procesamiento. Usa StringBuilder para crear el mensaje
    private void mostrarResultadoProcesamiento(List<String> reservasParaCancelar) {
        StringBuilder mensaje = new StringBuilder();
        if (reservasParaCancelar.isEmpty()) {
            mensaje.append("No hay reservas para eliminar.");
        } else {
            mensaje.append("Debe eliminar las siguientes reservas:\n\n");
            for (String idReserva : reservasParaCancelar) {
                mensaje.append("ID_RESERVA: ").append(idReserva).append("\n");
            }
        }

        TextArea textArea = crearTextAreaResultado(mensaje.toString());
        VBox contenedor = crearContenedorResultado(textArea);

        // Mostrar alerta con el id de las reservas a cancelar
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Reservas para eliminar");
        alerta.setHeaderText("Atención Administrador");
        alerta.getDialogPane().setContent(contenedor);
        alerta.setWidth(200);
        alerta.setHeight(200);
        alerta.setResizable(true);
        alerta.showAndWait();
    }

    // Método auxiliar para crear TextArea para mostrar el resultado
    private TextArea crearTextAreaResultado(String texto) {
        TextArea textArea = new TextArea(texto);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefHeight(150);
        textArea.setPrefWidth(300);
        return textArea;
    }

    // Método auxiliar para crear contenedor de resultado
    private VBox crearContenedorResultado(TextArea textArea) {
        VBox vbox = new VBox(textArea);
        vbox.setPadding(new Insets(10));
        return vbox;
    }

    // Método auxiliar para manejar error de transacción con la bbdd
    private void manejarErrorTransaccion(Connection conn, SQLException e) {
        try {
            if (conn != null){
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        e.printStackTrace();
        mostrarError("Error al guardar los cambios: " + e.getMessage());
    }

    // Método auxiliar para limpiar después de procesar cambios
    private void limpiarDespuesDeProcesar(Connection conn) {
        try {
            if (conn != null) conn.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        cambiosPendientes.clear();
        guardarBtn.setDisable(true);
        cancelarBtn.setDisable(true);
        ocultarBotonesConfirmacion();
    }

    // Método para actualizar la vista de reservas. Usa cargarReservas() del controlador de reservas
    private void actualizarVistaReservas() {
        if (reservasController != null) {
            reservasController.cargarReservas();
        }
    }

    // Método para cancelar cambios. Muestra una alerta de confirmación y restaura los estados originales.
    private void cancelarCambios() {
        if (cambiosPendientes.isEmpty()) {
            mostrarAlerta("Información", "No hay cambios pendientes para cancelar", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmacion = crearAlertaConfirmacionCancelacion();
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                restaurarEstadosOriginales();
                limpiarCambiosPendientes();
                ocultarBotonesConfirmacion();
                mostrarAlerta("Información", "Cambios cancelados", Alert.AlertType.INFORMATION);
            }
        });
    }

    // Método auxiliar para crear alerta de confirmación de cancelación
    private Alert crearAlertaConfirmacionCancelacion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Cancelar cambios");
        alerta.setHeaderText("¿Está seguro que desea descartar los cambios?");
        alerta.setContentText("Se perderán " + cambiosPendientes.size() + " cambios pendientes.");
        return alerta;
    }

    // Método auxiliar para restaurar los estados originales de los mensajes
    private void restaurarEstadosOriginales() {
        for (Map.Entry<Integer, Character> entry : estadosOriginales.entrySet()) {
            int id = entry.getKey();
            char estadoOriginal = entry.getValue();
            ComboBox<String> combo = estadosComboBoxes.get(id);
            if (combo != null) {
                combo.setValue(obtenerValorEstado(estadoOriginal));
            }
        }
    }

    // Método auxiliar para obtener el valor del estado del dropdown de mensajes
    private String obtenerValorEstado(char estado) {
        return switch (estado) {
            case 'A' -> "A - Aprobada";
            case 'R' -> "R - Rechazada";
            default -> "P - Pendiente";
        };
    }

    // Método para eliminar un mensaje. Muestra una alerta de confirmación y elimina el mensaje si se confirma.
    private void eliminarMensaje(Mensajes mensaje) {
        Alert confirmacion = crearAlertaConfirmacionEliminacion();
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ejecutarEliminacionMensaje(mensaje);
            }
        });
    }

    // Método auxiliar para crear alerta de confirmación de eliminación
    private Alert crearAlertaConfirmacionEliminacion() {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar eliminación");
        alerta.setHeaderText("¿Está seguro que desea eliminar este mensaje?");
        alerta.setContentText("Esta acción no se puede deshacer.");
        return alerta;
    }

    // Método auxiliar para ejecutar la eliminación del mensaje. Usa el DAO para eliminarlo de la base de datos.
    private void ejecutarEliminacionMensaje(Mensajes mensaje) {
        try {
            boolean exito = mensajeDao.eliminarMensaje(mensaje.getId_solicitud());

            // Si la eliminación es exitosa, eliminar el mensaje de la interfaz y actualizar la vista
            if (exito) {
                removerMensajeDeInterfaz(mensaje);
                actualizarEstadoInterfazDespuesEliminacion();
                mostrarAlerta("Éxito", "Mensaje eliminado correctamente", Alert.AlertType.INFORMATION);
            } else {
                mostrarError("No se pudo eliminar el mensaje");
            }
        } catch (SQLException e) {
            mostrarError("Error al eliminar el mensaje");
        }
    }

    // Método auxiliar para eliminar el mensaje de la interfaz
    private void removerMensajeDeInterfaz(Mensajes mensaje) {
        HBox fila = filasMap.get(mensaje.getId_solicitud());
        if (fila != null) {
            mensajesVBox.getChildren().remove(fila);
        }
        mensajesList.remove(mensaje);
        filasMap.remove(mensaje.getId_solicitud());
        estadosComboBoxes.remove(mensaje.getId_solicitud());
        cambiosPendientes.remove(mensaje.getId_solicitud());
    }

    // Método auxiliar para actualizar el estado de la interfaz después de eliminar un mensaje
    private void actualizarEstadoInterfazDespuesEliminacion() {
        if (mensajesList.isEmpty()) {
            mostrarMensajeNoHayDatos();
        } else {
            eliminarBtn.setDisable(false);
        }
        guardarBtn.setDisable(cambiosPendientes.isEmpty());
        cancelarBtn.setDisable(cambiosPendientes.isEmpty());
    }

    // Método para eliminar mensajes seleccionados. Muestra una alerta de confirmación y elimina los mensajes seleccionados.
    private void eliminarSeleccionados() {
        List<Mensajes> seleccionados = obtenerMensajesSeleccionados();

        // Si no hay mensajes seleccionados, mostrar alerta
        if (seleccionados.isEmpty()) {
            mostrarAlerta("Información", "No hay mensajes seleccionados para eliminar", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmacion = crearAlertaConfirmacionEliminacionMultiple(seleccionados.size());
        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                ejecutarEliminacionMultiple(seleccionados);
            }
        });
    }

    // Método auxiliar para obtener los mensajes seleccionados. Filtra los mensajes seleccionados en la interfaz.
    private List<Mensajes> obtenerMensajesSeleccionados() {
        List<Mensajes> seleccionados = new ArrayList<>();

        // Filtrar los mensajes seleccionados mediante un filtro de un stream de los mensajes. Este método ha sido con ayuda, el mío era horrible y muy largo :)
        for (HBox fila : filasMap.values()) {
            CheckBox checkBox = (CheckBox) ((HBox) fila.getChildren().get(0)).getChildren().get(0);
            if (checkBox.isSelected()) {
                int id = Integer.parseInt(((Label) fila.getChildren().get(1)).getText());
                mensajesList.stream()
                        .filter(m -> m.getId_solicitud() == id)
                        .findFirst()
                        .ifPresent(seleccionados::add);
            }
        }

        return seleccionados;
    }


    // Método auxiliar para crear alerta de confirmación de eliminación múltiple
    private Alert crearAlertaConfirmacionEliminacionMultiple(int cantidad) {
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.setTitle("Confirmar eliminación múltiple");
        alerta.setHeaderText("¿Está seguro que desea eliminar los mensajes seleccionados?");
        alerta.setContentText("Se eliminarán " + cantidad + " mensajes. Esta acción no se puede deshacer.");
        return alerta;
    }

    // Método auxiliar para ejecutar la eliminación de múltiples mensajes. Usa el DAO para eliminar los mensajes de la base de datos.
    private void ejecutarEliminacionMultiple(List<Mensajes> seleccionados) {
        int eliminados = 0;
        for (Mensajes mensaje : seleccionados) {
            try {
                boolean exito = mensajeDao.eliminarMensaje(mensaje.getId_solicitud());

                // Si la eliminación es exitosa, eliminar el mensaje de la interfaz y actualizar la vista
                if (exito) {
                    removerMensajeDeInterfaz(mensaje);
                    eliminados++;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        mensajesList.removeAll(seleccionados);
        actualizarEstadoBotonesDespuesEliminacion();

        // Si se eliminaron mensajes, mostrar alerta de éxito
        if (eliminados > 0) {
            mostrarAlerta("Éxito", "Se han eliminado " + eliminados + " mensajes", Alert.AlertType.INFORMATION);
        } else {
            mostrarError("No se pudo eliminar ningún mensaje");
        }
    }

    // Método auxiliar para actualizar el estado de los botones después de la eliminación
    private void actualizarEstadoBotonesDespuesEliminacion() {
        guardarBtn.setDisable(cambiosPendientes.isEmpty());
        cancelarBtn.setDisable(cambiosPendientes.isEmpty());
    }

    // Método auxiliar para mostrar un mensaje de error
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.show();
    }

    // Método auxiliar para mostrar una alerta con título, mensaje y tipo
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }

    // Método para establecer el controlador de reservas. Se usa para actualizar la vista de reservas después de procesar cambios.
    public void setReservasController(ListarReservasController controller) {
        this.reservasController = controller;
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