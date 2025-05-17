package models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class Usuario {
    private String dni;
    private String nombre;
    private String email;
    private String password;
    private final SimpleBooleanProperty seleccionado = new SimpleBooleanProperty(false);

    public Usuario(){
        
    }

    public Usuario(String dni, String nombre, String email, String password) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
    }


    public boolean isSeleccionado() {
        return seleccionado.get();
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado.set(seleccionado);
    }

    public SimpleBooleanProperty seleccionadoProperty() {
        return seleccionado;
    }



    public String getDni() { return dni; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setDni(String dni) { this.dni = dni; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
