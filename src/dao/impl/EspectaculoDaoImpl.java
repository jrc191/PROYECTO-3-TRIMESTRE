package dao.impl;

import dao.EspectaculoDaoI;
import models.Espectaculo;
import models.Reservas;

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
        String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip FROM ESPECTACULOS";
        return ejecutarConsulta(query, null);
    }

    @Override
    public List<Espectaculo> obtenerPorNombre(String nombre) {
        String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip " +
                "FROM ESPECTACULOS WHERE LOWER(nombre) LIKE LOWER(?)";
        return ejecutarConsulta(query, ps -> ps.setString(1, "%" + nombre + "%"));
    }

    @Override
    public List<Espectaculo> obtenerPorFecha(LocalDate fecha) {
        String query = "SELECT id_espectaculo, nombre, fecha, precio_base, precio_vip " +
                "FROM ESPECTACULOS WHERE TRUNC(fecha) = ?";
        return ejecutarConsulta(query, ps -> ps.setDate(1, Date.valueOf(fecha)));
    }

    @Override
    public String obtenerNombrePorId(String id) throws SQLException {
        String query = "SELECT NOMBRE " +
                "FROM ESPECTACULOS WHERE ID_ESPECTACULO = ?";
        String dato ="";
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()){
                dato = rs.getString("NOMBRE");
            }


        }


        return dato;
    }

    private List<Espectaculo> ejecutarConsulta(String query, PreparedStatementSetter setter) {
        List<Espectaculo> lista = new ArrayList<>();
        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            if (setter != null) setter.setValues(pstmt);

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
            throw new RuntimeException("Error al consultar espectáculos", e);
        }
        return lista;
    }

    // Interfaz funcional para aplicar parámetros dinámicamente
    @FunctionalInterface
    private interface PreparedStatementSetter {
        void setValues(PreparedStatement ps) throws SQLException;
    }
}
