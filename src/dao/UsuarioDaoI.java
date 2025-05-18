package dao;

import models.Usuario;
import java.sql.SQLException;
import java.util.List;

public interface UsuarioDaoI {
    boolean registrarUsuario(Usuario usuario) throws SQLException;
    boolean validarUsuario(String email, String password) throws SQLException;
    boolean existeDni(String dni) throws SQLException;
    String getIDUsuarioByEmail(String email) throws SQLException;
    List<Usuario> listUsuariosAdmin() throws SQLException;
    String getNombreUsuarioByEmail(String email) throws SQLException;
    int eliminarUsuarioByID(String dni) throws SQLException;
    boolean actualizarUsuario(Usuario usuario) throws SQLException;
    boolean existeEmail(String email) throws SQLException;
}
