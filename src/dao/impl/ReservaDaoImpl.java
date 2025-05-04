package dao.impl;

import dao.ReservasDaoI;
import models.Reservas;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservaDaoImpl implements ReservasDaoI {

    private final Connection conn;

    public ReservaDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public boolean registrarReservas(Reservas reserva) throws SQLException {
        String query = "INSERT INTO RESERVAS (id_reserva, id_espectaculo, id_butaca, id_usuario, estado, precio) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            char estado= reserva.getEstado();
            pstmt.setString(1, reserva.getId_reserva());
            pstmt.setString(2, reserva.getId_espectaculo());
            pstmt.setString(3, reserva.getId_butaca());
            pstmt.setString(4, reserva.getId_usuario());
            pstmt.setString(5, String.valueOf(estado));
            pstmt.setDouble(6, reserva.getPrecio());
            pstmt.executeUpdate();
            return true;
        }
    }

    @Override
    public boolean registrarReservasTEMP(Reservas reserva) throws SQLException {
        String query = "INSERT INTO RESERVAS_TEMP (id_reserva, id_espectaculo, id_butaca, id_usuario) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, reserva.getId_reserva());
            pstmt.setString(2, reserva.getId_espectaculo());
            pstmt.setString(3, reserva.getId_butaca());
            pstmt.setString(4, reserva.getId_usuario());
            pstmt.executeUpdate();
            return true;
        }
    }

    @Override
    public List<Reservas> consultarReservas(String id_espectaculo, String id_usuario) throws SQLException {

        List<Reservas> reservasList = new ArrayList<>();
        String query = "SELECT r.id_reserva, r.id_espectaculo, r.id_butaca, r.id_usuario, r.estado, r.precio FROM RESERVAS r, USUARIOS u WHERE r.id_usuario = ? AND r.id_espectaculo = ?" ;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id_usuario);
            pstmt.setString(2, id_espectaculo);
            ResultSet rs = pstmt.executeQuery();

        }

        return reservasList;
    }

    @Override
    public List<Reservas> consultarReservasTEMP(String id_espectaculo, String id_usuario) throws SQLException {
        List<Reservas> reservasList = new ArrayList<>();
        String query = "SELECT id_reserva, id_espectaculo, id_butaca, id_usuario FROM RESERVAS_TEMP WHERE id_usuario = ? AND id_espectaculo = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id_usuario);
            pstmt.setString(2, id_espectaculo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservas reserva = new Reservas();
                reserva.setId_reserva(rs.getString("id_reserva"));
                reserva.setId_espectaculo(rs.getString("id_espectaculo"));
                reserva.setId_butaca(rs.getString("id_butaca"));
                reserva.setId_usuario(rs.getString("id_usuario"));
                reservasList.add(reserva);
            }
        }
        return reservasList;
    }

    @Override
    public void eliminarReservaTemporal(String idReserva) throws SQLException {
        String query = "DELETE FROM RESERVAS_TEMP WHERE id_reserva = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idReserva);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Filas afectadas al eliminar reserva temporal: " + affectedRows);
        }
    }

    //SE USA NORMALMENTE EN CASO DE FALLO EN ALGUNA DE LAS COMPRAS.
    @Override
    public void eliminarReservasById(String idReserva) throws SQLException {
        String query = "DELETE FROM RESERVAS WHERE WHERE id_reserva = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idReserva);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Filas afectadas al eliminar reserva: " + affectedRows);
        }
    }

    @Override
    public List<Reservas> listarTodasReservas() throws SQLException {
        List<Reservas> reservasList = new ArrayList<>();
        String query = "SELECT r.id_reserva, r.id_espectaculo, r.id_butaca, r.id_usuario, r.estado, r.precio " +
                "FROM RESERVAS r ORDER BY r.id_reserva";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Reservas reserva = new Reservas();
                reserva.setId_reserva(rs.getString("id_reserva"));
                reserva.setId_espectaculo(rs.getString("id_espectaculo"));
                reserva.setId_butaca(rs.getString("id_butaca"));
                reserva.setId_usuario(rs.getString("id_usuario"));
                reserva.setEstado(rs.getString("estado").charAt(0));
                reserva.setPrecio(rs.getDouble("precio"));
                reservasList.add(reserva);
            }
        }
        return reservasList;
    }

    /**
     * Elimina todas las reservas temporales de un usuario (por id_usuario) en RESERVAS_TEMP.
     */

    @Override
    public void eliminarReservasTemporalesUsuario(String idUsuario) throws SQLException {
        String query = "DELETE FROM RESERVAS_TEMP WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idUsuario);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Filas afectadas al eliminar reservas temporales del usuario: " + affectedRows);
        }
    }


}
