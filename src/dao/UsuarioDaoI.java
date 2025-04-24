package dao;

import models.Usuario;
import java.sql.SQLException;

public interface UsuarioDaoI {
    boolean registrarUsuario(Usuario usuario) throws SQLException;
    boolean validarUsuario(String email, String password) throws SQLException;
    boolean existeDni(String dni) throws SQLException;
    String getIDUsuarioByEmail(String email) throws SQLException;
}
