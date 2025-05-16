package dao;

import models.Mensajes;

import java.sql.SQLException;
import java.util.List;

public interface MensajesDaoI {

    List<Mensajes> mostrarMensajes() throws SQLException;
    boolean eliminarMensaje(int idSolicitud) throws SQLException;
    boolean actualizarEstadoMensaje(int idSolicitud, char nuevoEstado) throws SQLException;
    boolean crearSolicitud(Mensajes solicitud) throws SQLException;
}
