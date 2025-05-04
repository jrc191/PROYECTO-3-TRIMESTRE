package controllers.admin;

import dao.ReservasDaoI;
import dao.impl.ReservaDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Reservas;
import utils.DatabaseConnection;
import utils.Transitions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ListarReservasController {

    @FXML
    private VBox contenedorReservas;
    @FXML
    private ScrollPane scrollReservas;
    @FXML
    private Label mensajeLabel;
    @FXML
    private Label flechaArriba;
    @FXML
    private Label flechaAbajo;

    private ReservasDaoI reservasDao;
    private boolean scrollConfigDone = false;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            reservasDao = new ReservaDaoImpl(conn);
            cargarReservas();
            configurarScroll();
        } catch (SQLException e) {
            e.printStackTrace();
            mensajeLabel.setText("Error al conectar con la base de datos");
        }
    }

    private void configurarScroll() {
        if (!scrollConfigDone && scrollReservas != null && flechaArriba != null && flechaAbajo != null) {
            Transitions.configurarListenersScroll(scrollReservas, flechaArriba, flechaAbajo);
            scrollReservas.setPannable(true);
            scrollConfigDone = true;
        }
    }

    public void cargarReservas() {
        try {
            List<Reservas> reservas = reservasDao.listarTodasReservas();
            contenedorReservas.getChildren().clear();

            if (reservas.isEmpty()) {
                mensajeLabel.setText("No hay reservas registradas");
                return;
            }

            mensajeLabel.setText("Total de reservas: " + reservas.size());

            // Crear encabezado de la tabla
            HBox header = new HBox();
            header.setStyle("-fx-background-color: #232a4d; -fx-padding: 8px;");

            // Columnas del encabezado
            Label espHeader = new Label("Espectaculo");
            Label butacaHeader = new Label("Butaca");
            Label usuarioHeader = new Label("Usuario");
            Label precioHeader = new Label("Precio");

            // Ajustar ancho de columnas
            espHeader.setPrefWidth(125);
            butacaHeader.setPrefWidth(125);
            usuarioHeader.setPrefWidth(125);
            precioHeader.setPrefWidth(125);

            // Estilo del encabezado
            String headerStyle = "-fx-text-fill: #FFC107; -fx-font-weight: bold;";
            espHeader.setStyle(headerStyle);
            butacaHeader.setStyle(headerStyle);
            usuarioHeader.setStyle(headerStyle);
            precioHeader.setStyle(headerStyle);

            header.getChildren().addAll(espHeader, butacaHeader,
                    usuarioHeader, precioHeader);
            contenedorReservas.getChildren().add(header);

            // Filas de datos
            for (Reservas reserva : reservas) {
                HBox row = new HBox();
                row.setStyle("-fx-background-color: transparent; -fx-padding: 7px;");

                // Crear celdas
                Label espCell = new Label(reserva.getId_espectaculo());
                Label butacaCell = new Label(reserva.getId_butaca());
                Label usuarioCell = new Label(reserva.getId_usuario());
                Label precioCell = new Label(String.format("%.2f", reserva.getPrecio())+" €");

                // Ajustar ancho de celdas (debe coincidir con el encabezado)
                espCell.setPrefWidth(125);
                butacaCell.setPrefWidth(125);
                usuarioCell.setPrefWidth(125);
                precioCell.setPrefWidth(125);

                // Estilo de celdas
                String cellStyle = "-fx-text-fill: white;";
                espCell.setStyle(cellStyle);
                butacaCell.setStyle(cellStyle);
                usuarioCell.setStyle(cellStyle);
                precioCell.setStyle(cellStyle);

                row.getChildren().addAll(espCell, butacaCell,
                        usuarioCell, precioCell);
                contenedorReservas.getChildren().add(row);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            mensajeLabel.setText("Error al cargar las reservas");
        }
    }
}