package controllers.admin;

import dao.ReservasDaoI;
import dao.impl.ReservaDaoImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import models.Reservas;
import utils.DatabaseConnection;
import utils.Transitions;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EstadisticasController {
    @FXML public VBox leftPane;
    @FXML private ScrollPane scrollStats;
    @FXML private Label messageLabel;
    @FXML private Label flechaArriba;
    @FXML private Label flechaAbajo;
    @FXML private VBox stats;

    private ReservasDaoI reservasDao;
    private boolean scrollConfigDone = false;

    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            reservasDao = new ReservaDaoImpl(conn);
            configurarScroll();
            List<Reservas> reservasList = reservasDao.listarTodasReservas();

            scrollStats.setStyle("-fx-background: transparent; -fx-background-color: transparent;");

            stats.getChildren().clear();

            // PIECHART DE RESERVAS POR ESPECTÁCULO
            reservasPorEspectaculo(reservasList);

        } catch (SQLException e) {
            e.printStackTrace();
            messageLabel.setText("Error al conectar con la base de datos");
        }
    }

    private void reservasPorEspectaculo(List<Reservas> reservasList) {
        Map<String, Long> reservasCount = reservasList.stream()
                .collect(Collectors.groupingBy(
                        Reservas::getId_espectaculo,
                        Collectors.counting()
                ));

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        reservasCount.forEach((id, count) ->
                pieData.add(new PieChart.Data(id + " (" + count + ")", count))
        );

        PieChart pieChart = new PieChart(pieData);
        pieChart.setTitle("Reservas por Espectáculo");
        pieChart.setStyle("-fx-text-fill: white");
        pieChart.setLegendVisible(true);
        pieChart.setLabelsVisible(true);

        stats.getChildren().add(pieChart);
    }

    private void configurarScroll() {
        if (!scrollConfigDone && scrollStats != null && flechaArriba != null && flechaAbajo != null) {
            Transitions.configurarListenersScroll(scrollStats, flechaArriba, flechaAbajo);
            scrollStats.setPannable(true);
            scrollConfigDone = true;
        }
    }
}