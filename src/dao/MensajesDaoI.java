package dao;

import models.Mensajes;

import java.sql.SQLException;
import java.util.List;

public interface MensajesDaoI {

    List<Mensajes> mostrarMensajes() throws SQLException;
}
