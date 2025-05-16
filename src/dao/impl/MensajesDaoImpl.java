package dao.impl;

import dao.MensajesDaoI;
import models.Mensajes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MensajesDaoImpl implements MensajesDaoI {

    private final Connection conn;

    public MensajesDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Mensajes> mostrarMensajes() throws SQLException {
        List<Mensajes> mensajesList = new ArrayList<>();
        String query = "SELECT * FROM SOLICITUDES m ORDER BY m.ID_USUARIO" ;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Mensajes mensajes = new Mensajes();
                    mensajes.setId_solicitud(rs.getInt("ID_SOLICITUD"));
                    mensajes.setId_usuario(rs.getString("ID_USUARIO"));
                    mensajes.setId_reserva(rs.getString("ID_RESERVA"));
                    mensajes.setFecha(rs.getTimestamp("FECHA"));
                    mensajes.setTipo_solicitud(rs.getString("TIPO_SOLICITUD"));
                    mensajes.setEstado_solicitud(rs.getString("ESTADO_SOLICITUD").charAt(0));

                    mensajesList.add(mensajes);

                }
            }
        }

        return mensajesList;

    }

    @Override
    public boolean eliminarMensaje(int idSolicitud) throws SQLException {
        String query = "DELETE FROM SOLICITUDES WHERE ID_SOLICITUD = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSolicitud);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean actualizarEstadoMensaje(int idSolicitud, char nuevoEstado) throws SQLException {
        String query = "UPDATE SOLICITUDES SET ESTADO_SOLICITUD = ? WHERE ID_SOLICITUD = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, String.valueOf(nuevoEstado));
            pstmt.setInt(2, idSolicitud);
            return pstmt.executeUpdate() > 0;
        }
    }

    @Override
    public boolean crearSolicitud(Mensajes solicitud) throws SQLException {
        String query = "INSERT INTO SOLICITUDES (ID_USUARIO, ID_RESERVA, TIPO_SOLICITUD, ESTADO_SOLICITUD) " +
                "VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, solicitud.getId_usuario());
            pstmt.setString(2, solicitud.getId_reserva());
            pstmt.setString(3, solicitud.getTipo_solicitud());
            pstmt.setString(4, String.valueOf(solicitud.getEstado_solicitud()));

            return pstmt.executeUpdate() > 0;
        }
    }
}


