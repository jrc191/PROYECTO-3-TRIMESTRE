package dao;

import models.Butaca;
import models.Reservas;
import models.Usuario;

import java.sql.SQLException;
import java.util.List;

public interface ReservasDaoI {
    boolean registrarReservas(Reservas reserva) throws SQLException;
    List<Reservas> consultarReservas(String id_espectaculo, String id_usuario) throws SQLException;

}
