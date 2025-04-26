package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import dao.UsuarioDaoI;
import dao.impl.UsuarioDaoImpl;
import models.Usuario;
import java.sql.Connection;
import java.sql.SQLException;
import utils.DatabaseConnection;

public class RegistroController {
    @FXML private TextField dniField;
    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabelRegistro;

    private UsuarioDaoI usuarioDao;

    public RegistroController() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleRegistro() {
        String dni = dniField.getText();
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (dni.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabelRegistro.setText("Todos los campos son obligatorios (*)");
            return;
        }

        try {
            Usuario nuevoUsuario = new Usuario(dni, nombre, email, password);
            boolean success = usuarioDao.registrarUsuario(nuevoUsuario);

            if (success) {
                messageLabelRegistro.setText("Usuario registrado con éxito!");
                clearFields();
            } else {
                messageLabelRegistro.setText("Error al registrar el usuario");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabelRegistro.setText("Error en la base de datos");
        }
    }

    private void clearFields() {
        dniField.clear();
        nombreField.clear();
        emailField.clear();
        passwordField.clear();
    }
}