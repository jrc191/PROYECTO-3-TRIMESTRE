package dao.impl;

import dao.EspectaculoDaoI;
import models.Espectaculo;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class EspectaculoDaoImpl implements EspectaculoDaoI {
    private final Connection connection;

    public EspectaculoDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public List<Espectaculo> obtenerTodos() {
        List<Espectaculo> lista = new ArrayList<>();
        String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip FROM ESPECTACULOS";

        try (PreparedStatement pstmt = connection.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Espectaculo(
                        rs.getString("id_espectaculo"),
                        rs.getString("nombre"),
                        rs.getDate("fecha").toLocalDate(),
                        rs.getDouble("precio_base"),
                        rs.getDouble("precio_vip")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al consultar todos los espectáculos", e);
        }
        return lista;
    }

    @Override
    public List<Espectaculo> obtenerPorNombre(String nombre) {
        List<Espectaculo> lista = new ArrayList<>();
        String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip " +
                "FROM ESPECTACULOS WHERE LOWER(nombre) LIKE LOWER(?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, "%" + nombre + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Espectaculo(
                            rs.getString("id_espectaculo"),
                            rs.getString("nombre"),
                            rs.getDate("fecha").toLocalDate(),
                            rs.getDouble("precio_base"),
                            rs.getDouble("precio_vip")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al consultar espectáculos por nombre", e);
        }
        return lista;
    }

    @Override
    public List<Espectaculo> obtenerPorFecha(LocalDate fecha) {
        List<Espectaculo> lista = new ArrayList<>();
        String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip " +
                "FROM ESPECTACULOS WHERE TRUNC(fecha) = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Espectaculo(
                            rs.getString("id_espectaculo"),
                            rs.getString("nombre"),
                            rs.getDate("fecha").toLocalDate(),
                            rs.getDouble("precio_base"),
                            rs.getDouble("precio_vip")
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error al consultar espectáculos por fecha", e);
        }
        return lista;
    }

    @Override
    public String obtenerNombrePorId(String id) throws SQLException {
        String query = "SELECT NOMBRE FROM ESPECTACULOS WHERE ID_ESPECTACULO = ?";
        String nombre = "";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nombre = rs.getString("NOMBRE");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al obtener nombre por ID", e);
        }
        return nombre;
    }

    @Override
    public boolean eliminarEspectaculo(String id) throws SQLException {
        String query = "DELETE FROM ESPECTACULOS WHERE ID_ESPECTACULO = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al eliminar espectáculo", e);
        }
    }

    @Override
    public boolean existeEspectaculo(String id) throws SQLException {
        String query = "SELECT 1 FROM ESPECTACULOS WHERE ID_ESPECTACULO = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al verificar existencia de espectáculo", e);
        }
    }

    @Override
    public boolean insertarEspectaculo(Espectaculo espectaculo) throws SQLException {
        String query = "INSERT INTO ESPECTACULOS (ID_ESPECTACULO, NOMBRE, FECHA, PRECIO_BASE, PRECIO_VIP) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, espectaculo.getId());
            pstmt.setString(2, espectaculo.getNombre());
            pstmt.setDate(3, Date.valueOf(espectaculo.getFecha()));
            pstmt.setDouble(4, espectaculo.getPrecioBase());
            pstmt.setDouble(5, espectaculo.getPrecioVip());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al insertar espectáculo", e);
        }
    }

    @Override
    public boolean actualizarEspectaculo(Espectaculo espectaculo) throws SQLException {
        String query = "UPDATE ESPECTACULOS SET NOMBRE = ?, FECHA = ?, PRECIO_BASE = ?, PRECIO_VIP = ? " +
                "WHERE ID_ESPECTACULO = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, espectaculo.getNombre());
            pstmt.setDate(2, Date.valueOf(espectaculo.getFecha()));
            pstmt.setDouble(3, espectaculo.getPrecioBase());
            pstmt.setDouble(4, espectaculo.getPrecioVip());
            pstmt.setString(5, espectaculo.getId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Error al actualizar espectáculo", e);
        }
    }


}