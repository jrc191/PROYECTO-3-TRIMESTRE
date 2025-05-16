package dao.impl;

import dao.UsuarioDaoI;
import models.Reservas;
import models.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    @Override
    public String getNombreUsuarioByEmail(String email) throws SQLException {
        String query = "SELECT nombre FROM USUARIOS WHERE email = ?";
        String dato ="";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()){
                dato = rs.getString("nombre");
            }


        }
        return dato;
    }


    @Override
    public List<Usuario> listUsuariosAdmin(){
        List<Usuario> usuarioList= new ArrayList<>();

        String query = "SELECT id_usuario, nombre, email FROM USUARIOS";

        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setDni(rs.getString("id_usuario"));
                usuario.setNombre(rs.getString("nombre"));
                usuario.setEmail(rs.getString("email"));
                usuarioList.add(usuario);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return usuarioList;


    }

    @Override
    public int eliminarUsuarioByID(String dni) throws SQLException{
        String query = "DELETE FROM USUARIOS WHERE id_usuario = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, dni);
            int affectedRows = pstmt.executeUpdate();
            System.out.println("Filas afectadas: " + affectedRows);
            return affectedRows;
        }

    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) throws SQLException {
        // SQL para actualizar sin incluir el DNI
        String sql = "UPDATE USUARIOS SET nombre = ?, email = ? WHERE id_usuario = ?";

        String sqlConPassword = "UPDATE USUARIOS SET nombre = ?, email = ?, password = ? WHERE id_usuario = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(
                usuario.getPassword() != null && !usuario.getPassword().isEmpty()
                        ? sqlConPassword
                        : sql)) {

            pstmt.setString(1, usuario.getNombre());
            pstmt.setString(2, usuario.getEmail());

            if (usuario.getPassword() != null && !usuario.getPassword().isEmpty()) {
                pstmt.setString(3, usuario.getPassword()); //PARA ACTUALIZAR PASSWORD
                pstmt.setString(4, usuario.getDni()); //DNI PASA A SER EL 4 PARÁMETRO
            } else {
                pstmt.setString(3, usuario.getDni()); // SIN PASSWORD. DNI PASA A SER EL 3 PARÁMETRO
            }

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }
}
