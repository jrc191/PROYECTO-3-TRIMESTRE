package dao.impl;

import dao.ButacaDaoI;
import models.Butaca;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ButacaDaoImpl implements ButacaDaoI {

    private final Connection conn;

    public ButacaDaoImpl(Connection conn) {
        this.conn = conn;
    }

    @Override
    public List<Butaca> obtenerButacasOcupadas(String idEspectaculo) {
        List<Butaca> lista = new ArrayList<>();
        String query = """
            SELECT b.id_butaca, b.fila, b.columna, b.tipo
            FROM BUTACAS b
            JOIN RESERVAS r ON b.id_butaca = r.id_butaca
            WHERE r.id_espectaculo = ? 
            AND r.estado = 'O'
            """;

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, idEspectaculo);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                lista.add(new Butaca(
                        rs.getString("id_butaca"),
                        rs.getInt("fila"),
                        rs.getInt("columna"),
                        rs.getString("tipo").charAt(0)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener butacas ocupadas", e);
        }

        return lista;
    }

    @Override
    public List<Butaca> obtenerButacasVIP() {
        List<Butaca> lista = new ArrayList<>();
        String query = "SELECT id_butaca, fila, columna, tipo FROM BUTACAS WHERE tipo = 'V'";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Butaca(
                        rs.getString("id_butaca"),
                        rs.getInt("fila"),
                        rs.getInt("columna"),
                        rs.getString("tipo").charAt(0)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener butacas VIP", e);
        }

        return lista;
    }

    @Override
    public List<Butaca> obtenerTodasButacas(String idEspectaculoSeleccionado) {
        List<Butaca> lista = new ArrayList<>();
        String query = "SELECT id_butaca, fila, columna, tipo FROM BUTACAS";

        try (PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                lista.add(new Butaca(
                        rs.getString("id_butaca"),
                        rs.getInt("fila"),
                        rs.getInt("columna"),
                        rs.getString("tipo").charAt(0)
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al obtener todas las butacas", e);
        }

        return lista;
    }



}
