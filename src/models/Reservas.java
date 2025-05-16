package models;

import java.sql.Timestamp;

public class Reservas {
    private String id_reserva;
    private String id_espectaculo;
    private String id_butaca;
    private String id_usuario;
    private char estado;
    private double precio;
    private Timestamp fecha;
    private Timestamp fechaCancelacion; // Solo para historial

    public Reservas(String id_reserva, String id_espectaculo, String id_butaca,
                    String id_usuario, double precio, char estado, Timestamp fecha) {
        this.id_reserva = id_reserva;
        this.id_espectaculo = id_espectaculo;
        this.id_butaca = id_butaca;
        this.id_usuario = id_usuario;
        this.precio = precio;
        this.estado = estado;
        this.fecha = fecha;
    }

    public Reservas() {
    }



    public Timestamp getFecha() { return fecha; }
    public void setFecha(Timestamp fecha) { this.fecha = fecha; }

    public Timestamp getFechaCancelacion() { return fechaCancelacion; }
    public void setFechaCancelacion(Timestamp fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getId_reserva() { return id_reserva; }
    public String getId_espectaculo() { return id_espectaculo; }
    public String getId_butaca() { return id_butaca; }
    public String getId_usuario() { return id_usuario; }
    public char getEstado() { return estado; }
    public double getPrecio() { return precio; }

    public void setId_reserva(String id_reserva) { this.id_reserva = id_reserva; }
    public void setId_espectaculo(String id_espectaculo) { this.id_espectaculo = id_espectaculo; }
    public void setId_butaca(String id_butaca) { this.id_butaca = id_butaca; }
    public void setId_usuario(String id_usuario) { this.id_usuario = id_usuario; }
    public void setEstado(char estado) { this.estado = estado; }
    public void setPrecio(double precio) { this.precio = precio; }
}
