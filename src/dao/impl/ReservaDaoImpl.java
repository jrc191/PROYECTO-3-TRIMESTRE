package dao.impl;

import dao.ReservasDaoI;
import models.Reservas;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

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

    @Override
    public int contarReservasPorUsuarioYEspectaculo(String idUsuario, String idEspectaculo, String idButaca) throws SQLException {
        String query = """
        SELECT COUNT(*) as total 
        FROM RESERVAS 
        WHERE id_usuario = ? 
        AND id_espectaculo = ?
        AND id_butaca = ?
        AND estado = 'O'
        """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idUsuario);
            pstmt.setString(2, idEspectaculo);
            pstmt.setString(3, idButaca);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt("total");
            }
            return 0;
        }
    }

    @Override
    public int cancelarReservasByUsuarioID(String idUsuario) throws SQLException {
        String query = "UPDATE RESERVAS SET estado = 'C' WHERE id_usuario = ? AND estado = 'O'";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idUsuario);
            return pstmt.executeUpdate();
        }
    }

    @Override
    public int reactivarReserva(String idReserva) throws SQLException {
        // 1. Obtener los datos de la reserva del historial
        String selectQuery = "SELECT * FROM HISTORIAL_RESERVAS WHERE ID_RESERVA = ?";
        Reservas reserva = null;

        try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
            pstmt.setString(1, idReserva);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado('O'); // 'O' para Activa
                reserva.setFecha(rs.getTimestamp("FECHA"));
            }
        }

        if (reserva == null) {
            return 0;
        }

        // 2. Verificar si la butaca ya está ocupada
        String checkQuery = "SELECT COUNT(*) FROM RESERVAS WHERE ID_ESPECTACULO = ? AND ID_BUTACA = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
            checkStmt.setString(1, reserva.getId_espectaculo());
            checkStmt.setString(2, reserva.getId_butaca());
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return -1; // Butaca ya ocupada
            }
        }

        // 3. Insertar en RESERVAS
        String insertQuery = "INSERT INTO RESERVAS (ID_RESERVA, ID_ESPECTACULO, ID_BUTACA, ID_USUARIO, PRECIO, ESTADO, FECHA_RESERVA) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, reserva.getId_reserva());
            pstmt.setString(2, reserva.getId_espectaculo());
            pstmt.setString(3, reserva.getId_butaca());
            pstmt.setString(4, reserva.getId_usuario());
            pstmt.setDouble(5, reserva.getPrecio());
            pstmt.setString(6, String.valueOf(reserva.getEstado()));
            pstmt.setTimestamp(7, reserva.getFecha());

            int inserted = pstmt.executeUpdate();

            // 4. Eliminar del historial si se insertó correctamente
            if (inserted > 0) {
                String deleteQuery = "DELETE FROM HISTORIAL_RESERVAS WHERE ID_RESERVA = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                    deleteStmt.setString(1, idReserva);
                    return deleteStmt.executeUpdate();
                }
            }
        }
        return 0;
    }

    @Override
    public int cancelarReserva(String idReserva) throws SQLException {
        // 1. Obtener los datos de la reserva
        String selectQuery = "SELECT * FROM RESERVAS WHERE ID_RESERVA = ?";
        Reservas reserva = null;

        try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
            pstmt.setString(1, idReserva);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado('C'); // 'C' para Cancelada
                reserva.setFecha(rs.getTimestamp("FECHA_RESERVA"));
            }
        }

        if (reserva == null) {
            return 0;
        }

        // 2. Insertar en historial
        String insertQuery = "INSERT INTO HISTORIAL_RESERVAS (ID_RESERVA, ID_ESPECTACULO, ID_BUTACA, " +
                "ID_USUARIO, PRECIO, ESTADO, FECHA, FECHA_CANCELACION) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, reserva.getId_reserva());
            pstmt.setString(2, reserva.getId_espectaculo());
            pstmt.setString(3, reserva.getId_butaca());
            pstmt.setString(4, reserva.getId_usuario());
            pstmt.setDouble(5, reserva.getPrecio());
            pstmt.setString(6, String.valueOf(reserva.getEstado()));
            pstmt.setTimestamp(7, reserva.getFecha());

            int inserted = pstmt.executeUpdate();

            // 3. Eliminar de reservas activas si se insertó correctamente
            if (inserted > 0) {
                String deleteQuery = "DELETE FROM RESERVAS WHERE ID_RESERVA = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                    deleteStmt.setString(1, idReserva);
                    return deleteStmt.executeUpdate();
                }
            }
        }
        return 0;
    }

    @Override
    public int eliminarReservasTemporalesByUsuario(String idUsuario) throws SQLException {
        String query = "DELETE FROM RESERVAS_TEMP WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idUsuario);
            return pstmt.executeUpdate();
        }
    }

    public boolean estaDentroPlazoCancelacion(String idReserva) throws SQLException {
        String query = "SELECT FECHA_RESERVA FROM RESERVAS WHERE ID_RESERVA = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idReserva);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Timestamp fechaReserva = rs.getTimestamp("FECHA_RESERVA");
                long diferencia = System.currentTimeMillis() - fechaReserva.getTime();
                return diferencia < TimeUnit.HOURS.toMillis(24); // Menos de 24 horas
            }
        }
        return false;
    }

    public boolean moverAHistorial(String idReserva, char estado) throws SQLException {
        // 1. Obtener los datos de la reserva
        String selectQuery = "SELECT * FROM RESERVAS WHERE ID_RESERVA = ?";
        Reservas reserva = null;

        try (PreparedStatement pstmt = conn.prepareStatement(selectQuery)) {
            pstmt.setString(1, idReserva);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado(estado);
                reserva.setFecha(rs.getTimestamp("FECHA_RESERVA"));
            }
        }

        if (reserva == null) {
            return false;
        }

        // 2. Insertar en historial
        String insertQuery = "INSERT INTO HISTORIAL_RESERVAS (ID_RESERVA, ID_ESPECTACULO, ID_BUTACA, " +
                "ID_USUARIO, PRECIO, ESTADO, FECHA, FECHA_CANCELACION) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertQuery)) {
            pstmt.setString(1, reserva.getId_reserva());
            pstmt.setString(2, reserva.getId_espectaculo());
            pstmt.setString(3, reserva.getId_butaca());
            pstmt.setString(4, reserva.getId_usuario());
            pstmt.setDouble(5, reserva.getPrecio());
            pstmt.setString(6, String.valueOf(reserva.getEstado()));
            pstmt.setTimestamp(7, reserva.getFecha());

            int inserted = pstmt.executeUpdate();

            // 3. Eliminar de reservas activas si se insertó correctamente
            if (inserted > 0) {
                String deleteQuery = "DELETE FROM RESERVAS WHERE ID_RESERVA = ?";
                try (PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {
                    deleteStmt.setString(1, idReserva);
                    return deleteStmt.executeUpdate() > 0;
                }
            }
        }
        return false;
    }


    @Override
    public List<Reservas> listarHistorialReservas() throws SQLException {
        List<Reservas> historial = new ArrayList<>();
        String query = "SELECT * FROM HISTORIAL_RESERVAS ORDER BY FECHA_CANCELACION DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservas reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado(rs.getString("ESTADO").charAt(0));
                reserva.setFecha(rs.getTimestamp("FECHA"));
                reserva.setFechaCancelacion(rs.getTimestamp("FECHA_CANCELACION"));

                historial.add(reserva);
            }
        }
        return historial;
    }

    @Override
    public List<Reservas> consultarHistorialByUsuario(String idUsuario) throws SQLException {
        List<Reservas> historial = new ArrayList<>();
        String query = "SELECT * FROM HISTORIAL_RESERVAS WHERE ID_USUARIO = ? ORDER BY FECHA_CANCELACION DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idUsuario);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservas reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado(rs.getString("ESTADO").charAt(0));
                reserva.setFecha(rs.getTimestamp("FECHA"));
                reserva.setFechaCancelacion(rs.getTimestamp("FECHA_CANCELACION"));

                historial.add(reserva);
            }
        }
        return historial;
    }

    // También necesitamos actualizar los métodos existentes para incluir la fecha
    @Override
    public List<Reservas> listarTodasReservas() throws SQLException {
        List<Reservas> reservas = new ArrayList<>();
        String query = "SELECT * FROM RESERVAS ORDER BY FECHA_RESERVA DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservas reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado(rs.getString("ESTADO").charAt(0));
                reserva.setFecha(rs.getTimestamp("FECHA_RESERVA"));

                reservas.add(reserva);
            }
        }
        return reservas;
    }

    @Override
    public List<Reservas> consultarReservasByUsuario(String id_usuario) throws SQLException {
        List<Reservas> reservas = new ArrayList<>();
        String query = "SELECT * FROM RESERVAS WHERE ID_USUARIO = ? ORDER BY FECHA_RESERVA DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, id_usuario);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Reservas reserva = new Reservas();
                reserva.setId_reserva(rs.getString("ID_RESERVA"));
                reserva.setId_espectaculo(rs.getString("ID_ESPECTACULO"));
                reserva.setId_butaca(rs.getString("ID_BUTACA"));
                reserva.setId_usuario(rs.getString("ID_USUARIO"));
                reserva.setPrecio(rs.getDouble("PRECIO"));
                reserva.setEstado(rs.getString("ESTADO").charAt(0));
                reserva.setFecha(rs.getTimestamp("FECHA_RESERVA"));

                reservas.add(reserva);
            }
        }
        return reservas;
    }

}
