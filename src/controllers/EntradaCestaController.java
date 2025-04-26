package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import models.EntradaCesta;

public class EntradaCestaController {

    @FXML
    private Label nombreLabel;

    @FXML
    private Label detalleLabel;

    @FXML
    private Label precioLabel;

    @FXML
    private Button eliminarBtn;

    private Runnable onEliminarCallback;

    private EntradaCesta entrada;

    public void setEntrada(EntradaCesta entrada) {
        this.entrada = entrada;
        actualizarVista();
    }

    private void actualizarVista() {
        if (entrada != null) {
            nombreLabel.setText(entrada.getNombreEspectaculo());
            detalleLabel.setText("Butaca: " + entrada.getFila() + ", " + entrada.getCol() +
                    (entrada.isVip() ? " (VIP)" : " (Estándar)"));
            precioLabel.setText(String.format("Precio: %.2f €", entrada.getPrecio()));
        }
    }

    public void setOnEliminar(Runnable callback) {
        this.onEliminarCallback = callback; //para eliminar entradas.
    }

    @FXML
    private void initialize() {
        eliminarBtn.setOnAction(event -> {
            if (onEliminarCallback != null) {
                onEliminarCallback.run();
            }
        });
    }
}