package controllers.admin;

import dao.MensajesDaoI;
import dao.UsuarioDaoI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class ListarMensajesController {
    @FXML public ImageView actualizarBtn;
    @FXML public ImageView eliminarBtn;
    @FXML public Button guardarBtn;
    @FXML public Button cancelarBtn;
    @FXML public ScrollPane scrollMensajes;
    @FXML public VBox scrollVBox;

    private MensajesDaoI mensajeDao;

    public void initialize() {
        guardarBtn.setOnAction(e -> actualizarEstadoMensaje());
        cancelarBtn.setOnAction(e-> borrarNuevoMensaje());

        
    }

    private void borrarNuevoMensaje() {


    }

    private void actualizarEstadoMensaje() {
    }


}
