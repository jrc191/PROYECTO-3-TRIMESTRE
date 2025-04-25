package models;

public class Usuario {
    private String dni;
    private String nombre;
    private String email;
    private String password;

    public Usuario(){
        
    }

    public Usuario(String dni, String nombre, String email, String password) {
        this.dni = dni;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
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
