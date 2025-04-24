package dao.impl;

import dao.UsuarioDaoI;
import models.Usuario;

import java.sql.*;

public class UsuarioDaoImpl implements UsuarioDaoI {

    private final Connection conn;

    public UsuarioDaoImpl(Connection conn) {
        this.conn = conn;
    }


    @Override
    public boolean registrarUsuario(Usuario usuario) throws SQLException {
        String query = "INSERT INTO USUARIOS (id_usuario, nombre, email, password) VALUES (?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, usuario.getDni());
            pstmt.setString(2, usuario.getNombre());
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getPassword());
            pstmt.executeUpdate();
            return true;
        }
    }

    @Override
    public boolean validarUsuario(String email, String password) throws SQLException {
        String query = "SELECT password FROM USUARIOS WHERE email = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return password.equals(rs.getString("password"));
            }
        }
        return false;
    }

    @Override
    public boolean existeDni(String dni) throws SQLException {
        String query = "SELECT COUNT(*) FROM USUARIOS WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, dni);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    @Override
    public String getIDUsuarioByEmail(String email) throws SQLException {
        String query = "SELECT id_usuario FROM USUARIOS WHERE email = ?";
        String dato ="";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()){
                dato = rs.getString("id_usuario");
            }


        }
        return dato;
    }
}
