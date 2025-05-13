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
    void eliminarReservaTemporal(String idReserva) throws SQLException;
    void eliminarReservasById(String idUser) throws SQLException;
    void eliminarReservasTemporalesUsuario(String idUsuario) throws SQLException;
    int contarReservasPorUsuarioYEspectaculo(String idUsuario, String idEspectaculo, String idButaca) throws SQLException;
    int cancelarReservasByUsuarioID(String idUsuario) throws SQLException;
    int eliminarReservasTemporalesByUsuario(String idUsuario) throws SQLException;
    int cancelarReserva(String idReserva) throws SQLException;
}
