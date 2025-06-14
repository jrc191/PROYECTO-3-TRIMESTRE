package controllers;

import javafx.stage.Stage;
import javafx.scene.control.Label;

import utils.CerrarSesion;
import utils.DatabaseConnection;
import dao.UsuarioDaoI;
import dao.impl.ReservaDaoImpl;
import dao.impl.UsuarioDaoImpl;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import models.EntradaCesta;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import models.Reservas;
import utils.CestaStorage;
import utils.Transitions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CestaController {

    @FXML private VBox contenedorEntradas;
    @FXML private Label totalLabel;
    @FXML public Label espectaculoLabel;
    @FXML private Label usuarioLabel;
    @FXML private VBox plantillaEntrada;
    @FXML private ScrollPane scrollEntradas;
    @FXML private Label arribaBtn, abajoBtn;
    @FXML private ChoiceBox<String> eleccionBox;

    // Parámetros usados para cerrar sesión, reservar ... entre otros
    private String emailUsuarioLogueado;
    private String idUsuario;
    private UsuarioDaoI usuarioDao;
    public DatePicker filtroFechaField;
    public TextField filtroNombreField;

    private List<EntradaCesta> entradas = new ArrayList<>();
    private double total = 0.0; //precio total de las entradas de la cesta

    public CestaController() throws SQLException {
    }

    // Método para inicializar el controlador con el email del usuario logueado y configuraciones iniciales
    @FXML
    public void initialize() {
        if (emailUsuarioLogueado != null) {
            usuarioLabel.setText("Email: " + emailUsuarioLogueado);
            // Inicializamos opciones del ChoiceBox
            eleccionBox.getItems().addAll( "OPCION 1", "OPCION 2");
            eleccionBox.setValue("-");

            cargarUsuario(idUsuario, emailUsuarioLogueado);
        }


        // Configurar el scroll
        scrollEntradas.setStyle("-fx-background: #1c2242; -fx-background-color: #1c2242;");
        scrollEntradas.setFitToWidth(true);

        // Configurar listeners para las flechas
        agregarListenersScroll();

        // Cargar la cesta desde el almacenamiento
        actualizarCesta();
    }

    // Para manejar el scroll de la cesta.
    private void agregarListenersScroll() {
        Transitions.configurarListenersScroll(scrollEntradas, arribaBtn, abajoBtn);
    }

    // Método para inicializar datos del usuario
    private void cargarUsuario(String idUsuario, String emailUsuarioLogueado) {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
            idUsuario = usuarioDao.getIDUsuarioByEmail(emailUsuarioLogueado);
            System.out.println("ID Usuario obtenido: " + idUsuario); // Debug
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Método para verificar si se puede agregar una entrada a la cesta. Se vale de otros métodos para contar entradas en la cesta y en la base de datos.
    public boolean puedeAgregarEntrada(String idEspectaculo, int fila, int col) {
        if (entradas == null) {
            entradas = new ArrayList<>();
        }

        String idButaca = "F" + fila + "-C" + col;

        // Contar entradas en la cesta para este espectáculo
        long enCesta = entradas.stream()
                .filter(e -> e.getIdEspectaculo().equals(idEspectaculo))
                .count();

        // Contar reservas ya existentes en la base de datos
        int enBaseDatos = contarReservas(idEspectaculo, idButaca);

        // El total no puede superar 4. Si es falso, no puede agregar. Si es verdadero, puede agregar.
        return (enCesta + enBaseDatos) < 4;
    }

    // Método para contar reservas en la base de datos
    private int contarReservas(String idEspectaculo, String idButaca) {
        int enBaseDatos = 0;
        try {
            Connection conn = DatabaseConnection.getConnection();
            ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);
            enBaseDatos = reservaDao.contarReservasPorUsuarioYEspectaculo(idUsuario, idEspectaculo, idButaca);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return enBaseDatos;
    }


    // Método para agregar una entrada a la cesta
    public void agregarEntrada(String nombreEspectaculo, String idEspectaculo, int fila, int col, double precio, boolean esVip) {
        if (entradas == null) {
            entradas = new ArrayList<>();
        }

        // Primero verificar límite general por espectáculo (4 entradas)
        long enCestaEspectaculo = entradas.stream()
                .filter(e -> e.getIdEspectaculo().equals(idEspectaculo))
                .count();

        int enBaseDatosEspectaculo = contarReservas(idEspectaculo, "%");

        if (verificacionesReservas(idEspectaculo, fila, col, enCestaEspectaculo, enBaseDatosEspectaculo)){
            return;
        }

        EntradaCesta entrada = new EntradaCesta(nombreEspectaculo, idEspectaculo, fila, col, precio, esVip);
        entradas.add(entrada);
        total += precio;
        actualizarCesta();

        CestaStorage.guardarCesta(emailUsuarioLogueado, entradas);
    }

    private boolean verificacionesReservas(String idEspectaculo, int fila, int col, long enCestaEspectaculo, int enBaseDatosEspectaculo) {
        if ((enCestaEspectaculo + enBaseDatosEspectaculo) >= 4) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Límite alcanzado");
            alert.setContentText("No puedes agregar más de 4 entradas para este espectáculo.");
            alert.show();
            return true;
        }

        // Luego verificar disponibilidad específica de la butaca
        if (!puedeAgregarEntrada(idEspectaculo, fila, col)) {
            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Butaca no disponible");
            alert.setContentText("Esta butaca ya está reservada por ti (aunque hayas cancelado reservas previas).");
            alert.show();
            return true;
        }
        return false;
    }

    // Método para filtrar por asiento/selección del ChoiceBox
    public void filtrarPorAsiento(ActionEvent actionEvent) {
        String seleccion = eleccionBox.getValue();
        if (seleccion == null || seleccion.equals("-") || seleccion.equals("Mostrar todas")) {
            actualizarCesta(); // Mostrar todas si no hay selección o es el valor por defecto
            return;
        }

        List<EntradaCesta> entradasFiltradas = new ArrayList<>();
        for (EntradaCesta entrada : entradas) {
            String entradaInfo = entrada.getNombreEspectaculo() + "-F" + entrada.getFila() + "_C" + entrada.getCol();
            if (entradaInfo.equals(seleccion)) {
                entradasFiltradas.add(entrada);
            }
        }

        mostrarEntradasFiltradas(entradasFiltradas);
    }

    // Método para filtrar por nombre o información de la entrada
    public void filtrarPorNombre(ActionEvent actionEvent) {
        String textoBusqueda = filtroNombreField.getText().trim().toLowerCase();
        if (textoBusqueda.isEmpty()) {
            actualizarCesta(); // Mostrar todas si no hay texto de búsqueda
            return;
        }

        List<EntradaCesta> entradasFiltradas = new ArrayList<>();
        for (EntradaCesta entrada : entradas) {
            // Buscar en todas las propiedades de la entrada
            if (entrada.getNombreEspectaculo().toLowerCase().contains(textoBusqueda) ||
                    ("F" + entrada.getFila() + "-C" + entrada.getCol()).toLowerCase().contains(textoBusqueda) ||
                    (entrada.isVip() ? "vip" : "estándar").contains(textoBusqueda) ||
                    String.valueOf(entrada.getPrecio()).contains(textoBusqueda)) {
                entradasFiltradas.add(entrada);
            }
        }

        mostrarEntradasFiltradas(entradasFiltradas);
    }

    // Método auxiliar para mostrar entradas filtradas
    private void mostrarEntradasFiltradas(List<EntradaCesta> entradasFiltradas) {
        contenedorEntradas.getChildren().clear();
        double subtotal = 0.0;

        if (entradasFiltradas.isEmpty()) {
            Label mensaje = new Label("No se encontraron entradas que coincidan con el criterio de búsqueda");
            mensaje.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            contenedorEntradas.getChildren().add(mensaje);
        } else {
            for (EntradaCesta entrada : entradasFiltradas) {
                VBox entradaCard = crearTarjetaEntrada(entrada);
                contenedorEntradas.getChildren().add(entradaCard);
                subtotal += entrada.getPrecio();
            }
        }

        totalLabel.setText(String.format("Subtotal: %.2f €", subtotal));
    }

    // Método para mostrar todas las entradas (sin filtros)
    public void mostrarTodas(ActionEvent actionEvent) {
        filtroNombreField.clear();
        eleccionBox.setValue("-");
        actualizarCesta();
    }

    // Modificar el método actualizarCesta para incluir las opciones en el ChoiceBox
    private void actualizarCesta() {
        contenedorEntradas.getChildren().clear();
        total = 0.0;
        eleccionBox.getItems().clear();
        eleccionBox.getItems().add("-"); // Valor por defecto
        eleccionBox.getItems().add("Mostrar todas");

        for (EntradaCesta entrada : entradas) {
            VBox entradaCard = crearTarjetaEntrada(entrada);
            contenedorEntradas.getChildren().add(entradaCard);
            total += entrada.getPrecio();

            // Añadir opción al ChoiceBox
            String opcion = entrada.getNombreEspectaculo() + "-F" + entrada.getFila() + "_C" + entrada.getCol();
            eleccionBox.getItems().add(opcion);
        }

        eleccionBox.setValue("-");
        totalLabel.setText(String.format("Total: %.2f €", total));
    }

    // Método auxiliar para crear tarjetas de entrada (extraído del código existente)
    private VBox crearTarjetaEntrada(EntradaCesta entrada) {
        VBox entradaCard = new VBox(10);
        entradaCard.setStyle("-fx-background-color: #2a325c; -fx-padding: 15; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 5);");
        entradaCard.setPrefWidth(680);

        HBox contentBox = new HBox(15);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        VBox infoBox = new VBox(5);

        Label nombreLabel = new Label(entrada.getNombreEspectaculo());
        nombreLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: white; -fx-font-size: 16px;");

        Label detalleLabel = new Label("Butaca: F" + entrada.getFila() + ", C" + entrada.getCol() +
                (entrada.isVip() ? " (VIP)" : " (Estándar)"));
        detalleLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

        Label precioLabel = new Label(String.format("Precio: %.2f €", entrada.getPrecio()));
        precioLabel.setStyle("-fx-text-fill: #e0e0e0; -fx-font-size: 14px;");

        infoBox.getChildren().addAll(nombreLabel, detalleLabel, precioLabel);

        Button eliminarBtn = eliminarReservasBtn(entrada);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        contentBox.getChildren().addAll(infoBox, spacer, eliminarBtn);
        entradaCard.getChildren().add(contentBox);

        return entradaCard;
    }

    // Método para eliminar una entrada de la cesta
    private Button eliminarReservasBtn(EntradaCesta entrada) {
        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");

        eliminarBtn.setOnAction(e -> {
            // Eliminar la reserva temporal
            try {
                Connection conn = DatabaseConnection.getConnection();
                ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);
                String idReservaTemp = entrada.getIdEspectaculo() + "_" + idUsuario + "_F" + entrada.getFila() + "-C" + entrada.getCol();
                reservaDao.eliminarReservaTemporal(idReservaTemp);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            // Eliminar de la cesta
            entradas.remove(entrada);
            total -= entrada.getPrecio();
            actualizarCesta();
            CestaStorage.guardarCesta(emailUsuarioLogueado, entradas);
        });
        return eliminarBtn;
    }

    // Método para confirmar la compra de entradas
    public void confirmarCompra(ActionEvent actionEvent) {
        if (entradas.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Cesta vacía");
            alert.setContentText("No hay entradas en la cesta para confirmar.");
            alert.show();
            return;
        }

        // Verificar si hay usuario está logueado. NECESARIO EN CASO DE BUGS DE LOGIN
        if (idUsuario == null || idUsuario.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error de usuario");
            alert.setContentText("No se ha identificado correctamente al usuario.");
            alert.show();
            return;
        }

        Alert confirmAlert = new Alert(AlertType.CONFIRMATION);
        confirmAlert.setTitle("CONFIRMAR COMPRA");
        confirmAlert.setContentText("¿Quiere confirmar la compra de todas las entradas en la cesta?");

        confirmarCompraEvento(confirmAlert);
    }

    private void confirmarCompraEvento(Alert confirmAlert) {
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Connection conn = null;
                ReservaDaoImpl reservaDao = null;
                boolean errorOcurrido = false;
                List<String> reservasFallidas = new ArrayList<>();

                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);
                    reservaDao = new ReservaDaoImpl(conn);

                    for (EntradaCesta entrada : entradas) {
                        try {
                            // Reservar la entrada
                            reservarEntrada(entrada, reservaDao);
                        } catch (SQLException e) {
                            // Si ocurre un error al reservar, se añade a la lista de reservas fallidas
                            reservasFallidas.add(entrada.getNombreEspectaculo() + " _ Butaca: F" + entrada.getFila() + "-C" + entrada.getCol());
                            e.printStackTrace();
                        }
                    }

                    // Si no hay reservas fallidas, se confirma la compra
                    if (reservasFallidas.isEmpty()) {
                        exitoCompra(conn);
                    } else {
                        falloCompra(conn, reservasFallidas);
                    }

                } catch (SQLException e) {
                    errorOcurrido = true;
                    rollbackCompra(conn);

                    mostrarError(e);
                } finally {
                    volverEstadoOriginal(conn);
                }
            }
        });
    }

    private static void rollbackCompra(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void mostrarError(SQLException e) {
        Alert errorAlert = new Alert(AlertType.ERROR);
        errorAlert.setTitle("Error en la compra");
        errorAlert.setContentText("Error grave al procesar la compra: " + e.getMessage());
        errorAlert.show();
        e.printStackTrace();
    }

    private static void volverEstadoOriginal(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void falloCompra(Connection conn, List<String> reservasFallidas) throws SQLException {
        conn.rollback();
        Alert partialAlert = new Alert(AlertType.WARNING);
        partialAlert.setTitle("Compra parcialmente completada");
        partialAlert.setContentText("No se pudieron reservar las siguientes entradas:\n" +
                String.join("\n", reservasFallidas) +
                "\n\nNinguna reserva ha sido procesada. Por favor, inténtelo de nuevo.");
        partialAlert.show();
    }

    // Método para confirmar la compra de entradas. Si se confirma, se procesan las reservas y se actualiza la cesta.
    private void exitoCompra(Connection conn) throws SQLException {
        // Limpiar reservas temporales
        ReservaDaoImpl reservaDao = new ReservaDaoImpl(conn);
        for (EntradaCesta entrada : entradas) {
            String idReservaTemp = entrada.getIdEspectaculo() + "_" + idUsuario + "_F" + entrada.getFila() + "-C" + entrada.getCol();
            reservaDao.eliminarReservaTemporal(idReservaTemp);
        }

        conn.commit();
        entradas.clear();
        CestaStorage.guardarCesta(emailUsuarioLogueado, entradas);
        actualizarCesta();

        Alert successAlert = new Alert(AlertType.INFORMATION);
        successAlert.setTitle("Compra confirmada");
        successAlert.setContentText("Todas las entradas se han reservado con éxito.");
        successAlert.show();
    }

    // Método para reservar una entrada. Verifica si el ID de usuario está disponible y luego registra la reserva en la base de datos.
    private void reservarEntrada(EntradaCesta entrada, ReservaDaoImpl reservaDao) throws SQLException {
        if (idUsuario == null || idUsuario.isEmpty()) {
            throw new SQLException("ID de usuario no está disponible");
        }

        String idReservaTemp = entrada.getIdEspectaculo() + "_" + idUsuario + "_F" + entrada.getFila() + "-C" + entrada.getCol();
        reservaDao.eliminarReservaTemporal(idReservaTemp);

        Reservas reserva = new Reservas();
        String idReserva = idReservaTemp;
        reserva.setId_reserva(idReserva);
        reserva.setId_espectaculo(entrada.getIdEspectaculo());
        reserva.setId_butaca("F" + entrada.getFila() + "-C" + entrada.getCol());
        reserva.setId_usuario(idUsuario);
        reserva.setPrecio(entrada.getPrecio());
        reserva.setEstado('O');

        reservaDao.registrarReservas(reserva);
    }


    //método para cerrar sesión y volver al login
    //bastante sencillo, setea el valor del mail a nulo y manda de vuelta al login
    public void cerrarSesion(ActionEvent actionEvent) {
        emailUsuarioLogueado = null;
        Stage stage = (Stage) usuarioLabel.getScene().getWindow();
        CerrarSesion.cerrarSesion(stage, "/resources/styles/styles.css", "/resources/images/logo.png");
    }

    //A IMPLEMENTAR
    public void volverCartelera(ActionEvent actionEvent) {
        cambioEscena("/views/cartelera.fxml");
    }

    //método para cambiar de escena
    // Ahora se usa Transitions.cambioEscena para cambiar de escena
    private void cambioEscena(String name) {
        Stage stage = (Stage) contenedorEntradas.getScene().getWindow();
        // Se asume que el stylesheet y el icon path son los mismos siempre
        Transitions.cambioEscena(stage, name, "/resources/styles/styles.css", "CINES JRC", "/resources/images/logo.png", null);
    }

    public void filtrarPorFecha(ActionEvent actionEvent) {
    }

    public String getValorEleccionBox() {
        return eleccionBox.getValue().toString();
    }


    public void setEmailUsuarioLogueado(String email) {
        this.emailUsuarioLogueado = email;
        if (usuarioLabel != null) {
            usuarioLabel.setText("Email: " + email);
        }

        // Obtener el ID del usuario al establecer el email
        cargarUsuario(this.idUsuario, email);

        // Cargar el ID del usuario desde la base de datos
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
            this.idUsuario = usuarioDao.getIDUsuarioByEmail(email);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Cargar la cesta desde el almacenamiento
        this.entradas = CestaStorage.cargarCesta(email);
        // Recalcular el total
        this.total = entradas.stream().mapToDouble(EntradaCesta::getPrecio).sum();
        actualizarCesta();
    }



    public void setIdUsuario(String idUsuario) {
        this.idUsuario=idUsuario;
    }
}