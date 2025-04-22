package dao.impl;

import dao.ReservasDaoI;
import models.Butaca;
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
            String estado= ""+reserva.getEstado();
            pstmt.setString(1, reserva.getId_reserva());
            pstmt.setString(2, reserva.getId_espectaculo());
            pstmt.setString(3, reserva.getId_butaca());
            pstmt.setString(4, reserva.getId_usuario());
            pstmt.setString(5, estado);
            pstmt.setDouble(6, reserva.getPrecio());
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
}
