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
}


