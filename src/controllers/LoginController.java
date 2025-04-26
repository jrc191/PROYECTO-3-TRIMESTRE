package controllers;

import utils.DatabaseConnection;
import dao.UsuarioDaoI;
import dao.impl.UsuarioDaoImpl;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;
import models.Usuario;
import utils.Transitions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginController {

    // FXML Login
    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;

    // FXML Registro
    @FXML private TextField dniField;
    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // Mensajes
    @FXML private Label messageLabelLogin;
    @FXML private Label messageLabelRegistro;

    // DAO
    private UsuarioDaoI usuarioDao;

    // Mail del usuario logueado (para mantener sesión)
    private static String usuarioLogueadoEmail;

    @FXML
    public void initialize() {
        try {
            Connection conn = DatabaseConnection.getConnection();
            this.usuarioDao = new UsuarioDaoImpl(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setUsuarioLogueado(String email) {
        usuarioLogueadoEmail = email;
    }

    public static String getUsuarioLogueadoEmail() {
        return usuarioLogueadoEmail;
    }

    public void configureStage(Stage stage) {
        stage.setMinWidth(700);
        stage.setMinHeight(550);
        stage.setMaxWidth(750);
        stage.setMaxHeight(700);
    }

    public void fadeInScene(Node rootNode) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    @FXML
    private void showLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            Transitions transitions = new Transitions();
            transitions.fadeInScene(root);
            Stage stage = (Stage) messageLabelRegistro.getScene().getWindow();
            controller.configureStage(stage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabelRegistro.setText("Error al cargar la vista de login");
        }
    }

    @FXML
    private void showRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/registro.fxml"));
            Parent root = loader.load();
            LoginController controller = loader.getController();
            Transitions transitions = new Transitions();
            transitions.fadeInScene(root);
            Stage stage = (Stage) messageLabelLogin.getScene().getWindow();
            controller.configureStage(stage);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabelLogin.setText("Error al cargar el formulario de registro");
        }
    }

    @FXML
    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = loginPasswordField.getText().trim();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabelLogin.setText("Todos los campos son obligatorios");
            return;
        }

        try {

            if (usuarioDao!=null && usuarioDao.validarUsuario(email, password) && email.equals("admin@admin.com")){
                messageLabelLogin.setText("Login exitoso!");
                messageLabelLogin.setStyle("-fx-text-fill: green;");
                setUsuarioLogueado(email);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/admin.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) loginEmailField.getScene().getWindow();
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/Resources/styles.css").toExternalForm());
                stage.setTitle("CINES JRC");
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/Resources/logo.png")));
                stage.setScene(scene);
                stage.show();
            }
            else{
                if (usuarioDao != null && usuarioDao.validarUsuario(email, password)) {
                    messageLabelLogin.setText("Login exitoso!");
                    messageLabelLogin.setStyle("-fx-text-fill: green;");
                    setUsuarioLogueado(email);

                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/cartelera.fxml"));
                    Parent root = loader.load();

                    Stage stage = (Stage) loginEmailField.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/Resources/styles.css").toExternalForm());
                    stage.setTitle("CINES JRC");
                    stage.getIcons().add(new Image(getClass().getResourceAsStream("/Resources/logo.png")));
                    stage.setScene(scene);
                    stage.show();
                } else {
                    messageLabelLogin.setText("Error en el login. Inténtelo nuevamente.");
                    messageLabelLogin.setStyle("-fx-text-fill: red;");
                }
            }


        } catch (IOException | SQLException e) {
            e.printStackTrace();
            messageLabelLogin.setText("Error al procesar el login.");
        }
    }

    @FXML
    private void handleRegistro() {
        String dni = dniField.getText().trim();
        String nombre = nombreField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText().trim();

        if (dni.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabelRegistro.setText("Todos los campos son obligatorios");
            return;
        }

        if (!dniValido(dni)) {
            messageLabelRegistro.setText("El DNI es inválido");
            return;
        }

        try {
            if (usuarioDao.existeDni(dni)) {
                messageLabelRegistro.setText("El DNI ya está registrado");
                return;
            }

            Usuario nuevoUsuario = new Usuario(dni, nombre, email, password);
            boolean registrado = usuarioDao.registrarUsuario(nuevoUsuario);

            if (registrado) {
                messageLabelRegistro.setText("Registro exitoso!");
                messageLabelRegistro.setStyle("-fx-text-fill: green;");
            } else {
                messageLabelRegistro.setText("Error en el registro.");
                messageLabelRegistro.setStyle("-fx-text-fill: red;");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            messageLabelRegistro.setText("Error al acceder a la base de datos.");
        }
    }

    private boolean dniValido(String dni) {
        final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";
        if (dni == null || !dni.matches("\\d{8}[A-Z]")) {
            return false;
        }

        int num = Integer.parseInt(dni.substring(0, 8));
        char letraEsperada = LETRAS_DNI.charAt(num % 23);
        char letraActual = dni.charAt(8);

        return letraEsperada == letraActual;
    }
}
