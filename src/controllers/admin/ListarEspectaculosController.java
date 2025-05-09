package controllers.admin;

import dao.EspectaculoDaoI;
import dao.impl.EspectaculoDaoImpl;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import models.Espectaculo;
import utils.DatabaseConnection;
import utils.Transitions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ListarEspectaculosController {

    @FXML
    private VBox contenedorEspectaculos;
    @FXML
    private ScrollPane scrollEspectaculos;
    @FXML
    private Label mensajeLabel;
    @FXML
    private Label flechaArriba;
    @FXML
    private Label flechaAbajo;

    private EspectaculoDaoI espectaculoDao;
    private boolean scrollConfigDone = false;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            espectaculoDao = new EspectaculoDaoImpl(conn);
            cargarEspectaculos();
            configurarScroll();
        } catch (SQLException e) {
            e.printStackTrace();
            mensajeLabel.setText("Error al conectar con la base de datos");
        }
    }

    private void configurarScroll() {
        if (!scrollConfigDone && scrollEspectaculos != null && flechaArriba != null && flechaAbajo != null) {
            Transitions.configurarListenersScroll(scrollEspectaculos, flechaArriba, flechaAbajo);
            scrollEspectaculos.setPannable(true);
            scrollConfigDone = true;
        }
    }

    public void cargarEspectaculos() {
        List<Espectaculo> espectaculos = espectaculoDao.obtenerTodos();
        contenedorEspectaculos.getChildren().clear();

        if (espectaculos.isEmpty()) {
            mensajeLabel.setText("No hay espectáculos registrados");
            mensajeLabel.setStyle("fx-text-fill: red");
            return;
        }

        mensajeLabel.setText("Total de Espectaculos: " + espectaculos.size());
        mensajeLabel.setStyle("fx-text-fill: green");

        // Crear encabezado de la tabla
        HBox header = new HBox();
        header.setStyle("-fx-background-color: #232a4d; -fx-padding: 8px;");

        // Columnas del encabezado
        Label nombreHeader = new Label("Nombre");
        Label fechaHeader = new Label("Fecha");
        Label precioHeader = new Label("Precio");
        Label precioVIPHeader = new Label("Precio VIP");

        // Ajustar ancho de columnas
        nombreHeader.setPrefWidth(160);
        fechaHeader.setPrefWidth(125);
        precioHeader.setPrefWidth(100);
        precioVIPHeader.setPrefWidth(100);

        // Estilo del encabezado
        String headerStyle = "-fx-text-fill: #FFC107; -fx-font-weight: bold;";
        nombreHeader.setStyle(headerStyle);
        fechaHeader.setStyle(headerStyle);
        precioHeader.setStyle(headerStyle);
        precioVIPHeader.setStyle(headerStyle);

        header.getChildren().addAll(nombreHeader,
                fechaHeader, precioHeader, precioVIPHeader);
        contenedorEspectaculos.getChildren().add(header);

        // Filas de datos
        for (Espectaculo espectaculo : espectaculos) {
            HBox row = new HBox();
            row.setStyle("-fx-background-color: transparent; -fx-padding: 7px;");

            // Crear celdas
            Label nombreCell = new Label(espectaculo.getNombre());
            Label fechaCell = new Label(espectaculo.getFecha().toString());
            Label precioCell = new Label(String.format("%.2f", espectaculo.getPrecioBase())+" €");
            Label precioVIPCell = new Label(String.format("%.2f", espectaculo.getPrecioVip())+" €");

            // Ajustar ancho de celdas (debe coincidir con el encabezado)
            nombreCell.setPrefWidth(160);
            fechaCell.setPrefWidth(125);
            precioCell.setPrefWidth(100);
            precioVIPCell.setPrefWidth(100);

            // Estilo de celdas
            String cellStyle = "-fx-text-fill: white;";
            nombreCell.setStyle(cellStyle);
            fechaCell.setStyle(cellStyle);
            precioCell.setStyle(cellStyle);
            precioVIPCell.setStyle(cellStyle);

            row.getChildren().addAll(nombreCell,
                    fechaCell, precioCell, precioVIPCell);
            contenedorEspectaculos.getChildren().add(row);
        }

    }
}