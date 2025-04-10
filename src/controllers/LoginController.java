package controllers;

import javafx.fxml.FXML;
import javafx.animation.FadeTransition;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class LoginController {

    //PARÁMETRO DEL MAIL DEL USUARIO LOGUEADO
    private static String usuarioLogueadoEmail;
	
	//PARÁMETROS CONEXIÓN BBDD
    final static String USER="system";
    final static String PASS="66924463";
    final static String URL="jdbc:oracle:thin:@//localhost:1521/XE";
    
    //ATRIBUTOS FXML DE LOGIN.FXML
    @FXML private TextField loginUserField;
    @FXML private TextField loginEmailField;
    @FXML private PasswordField loginPasswordField;

    //ATRIBUTOS FXML DE REGISTRO.FXML
    @FXML private TextField dniField;
    @FXML private TextField nombreField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    //MENSAJES DE CORRECTO O FALLO PARA LOGIN Y REGISTRO
    @FXML private Label messageLabelLogin;   
    @FXML private Label messageLabelRegistro; 

    //CONFIG TAMAÑO LOGIN Y REGISTRO FIJOS
    public void configureStage(Stage stage) {
        stage.setMinWidth(700);
        stage.setMinHeight(550);
        stage.setMaxWidth(750);
        stage.setMaxHeight(700);
    }
    
    //EFECTO FADE al cambiar de escena
    public void fadeInScene(Node rootNode) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), rootNode);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }
    
    
    //MÉTODO FXML PARA MOSTRAR EL LOGIN AL HACER CLIC EN "¿Ya registrado? Inicia sesión."
    @FXML
    private void showLogin() {
        try {
        	
        	FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
        	Parent root = loader.load();
        	
            LoginController controller = loader.getController();
            
            fadeInScene(root);
            Stage stage = (Stage) messageLabelRegistro.getScene().getWindow();
            controller.configureStage(stage);
            
            stage.setScene(new Scene(root));
            stage.show();
            
        } catch (IOException e) {
            e.printStackTrace();
            messageLabelRegistro.setText("Error al cargar la vista de login");
        }
    }

    //MÉTODO FXML PARA MOSTRAR EL LOGIN AL HACER CLIC EN "¿No tienes cuenta? Regístrate aquí."
    @FXML
    private void showRegistro() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/registro.fxml"));
            Parent root = loader.load();
            
            LoginController controller = loader.getController();
            
            fadeInScene(root);
            Stage stage = (Stage) messageLabelLogin.getScene().getWindow();
            controller.configureStage(stage);
            
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            messageLabelLogin.setText("Error al cargar el formulario de registro");
        }
    }

    //MÉTODO ENCARGADO DE CONTROLAR EL REGISTRO. COMPRUEBA QUE TODOS LOS CAMPOS SE HAYAN INTRODUCIDO, QUE EL DNI SEA VALIDO Y NO DUPLICADO
    //COMPROBADO TODO, USA UN MÉTODO AUXILIAR PARA REGISTRAR AL USUARIO LLAMADO REGISTROUSUARIO

    @FXML
    private void handleRegistro() {
        String dni = dniField.getText();
        String nombre = nombreField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        //Hacemos que todos los campos sean obligatorios
        if (dni.isEmpty() || nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabelRegistro.setText("Todos los campos son obligatorios");
            return;
        }
        
        if (!dniValido(dni)) {
        	messageLabelRegistro.setText("El dni es inválido");
            return;
        }

        //Creamos conexion para preparar el registro
        Connection conn = conexion();
        if (conn != null) {
        	
        	try {
				if (!dniExistente(conn, dni)) {
					//messageLabelRegistro.setText("El dni es inválido");
		            return;
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
        	
        	//Guardamos el éxito del registro
            boolean success = registroUsuario(conn, dni, nombre, email, password);
            if (success) {
                messageLabelRegistro.setText("Registro exitoso!");
                messageLabelRegistro.setStyle("-fx-text-fill: green;");
            } else {
                messageLabelRegistro.setText("Error en el registro. Intente nuevamente.");
                messageLabelRegistro.setStyle("-fx-text-fill: red;");
            }
        } else {
            messageLabelRegistro.setText("Error de conexión a la base de datos");
            messageLabelRegistro.setStyle("-fx-text-fill: red;");
        }
    }

    //MÉTODO PARA MANEJAR EL LOGIN. COMPRUEBA QUE LOS CAMPOS NO ESTÉN VACÍOS, QUE HAYA CONEXIÓN Y VALIDA QUE EL USUARIO SEA CORRECTO
    //CON EL MÉTODO AUXILIAR VALIDAUSUARIO
    @FXML
    private void handleLogin() {
        String email = loginEmailField.getText();
        String password = loginPasswordField.getText();

        // Hacemos que todos los campos sean obligatorios
        if (email.isEmpty() || password.isEmpty()) {
            messageLabelLogin.setText("Todos los campos son obligatorios");
            return;
        }

        // Comprobamos que haya conexión o no
        if (!checkConexion()) {
            messageLabelLogin.setText("Error de conexión a la base de datos");
            messageLabelLogin.setStyle("-fx-text-fill: red;");
        } else {
            // Validamos el usuario
            boolean success = validarUsuario(email, password);

            if (success) {
                messageLabelLogin.setText("Login exitoso!");
                messageLabelLogin.setStyle("-fx-text-fill: green;");

                LoginController.setUsuarioLogueado(email);

                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("../views/cartelera.fxml"));
                    Parent root = loader.load();

                    // Obtener el Stage actual
                    Stage stage = (Stage) loginEmailField.getScene().getWindow();

                    // Crear una nueva escena
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("../Resources/styles.css").toExternalForm());
                    stage.setTitle("CINES JRC");

                    // Establecer el icono de la ventana
                    Image icon = new Image(getClass().getResourceAsStream("../Resources/logo.png"));
                    stage.getIcons().add(icon);

                    // Cambiar la escena
                    stage.setScene(scene);
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                    messageLabelLogin.setText("Error al cargar la nueva vista.");
                    messageLabelLogin.setStyle("-fx-text-fill: red;");
                }
            } else {
                messageLabelLogin.setText("Error en el login. Inténtelo nuevamente.");
                messageLabelLogin.setStyle("-fx-text-fill: red;");
            }
        }
    }


    //método para devolver conexión
    public static Connection conexion() {
        try {
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //Para comprobar conexiones exitosas
    public static boolean checkConexion () {
        if (conexion() == null) {
            return false;
        } else {
            return true;
        }
    }

    //Validacion del usuario. Simplemente seleccionamos la password y la comparamos con la introducida
    public static boolean validarUsuario(String email, String password) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            
            String query = "SELECT PASSWORD FROM USUARIOS WHERE EMAIL = ?";
            pstmt = conexion().prepareStatement(query);
            pstmt.setString(1, email);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("PASSWORD");
                return storedPassword.equals(password); // Compara la contraseña ingresada con la almacenada, devuelve true si son correctas.
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    //método para registrar usuarios en la BBDD. comprueba que el dni tenga el formato correcto y que no exista previamente.
    public static boolean registroUsuario(Connection conexion, String dni, String nombre, String email, String password) {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
        	
        	if (!dniValido(dni)) {
        		//AÑADIR MOSTRAR EN LA APLICACIÓN QUE NO ES VALIDO
        		return false;
        	}
        	
        	if (!dniExistente(conexion, dni)) {
        		//AÑADIR MOSTRAR EN LA APLICACIÓN QUE NO ES VALIDO
        		return false;
        	}

            // Continuamos si el dni no está registrado
            String addUserQuery = "INSERT INTO USUARIOS_CINE (id_usuario, nombre, email, password) VALUES (?, ?, ?, ?)";
            
            pstmt = conexion.prepareStatement(addUserQuery);
            pstmt.setString(1, dni);  
            pstmt.setString(2, nombre);
            pstmt.setString(3, email);
            pstmt.setString(4, password); 
            
            pstmt.executeUpdate();
            
            return true;  // Registro exitoso
            
        } 
        catch (SQLException e) {
        	
            e.printStackTrace();
            return false;  // Error en el registro
        } 
        finally {
        	//buena práctica. cerramos resulset y statement
            try {
                if (rs != null) {
                    rs.close();
                }
                if (pstmt != null) {
                    pstmt.close();
                }
            } 
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        
    }
    
    //método para comprobar si el dni es válido
    private static boolean dniValido(String dni) {

        //ORDEN DE LAS LETRAS DEL DNI. SEGÚN EL RESULTADO DE LA DIVISIÓN DEL Nº DEL DNI, SE ASIGNA UNA LETRA U OTRA
    	final String LETRAS_DNI = "TRWAGMYFPDXBNJZSQVHLCKE";
    	
    	//si es nulo o no cumple los requisitos:
    	if (dni == null || !dni.matches("\\d{8}[A-Z]")) {
    		return false;
    	}
    	
    	//extrae el número
    	int num= Integer.parseInt(dni.substring(0,7));
    	
    	//comprueba las letras coincidentes
    	char letraEsperada=LETRAS_DNI.charAt(num%23);
    	char letraActual = dni.charAt(8);
    	
    	return letraEsperada==letraActual;
    	
    }
    
    //método para comprobar si el dni introducido está en la bbdd
    public static boolean dniExistente(Connection conn, String dni) throws SQLException{
    	
    	boolean existe=false;
    	
    	String sql = "SELECT COUNT(*) FROM USUARIOS_CINE WHERE ID_USUARIO = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dni);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                	
                	if (rs.getInt(1) > 0) {
                		existe=true;
                		return existe; //Ya existe
                	}
                }
            }
        }
        return existe;
    }

    public static String getUsuarioLogueadoEmail() {

        return usuarioLogueadoEmail;
    }

    public static void setUsuarioLogueado(String email) {
        usuarioLogueadoEmail = email;
    }

    //objeto usuario con los campos necesitados para mantener la sesión
    static class Usuario {
        private String dni;
        private String nombre;
        private String email;

        public Usuario(String dni, String nombre, String email) {
            this.dni = dni;
            this.nombre = nombre;
            this.email = email;
        }

        // Getters
        public String getDni() { return dni; }
        public String getNombre() { return nombre; }
        public String getEmail() { return email; }

    }
}
