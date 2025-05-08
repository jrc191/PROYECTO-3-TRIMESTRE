package dao;

import models.Reservas;

import java.sql.SQLException;
import java.util.List;

public interface ReservasDaoI {
    boolean registrarReservas(Reservas reserva) throws SQLException;
    boolean registrarReservasTEMP(Reservas reserva) throws SQLException;
    List<Reservas> consultarReservas(String id_espectaculo, String id_usuario) throws SQLException;
    List<Reservas> consultarReservasTEMP(String id_espectaculo, String id_usuario) throws SQLException;
    List<Reservas> listarTodasReservas() throws SQLException;
    public void eliminarReservaTemporal(String idReserva) throws SQLException;
    public void eliminarReservasById(String idUser) throws SQLException;
    public void eliminarReservasTemporalesUsuario(String idUsuario) throws SQLException;
    public int contarReservasPorUsuarioYEspectaculo(String idUsuario, String idEspectaculo) throws SQLException;
}
