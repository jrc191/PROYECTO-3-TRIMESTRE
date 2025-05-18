package controllers.admin;

import dao.EspectaculoDaoI;
import dao.ReservasDaoI;
import dao.impl.EspectaculoDaoImpl;
import dao.impl.ReservaDaoImpl;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import models.Espectaculo;
import utils.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

public class ListarEspectaculosController {

    @FXML private VBox contenedorEspectaculos;
    @FXML private ScrollPane scrollEspectaculos;
    @FXML private ImageView eliminarBtn;
    @FXML private ImageView editarBtn;
    @FXML private ImageView addBtn;
    @FXML private Button guardarBtn;
    @FXML private Button cancelarBtn;

    private EspectaculoDaoI espectaculoDao;
    private ReservasDaoI reservasDao;
    private List<Espectaculo> espectaculosList = new ArrayList<>();
    private Map<String, HBox> filasMap = new HashMap<>();
    private HBox filaNuevoEspectaculo;
    private HBox filaEditandoEspectaculo;
    private boolean modoEdicion = false;
    private boolean modoAgregar = false;
    private Espectaculo espectaculoEditando;
    private List<CheckBox> checkBoxes = new ArrayList<>();

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            espectaculoDao = new EspectaculoDaoImpl(conn);
            reservasDao = new ReservaDaoImpl(conn);

            // Configurar botones de acción con tooltips (dejar el ratón encima para ver el texto)
            Tooltip.install(eliminarBtn, new Tooltip("Eliminar espectáculos seleccionados"));
            Tooltip.install(editarBtn, new Tooltip("Editar espectáculo seleccionado"));
            Tooltip.install(addBtn, new Tooltip("Agregar nuevo espectáculo"));

            eliminarBtn.setOnMouseClicked(e -> eliminarSeleccionados());
            editarBtn.setOnMouseClicked(e -> editarSeleccionados());
            addBtn.setOnMouseClicked(e -> toggleModoAgregar());
            guardarBtn.setOnAction(e -> guardarCambios());
            cancelarBtn.setOnAction(e -> cancelarEdicion());

            cargarEspectaculos();
        } catch (SQLException e) {
            e.printStackTrace();
            mostrarError("Error al conectar con la base de datos");
            mostrarMensajesError("Error al conectar con la base de datos: " + e.getMessage());
        }
    }

    //Método auxiliar para habilitar añadir espectáculos
    private void toggleModoAgregar() {
        if (modoAgregar) {
            cancelarAgregar();
        } else {
            iniciarAgregar();
        }
    }

    // Método auxiliar para iniciar el modo de agregar espectáculo
    private void iniciarAgregar() {
        modoAgregar = true;
        addBtn.setImage(new Image(getClass().getResourceAsStream("/resources/images/tick.png")));
        guardarBtn.setVisible(true);
        cancelarBtn.setVisible(true);
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);

        // Fila de inserción de datos
        filaNuevoEspectaculo = new HBox();
        filaNuevoEspectaculo.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-background-color: #e3f2fd;");
        filaNuevoEspectaculo.setPadding(new Insets(8));
        filaNuevoEspectaculo.setAlignment(Pos.CENTER_LEFT);

        // Campos de texto para el nuevo espectáculo
        TextField idField = new TextField();
        idField.setPromptText("ESP-XX");
        idField.setPrefWidth(100);

        TextField nombreField = new TextField();
        nombreField.setPromptText("Nombre del espectáculo");
        nombreField.setPrefWidth(160);

        DatePicker fechaPicker = new DatePicker();
        fechaPicker.setPrefWidth(125);

        TextField precioField = new TextField();
        precioField.setPromptText("0.00");
        precioField.setPrefWidth(100);

        TextField precioVipField = new TextField();
        precioVipField.setPromptText("0.00");
        precioVipField.setPrefWidth(100);

        // Botones de acción para guardar o cancelar los cambios
        ImageView guardarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/tick.png")));
        ImageView cancelarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/cancel.png")));

        // Configurar tooltips de los iconos
        Tooltip.install(guardarIcon, new Tooltip("Guardar espectáculo"));
        Tooltip.install(cancelarIcon, new Tooltip("Cancelar"));

        // Estilo y comportamiento de los botones
        guardarIcon.setFitHeight(16);
        guardarIcon.setFitWidth(16);
        guardarIcon.setStyle("-fx-cursor: hand;");
        guardarIcon.setOnMouseClicked(e -> guardarNuevoEspectaculo());

        cancelarIcon.setFitHeight(16);
        cancelarIcon.setFitWidth(16);
        cancelarIcon.setStyle("-fx-cursor: hand;");
        cancelarIcon.setOnMouseClicked(e -> cancelarAgregar());

        HBox accionesBox = new HBox(5, guardarIcon, cancelarIcon);
        accionesBox.setAlignment(Pos.CENTER);
        accionesBox.setPrefWidth(80);

        // Validación en tiempo real del ID (formato ESP-XX)
        idField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!Pattern.matches("^ESP-\\d{0,2}$", newVal)) {
                idField.setStyle("-fx-text-fill: red;");
            } else {
                idField.setStyle("-fx-text-fill: black;");
            }
        });

        // Validación de precios
        precioField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                precioField.setText(oldVal);
            }
        });

        precioVipField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                precioVipField.setText(oldVal);
            }
        });

        filaNuevoEspectaculo.getChildren().addAll(
                new HBox(),
                idField, nombreField, fechaPicker, precioField, precioVipField, accionesBox
        );

        // Añadir al principio de la lista
        contenedorEspectaculos.getChildren().add(1, filaNuevoEspectaculo);
        scrollEspectaculos.setVvalue(0);
    }

    // Método auxiliar para guardar el nuevo espectáculo
    private void guardarNuevoEspectaculo() {
        if (filaNuevoEspectaculo == null) {
            return;
        }

        //CAMPOS PARA EL NUEVO ESPECTÁCULO. ID, NOMBRE, FECHA, PRECIO, PRECIO VIP
        TextField idField = (TextField) filaNuevoEspectaculo.getChildren().get(1);
        TextField nombreField = (TextField) filaNuevoEspectaculo.getChildren().get(2);
        DatePicker fechaPicker = (DatePicker) filaNuevoEspectaculo.getChildren().get(3);
        TextField precioField = (TextField) filaNuevoEspectaculo.getChildren().get(4);
        TextField precioVipField = (TextField) filaNuevoEspectaculo.getChildren().get(5);

        // Validaciones
        try {

            if (!Pattern.matches("^ESP-\\d{2}$", idField.getText())) {
                mostrarError("El ID debe tener el formato ESP-XX (donde XX son números)");
                return;
            }

            if (espectaculoDao.existeEspectaculo(idField.getText())) {
                mostrarError("Ya existe un espectáculo con ese ID");
                return;
            }

            if (nombreField.getText().isEmpty()) {
                mostrarError("El nombre no puede estar vacío");
                return;
            }

            if (fechaPicker.getValue() == null) {
                mostrarError("Debe seleccionar una fecha");
                return;
            }

            if (fechaPicker.getValue().isBefore(LocalDate.now())) {
                mostrarError("La fecha debe ser futura");
                return;
            }

            double precio, precioVip;
            try {
                precio = Double.parseDouble(precioField.getText());
                precioVip = Double.parseDouble(precioVipField.getText());
            } catch (NumberFormatException e) {
                mostrarError("Los precios deben ser números válidos");
                return;
            }

            if (precio <= 0 || precioVip <= 0) {
                mostrarError("Los precios deben ser mayores que cero");
                return;
            }

            // Crear y guardar el nuevo espectáculo
            Espectaculo nuevo = new Espectaculo(
                    idField.getText(),
                    nombreField.getText(),
                    fechaPicker.getValue(),
                    precio,
                    precioVip
            );

            //Insertamos el espectáculo en la base de datos
            if (espectaculoDao.insertarEspectaculo(nuevo)) {
                mostrarAlerta("Éxito", "Espectáculo agregado correctamente", Alert.AlertType.INFORMATION);
                cancelarAgregar(); //volvemos al modo normal
                cargarEspectaculos(); //actualizamos los espectáculos
            } else {
                mostrarError("No se pudo agregar el espectáculo");
            }
        } catch (SQLException e) {
            mostrarError("Error al agregar el espectáculo: " + e.getMessage());
        }
    }

    // Método auxiliar para cancelar el agregado de un nuevo espectáculo
    private void cancelarAgregar() {

        if (filaNuevoEspectaculo != null) {
            contenedorEspectaculos.getChildren().remove(filaNuevoEspectaculo);
            filaNuevoEspectaculo = null;
        }
        // Volver a la vista normal
        modoAgregar = false;
        addBtn.setImage(new Image(getClass().getResourceAsStream("/resources/images/add-icon.png")));
        guardarBtn.setVisible(false);
        cancelarBtn.setVisible(false);
        editarBtn.setDisable(false);
        eliminarBtn.setDisable(false);
    }

    // Método auxiliar para editar los espectáculos seleccionados (sólo se puede uno a uno)
    private void editarSeleccionados() {
        List<Espectaculo> seleccionados = obtenerEspectaculosSeleccionados();

        if (seleccionados.isEmpty()) {
            mostrarAlerta("Información", "Seleccione al menos un espectáculo para editar", Alert.AlertType.INFORMATION);
            return;
        }

        if (seleccionados.size() > 1) {
            mostrarAlerta("Error", "Solo puede editar un espectáculo a la vez", Alert.AlertType.ERROR);
            return;
        }

        iniciarEdicion(seleccionados.get(0));
    }

    // Método auxiliar para iniciar la edición de un espectáculo
    private void iniciarEdicion(Espectaculo espectaculo) {
        modoEdicion = true;
        espectaculoEditando = espectaculo;

        // Ocultar/mostrar botones. Deshabilitamos el agregar, editar y eliminar mientras estamos editando
        addBtn.setDisable(true);
        editarBtn.setDisable(true);
        eliminarBtn.setDisable(true);
        guardarBtn.setVisible(true);
        cancelarBtn.setVisible(true);

        // Crear fila de edición
        filaEditandoEspectaculo = new HBox();
        filaEditandoEspectaculo.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0; -fx-background-color: #fffde7;");
        filaEditandoEspectaculo.setPadding(new Insets(8));
        filaEditandoEspectaculo.setAlignment(Pos.CENTER_LEFT);

        // ID como label (no editable). PARA EVITAR QUE SE CAMBIE EL ID EN LA BBDD Y HAYA PROBLEMAS
        Label idLabel = new Label(espectaculo.getId());
        idLabel.setPrefWidth(100);

        // Campos editables
        TextField nombreField = new TextField(espectaculo.getNombre());
        nombreField.setPrefWidth(160);

        DatePicker fechaPicker = new DatePicker(espectaculo.getFecha());
        fechaPicker.setPrefWidth(125);

        TextField precioField = new TextField(String.format("%.2f", espectaculo.getPrecioBase()));
        precioField.setPrefWidth(100);

        TextField precioVipField = new TextField(String.format("%.2f", espectaculo.getPrecioVip()));
        precioVipField.setPrefWidth(100);

        // Validación de precios
        precioField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                precioField.setText(oldVal);
            }
        });

        precioVipField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                precioVipField.setText(oldVal);
            }
        });

        filaEditandoEspectaculo.getChildren().addAll(
                new HBox(), // Espacio para checkbox (vacío)
                idLabel, nombreField, fechaPicker, precioField, precioVipField
        );

        // Ocultar la fila original
        filasMap.get(espectaculo.getId()).setVisible(false);

        // Añadir fila de edición al principio
        contenedorEspectaculos.getChildren().add(1, filaEditandoEspectaculo);
        scrollEspectaculos.setVvalue(0);
    }

    // Método auxiliar para guardar los cambios realizados según si el modo es editar o agregar
    private void guardarCambios() {
        if (modoEdicion) {
            guardarEdicion();
        } else if (modoAgregar) {
            guardarNuevoEspectaculo();
        }
    }

    // Método auxiliar para guardar los cambios realizados en la edición de un espectáculo
    private void guardarEdicion() {
        //No pueden ser nulos
        if (filaEditandoEspectaculo == null || espectaculoEditando == null) {
            return;
        }

        //Campos editables
        TextField nombreField = (TextField) filaEditandoEspectaculo.getChildren().get(2);
        DatePicker fechaPicker = (DatePicker) filaEditandoEspectaculo.getChildren().get(3);
        TextField precioField = (TextField) filaEditandoEspectaculo.getChildren().get(4);
        TextField precioVipField = (TextField) filaEditandoEspectaculo.getChildren().get(5);

        try {
            // Validaciones
            if (nombreField.getText().isEmpty()) {
                mostrarError("El nombre no puede estar vacío");
                return;
            }

            if (fechaPicker.getValue() == null) {
                mostrarError("Debe seleccionar una fecha");
                return;
            }

            if (fechaPicker.getValue().isBefore(LocalDate.now())) {
                mostrarError("La fecha debe ser futura");
                return;
            }

            double precio, precioVip;
            try {
                precio = Double.parseDouble(precioField.getText());
                precioVip = Double.parseDouble(precioVipField.getText());
            } catch (NumberFormatException e) {
                mostrarError("Los precios deben ser números válidos");
                return;
            }

            if (precio <= 0 || precioVip <= 0) {
                mostrarError("Los precios deben ser mayores que cero");
                return;
            }

            // Actualizar el espectáculo
            espectaculoEditando.setNombre(nombreField.getText());
            espectaculoEditando.setFecha(fechaPicker.getValue());
            espectaculoEditando.setPrecioBase(precio);
            espectaculoEditando.setPrecioVip(precioVip);

            // Guardar en la base de datos
            if (espectaculoDao.actualizarEspectaculo(espectaculoEditando)) {
                mostrarAlerta("Éxito", "Espectáculo actualizado correctamente", Alert.AlertType.INFORMATION);
                cancelarEdicion();
                cargarEspectaculos();
            } else {
                mostrarError("No se pudo actualizar el espectáculo");
            }
        } catch (SQLException e) {
            mostrarError("Error al actualizar el espectáculo: " + e.getMessage());
        }
    }

    // Método auxiliar para cancelar la edición de un espectáculo
    private void cancelarEdicion() {
        if (modoEdicion) {
            if (filaEditandoEspectaculo != null) {
                contenedorEspectaculos.getChildren().remove(filaEditandoEspectaculo);
                filaEditandoEspectaculo = null;
            }

            // Mostrar la fila original
            if (espectaculoEditando != null) {
                filasMap.get(espectaculoEditando.getId()).setVisible(true);
            }

            resetearModoEdicion();
        } else if (modoAgregar) {
            cancelarAgregar();
        }
    }

    // Método auxiliar para resetear el modo de edición
    private void resetearModoEdicion() {
        modoEdicion = false;
        espectaculoEditando = null;
        addBtn.setDisable(false);
        editarBtn.setDisable(false);
        eliminarBtn.setDisable(false);
        guardarBtn.setVisible(false);
        cancelarBtn.setVisible(false);
    }

    // Método auxiliar para obtener los espectáculos seleccionados. Usado para eliminar o editar
    private List<Espectaculo> obtenerEspectaculosSeleccionados() {
        List<Espectaculo> seleccionados = new ArrayList<>();

        for (int i = 0; i < checkBoxes.size(); i++) {
            if (checkBoxes.get(i).isSelected()) {
                seleccionados.add(espectaculosList.get(i));
            }
        }

        return seleccionados;
    }

    // Método auxiliar para mostrar mensajes de error en la interfaz
    private void mostrarMensajesError(String mensaje) {
        contenedorEspectaculos.getChildren().clear();

        Label errorLabel = new Label(mensaje);
        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #d32f2f; -fx-padding: 20px;");

        VBox contenedorError = new VBox(errorLabel);
        contenedorError.setAlignment(Pos.CENTER);
        contenedorError.setPrefHeight(100);
        contenedorEspectaculos.getChildren().add(contenedorError);

        eliminarBtn.setDisable(true);
        addBtn.setDisable(true);
        editarBtn.setDisable(true);
    }

    // Método auxiliar para cargar los espectáculos desde la base de datos
    public void cargarEspectaculos() {
        try {
            espectaculosList = espectaculoDao.obtenerTodos();
            mostrarEspectaculos();
        } catch (Exception e) {
            e.printStackTrace();
            mostrarError("Error al cargar los espectáculos");
            mostrarMensajesError("Error al cargar los espectáculos: " + e.getMessage());
        }
    }

    // Método auxiliar para mostrar los espectáculos en la interfaz
    private void mostrarEspectaculos() {
        // Limpiar el contenedor antes de agregar nuevos elementos
        contenedorEspectaculos.getChildren().clear();
        filasMap.clear();
        checkBoxes.clear();

        // Cabecera
        HBox cabecera = crearCabeceraTabla();
        contenedorEspectaculos.getChildren().add(cabecera);

        if (espectaculosList.isEmpty()) {
            Label noEspectaculosLabel = new Label("No hay espectáculos registrados");
            noEspectaculosLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-padding: 20px;");

            VBox contenedorMensaje = new VBox(noEspectaculosLabel);
            contenedorMensaje.setAlignment(Pos.CENTER);
            contenedorMensaje.setPrefHeight(100);
            contenedorEspectaculos.getChildren().add(contenedorMensaje);

            eliminarBtn.setDisable(true);
            editarBtn.setDisable(true);
        } else {
            eliminarBtn.setDisable(false);
            editarBtn.setDisable(false);

            // Filas de datos
            for (Espectaculo espectaculo : espectaculosList) {
                HBox fila = crearFilaEspectaculo(espectaculo);
                contenedorEspectaculos.getChildren().add(fila);
                filasMap.put(espectaculo.getId(), fila);
            }
        }
    }

    // Método auxiliar para crear la cabecera de la tabla
    private HBox crearCabeceraTabla() {
        HBox cabecera = new HBox();
        cabecera.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        cabecera.setPadding(new Insets(10));
        cabecera.setAlignment(Pos.CENTER_LEFT);

        Label seleccionar = crearLabelCabecera("  V", 50);
        Label id = crearLabelCabecera("ID", 100);
        Label nombre = crearLabelCabecera("Nombre", 160);
        Label fecha = crearLabelCabecera("Fecha", 125);
        Label precio = crearLabelCabecera("Precio", 100);
        Label precioVIP = crearLabelCabecera("Precio VIP", 100);
        Label acciones = crearLabelCabecera("Acciones", 80);

        cabecera.getChildren().addAll(seleccionar, id, nombre, fecha, precio, precioVIP, acciones);
        return cabecera;
    }

    // Método auxiliar para crear un label de cabecera
    private Label crearLabelCabecera(String texto, double ancho) {
        Label label = new Label(texto);
        HBox.setHgrow(label, Priority.SOMETIMES);
        label.setPrefWidth(ancho);
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #6c757d;");
        return label;
    }

    // Método auxiliar para crear una fila de espectáculo
    private HBox crearFilaEspectaculo(Espectaculo espectaculo) {
        HBox fila = new HBox();
        fila.setStyle("-fx-border-color: #e0e0e0; -fx-border-width: 0 0 1 0;");
        fila.setPadding(new Insets(8));
        fila.setAlignment(Pos.CENTER_LEFT);

        CheckBox seleccion = new CheckBox();
        seleccion.setSelected(false);
        checkBoxes.add(seleccion);
        HBox seleccionBox = new HBox(seleccion);
        seleccionBox.setAlignment(Pos.CENTER);
        seleccionBox.setPrefWidth(50);

        Label id = new Label(espectaculo.getId());
        id.setPrefWidth(100);

        Label nombre = new Label(espectaculo.getNombre());
        nombre.setPrefWidth(160);

        Label fecha = new Label(espectaculo.getFecha().toString());
        fecha.setPrefWidth(125);

        Label precio = new Label(String.format("%.2f €", espectaculo.getPrecioBase()));
        precio.setPrefWidth(100);

        Label precioVIP = new Label(String.format("%.2f €", espectaculo.getPrecioVip()));
        precioVIP.setPrefWidth(100);

        // Botones de acción con iconos consistentes
        ImageView editarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/tick.png")));
        ImageView eliminarIcon = new ImageView(new Image(getClass().getResourceAsStream("/resources/images/cancel.png")));

        // Configurar tooltips
        Tooltip.install(editarIcon, new Tooltip("Editar espectáculo"));
        Tooltip.install(eliminarIcon, new Tooltip("Eliminar espectáculo"));

        // Estilo y comportamiento de los botones
        editarIcon.setFitHeight(16);
        editarIcon.setFitWidth(16);
        editarIcon.setStyle("-fx-cursor: hand;");
        editarIcon.setOnMouseClicked(e -> iniciarEdicion(espectaculo));

        eliminarIcon.setFitHeight(16);
        eliminarIcon.setFitWidth(16);
        eliminarIcon.setStyle("-fx-cursor: hand;");
        eliminarIcon.setOnMouseClicked(e -> eliminarEspectaculo(espectaculo));

        HBox accionesBox = new HBox(5, editarIcon, eliminarIcon);
        accionesBox.setAlignment(Pos.CENTER);
        accionesBox.setPrefWidth(80);

        fila.getChildren().addAll(seleccionBox, id, nombre, fecha, precio, precioVIP, accionesBox);
        return fila;
    }

    // Método auxiliar para eliminar un espectáculo
    private void eliminarEspectaculo(Espectaculo espectaculo) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar este espectáculo?");
        confirmacion.setContentText("Esta acción eliminará todas las reservas asociadas y no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    // Primero eliminamos las reservas asociadas
                    int reservasEliminadas = reservasDao.eliminarReservasPorEspectaculo(espectaculo.getId());

                    // Luego eliminamos el espectáculo
                    boolean exito = espectaculoDao.eliminarEspectaculo(espectaculo.getId());

                    if (exito) {
                        HBox fila = filasMap.get(espectaculo.getId());
                        if (fila != null) {
                            contenedorEspectaculos.getChildren().remove(fila);
                        }
                        espectaculosList.remove(espectaculo);
                        filasMap.remove(espectaculo.getId());
                        mostrarAlerta("Éxito",
                                String.format("Espectáculo eliminado correctamente. %d reservas eliminadas.", reservasEliminadas),
                                Alert.AlertType.INFORMATION);
                    } else {
                        mostrarError("No se pudo eliminar el espectáculo");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    mostrarError("Error al eliminar el espectáculo: " + e.getMessage());
                }
            }
        });
    }

    // Método auxiliar para eliminar múltiples espectáculos seleccionados
    private void eliminarSeleccionados() {
        List<Espectaculo> seleccionados = obtenerEspectaculosSeleccionados();

        if (seleccionados.isEmpty()) {
            mostrarAlerta("Información", "No hay espectáculos seleccionados para eliminar", Alert.AlertType.INFORMATION);
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación múltiple");
        confirmacion.setHeaderText("¿Está seguro que desea eliminar los espectáculos seleccionados?");
        confirmacion.setContentText(String.format(
                "Se eliminarán %d espectáculos y todas sus reservas asociadas. Esta acción no se puede deshacer.",
                seleccionados.size()));

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int eliminados = 0;
                int reservasEliminadas = 0;

                for (Espectaculo espectaculo : seleccionados) {
                    try {
                        // Eliminar reservas primero
                        int resElim = reservasDao.eliminarReservasPorEspectaculo(espectaculo.getId());
                        reservasEliminadas += resElim;

                        // Luego eliminar espectáculo
                        boolean exito = espectaculoDao.eliminarEspectaculo(espectaculo.getId());
                        if (exito) {
                            HBox fila = filasMap.get(espectaculo.getId());
                            if (fila != null) {
                                contenedorEspectaculos.getChildren().remove(fila);
                            }
                            eliminados++;
                            filasMap.remove(espectaculo.getId());
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }

                espectaculosList.removeAll(seleccionados);

                if (eliminados > 0) {
                    mostrarAlerta("Éxito",
                            String.format("Se han eliminado %d espectáculos y %d reservas asociadas",
                                    eliminados, reservasEliminadas),
                            Alert.AlertType.INFORMATION);
                } else {
                    mostrarError("No se pudo eliminar ningún espectáculo");
                }
            }
        });
    }

    // Método auxiliar para mostrar un mensaje de error
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(mensaje);
        alert.show();
    }

    // Método auxiliar para mostrar una alerta genérica
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setContentText(mensaje);
        alert.show();
    }
}